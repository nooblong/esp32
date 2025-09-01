package github.nooblong;

import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ESP32 HID设备控制示例
 * 演示如何使用HidController与ESP32进行通信
 */
public class HidExample {
    
    // ESP32 设备ID (需要根据实际设备修改)
    private static final int VENDOR_ID = 0x303A;  // Espressif VID
    private static final int PRODUCT_ID = 0x8000; // 需要根据实际情况修改
    
    private HidController controller;
    private ScheduledExecutorService scheduler;
    
    public static void main(String[] args) {
        HidExample example = new HidExample();
        example.run();
    }
    
    public void run() {
        System.out.println("ESP32 HID控制示例");
        System.out.println("==================");
        
        // 加载配置
        DeviceConfig config = new DeviceConfig();
        
        // 初始化控制器
        controller = new HidController(config.getVendorId(), config.getProductId());
        
        if (!controller.initialize()) {
            System.err.println("初始化失败，显示可用设备:");
            controller.listAvailableDevices();
            return;
        }
        
        // 启动定时器用于定期读取传感器数据
        startPeriodicDataReading(config);
        
        // 启动交互模式
        startInteractiveMode();
        
        // 清理资源
        cleanup();
    }
    
    /**
     * 使用外部控制器和配置运行
     */
    public void runWithController(HidController externalController, DeviceConfig config) {
        this.controller = externalController;
        
        System.out.println("使用外部控制器启动HID示例");
        
        // 启动定时器用于定期读取传感器数据
        startPeriodicDataReading(config);
        
        // 启动交互模式
        startInteractiveMode();
        
        // 清理资源 (不关闭外部控制器)
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
    
    /**
     * 启动定期数据读取
     */
    private void startPeriodicDataReading(DeviceConfig config) {
        if (!config.isTemperatureEnabled() && !config.isHumidityEnabled()) {
            System.out.println("传感器读取已禁用");
            return;
        }
        
        scheduler = Executors.newScheduledThreadPool(1);
        
        // 根据配置的间隔读取传感器数据
        scheduler.scheduleAtFixedRate(() -> {
            try {
                boolean hasData = false;
                StringBuilder data = new StringBuilder("传感器数据 - ");
                
                if (config.isTemperatureEnabled()) {
                    Float temperature = controller.getTemperature();
                    if (temperature != null) {
                        data.append(String.format("温度: %.1f°C", temperature));
                        hasData = true;
                    }
                }
                
                if (config.isHumidityEnabled()) {
                    Float humidity = controller.getHumidity();
                    if (humidity != null) {
                        if (hasData) data.append(", ");
                        data.append(String.format("湿度: %.1f%%", humidity));
                        hasData = true;
                    }
                }
                
                if (hasData) {
                    System.out.println(data.toString());
                }
            } catch (Exception e) {
                System.err.println("读取传感器数据失败: " + e.getMessage());
            }
        }, 5, config.getSensorInterval(), TimeUnit.SECONDS);
    }
    
    /**
     * 启动交互模式
     */
    private void startInteractiveMode() {
        Scanner scanner = new Scanner(System.in);
        
        printMenu();
        
        while (true) {
            System.out.print("\n请输入命令: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            try {
                if ("exit".equals(input) || "quit".equals(input)) {
                    break;
                } else if ("help".equals(input) || "h".equals(input)) {
                    printMenu();
                } else if ("status".equals(input)) {
                    showDeviceStatus();
                } else if ("list".equals(input)) {
                    controller.listAvailableDevices();
                } else if ("1".equals(input)) {
                    controller.clearConsole();
                } else if ("2".equals(input)) {
                    controller.showSystemInfo();
                } else if ("3".equals(input)) {
                    controller.performMouseClick();
                } else if ("4".equals(input)) {
                    setLedColorInteractive(scanner);
                } else if ("5".equals(input)) {
                    readTemperature();
                } else if ("6".equals(input)) {
                    readHumidity();
                } else if ("7".equals(input)) {
                    readAllSensors();
                } else if ("8".equals(input)) {
                    testBatchCommands();
                } else if ("9".equals(input)) {
                    customCommand(scanner);
                } else {
                    System.out.println("未知命令: " + input + " (输入 'help' 查看帮助)");
                }
            } catch (Exception e) {
                System.err.println("执行命令时出错: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    /**
     * 打印菜单
     */
    private void printMenu() {
        System.out.println("\n可用命令:");
        System.out.println("==========");
        System.out.println("1  - 清空ESP32控制台");
        System.out.println("2  - 显示ESP32系统信息");
        System.out.println("3  - 执行鼠标点击");
        System.out.println("4  - 设置LED颜色");
        System.out.println("5  - 读取温度");
        System.out.println("6  - 读取湿度");
        System.out.println("7  - 读取所有传感器");
        System.out.println("8  - 测试批量命令");
        System.out.println("9  - 自定义命令");
        System.out.println("list   - 列出可用设备");
        System.out.println("status - 显示设备状态");
        System.out.println("help   - 显示此帮助");
        System.out.println("exit   - 退出程序");
    }
    
    /**
     * 显示设备状态
     */
    private void showDeviceStatus() {
        System.out.println("\n设备状态:");
        System.out.println("连接状态: " + (controller.isConnected() ? "已连接" : "未连接"));
        System.out.println("设备信息: " + controller.getDeviceInfo());
    }
    
    /**
     * 交互式设置LED颜色
     */
    private void setLedColorInteractive(Scanner scanner) {
        try {
            System.out.print("请输入红色值 (0-255): ");
            int red = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("请输入绿色值 (0-255): ");
            int green = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("请输入蓝色值 (0-255): ");
            int blue = Integer.parseInt(scanner.nextLine().trim());
            
            if (red >= 0 && red <= 255 && green >= 0 && green <= 255 && blue >= 0 && blue <= 255) {
                boolean success = controller.setLedColor(red, green, blue);
                System.out.printf("设置LED颜色 RGB(%d, %d, %d): %s%n", 
                    red, green, blue, success ? "成功" : "失败");
            } else {
                System.out.println("颜色值必须在0-255范围内");
            }
        } catch (NumberFormatException e) {
            System.out.println("无效的数字格式");
        }
    }
    
    /**
     * 读取温度
     */
    private void readTemperature() {
        Float temperature = controller.getTemperature();
        if (temperature != null) {
            System.out.printf("当前温度: %.1f°C%n", temperature);
        } else {
            System.out.println("读取温度失败");
        }
    }
    
    /**
     * 读取湿度
     */
    private void readHumidity() {
        Float humidity = controller.getHumidity();
        if (humidity != null) {
            System.out.printf("当前湿度: %.1f%%%n", humidity);
        } else {
            System.out.println("读取湿度失败");
        }
    }
    
    /**
     * 读取所有传感器数据
     */
    private void readAllSensors() {
        System.out.println("读取传感器数据...");
        
        Float temperature = controller.getTemperature();
        Float humidity = controller.getHumidity();
        
        System.out.println("传感器数据:");
        System.out.printf("温度: %s%n", temperature != null ? String.format("%.1f°C", temperature) : "读取失败");
        System.out.printf("湿度: %s%n", humidity != null ? String.format("%.1f%%", humidity) : "读取失败");
    }
    
    /**
     * 测试批量命令
     */
    private void testBatchCommands() {
        System.out.println("执行批量命令测试...");
        
        // 清空控制台
        System.out.print("1. 清空控制台... ");
        System.out.println(controller.clearConsole() ? "成功" : "失败");
        
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        // 显示系统信息
        System.out.print("2. 显示系统信息... ");
        System.out.println(controller.showSystemInfo() ? "成功" : "失败");
        
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        // 设置LED为红色
        System.out.print("3. 设置LED为红色... ");
        System.out.println(controller.setLedColor(255, 0, 0) ? "成功" : "失败");
        
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        
        // 设置LED为绿色
        System.out.print("4. 设置LED为绿色... ");
        System.out.println(controller.setLedColor(0, 255, 0) ? "成功" : "失败");
        
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        
        // 设置LED为蓝色
        System.out.print("5. 设置LED为蓝色... ");
        System.out.println(controller.setLedColor(0, 0, 255) ? "成功" : "失败");
        
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        
        // 关闭LED
        System.out.print("6. 关闭LED... ");
        System.out.println(controller.setLedColor(0, 0, 0) ? "成功" : "失败");
        
        System.out.println("批量命令测试完成");
    }
    
    /**
     * 自定义命令
     */
    private void customCommand(Scanner scanner) {
        try {
            System.out.print("请输入命令值 (0-255): ");
            int command = Integer.parseInt(scanner.nextLine().trim());
            
            if (command >= 0 && command <= 255) {
                System.out.print("是否添加附加数据? (y/n): ");
                String addData = scanner.nextLine().trim().toLowerCase();
                
                if ("y".equals(addData) || "yes".equals(addData)) {
                    System.out.print("请输入附加数据 (用空格分隔的字节值): ");
                    String[] dataStr = scanner.nextLine().trim().split("\\s+");
                    
                    byte[] data = new byte[dataStr.length];
                    for (int i = 0; i < dataStr.length; i++) {
                        data[i] = (byte)(Integer.parseInt(dataStr[i]) & 0xFF);
                    }
                    
                    boolean success = controller.sendCommand((byte)command, data);
                    System.out.printf("发送自定义命令 0x%02X: %s%n", command, success ? "成功" : "失败");
                } else {
                    boolean success = controller.sendCommand((byte)command);
                    System.out.printf("发送自定义命令 0x%02X: %s%n", command, success ? "成功" : "失败");
                }
            } else {
                System.out.println("命令值必须在0-255范围内");
            }
        } catch (NumberFormatException e) {
            System.out.println("无效的数字格式");
        }
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        System.out.println("\n正在关闭程序...");
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        
        if (controller != null) {
            controller.close();
        }
        
        System.out.println("程序已退出");
    }
}
