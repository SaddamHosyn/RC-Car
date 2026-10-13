#!/usr/bin/env python3
"""
RC Car Terminal Simulator
=========================
Test the full RC car command protocol interactively in the terminal —
no Android app, no emulator, no ADB, no hardware needed.

Run it:
    python car_simulator_terminal.py

Controls:
    W / ↑   Forward
    S / ↓   Backward
    A / ←   Turn Left
    D / →   Turn Right
    SPACE   Stop
    1       Speed Low
    2       Speed Mid
    3       Speed High
    L       Toggle Line Follow mode
    T       Request & display telemetry
    H       Show help
    Q       Quit

Requires Python 3.6+ (stdlib only — no pip installs needed).
"""

import sys
import time
import random
import os
import threading

# Force UTF-8 output on Windows (avoids cp1252 UnicodeEncodeError)
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

# ── Platform-specific key reading ─────────────────────────────────────────────
# We need raw (non-blocking) single-keypress reading without pressing Enter.

if sys.platform == "win32":
    import msvcrt

    def get_key() -> str:
        """Read a single key on Windows (no Enter needed)."""
        ch = msvcrt.getwch()
        if ch in ('\x00', '\xe0'):          # special key prefix (arrows, function keys)
            ch2 = msvcrt.getwch()
            mapping = {
                'H': 'UP', 'P': 'DOWN', 'K': 'LEFT', 'M': 'RIGHT',
            }
            return mapping.get(ch2, f'SPECIAL_{ch2}')
        return ch

else:
    import tty, termios

    def get_key() -> str:
        """Read a single key on Linux/macOS."""
        fd = sys.stdin.fileno()
        old = termios.tcgetattr(fd)
        try:
            tty.setraw(fd)
            ch = sys.stdin.read(1)
            if ch == '\x1b':
                ch2 = sys.stdin.read(2)
                mapping = {
                    '[A': 'UP', '[B': 'DOWN', '[D': 'LEFT', '[C': 'RIGHT',
                }
                return mapping.get(ch2, f'ESC_{ch2}')
            return ch
        finally:
            termios.tcsetattr(fd, termios.TCSADRAIN, old)


# ── ANSI colour helpers ───────────────────────────────────────────────────────
def _c(code, t): return f"\033[{code}m{t}\033[0m"
def green(t):   return _c("92", t)
def cyan(t):    return _c("96", t)
def yellow(t):  return _c("93", t)
def red(t):     return _c("91", t)
def grey(t):    return _c("90", t)
def purple(t):  return _c("95", t)
def bold(t):    return _c("1",  t)
def dim(t):     return _c("2",  t)


# ── Car State ─────────────────────────────────────────────────────────────────

class Car:
    SPEEDS = {1: ("LOW",  "🐢"), 2: ("MID", "🚗"), 3: ("HIGH", "🚀")}

    def __init__(self):
        self.direction  = "STOPPED"
        self.speed_level= 2
        self.battery    = 100.0
        self.distance   = 80
        self.auto_mode  = False
        self._lock      = threading.Lock()

        # Drain battery while moving
        self._running = True
        threading.Thread(target=self._background_tick, daemon=True).start()

    def _background_tick(self):
        while self._running:
            time.sleep(0.5)
            with self._lock:
                if self.direction != "STOPPED":
                    drain = {"LOW": 0.05, "MID": 0.12, "HIGH": 0.25}
                    self.battery = max(0.0, self.battery - drain[self.SPEEDS[self.speed_level][0]])
                self.distance = max(2, self.distance + random.randint(-4, 4))

    def apply(self, byte: str) -> tuple[str, str]:
        """
        Apply a command character.
        Returns (description, emoji) for display.
        """
        with self._lock:
            if   byte == 'a': self.direction = "FORWARD";   return green("FORWARD"),   "▲"
            elif byte == 'e': self.direction = "BACKWARD";  return yellow("BACKWARD"),  "▼"
            elif byte == 'b': self.direction = "RIGHT";     return cyan("TURN RIGHT"),  "►"
            elif byte == 'd': self.direction = "LEFT";      return cyan("TURN LEFT"),   "◄"
            elif byte == '5': self.direction = "STOPPED";   return grey("STOP"),        "⬛"
            elif byte == '1': self.speed_level = 1;         return "Speed LOW",         "🐢"
            elif byte == '2': self.speed_level = 2;         return "Speed MID",         "🚗"
            elif byte == '3': self.speed_level = 3;         return "Speed HIGH",        "🚀"
            elif byte == 'l': self.auto_mode = True;        return cyan("Line Follow ON"),  "🔄"
            elif byte == 'm': self.auto_mode = False;       return "Line Follow OFF",   "🕹️"
            elif byte == 't': return self._telemetry_str(), "📡"
            else: return grey(f"Unknown: {byte!r}"), "?"

    def _telemetry_str(self):
        with self._lock:
            return f'{{"bat":{round(self.battery)},"dist":{self.distance}}}'

    def telemetry(self) -> dict:
        with self._lock:
            return {"bat": round(self.battery), "dist": self.distance}

    def dashboard(self) -> str:
        with self._lock:
            spd_name, spd_emoji = self.SPEEDS[self.speed_level]
            bat = round(self.battery)
            filled = bat // 10
            bar = green("#" * filled) + dim("." * (10 - filled))
            if bat <= 20:
                bar = red("#" * filled) + dim("." * (10 - filled))
            elif bat <= 50:
                bar = yellow("#" * filled) + dim("." * (10 - filled))

            dir_art = {
                "FORWARD":  f"      {bold(green('^'))}      ",
                "BACKWARD": f"      {bold(yellow('v'))}      ",
                "LEFT":     f"  {bold(cyan('<'))}            ",
                "RIGHT":    f"            {bold(cyan('>'))}  ",
                "STOPPED":  f"      {bold(grey('[STOP]'))}   ",
            }.get(self.direction, "      ?      ")

            mode_str = purple("[AUTO]") if self.auto_mode else dim("[MANUAL]")

        return (
            f"\n  +------------------------------------------+\n"
            f"  |{'RC CAR DASHBOARD':^42}|\n"
            f"  +------------------------------------------+\n"
            f"  |  Direction  |  {dir_art:<30}|\n"
            f"  |  Speed      |  {spd_name:<30}|\n"
            f"  |  Mode       |  {mode_str:<39}|\n"
            f"  |  Battery    |  [{bar}] {bat:>3}%      |\n"
            f"  |  Distance   |  {self.distance} cm{'':30}|\n"
            f"  +------------------------------------------+"
        )

    def stop(self):
        self._running = False


