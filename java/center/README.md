# Java HID通信程序使用说明

## 概述

这个Java程序用于与ESP32 HID设备进行双向通信。程序提供了完整的HID通信功能，包括发送命令到ESP32、读取传感器数据、控制LED等。

## 项目结构

```
java/center/
├── pom.xml                 # Maven项目配置文件
├── src/main/
│   ├── java/github/nooblong/
│   │   ├── Main.java           # 主程序入口
│   │   ├── HidController.java  # HID设备控制器
│   │   ├── HidExample.java     # 使用示例
│   │   └── DeviceConfig.java   # 配置管理
│   └── resources/
│       └── device.properties   # 设备配置文件
└── target/                 # 编译输出目录
```

## 功能特性

### 1. 核心功能
- **HID设备连接**: 自动检测和连接ESP32 HID设备
- **命令发送**: 支持多种预定义命令和自定义命令
- **数据读取**: 读取ESP32返回的传感器数据
- **实时监控**: 定期读取温湿度数据
- **配置管理**: 通过配置文件管理设备参数

### 2. 支持的命令
- `0x01`: 清空ESP32控制台
- `0x02`: 显示ESP32系统信息
- `0x03`: 执行鼠标点击操作
- `0x04`: 设置LED颜色 (RGB)
- `0x05`: 获取温度数据
- `0x06`: 获取湿度数据
- `0xFF`: 重置设备

## 安装和配置

### 1. 环境要求
- Java 17 或更高版本
- Maven 3.6 或更高版本
- 已正确安装的ESP32 HID设备

### 2. 安装依赖
```bash
cd java/center
mvn clean install
```

### 3. 配置设备参数

编辑 `src/main/resources/device.properties` 文件：

```properties
# 设备标识符 (需要根据实际ESP32设备修改)
device.vendor.id=0x303A     # Espressif 供应商ID
device.product.id=0x8000    # 产品ID (需要根据实际设备修改)

# 通信参数
communication.timeout.ms=2000
communication.retry.count=3
communication.report.size=64

# 传感器配置
sensor.read.interval.seconds=10
sensor.temperature.enabled=true
sensor.humidity.enabled=true

# LED配置
led.default.red=0
led.default.green=0
led.default.blue=0

# 调试设置
debug.enabled=true
debug.show.raw.data=false
debug.log.commands=true
```

## 使用方法

### 1. 编译程序
```bash
mvn compile
```

### 2. 运行主程序
```bash
mvn exec:java -Dexec.mainClass="github.nooblong.Main"
```

或者使用已编译的class文件：
```bash
java -cp target/classes:target/dependency/* github.nooblong.Main
```

### 3. 交互式命令

程序启动后会显示可用命令菜单：

```
可用命令:
==========
1  - 清空ESP32控制台
2  - 显示ESP32系统信息
3  - 执行鼠标点击
4  - 设置LED颜色
5  - 读取温度
6  - 读取湿度
7  - 读取所有传感器
8  - 测试批量命令
9  - 自定义命令
list   - 列出可用设备
status - 显示设备状态
help   - 显示此帮助
exit   - 退出程序
```

### 4. 设置LED颜色示例
```
请输入命令: 4
请输入红色值 (0-255): 255
请输入绿色值 (0-255): 0
请输入蓝色值 (0-255): 0
设置LED颜色 RGB(255, 0, 0): 成功
```

### 5. 读取传感器数据
```
请输入命令: 7
读取传感器数据...
传感器数据:
温度: 25.3°C
湿度: 60.2%
```

## 故障排除

### 1. 设备未找到
如果程序提示"未找到ESP32 HID设备"，请：
1. 确认ESP32设备已正确连接到电脑
2. 运行 `list` 命令查看所有可用HID设备
3. 根据实际设备的VID/PID修改配置文件

### 2. 连接失败
如果设备找到但无法打开：
1. 检查设备是否被其他程序占用
2. 确认当前用户有访问HID设备的权限
3. 尝试重新插拔设备

### 3. 命令发送失败
如果命令发送失败：
1. 检查ESP32固件是否正确实现了HID接收功能
2. 确认报告大小设置是否正确
3. 启用调试模式查看详细错误信息

### 4. 传感器数据读取失败
如果无法读取传感器数据：
1. 确认ESP32端已正确连接传感器
2. 检查ESP32固件是否实现了相应的响应功能
3. 调整通信超时时间设置

## 开发指南

### 1. 添加新命令
在 `HidController.Commands` 类中定义新的命令常量：
```java
public static final byte NEW_COMMAND = 0x10;
```

在 `HidController` 类中添加对应的方法：
```java
public boolean executeNewCommand() {
    return sendCommand(Commands.NEW_COMMAND);
}
```

### 2. 自定义数据协议
修改 `sendCommand` 方法以支持自定义数据格式：
```java
// 自定义数据包格式
byte[] packet = new byte[64];
packet[0] = 0x00;           // 报告ID
packet[1] = command;        // 命令
packet[2] = dataLength;     // 数据长度
// ... 添加更多数据字段
```

### 3. 添加数据解析
在读取响应时添加数据解析逻辑：
```java
public CustomData parseCustomResponse(byte[] response) {
    // 解析响应数据
    // 返回自定义数据对象
}
```

## API参考

### HidController 主要方法

- `boolean initialize()`: 初始化HID连接
- `boolean sendCommand(byte command, byte... data)`: 发送命令
- `byte[] readResponse(int timeoutMs)`: 读取响应
- `boolean clearConsole()`: 清空控制台
- `boolean setLedColor(int r, int g, int b)`: 设置LED颜色
- `Float getTemperature()`: 获取温度
- `Float getHumidity()`: 获取湿度
- `void listAvailableDevices()`: 列出可用设备
- `void close()`: 关闭连接

### DeviceConfig 配置方法

- `int getVendorId()`: 获取供应商ID
- `int getProductId()`: 获取产品ID
- `int getTimeout()`: 获取超时时间
- `boolean isTemperatureEnabled()`: 检查温度传感器是否启用
- `void printConfig()`: 打印所有配置

## 版本历史

- **v2.0**: 添加配置管理、完善错误处理、支持批量命令
- **v1.0**: 基础HID通信功能

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题或建议，请通过以下方式联系：
- GitHub Issues
- 项目维护者邮箱
