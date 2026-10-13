# 🚗 Bluetooth-Controlled RC Car — Undergraduate Final Year Project (FYP)

> **App Name:** BT Control Car  
> **Package:** `com.ars.myrccar`  
> **Platform:** Android (min SDK 8 — Android 2.2 Froyo, target SDK 20 — Android 4.4 KitKat)  
> **Language:** Java  
> **Build System:** Android Eclipse ADT (legacy)  
> **Author credit:** Arvind Sanjeev — DIYhacking.com (original base), adapted for FYP

---

## 📌 Project Overview

This is an **Android mobile application** developed as a Final Year Project (FYP). It allows a user to **remotely control a physical RC (Radio-Controlled) car over a Bluetooth connection**. The app pairs with a Bluetooth module (e.g., a FireFly or HC-05 module) attached to the RC car's microcontroller, then sends single-character commands to drive the car in different directions.

The physical hardware side consists of a microcontroller (e.g., Arduino) mounted on the RC car chassis, which receives Bluetooth serial commands and translates them into motor control signals.

---

## 🏗️ Project Structure

```
Undergraduate_Project_Work/
│
├── My RC Car/                          ← Main Android application source
│   ├── AndroidManifest.xml             ← App configuration & permissions
│   ├── project.properties              ← Android SDK target (API 20)
│   ├── proguard.cfg                    ← Code obfuscation config
│   ├── .classpath / .project           ← Eclipse IDE project files
│   │
│   ├── src/com/ars/myrccar/            ← Java source files
│   │   ├── FrontPage.java              ← Launch/home screen Activity
│   │   ├── Menuuu.java                 ← Main control screen (Bluetooth + buttons)
│   │   ├── MyRCCarActivity.java        ← Entry-point Activity (BT check & redirect)
│   │   ├── Abouut.java                 ← About screen Activity
│   │   └── Help.java                   ← Help screen Activity
│   │
│   └── res/
│       ├── layout/
│       │   ├── frontpage.xml           ← Home screen UI layout
│       │   ├── main.xml                ← Control pad UI layout
│       │   ├── abouut.xml              ← About page UI layout
│       │   └── help.xml                ← Help page UI layout
│       ├── drawable-*/                 ← Icons and button images (multi-density)
│       ├── values/strings.xml          ← App string resources
│       └── raw/                        ← Raw assets (audio, etc.)
│
├── RC Car/My RC Car/                   ← Duplicate/backup copy of the app
├── Project Raw Data/                   ← Supporting research & thesis documents
│   ├── FYP THESIS.docx                 ← Full written thesis
│   ├── bluetooth.docx                  ← Bluetooth research notes
│   ├── vs.docx                         ← Comparison/analysis document
│   ├── classdi.jpg                     ← UML Class diagram image
│   └── hjdgdu/                         ← Physical car photos (Nov 2015)
│       ├── 20151112_125005.jpg
│       ├── 20151112_125025.jpg
│       └── 20151112_125034.jpg
│
├── FYP Final PPT.PPT                   ← Final presentation slides
├── Final Final....docx / .pdf          ← Final project report (Word + PDF)
├── Final last final.pdf                ← Another version of the final report
├── fyp SRS.doc                         ← Software Requirements Specification
├── fy project work.doc                 ← Project work documentation
└── connectbot-master.zip               ← ConnectBot SSH/Bluetooth terminal (reference)
```

---

## 🔑 Key Features

| Feature | Description |
|---|---|
| **Bluetooth Pairing** | Connects to a named Bluetooth device entered by the user |
| **Directional Control** | Forward, Backward, Left, Right buttons send commands to the car |
| **Stop Command** | Dedicated stop button halts all movement |
| **BT Availability Check** | Detects if Bluetooth hardware exists; prompts to enable it if off |
| **Help Screen** | In-app help page for user guidance |
| **About Screen** | Credits and app information screen |

---

## 🧭 App Flow (Screen Navigation)

```
[App Launch]
     │
     ▼
MyRCCarActivity  ──► checks Bluetooth availability
     │                   └── no BT → shows Toast + finishes
     │
     ▼
FrontPage  (Home Screen)
     │
     ├──[Control Button]──► Menuuu  (Control Pad)
     ├──[Help Button]────► Help
     └──[About Button]───► Abouut
```

---

## 📱 Activity / Screen Breakdown

### 1. `MyRCCarActivity.java` — App Entry Point
- The very first Activity launched (though `FrontPage` is the LAUNCHER in the manifest — this acts as an intermediate initializer).
- Checks if the device has a Bluetooth adapter.
- If no Bluetooth is found → shows a Toast message and calls `finish()`.
- If Bluetooth is found but disabled → requests the user to enable it via a system dialog.
- Immediately redirects to `Menuuu` via an explicit Intent.

