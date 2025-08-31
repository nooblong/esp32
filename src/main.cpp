#include <TFT_eSPI.h>
#include <SPI.h>
#include <DHT.h>

// DHT11配置
#define DHT_PIN 9
#define DHT_TYPE DHT11

// 屏幕配置
#define SCREEN_WIDTH 240
#define SCREEN_HEIGHT 240
#define MAX_LINES 10
#define LINE_HEIGHT 20
#define TEXT_SIZE 2
#define CONSOLE_START_Y 20

// 颜色定义
#define BACKGROUND_COLOR TFT_BLACK
#define TEXT_COLOR TFT_WHITE
#define TEMP_COLOR TFT_YELLOW
#define BORDER_COLOR TFT_BLUE

class ConsoleDisplay
{
private:
  TFT_eSPI tft;
  DHT dht;
  String consoleLines[MAX_LINES];
  int currentLine;
  float lastTemperature;
  unsigned long lastTempRead;
  const unsigned long TEMP_READ_INTERVAL = 2000; // 2秒读取一次温度

public:
  ConsoleDisplay() : tft(TFT_eSPI()), dht(DHT_PIN, DHT_TYPE),
                     currentLine(0), lastTemperature(0), lastTempRead(0)
  {
    // 初始化控制台行
    for (int i = 0; i < MAX_LINES; i++)
    {
      consoleLines[i] = "";
    }
  }

  void begin()
  {
    // 初始化TFT屏幕
    tft.init();
    tft.setRotation(0);
    tft.fillScreen(BACKGROUND_COLOR);
    tft.setTextColor(TEXT_COLOR, BACKGROUND_COLOR);
    tft.setTextSize(TEXT_SIZE);

    // 初始化DHT传感器
    dht.begin();

    // 初始化信息
    writeLine("Console Ready...");
    writeLine("DHT11 Initialized");
    writeLine("Temperature Monitor Active");
    writeLine("------------------------");

    Serial.begin(912600);
    Serial.println("Console Display System Started");
  }


  void writeLine(const String &text)
  {
    // 将新行添加到数组中
    consoleLines[currentLine] = text;
    currentLine = (currentLine + 1) % MAX_LINES;

    // 刷新显示
    refreshDisplay();

    // 串口输出
    Serial.println(text);
  }

  void refreshDisplay()
  {
    // 清除控制台区域
    tft.fillRect(0, CONSOLE_START_Y, SCREEN_WIDTH,
                 MAX_LINES * LINE_HEIGHT, BACKGROUND_COLOR);

    // 显示所有行，从最老的行开始
    int displayLine = 0;
    for (int i = currentLine; i < currentLine + MAX_LINES; i++)
    {
      int index = i % MAX_LINES;
      if (consoleLines[index].length() > 0)
      {
        int yPos = CONSOLE_START_Y + (displayLine * LINE_HEIGHT);

        // 如果是温度信息，使用不同颜色
        if (consoleLines[index].indexOf("Temp:") != -1)
        {
          tft.setTextColor(TEMP_COLOR, BACKGROUND_COLOR);
        }
        else
        {
          tft.setTextColor(TEXT_COLOR, BACKGROUND_COLOR);
        }

        // 截断过长的文本
        String displayText = consoleLines[index];
        if (displayText.length() > 38)
        { // 根据屏幕宽度调整
          displayText = displayText.substring(0, 35) + "...";
        }

        tft.drawString(displayText, 2, yPos, 1);
        displayLine++;
      }
    }
  }

  void checkTemperature()
  {
    unsigned long currentTime = millis();

    // 检查是否到了读取温度的时间
    if (currentTime - lastTempRead >= TEMP_READ_INTERVAL)
    {
      lastTempRead = currentTime;

      // 读取温度
      float temperature = dht.readTemperature();
      float humidity = dht.readHumidity();

      // 检查读取是否成功
      if (isnan(temperature) || isnan(humidity))
      {
        writeLine("DHT11 Read Error!");
        return;
      }

      // 检查温度是否有显著变化（0.5度以上）
      if (abs(temperature - lastTemperature) >= 0.5)
      {
        lastTemperature = temperature;

        // 格式化温度信息
        writeLine("Temp:" + String(temperature, 1));
        writeLine("Hum:" + String(humidity, 1) + "%");
      }
    }
  }

  void update()
  {
    checkTemperature();
    // 这里可以添加其他需要定期更新的功能
  }

  // 获取当前温度
  float getCurrentTemperature()
  {
    return lastTemperature;
  }

  // 清空控制台
  void clearConsole()
  {
    for (int i = 0; i < MAX_LINES; i++)
    {
      consoleLines[i] = "";
    }
    currentLine = 0;
    refreshDisplay();
    writeLine("Console Cleared");
  }

  // 添加分隔线
  void addSeparator()
  {
    writeLine("------------------------");
  }

  // 显示系统信息
  void showSystemInfo()
  {
    addSeparator();
    writeLine("System Info:");
    writeLine("Chip: ESP32");
    writeLine("Free Heap: " + String(ESP.getFreeHeap()) + " bytes");
    writeLine("Uptime: " + String(millis() / 1000) + " sec");
    addSeparator();
  }
};

// 全局对象
ConsoleDisplay console;

void setup()
{
  // 初始化控制台显示系统
  console.begin();

  // 显示启动信息
  delay(1000);
  console.writeLine("System Started Successfully");
  console.showSystemInfo();

  // 模拟一些系统消息
  console.writeLine("Sensors: OK");
  console.writeLine("Display: OK");
  console.writeLine("Ready for operation");
}

void loop()
{
  // 更新控制台（检查温度等）
  console.update();

  // 检查串口输入（可选功能）
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
      console.writeLine("User: " + input);
    }
  }

  delay(100); // 小延迟避免过度刷新
}
