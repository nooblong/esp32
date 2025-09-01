package github.nooblong;

import java.util.Scanner;

/**
 * ESP32 HID通信主程序
 * 使用配置文件和HidController进行设备通信
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("ESP32 HID通信工具 v2.0");
        System.out.println("========================");
        
        // 加载配置
        DeviceConfig config = new DeviceConfig();
        config.printConfig();
        
        // 创建控制器
        HidController controller = new HidController(config.getVendorId(), config.getProductId());
        
        // 初始化连接
        if (!controller.initialize()) {
            System.err.println("初始化失败！");
            
            Scanner scanner = new Scanner(System.in);
            System.out.print("是否查看可用设备列表? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            
            if ("y".equals(response) || "yes".equals(response)) {
                controller.listAvailableDevices();
                System.out.println("\n请根据上述列表修改 src/main/resources/device.properties 文件中的设备ID");
            }
            
            scanner.close();
            return;
        }
        
        // 启动示例程序
        HidExample example = new HidExample();
        example.runWithController(controller, config);
    }
}