package github.nooblong;

import org.hid4java.*;
import org.hid4java.event.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * ESP32 HID设备控制器
 * 提供更高级的HID通信功能
 */
public class HidController implements HidServicesListener {
    
    // ESP32 HID命令定义
    public static class Commands {
        public static final byte CLEAR_CONSOLE = 0x01;
        public static final byte SHOW_SYSTEM_INFO = 0x02;
        public static final byte MOUSE_CLICK = 0x03;
        public static final byte SET_LED_COLOR = 0x04;
        public static final byte GET_TEMPERATURE = 0x05;
        public static final byte GET_HUMIDITY = 0x06;
        public static final byte RESET_DEVICE = (byte) 0xFF;
    }
    
    private HidServices hidServices;
    private HidDevice hidDevice;
    private boolean isConnected = false;
    private final int vendorId;
    private final int productId;
    
    /**
     * 构造函数
     * @param vendorId 供应商ID
     * @param productId 产品ID
     */
    public HidController(int vendorId, int productId) {
        this.vendorId = vendorId;
        this.productId = productId;
    }
    
    /**
     * 初始化并连接到HID设备
     * @return 连接是否成功
     */
    public boolean initialize() {
        try {
            // 配置HID服务
            HidServicesSpecification spec = new HidServicesSpecification();
            spec.setAutoShutdown(true);
            spec.setAutoStart(false);
            
            // 启动HID服务
            hidServices = HidManager.getHidServices(spec);
            hidServices.addHidServicesListener(this);
            hidServices.start();
            
            // 连接到设备
            return connectToDevice();
            
        } catch (Exception e) {
            System.err.println("初始化HID控制器失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 连接到指定的HID设备
     * @return 连接是否成功
     */
    private boolean connectToDevice() {
        hidDevice = hidServices.getHidDevice(vendorId, productId, null);
        
        if (hidDevice != null) {
            isConnected = hidDevice.open();
            if (isConnected) {
                System.out.printf("成功连接到HID设备: %s (VID: 0x%04X, PID: 0x%04X)%n",
                    hidDevice.getProduct(), vendorId, productId);
                return true;
            } else {
                System.err.println("无法打开HID设备");
                return false;
            }
        } else {
            System.err.printf("未找到HID设备 (VID: 0x%04X, PID: 0x%04X)%n", vendorId, productId);
            return false;
        }
    }
    
    /**
     * 发送命令到ESP32设备
     * @param command 命令字节
     * @param data 附加数据 (可选)
     * @return 发送是否成功
     */
    public boolean sendCommand(byte command, byte... data) {
        if (!isConnected || hidDevice == null) {
            System.err.println("设备未连接");
            return false;
        }
        
        try {
            // 创建数据包
            byte[] packet = new byte[64]; // 标准HID报告大小
            packet[0] = 0x00; // 报告ID
            packet[1] = command; // 命令
            
            // 添加附加数据
            if (data != null && data.length > 0) {
                System.arraycopy(data, 0, packet, 2, Math.min(data.length, 62));
            }
            
            // 发送数据
            int result = hidDevice.write(packet, packet.length, (byte)0x00);
            
            if (result >= 0) {
                System.out.printf("命令发送成功: 0x%02X%n", command & 0xFF);
                return true;
            } else {
                System.err.printf("命令发送失败: 0x%02X (错误: %d)%n", command & 0xFF, result);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("发送命令时出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 异步发送命令
     * @param command 命令字节
     * @param data 附加数据
     * @return CompletableFuture<Boolean>
     */
    public CompletableFuture<Boolean> sendCommandAsync(byte command, byte... data) {
        return CompletableFuture.supplyAsync(() -> sendCommand(command, data));
    }
    
    /**
     * 读取设备响应数据
     * @param timeoutMs 超时时间(毫秒)
     * @return 读取到的数据，如果失败返回null
     */
    public byte[] readResponse(int timeoutMs) {
        if (!isConnected || hidDevice == null) {
            return null;
        }
        
        try {
            byte[] buffer = new byte[64];
            int bytesRead = hidDevice.read(buffer, timeoutMs);
            
            if (bytesRead > 0) {
                byte[] result = new byte[bytesRead];
                System.arraycopy(buffer, 0, result, 0, bytesRead);
                return result;
            }
            
        } catch (Exception e) {
            System.err.println("读取响应时出错: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 发送命令并等待响应
     * @param command 命令字节
     * @param timeoutMs 超时时间
     * @param data 附加数据
     * @return 响应数据
     */
    public byte[] sendCommandWithResponse(byte command, int timeoutMs, byte... data) {
        if (sendCommand(command, data)) {
            return readResponse(timeoutMs);
        }
        return null;
    }
    
    /**
     * 清空ESP32控制台
     */
    public boolean clearConsole() {
        return sendCommand(Commands.CLEAR_CONSOLE);
    }
    
    /**
     * 显示ESP32系统信息
     */
    public boolean showSystemInfo() {
        return sendCommand(Commands.SHOW_SYSTEM_INFO);
    }
    
    /**
     * 执行鼠标点击
     */
    public boolean performMouseClick() {
        return sendCommand(Commands.MOUSE_CLICK);
    }
    
    /**
     * 设置LED颜色
     * @param red 红色分量 (0-255)
     * @param green 绿色分量 (0-255)
     * @param blue 蓝色分量 (0-255)
     */
    public boolean setLedColor(int red, int green, int blue) {
        byte[] colorData = {
            (byte)(red & 0xFF),
            (byte)(green & 0xFF),
            (byte)(blue & 0xFF)
        };
        return sendCommand(Commands.SET_LED_COLOR, colorData);
    }
    
    /**
     * 获取温度数据
     * @return 温度值，失败返回null
     */
    public Float getTemperature() {
        byte[] response = sendCommandWithResponse(Commands.GET_TEMPERATURE, 2000);
        if (response != null && response.length >= 4) {
            // 假设ESP32返回4字节的浮点数
            int bits = ((response[0] & 0xFF) << 24) |
                      ((response[1] & 0xFF) << 16) |
                      ((response[2] & 0xFF) << 8) |
                      (response[3] & 0xFF);
            return Float.intBitsToFloat(bits);
        }
        return null;
    }
    
    /**
     * 获取湿度数据
     * @return 湿度值，失败返回null
     */
    public Float getHumidity() {
        byte[] response = sendCommandWithResponse(Commands.GET_HUMIDITY, 2000);
        if (response != null && response.length >= 4) {
            // 假设ESP32返回4字节的浮点数
            int bits = ((response[0] & 0xFF) << 24) |
                      ((response[1] & 0xFF) << 16) |
                      ((response[2] & 0xFF) << 8) |
                      (response[3] & 0xFF);
            return Float.intBitsToFloat(bits);
        }
        return null;
    }
    
    /**
     * 列出所有可用的HID设备
     */
    public void listAvailableDevices() {
        if (hidServices == null) {
            System.out.println("HID服务未初始化");
            return;
        }
        
        List<HidDevice> devices = hidServices.getAttachedHidDevices();
        System.out.println("可用的HID设备:");
        System.out.println("================");
        
        for (HidDevice device : devices) {
            System.out.printf("VID: 0x%04X | PID: 0x%04X | 产品: %-30s | 制造商: %s%n",
                device.getVendorId(),
                device.getProductId(),
                device.getProduct() != null ? device.getProduct() : "Unknown",
                device.getManufacturer() != null ? device.getManufacturer() : "Unknown"
            );
        }
        System.out.println();
    }
    
    /**
     * 检查设备是否已连接
     */
    public boolean isConnected() {
        return isConnected && hidDevice != null && hidDevice.isOpen();
    }
    
    /**
     * 获取设备信息
     */
    public String getDeviceInfo() {
        if (hidDevice != null) {
            return String.format("产品: %s, 制造商: %s, VID: 0x%04X, PID: 0x%04X",
                hidDevice.getProduct(),
                hidDevice.getManufacturer(),
                hidDevice.getVendorId(),
                hidDevice.getProductId()
            );
        }
        return "无设备连接";
    }
    
    /**
     * 关闭连接并清理资源
     */
    public void close() {
        try {
            if (hidDevice != null && hidDevice.isOpen()) {
                hidDevice.close();
                System.out.println("HID设备连接已关闭");
            }
            
            if (hidServices != null) {
                hidServices.stop();
                System.out.println("HID服务已停止");
            }
            
            isConnected = false;
            
        } catch (Exception e) {
            System.err.println("关闭连接时出错: " + e.getMessage());
        }
    }
    
    // HidServicesListener 接口实现
    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        HidDevice device = event.getHidDevice();
        if (device.getVendorId() == vendorId && device.getProductId() == productId) {
            System.out.println("目标HID设备已连接: " + device.getProduct());
            if (!isConnected) {
                connectToDevice();
            }
        }
    }
    
    @Override
    public void hidDeviceDetached(HidServicesEvent event) {
        HidDevice device = event.getHidDevice();
        if (device.getVendorId() == vendorId && device.getProductId() == productId) {
            System.out.println("目标HID设备已断开: " + device.getProduct());
            isConnected = false;
        }
    }
    
    @Override
    public void hidFailure(HidServicesEvent event) {
        System.err.println("HID服务失败: " + event);
        isConnected = false;
    }
}
