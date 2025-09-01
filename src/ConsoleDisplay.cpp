#include "../include/ConsoleDisplay.h"

ConsoleDisplay::ConsoleDisplay() : tft(TFT_eSPI()), dht(DHT_PIN, DHT_TYPE),
                                   currentLine(0), lastTemperature(0), lastTempRead(0)
{
    // 初始化控制台行
    for (int i = 0; i < MAX_LINES; i++)
    {
        consoleLines[i] = "";
    }
}

void ConsoleDisplay::begin()
{
    // 初始化TFT屏幕
    tft.init();
    tft.setRotation(0);
    tft.fillScreen(BACKGROUND_COLOR);
    tft.setTextColor(TEXT_COLOR, BACKGROUND_COLOR);
    tft.setTextSize(TEXT_SIZE);

    // 初始化DHT传感器
    dht.begin();
}

void ConsoleDisplay::writeLine(const String &text)
{
    // 将新行添加到数组中
    consoleLines[currentLine] = text;
    currentLine = (currentLine + 1) % MAX_LINES;

    // 刷新显示
    refreshDisplay();

    // 串口输出
    Serial.println(text);
}

void ConsoleDisplay::refreshDisplay()
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

void ConsoleDisplay::checkTemperature()
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

void ConsoleDisplay::update()
{
    checkTemperature();
    // 这里可以添加其他需要定期更新的功能
}

// 获取当前温度
float ConsoleDisplay::getCurrentTemperature()
{
    return lastTemperature;
}

// 清空控制台
void ConsoleDisplay::clearConsole()
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
void ConsoleDisplay::addSeparator()
{
    writeLine("------------------------");
}

// 显示系统信息
void ConsoleDisplay::showSystemInfo()
{
    addSeparator();
    writeLine("System Info:");
    writeLine("Chip: ESP32");
    writeLine("Free Heap: " + String(ESP.getFreeHeap()) + " bytes");
    writeLine("Uptime: " + String(millis() / 1000) + " sec");
    addSeparator();
}

// 全局对象
ConsoleDisplay console;