---

### 2. `FrontPage.java` — Home / Splash Screen
- **Launcher Activity** (entry point as registered in `AndroidManifest.xml`).
- Displays the app title: **"Bluetooth Control Car"**.
- Shows three navigation buttons:
  - **Control** → launches `Menuuu` (the driving control screen)
  - **Help** → launches `Help`
  - **About** → launches `Abouut`

---

### 3. `Menuuu.java` — Main Control Activity ⭐ (Core Logic)
This is the heart of the application. It handles:

#### Bluetooth Connection (`findBT()` method)
1. Gets the default `BluetoothAdapter`.
2. If Bluetooth is off, sends an `ACTION_REQUEST_ENABLE` Intent to turn it on.
3. Iterates through all **already-paired devices** (`getBondedDevices()`).
4. Matches the device by name (typed into the `EditText` field by the user).
5. Opens an **RFCOMM socket** using the standard Serial Port Profile UUID:  
   `00001101-0000-1000-8000-00805f9b34fb`
6. Connects the socket and obtains `OutputStream` + `InputStream`.

#### Control Commands (Button → Byte sent over Bluetooth)

| Button | Character Sent | Direction |
|--------|---------------|-----------|
| button1 (▲) | `'a'` | Forward |
| button2 (▼) | `'e'` | Backward |
| button3 (►) | `'b'` | Right |
| button4 (◄) | `'d'` | Left |
| button5 (⬛) | `'5'` | Stop |
| button6 (OK) | *(triggers BT connect)* | — |

> Each button press writes a single ASCII character byte to the Bluetooth `OutputStream`. The microcontroller on the car reads these bytes and acts accordingly.

#### UI Layout (`res/layout/main.xml`)
- A **D-pad style layout** with arrow buttons arranged around a central Stop button.
- An `EditText` at the bottom for entering the Bluetooth device name.
- An **OK button** to initiate connection.
- A background image (`@drawable/bg`) for styling.

---

### 4. `Abouut.java` — About Screen
- Simple Activity that loads `res/layout/abouut.xml`.
- Displays credit/about information.

### 5. `Help.java` — Help Screen
- Simple Activity that loads `res/layout/help.xml`.
- Displays usage instructions.

---

## 🔵 Bluetooth Protocol

The app uses the **Android Bluetooth SPP (Serial Port Profile)** — a classic Bluetooth protocol that emulates a serial RS-232 connection. This is the standard way to communicate with microcontroller Bluetooth modules like:

- **HC-05 / HC-06** (most common for Arduino projects)
- **FireFly** (referenced in code comments as `FireFly-B1A7`)
- Any module that presents itself as a serial COM port over Bluetooth

The UUID `00001101-0000-1000-8000-00805f9b34fb` is the **well-known UUID for SPP** and is universally recognized by all such modules.

**Communication is one-way (phone → car):** The app only writes bytes out. The `InputStream` is obtained but never read (the `beginListenForData()` call is commented out), meaning there is no feedback from the car back to the phone.

---

## 📋 Android Manifest Summary

```xml
Package: com.ars.myrccar
Min SDK: 8 (Android 2.2)
Target SDK: 20 (Android 4.4)

Permissions:
  - BLUETOOTH       → communicate with paired devices
  - BLUETOOTH_ADMIN → enable/disable Bluetooth, manage pairing

Activities:
  - FrontPage       (LAUNCHER — shown on app open)
  - Menuuu          (control pad screen)
  - MyRCCarActivity (dialog-theme intermediate activity)
  - Abouut          (about screen)
  - Help            (help screen)
```

---

## 🔧 Hardware Context

Based on the project files and thesis documents, the physical RC car setup consists of:

- **RC Car chassis** with motors
- **Arduino (or similar microcontroller)** as the brain
- **Bluetooth module** (HC-05/HC-06 or FireFly) connected to Arduino via UART serial
- **Motor driver module** (e.g., L298N) controlled by the Arduino
- The Arduino firmware reads the single-character serial byte from Bluetooth and drives the motors accordingly

Photos of the actual physical car are stored in:  
`Project Raw Data/hjdgdu/` — taken November 12, 2015.

---

## 📄 Supporting Documents

