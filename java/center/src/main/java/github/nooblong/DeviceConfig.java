package github.nooblong;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 设备配置管理类
 * 负责读取和管理设备配置参数
 */
public class DeviceConfig {
    
    private static final String CONFIG_FILE = "/device.properties";
    private Properties properties;
    
    // 默认配置值
    private static final int DEFAULT_VENDOR_ID = 0x303A;
    private static final int DEFAULT_PRODUCT_ID = 0x8000;
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final int DEFAULT_REPORT_SIZE = 64;
    private static final int DEFAULT_SENSOR_INTERVAL = 10;
    
    public DeviceConfig() {
        loadConfig();
    }
    
    /**
     * 加载配置文件
     */
    private void loadConfig() {
        properties = new Properties();
        
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                System.out.println("配置文件加载成功: " + CONFIG_FILE);
            } else {
                System.out.println("配置文件未找到，使用默认配置: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage());
            System.out.println("使用默认配置");
        }
    }
    
    /**
     * 获取供应商ID
     */
    public int getVendorId() {
        String value = properties.getProperty("device.vendor.id", "0x" + Integer.toHexString(DEFAULT_VENDOR_ID));
        return parseHexValue(value, DEFAULT_VENDOR_ID);
    }
    
    /**
     * 获取产品ID
     */
    public int getProductId() {
        String value = properties.getProperty("device.product.id", "0x" + Integer.toHexString(DEFAULT_PRODUCT_ID));
        return parseHexValue(value, DEFAULT_PRODUCT_ID);
    }
    
    /**
     * 获取通信超时时间
     */
    public int getTimeout() {
        return getIntProperty("communication.timeout.ms", DEFAULT_TIMEOUT);
    }
    
    /**
     * 获取重试次数
     */
    public int getRetryCount() {
        return getIntProperty("communication.retry.count", DEFAULT_RETRY_COUNT);
    }
    
    /**
     * 获取报告大小
     */
    public int getReportSize() {
        return getIntProperty("communication.report.size", DEFAULT_REPORT_SIZE);
    }
    
    /**
     * 获取传感器读取间隔
     */
    public int getSensorInterval() {
        return getIntProperty("sensor.read.interval.seconds", DEFAULT_SENSOR_INTERVAL);
    }
    
    /**
     * 是否启用温度传感器
     */
    public boolean isTemperatureEnabled() {
        return getBooleanProperty("sensor.temperature.enabled", true);
    }
    
    /**
     * 是否启用湿度传感器
     */
    public boolean isHumidityEnabled() {
        return getBooleanProperty("sensor.humidity.enabled", true);
    }
    
    /**
     * 获取默认LED红色值
     */
    public int getDefaultLedRed() {
        return getIntProperty("led.default.red", 0);
    }
    
    /**
     * 获取默认LED绿色值
     */
    public int getDefaultLedGreen() {
        return getIntProperty("led.default.green", 0);
    }
    
    /**
     * 获取默认LED蓝色值
     */
    public int getDefaultLedBlue() {
        return getIntProperty("led.default.blue", 0);
    }
    
    /**
     * 是否启用调试
     */
    public boolean isDebugEnabled() {
        return getBooleanProperty("debug.enabled", false);
    }
    
    /**
     * 是否显示原始数据
     */
    public boolean showRawData() {
        return getBooleanProperty("debug.show.raw.data", false);
    }
    
    /**
     * 是否记录命令日志
     */
    public boolean logCommands() {
        return getBooleanProperty("debug.log.commands", false);
    }
    
    /**
     * 解析十六进制值
     */
    private int parseHexValue(String value, int defaultValue) {
        try {
            if (value.startsWith("0x") || value.startsWith("0X")) {
                return Integer.parseInt(value.substring(2), 16);
            } else {
                return Integer.parseInt(value, 16);
            }
        } catch (NumberFormatException e) {
            System.err.println("无效的十六进制值: " + value + ", 使用默认值: 0x" + Integer.toHexString(defaultValue));
            return defaultValue;
        }
    }
    
    /**
     * 获取整数属性
     */
    private int getIntProperty(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            System.err.println("无效的整数值 " + key + ": " + properties.getProperty(key) + ", 使用默认值: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔属性
     */
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }
    
    /**
     * 打印所有配置
     */
    public void printConfig() {
        System.out.println("当前设备配置:");
        System.out.println("==============");
        System.out.printf("供应商ID: 0x%04X%n", getVendorId());
        System.out.printf("产品ID: 0x%04X%n", getProductId());
        System.out.printf("通信超时: %d ms%n", getTimeout());
        System.out.printf("重试次数: %d%n", getRetryCount());
        System.out.printf("报告大小: %d bytes%n", getReportSize());
        System.out.printf("传感器间隔: %d seconds%n", getSensorInterval());
        System.out.printf("温度传感器: %s%n", isTemperatureEnabled() ? "启用" : "禁用");
        System.out.printf("湿度传感器: %s%n", isHumidityEnabled() ? "启用" : "禁用");
        System.out.printf("默认LED颜色: RGB(%d, %d, %d)%n", getDefaultLedRed(), getDefaultLedGreen(), getDefaultLedBlue());
        System.out.printf("调试模式: %s%n", isDebugEnabled() ? "启用" : "禁用");
        System.out.printf("显示原始数据: %s%n", showRawData() ? "是" : "否");
        System.out.printf("记录命令: %s%n", logCommands() ? "是" : "否");
        System.out.println();
    }
    
    /**
     * 获取设备描述字符串
     */
    public String getDeviceDescription() {
        return String.format("ESP32 HID设备 (VID: 0x%04X, PID: 0x%04X)", getVendorId(), getProductId());
    }
}