# ── Key → Command mapping ─────────────────────────────────────────────────────

KEY_MAP = {
    'w': ('a', "Forward"),     'W': ('a', "Forward"),     'UP':    ('a', "Forward"),
    's': ('e', "Backward"),    'S': ('e', "Backward"),    'DOWN':  ('e', "Backward"),
    'a': ('d', "Turn Left"),   'A': ('d', "Turn Left"),   'LEFT':  ('d', "Turn Left"),
    'd': ('b', "Turn Right"),  'D': ('b', "Turn Right"),  'RIGHT': ('b', "Turn Right"),
    ' ': ('5', "STOP"),
    '1': ('1', "Speed → Low"),
    '2': ('2', "Speed → Mid"),
    '3': ('3', "Speed → High"),
    'l': ('l', "Line Follow ON"),   'L': ('l', "Line Follow ON"),
    'm': ('m', "Line Follow OFF"),  'M': ('m', "Line Follow OFF"),
    't': ('t', "Telemetry"),        'T': ('t', "Telemetry"),
}

QUIT_KEYS  = {'q', 'Q', '\x03', '\x1b'}   # Q, Ctrl+C, Escape
HELP_KEY   = {'h', 'H', '?'}


# ── UI helpers ────────────────────────────────────────────────────────────────

def clear():
    os.system("cls" if sys.platform == "win32" else "clear")

def print_header():
    print(bold(cyan(
        "\n"
        "  RC CAR TERMINAL SIMULATOR v2.0\n"
        "  " + "=" * 40 + "\n"
    )))

def print_controls():
    print(f"""  {bold('Controls')}
  ─────────────────────────────────────────────
  {cyan('W')} / {cyan('↑')}      Forward          {cyan('SPACE')}  Stop
  {cyan('S')} / {cyan('↓')}      Backward         {cyan('1 2 3')}  Speed Low/Mid/High
  {cyan('A')} / {cyan('←')}      Turn Left        {cyan('L')}      Line Follow toggle
  {cyan('D')} / {cyan('→')}      Turn Right       {cyan('T')}      Request telemetry
  {cyan('H')}          Help / controls    {red('Q')}      Quit
  ─────────────────────────────────────────────
""")

def timestamp():
    return grey(time.strftime("%H:%M:%S"))


# ── Main loop ─────────────────────────────────────────────────────────────────

def main():
    clear()
    print_header()
    print_controls()

    car = Car()
    log: list[str] = []   # last N command lines
    MAX_LOG = 8

    print("  " + bold("Simulator running.") + "  Press a key to drive.\n")

    try:
        while True:
            key = get_key()

            if key in QUIT_KEYS:
                break

            if key in HELP_KEY:
                clear()
                print_header()
                print_controls()
                continue

            if key in KEY_MAP:
                cmd_byte, label = KEY_MAP[key]
                desc, emoji = car.apply(cmd_byte)

                if cmd_byte == 't':
                    tel = car.telemetry()
                    entry = f"  [{timestamp()}]  {emoji}  Telemetry → bat={green(str(tel['bat'])+'%')} dist={cyan(str(tel['dist'])+' cm')}"
                else:
                    entry = f"  [{timestamp()}]  {emoji}  {desc}"

                log.append(entry)
                if len(log) > MAX_LOG:
                    log.pop(0)

                # Redraw
                clear()
                print_header()
                print(car.dashboard())
                print()
                for line in log:
                    print(line)
                print()
                print(dim("  (press H for controls, Q to quit)"))

            else:
                # Unknown key — show it
                log.append(f"  [{timestamp()}]  {grey(f'Unknown key: {key!r}  (press H for help)')}")
                if len(log) > MAX_LOG:
                    log.pop(0)

    except KeyboardInterrupt:
        pass
    finally:
        car.stop()
        print(yellow("\n\n  Simulator stopped. Goodbye!\n"))


if __name__ == "__main__":
    main()