| File | Purpose |
|------|---------|
| `FYP THESIS.docx` | Full academic thesis write-up |
| `FYP Final PPT.PPT` | Presentation slides for the FYP defence |
| `fyp SRS.doc` | Software Requirements Specification document |
| `Final Final....docx/pdf` | Final submitted project report |
| `bluetooth.docx` | Research notes on Bluetooth technology |
| `vs.docx` | Comparative analysis (possibly comparing solutions/technologies) |
| `classdi.jpg` | UML Class diagram of the application |
| `connectbot-master.zip` | ConnectBot open-source app (likely used as a Bluetooth terminal reference) |

---

## ⚠️ Known Limitations & Notes

1. **No error recovery on BT disconnect** — if the Bluetooth connection drops mid-use, button presses will throw `IOException` and show a Toast, but won't attempt to reconnect automatically.
2. **Device must be pre-paired** — the app searches only through already-paired devices. It cannot discover/pair new devices on its own.
3. **One-way communication** — no feedback from the car (speed, battery, sensor data) is received.
4. **Legacy build system** — the project uses the old Eclipse ADT build system (not Gradle/Android Studio).
5. **`MyRCCarActivity` design issue** — it immediately starts `Menuuu` before even finishing the BT check, which is a logical race condition in the original code.
6. **Commented-out features** — several features are disabled/commented out: `beginListenForData()`, line-following mode (`line=6`), and a sound button (`button7` with `butt.start()`).

---

## 🗓️ Timeline

- **Development period:** ~2015 (based on photo timestamps: November 12, 2015)
- **Platform:** Android with Eclipse ADT (pre-Android Studio era)
- **Academic context:** Final Year Project (FYP) for undergraduate degree

---

---

# 🚀 Version 2.0 — Modern Rewrite (2026)

> **Location:** `RCCarController/` (same root directory)  
> **Package:** `com.rccar.controller`  
> **Language:** Kotlin 2.0  
> **Build System:** Android Studio + Gradle 8 (Kotlin DSL)  
> **UI:** Jetpack Compose + Material 3  
> **Architecture:** MVVM + Repository  
> **Min SDK:** 24 (Android 7.0)

The original FYP was fully rewritten from scratch using modern Android tooling. Every known limitation of v1.0 was directly addressed. The same Bluetooth serial protocol (`a`, `e`, `b`, `d`, `5`) is preserved so that the **existing Arduino firmware requires no changes**.

---

## 🆚 Before vs. After

| Problem in v1.0 | Solution in v2.0 |
|---|---|
| Java + Eclipse ADT | **Kotlin 2.0 + Android Studio + Gradle 8** |
| `Menuuu`, `Abouut` naming | **`ControlScreen`, `AboutScreen`, `ScanScreen`** — clear names |
| Bluetooth I/O on the main thread | **All I/O on `Dispatchers.IO` via Kotlin Coroutines** |
| No runtime permissions | **Accompanist handles API 31+ granular BT permissions** |
| User must type device name | **Live BT discovery with RSSI signal strength display** |
| Connection drop = dead, no recovery | **Auto-reconnect with exponential back-off (up to 5 attempts)** |
| Tap-to-drive (no auto-stop) | **Press-and-hold D-pad — `Stop` sent automatically on finger lift** |
| One-way communication only | **Bidirectional — parses battery % + ultrasonic distance data** |
| Multiple Activities, no shared state | **Single Activity + Navigation Compose + shared ViewModel** |
| XML layouts | **Jetpack Compose — dark navy + neon cyan Material 3 theme** |

---

## 🏗️ New Project Structure

```
RCCarController/
├── build.gradle.kts                        ← Root Gradle (Kotlin DSL)
├── settings.gradle.kts                     ← Project name + module includes
├── gradle/
│   ├── libs.versions.toml                  ← Version catalog (single source of truth)
│   └── wrapper/gradle-wrapper.properties   ← Gradle 8.10
├── ARDUINO_FIRMWARE.md                     ← Serial protocol reference + sample sketch
│
└── app/
    ├── build.gradle.kts                    ← App module dependencies
    └── src/main/
        ├── AndroidManifest.xml             ← Permissions (API 30 vs 31+ split)
        └── java/com/rccar/controller/
            ├── MainActivity.kt             ← Single Activity host, edge-to-edge
            ├── navigation/
            │   └── AppNavigation.kt        ← NavHost + route constants
            ├── bluetooth/
            │   ├── BluetoothService.kt     ← Core BT engine (coroutines, reconnect, RX/TX)
            │   └── CarCommand.kt           ← Sealed class of all commands
            ├── model/
            │   ├── CarDevice.kt            ← Domain model (wraps BluetoothDevice)
            │   ├── CarTelemetry.kt         ← Incoming data (battery %, distance cm)
            │   └── ConnectionState.kt      ← Sealed class: Disconnected/Connecting/Connected/Reconnecting/Error
            ├── viewmodel/
            │   └── BluetoothViewModel.kt   ← Shared VM: scan, connect, send, telemetry
            └── ui/
                ├── theme/
                │   ├── Color.kt            ← Dark navy + neon cyan palette
                │   ├── Theme.kt            ← RCCarTheme (always dark / OLED-friendly)
                │   └── Type.kt             ← Material 3 typography scale
                ├── components/
                │   ├── DPad.kt             ← Press-and-hold D-pad composable
                │   ├── StatusBar.kt        ← Animated connection status banner
                │   └── TelemetryCard.kt    ← Battery + distance cards
                ├── home/HomeScreen.kt      ← Landing screen with pulsing icon animation
                ├── scan/ScanScreen.kt      ← Permission request + discovery + pairing
                ├── control/ControlScreen.kt← D-pad + telemetry + speed + line-follow
                ├── help/HelpScreen.kt      ← Structured usage guide
                └── about/AboutScreen.kt    ← Tech stack + before/after comparison
```

