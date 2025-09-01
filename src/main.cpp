#include <Arduino.h>
#include "../include/ConsoleDisplay.h"

void setup()
{
  // 初始化USB CDC串口
  Serial.begin(115200);
  while (!Serial && millis() < 5000) {
    delay(10); // 等待USB串口连接，最多等5秒
  }
  
  // 初始化控制台显示系统
  console.begin();

  // 显示启动信息
  console.writeLine("System Started Successfully");
  console.showSystemInfo();
}

void loop()
{
  // 更新控制台（检查温度等）
  console.update();

  // 检查USB CDC串口输入
  if (Serial.available())
  {
    String input = Serial.readStringUntil('\n');
    input.trim();

    if (input == "clear")
    {
      console.clearConsole();
    }
    else if (input == "info")
    {
      console.showSystemInfo();
    }
    else if (input.length() > 0)
    {
      console.writeLine("USB: " + input);
      Serial.println("Echo: " + input);
    }
  }

  // delay(100); // 小延迟避免过度刷新
}
