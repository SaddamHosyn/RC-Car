# Arduino Firmware Protocol

This document describes the serial communication protocol between the Android app (`RCCarController`) and the Arduino/microcontroller on the RC car.

---

## Command Reference (App → Car)

The app sends **single ASCII bytes** over Bluetooth serial (9600 baud by default).

| Byte | Command | Notes |
|------|---------|-------|
| `a`  | Forward | While held |
| `e`  | Backward | While held |
| `b`  | Turn Right | While held |
| `d`  | Turn Left | While held |
| `5`  | Stop | Sent on button release |
| `l`  | Line Follow On | Autonomous mode |
| `m`  | Line Follow Off | Return to manual |
| `1`  | Speed Low | ~40% PWM |
| `2`  | Speed Mid | ~65% PWM |
| `3`  | Speed High | 100% PWM |
| `t`  | Telemetry Request | Reply with a data line |

> **Note:** Because the Android app uses **press-and-hold** semantics, Stop (`5`) is automatically sent every time the user lifts their finger off a directional button. Your firmware does NOT need a timeout — it will always receive an explicit stop.

---

## Telemetry (Car → App)

When the car receives `t`, it should respond with **one line** (terminated by `\n`) in one of these formats:

### JSON (recommended)
```
{"bat":78,"dist":34}
```
- `bat` — battery percentage (0–100)
- `dist` — distance from ultrasonic sensor in centimetres

### CSV (simpler alternative)
```
78,34
```
(battery first, then distance)

---

## Example Arduino Sketch (HC-05, Classic BT)

```cpp
#include <SoftwareSerial.h>

// Adjust pins for your wiring
SoftwareSerial btSerial(10, 11); // RX, TX

// Motor driver pins (L298N example)
const int ENA = 5, IN1 = 4, IN2 = 3;   // Left motor
const int ENB = 6, IN3 = 7, IN4 = 8;   // Right motor
const int TRIG = 9, ECHO = 12;          // HC-SR04 ultrasonic
const int BAT_PIN = A0;                  // Battery voltage divider

int speedPwm = 165;  // Default mid speed

void setup() {
  btSerial.begin(9600);
  Serial.begin(9600);
  pinMode(IN1,  OUTPUT); pinMode(IN2,  OUTPUT); pinMode(ENA, OUTPUT);
  pinMode(IN3,  OUTPUT); pinMode(IN4,  OUTPUT); pinMode(ENB, OUTPUT);
  pinMode(TRIG, OUTPUT); pinMode(ECHO, INPUT);
  stopMotors();
}

void loop() {
  if (btSerial.available()) {
    char cmd = btSerial.read();
    switch (cmd) {
      case 'a': moveForward();  break;
      case 'e': moveBackward(); break;
      case 'b': turnRight();    break;
      case 'd': turnLeft();     break;
      case '5': stopMotors();   break;
      case '1': speedPwm = 100; break;
      case '2': speedPwm = 165; break;
      case '3': speedPwm = 255; break;
      case 'l': lineFollowMode(); break;
      case 'm': stopMotors();   break;
      case 't': sendTelemetry(); break;
    }
  }
}

void moveForward()  { setMotors(speedPwm, speedPwm, true,  true);  }
void moveBackward() { setMotors(speedPwm, speedPwm, false, false); }
void turnRight()    { setMotors(speedPwm, speedPwm, true,  false); }
void turnLeft()     { setMotors(speedPwm, speedPwm, false, true);  }
void stopMotors()   { analogWrite(ENA, 0); analogWrite(ENB, 0);    }

void setMotors(int leftPwm, int rightPwm, bool leftFwd, bool rightFwd) {
  digitalWrite(IN1, leftFwd  ? HIGH : LOW);
  digitalWrite(IN2, leftFwd  ? LOW  : HIGH);
  digitalWrite(IN3, rightFwd ? HIGH : LOW);
  digitalWrite(IN4, rightFwd ? LOW  : HIGH);
  analogWrite(ENA, leftPwm);
  analogWrite(ENB, rightPwm);
}

void sendTelemetry() {
  int bat  = readBattery();
  int dist = readDistance();
  btSerial.print("{\"bat\":");
  btSerial.print(bat);
  btSerial.print(",\"dist\":");
  btSerial.print(dist);
  btSerial.println("}");
}

int readBattery() {
  // Assumes a voltage divider: Vbat → 10kΩ → A0 → 10kΩ → GND
  // Adjust constants for your actual divider ratio and battery max voltage.
  float v = analogRead(BAT_PIN) * (5.0 / 1023.0) * 2.0; // scale factor = 2
  float pct = (v - 3.0) / (4.2 - 3.0) * 100.0;          // 3V=0%, 4.2V=100%
  return (int)constrain(pct, 0, 100);
}

long readDistance() {
  digitalWrite(TRIG, LOW); delayMicroseconds(2);
  digitalWrite(TRIG, HIGH); delayMicroseconds(10);
  digitalWrite(TRIG, LOW);
  long duration = pulseIn(ECHO, HIGH, 30000); // 30ms timeout
  return duration * 0.034 / 2;                // cm
}

void lineFollowMode() {
  // Implement with IR sensors — example stub
  // while no 'm' received, keep following the line
}
```

---

## Bluetooth Module Setup (HC-05)

1. Set baud rate to 9600: `AT+UART=9600,0,0`
2. Set role to Slave: `AT+ROLE=0`
3. Give it a recognisable name: `AT+NAME=MyCar`
4. Default password: `1234` or `0000`

After pairing on Android, the device will appear in the app's **Paired Devices** list without needing a scan.