---

## 🧱 Architecture

```
┌──────────────────────────────────────────────┐
│                  Compose UI                  │
│  HomeScreen  ScanScreen  ControlScreen  ...  │
└──────────────────┬───────────────────────────┘
                   │ collects StateFlow
┌──────────────────▼───────────────────────────┐
│           BluetoothViewModel                 │
│  - connectionState: StateFlow<ConnectionState>│
│  - telemetry: StateFlow<CarTelemetry>        │
│  - discoveredDevices: StateFlow<List<...>>   │
│  - errors: SharedFlow<String>                │
└──────────────────┬───────────────────────────┘
                   │ calls
┌──────────────────▼───────────────────────────┐
│           BluetoothService                   │
│  - BroadcastReceiver (device discovery)      │
│  - RFCOMM socket on Dispatchers.IO           │
│  - BufferedReader RX loop                    │
│  - Exponential back-off reconnect job        │
└──────────────────────────────────────────────┘
```

---

## 🔵 Bluetooth Protocol (unchanged from v1.0)

The same single-byte ASCII command set is used, so the original car firmware works without modification.

### Commands (App → Car)

| Byte | Action |
|------|--------|
| `a` | Forward |
| `e` | Backward |
| `b` | Turn Right |
| `d` | Turn Left |
| `5` | Stop |
| `l` | Line Follow On |
| `m` | Line Follow Off |
| `1` / `2` / `3` | Speed Low / Mid / High |
| `t` | Request Telemetry |

### Telemetry (Car → App)

The car responds to `t` with one newline-terminated line:

```json
{"bat":78,"dist":34}
```

Or CSV: `78,34` (battery %, distance cm). The parser handles both formats.  
See [`ARDUINO_FIRMWARE.md`](RCCarController/ARDUINO_FIRMWARE.md) for the full Arduino sketch.

---

## 📱 Screen Breakdown (v2.0)

### `HomeScreen`
- Animated pulsing glow around the car icon (infinite transition)
- Feature pills summarising app capabilities
- Single "Find My Car" CTA button

### `ScanScreen`
- Requests Bluetooth runtime permissions via Accompanist (handles API 30 vs 31+ difference)
- **Paired devices** listed first — tap to connect instantly
- **Live discovery** populates as devices are found, showing RSSI signal quality
- Spinner on the connecting device row during handshake
- Auto-navigates to `ControlScreen` on successful connection

### `ControlScreen`
- Animated `ConnectionStatusBar` at bottom (color changes: grey → amber → green)
- `TelemetryRow` — battery card (turns red < 20%) and distance card at top
- **D-pad** with press-and-hold; `Stop` is sent the moment the finger leaves the button
- **Speed selector** — Low / Mid / High filter chips
- **Line-Follow toggle** — switch to autonomous mode and back
- Snackbar for error events (auto-reconnect failures, etc.)
- Disconnect button returns to `ScanScreen`

### `HelpScreen`
- Structured sections: Getting Started, Controls, Speed, Line Follow, Telemetry protocol, Auto-Reconnect, Supported Modules

### `AboutScreen`
- Full tech stack table (ideal for portfolio / interviews)
- Before-vs-after checklist comparing v1.0 FYP to v2.0
- Original author credit preserved

---

## ⚙️ Permissions (v2.0)

```xml
<!-- SDK ≤ 30 (classic) -->
BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_FINE_LOCATION

<!-- SDK ≥ 31 (granular) -->
BLUETOOTH_SCAN (neverForLocation), BLUETOOTH_CONNECT
```

Permissions are requested at runtime inside `ScanScreen` via Accompanist, with a rationale screen if denied.

---

## 🗓️ Updated Timeline

