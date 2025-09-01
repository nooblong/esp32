#include <TFT_eSPI.h>
#include <DHT.h>
#include <SPI.h>

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
public:
  ConsoleDisplay();
  void begin();
  void writeLine(const String &text);
  void refreshDisplay();
  void checkTemperature();
  void update();
  float getCurrentTemperature();
  void clearConsole();
  void addSeparator();
  void showSystemInfo();

private:
  TFT_eSPI tft;
  DHT dht;
  String consoleLines[MAX_LINES];
  int currentLine;
  float lastTemperature;
  unsigned long lastTempRead;
  const unsigned long TEMP_READ_INTERVAL = 2000; // 2秒读取一次温度
};

extern ConsoleDisplay console;
