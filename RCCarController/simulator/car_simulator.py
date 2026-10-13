#!/usr/bin/env python3
"""
RC Car Simulator
================
Simulates the Arduino car's Bluetooth serial interface over a TCP socket.

The Android app (in simulation mode) connects to this server via ADB port forwarding:
    adb reverse tcp:9035 tcp:9035

Then run the app in the emulator and tap "Simulation Mode" on the home screen.

Protocol (matches the real car firmware):
  Incoming bytes  → car command
  't' received    → server sends back telemetry JSON

Usage:
    python car_simulator.py
"""

import socket
import threading
import time
import random
import json
import os
import sys

HOST = "127.0.0.1"
PORT = 9035

# ── ANSI colour helpers ───────────────────────────────────────────────────────
def clr(code, text): return f"\033[{code}m{text}\033[0m"
def green(t):  return clr("92", t)
def cyan(t):   return clr("96", t)
def yellow(t): return clr("93", t)
def red(t):    return clr("91", t)
def grey(t):   return clr("90", t)
def bold(t):   return clr("1",  t)

# ── Car state ─────────────────────────────────────────────────────────────────
class CarState:
    def __init__(self):
        self.direction  = "STOPPED"
        self.speed      = "MID"
        self.battery    = 100           # % — slowly drains while moving
        self.distance   = 80            # cm — random ultrasonic reading
        self.auto_mode  = False
        self.lock       = threading.Lock()

    def apply_command(self, byte: int) -> str:
        """Apply a command byte and return a human-readable description."""
        ch = chr(byte)
        with self.lock:
            if   ch == 'a': self.direction = "FORWARD";    return green("▲  FORWARD")
            elif ch == 'e': self.direction = "BACKWARD";   return yellow("▼  BACKWARD")
            elif ch == 'b': self.direction = "RIGHT";      return cyan("►  TURN RIGHT")
            elif ch == 'd': self.direction = "LEFT";       return cyan("◄  TURN LEFT")
            elif ch == '5': self.direction = "STOPPED";    return grey("⬛  STOP")
            elif ch == '1': self.speed = "LOW";            return "🐢  Speed → LOW"
            elif ch == '2': self.speed = "MID";            return "🚗  Speed → MID"
            elif ch == '3': self.speed = "HIGH";           return "🚀  Speed → HIGH"
            elif ch == 'l': self.auto_mode = True;         return cyan("🔄  Line Follow ON (autonomous)")
            elif ch == 'm': self.auto_mode = False;        return "🕹️  Line Follow OFF (manual)"
            elif ch == 't':                                return None   # handled by caller
            else:           return grey(f"?  Unknown byte: 0x{byte:02X} ('{ch}')")

    def telemetry_json(self) -> bytes:
        """Generate a fake telemetry JSON line."""
        with self.lock:
            # Drain battery if moving
            if self.direction != "STOPPED":
                self.battery = max(0, self.battery - random.uniform(0.05, 0.2))
            # Simulate distance sensor noise
            self.distance = max(2, self.distance + random.randint(-5, 5))
        payload = json.dumps({
            "bat":  round(self.battery),
            "dist": self.distance,
        })
        return (payload + "\n").encode()

    def dashboard(self) -> str:
        """Render a small ASCII dashboard."""
        with self.lock:
            bat_bar = "█" * (self.battery // 10) + "░" * (10 - self.battery // 10)
            bat_color = green if self.battery > 50 else (yellow if self.battery > 20 else red)
            dir_map = {
                "FORWARD":  "    ▲    ",
                "BACKWARD": "    ▼    ",
                "LEFT":     "  ◄      ",
                "RIGHT":    "      ►  ",
                "STOPPED":  "   [■]   ",
            }
            arrow = dir_map.get(self.direction, "    ?    ")
            mode  = cyan("AUTO") if self.auto_mode else "MANUAL"
        return (
            f"\n{'─'*44}\n"
            f"  Direction : {bold(self.direction):<10}  {arrow}\n"
            f"  Speed     : {self.speed}\n"
            f"  Mode      : {mode}\n"
            f"  Battery   : {bat_color(bat_bar)} {round(self.battery)}%\n"
            f"  Distance  : {self.distance} cm\n"
            f"{'─'*44}"
        )


# ── Connection handler ─────────────────────────────────────────────────────────
def handle_client(conn: socket.socket, addr, state: CarState):
    print(cyan(f"\n  ✔  App connected from {addr[0]}:{addr[1]}"))
    print(grey("     Waiting for commands...\n"))

    try:
        while True:
            data = conn.recv(1)
            if not data:
                break                           # client disconnected
            byte = data[0]
            ch   = chr(byte)

            if ch == 't':
                # Telemetry request — send data back immediately
                tel = state.telemetry_json()
                conn.sendall(tel)
                print(grey(f"  📡  Telemetry sent: {tel.decode().strip()}"))
            else:
                desc = state.apply_command(byte)
                if desc:
                    ts = time.strftime("%H:%M:%S")
                    print(f"  [{grey(ts)}]  {desc}")
                    print(state.dashboard())

    except (ConnectionResetError, BrokenPipeError, OSError):
        pass
    finally:
        conn.close()
        print(red(f"\n  ✘  App disconnected.\n"))


# ── Main server loop ───────────────────────────────────────────────────────────
def main():
    # Clear terminal
    os.system("cls" if sys.platform == "win32" else "clear")

    print(bold(cyan("""
  ██████╗  ██████╗     ██████╗ █████╗ ██████╗
  ██╔══██╗██╔════╝    ██╔════╝██╔══██╗██╔══██╗
  ██████╔╝██║         ██║     ███████║██████╔╝
  ██╔══██╗██║         ██║     ██╔══██║██╔══██╗
  ██║  ██║╚██████╗    ╚██████╗██║  ██║██║  ██║
  ╚═╝  ╚═╝ ╚═════╝    ╚═════╝╚═╝  ╚═╝╚═╝  ╚═╝
       S I M U L A T O R  v2.0
""")))

    print(bold("  Setup instructions:"))
    print(f"  1. Run:  {cyan('adb reverse tcp:9035 tcp:9035')}")
    print(f"  2. Open the app in the Android Emulator")
    print(f"  3. On the Home screen, tap {cyan('Simulation Mode')}")
    print(f"  4. The app will connect here and you can drive!\n")
    print(f"  Listening on {bold(cyan(f'{HOST}:{PORT}'))} ...\n")

    state = CarState()

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as srv:
        srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        srv.bind((HOST, PORT))
        srv.listen(1)
        try:
            while True:
                conn, addr = srv.accept()
                t = threading.Thread(target=handle_client, args=(conn, addr, state), daemon=True)
                t.start()
        except KeyboardInterrupt:
            print(yellow("\n\n  Simulator stopped.\n"))


if __name__ == "__main__":
    main()