| Year | Event |
|------|-------|
| 2015 | Original FYP built in Java + Eclipse ADT, targeting Android 2.2–4.4 |
| 2026 | Full rewrite in Kotlin + Jetpack Compose + Android Studio, targeting Android 7.0+ |
| 2026 | TCP simulation mode added — run and demo without physical hardware |

---

---

# 🖥️ Simulation Mode — No Hardware Required

Because the Android Emulator has no Bluetooth radio, a **TCP-based simulation layer** was added. The Android app connects to a **Python server** running on your PC over a local socket, which pretends to be the Arduino car.

```
Android Emulator
      │
      │  TCP socket to 10.0.2.2:9035
      ▼
ADB Reverse Port Forwarding  (adb reverse tcp:9035 tcp:9035)
      │
      ▼
Python car_simulator.py  (127.0.0.1:9035)
  ├── receives command bytes  →  prints ASCII dashboard
  └── sends telemetry JSON    ←  on 't' byte
```

---

## 📁 Simulator Files

```
RCCarController/
└── simulator/
    ├── car_simulator.py       ← Python TCP server (no external deps — stdlib only)
    └── start_simulator.bat   ← Windows one-click launcher (ADB forward + Python)
```

---

## 🔌 App-Side Changes

| File | Change |
|------|--------|
| `ICarService.kt` | New interface shared by `BluetoothService` and `TcpSimulationService` |
| `TcpSimulationService.kt` | Connects to `10.0.2.2:9035` via `java.net.Socket` on `Dispatchers.IO` |
| `BluetoothViewModel.kt` | `enableSimulationMode()` swaps `activeService` to TCP; `disableSimulationMode()` swaps back |
| `HomeScreen.kt` | Added **"Simulation Mode"** button (purple outline) below the main Bluetooth button |
| `AppNavigation.kt` | New `SIMULATION` route calls `enableSimulationMode()` then navigates to `ControlScreen` |
| `AndroidManifest.xml` | Added `INTERNET` permission (required for TCP socket) |

---

## 🚀 How to Run the Simulation

### Step 1 — Start the emulator
Open Android Studio → **Device Manager** → launch any AVD (API 24+).

### Step 2 — Start the simulator (one of two ways)

**Option A — Double-click:**
```
simulator\start_simulator.bat
```
This runs `adb reverse` and then starts the Python server automatically.

**Option B — Manual:**
```powershell
# In one terminal — set up ADB port forwarding
adb reverse tcp:9035 tcp:9035

# In another terminal — start the car simulator
python simulator\car_simulator.py
```

### Step 3 — Open the app in the emulator
Run the app from Android Studio. On the **Home screen**, tap **"Simulation Mode"** (the purple button).

The app connects to the Python server and jumps straight to the **Control screen**.

### Step 4 — Drive!
Press the D-pad buttons. The Python terminal shows a live dashboard:

```
──────────────────────────────────────────────
  Direction : FORWARD       ▲
  Speed     : MID
  Mode      : MANUAL
  Battery   : ████████░░  80%
  Distance  : 43 cm
──────────────────────────────────────────────
```

The **battery** slowly drains while moving. The **distance** fluctuates randomly (simulating an ultrasonic sensor). Both values appear in the app's **Telemetry cards** at the top of the control screen.

---

## 🧪 What Gets Tested in Simulation

| Feature | Tested in sim? |
|---|---|
| D-pad press-and-hold + auto-stop | ✅ |
| Speed selection (Low/Mid/High) | ✅ |
| Line-follow mode toggle | ✅ |
| Battery + distance telemetry display | ✅ (fake values) |
| Connection status banner | ✅ |
| Auto-reconnect | ❌ (sim always stays connected) |
| Device discovery & pairing | ❌ (sim skips scan entirely) |
| Real motor control | ❌ (needs physical hardware) |

---

## ⚙️ Simulator Protocol Reference

The Python simulator uses the same byte protocol as the real Arduino firmware:

| Byte | Simulator response |
|------|--------------------|
| `a`  | Prints `▲ FORWARD`, updates dashboard |
| `e`  | Prints `▼ BACKWARD` |
| `b`  | Prints `► TURN RIGHT` |
| `d`  | Prints `◄ TURN LEFT` |
| `5`  | Prints `⬛ STOP` |
| `1`/`2`/`3` | Updates speed label |
| `l`  | Sets autonomous mode ON |
| `m`  | Sets autonomous mode OFF |
| `t`  | Sends `{"bat":N,"dist":N}\n` back over the socket |

No pip installs required — the simulator uses only Python's built-in `socket`, `threading`, `json`, `random`, and `time` modules.
