#include <Arduino.h>
#include <TFT_eSPI.h>         // TFT 屏幕库
#include <DHT.h>
#include <DHT_U.h>
#include <Adafruit_Sensor.h>

// DHT11 配置
#define DHTPIN 9
#define DHTTYPE DHT11
DHT_Unified dht(DHTPIN, DHTTYPE);

// TFT 配置
TFT_eSPI tft = TFT_eSPI();  // 使用 User_Setup.h 中配置的引脚

void setup() {
  Serial.begin(921600);

  // 初始化 TFT
  tft.init();
  tft.setRotation(1);  // 屏幕旋转，可根据需求调整
  tft.fillScreen(TFT_BLACK);

  // 初始化 DHT11
  dht.begin();

  // 清屏写标题
  tft.setTextSize(2);
  tft.setTextColor(TFT_WHITE, TFT_BLACK);
  tft.setCursor(10, 10);
  // tft.println("ESP32 DHT11 Demo");
}

void loop() {
  sensors_event_t event;

  // 读取温度
  dht.temperature().getEvent(&event);
  float temperature = isnan(event.temperature) ? 0 : event.temperature;

  // 读取湿度
  dht.humidity().getEvent(&event);
  float humidity = isnan(event.relative_humidity) ? 0 : event.relative_humidity;

  // 打印到串口
  Serial.print("温度: "); Serial.print(temperature); Serial.print(" C, ");
  Serial.print("湿度: "); Serial.print(humidity); Serial.println(" %");

  // 在屏幕显示
  tft.fillRect(0, 50, 240, 100, TFT_BLACK); // 清除上一条数据
  tft.setCursor(10, 60);
  tft.setTextSize(3);
  tft.setTextColor(TFT_RED, TFT_BLACK);
  tft.print("temp: "); tft.print(temperature); tft.println("C");

  tft.setCursor(10, 110);
  tft.setTextColor(TFT_BLUE, TFT_BLACK);
  tft.print("wet: "); tft.print(humidity); tft.println("%");

  delay(2000); // DHT11 采样间隔 2 秒
}
