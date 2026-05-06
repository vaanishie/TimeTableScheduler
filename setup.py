#!/usr/bin/env python3
"""
setup.py  —  One-time setup for Timetable Generator v3
═══════════════════════════════════════════════════════
Run this ONCE before opening the project in VS Code:

    python setup.py

What it does:
  1. Downloads sqlite-jdbc-3.36.0.3.jar into lib/
     (version 3.36.0.3 has ZERO extra dependencies — no slf4j needed)
  2. Creates .vscode/settings.json  so VS Code finds the JAR
  3. Creates .vscode/launch.json    so the Run button works
  4. Creates .classpath             so the Java Language Server works
  5. Verifies the database file exists
  6. Prints next steps

Requirements: Python 3.6+, internet connection.
"""

import urllib.request
import json
import os
import sys

# ── Config ────────────────────────────────────────────────────
# IMPORTANT: version 3.36.0.3 is fully self-contained.
# Versions 3.40+ require slf4j which causes ClassNotFoundException errors.
JAR_URL      = "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar"
JAR_FILENAME = "lib/sqlite-jdbc-3.36.0.3.jar"
VSCODE_DIR   = ".vscode"
SETTINGS_FILE = os.path.join(VSCODE_DIR, "settings.json")
LAUNCH_FILE   = os.path.join(VSCODE_DIR, "launch.json")
CLASSPATH_FILE = ".classpath"
DB_FILE       = "data/timetable.db"

def print_step(n, msg):
    print(f"\n  [{n}] {msg}")

def print_ok(msg):
    print(f"      ✅  {msg}")

def print_err(msg):
    print(f"      ❌  {msg}")

# ─────────────────────────────────────────────────────────────
print("\n" + "═"*57)
print("  Timetable Generator v3  —  Setup Script")
print("═"*57)

# ── Step 1: Download JAR ──────────────────────────────────────
print_step(1, "Downloading sqlite-jdbc-3.36.0.3.jar ...")
print("      (This version has NO extra dependencies — works out of the box)")

os.makedirs("lib", exist_ok=True)

if os.path.exists(JAR_FILENAME) and os.path.getsize(JAR_FILENAME) > 100_000:
    print_ok(f"JAR already exists: {JAR_FILENAME}  (skipping download)")
else:
    try:
        print(f"      Fetching from Maven Central...")
        urllib.request.urlretrieve(JAR_URL, JAR_FILENAME)
        size_kb = os.path.getsize(JAR_FILENAME) // 1024
        print_ok(f"Downloaded: {JAR_FILENAME}  ({size_kb} KB)")
    except Exception as e:
        print_err(f"Download failed: {e}")
        print(f"""
      MANUAL FIX — paste this URL into your browser and save
      the file into the lib/ folder:

      {JAR_URL}

      Save as:  {JAR_FILENAME}
      Then re-run:  python setup.py
      """)
        sys.exit(1)

# Absolute path to JAR (VS Code requires absolute paths on Windows)
jar_abs = os.path.abspath(JAR_FILENAME).replace("\\", "/")

# ── Step 2: .vscode/settings.json ────────────────────────────
print_step(2, "Writing .vscode/settings.json ...")

os.makedirs(VSCODE_DIR, exist_ok=True)

settings = {
    "java.project.referencedLibraries": [jar_abs],
    "java.project.sourcePaths": ["src"],
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "disabled",
    "editor.formatOnSave": False
}

with open(SETTINGS_FILE, "w") as f:
    json.dump(settings, f, indent=2)

print_ok(f"Written: {SETTINGS_FILE}")
print(f"         JAR → {jar_abs}")

# ── Step 3: .vscode/launch.json ──────────────────────────────
print_step(3, "Writing .vscode/launch.json ...")

launch = {
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Launch Timetable Generator v3",
            "request": "launch",
            "mainClass": "Main",
            "projectName": "TimetableV3",
            "classPaths": ["${workspaceFolder}/out", jar_abs],
            "sourcePaths": ["${workspaceFolder}/src"],
            "vmArgs": "-Djava.awt.headless=false"
        }
    ]
}

with open(LAUNCH_FILE, "w") as f:
    json.dump(launch, f, indent=2)

print_ok(f"Written: {LAUNCH_FILE}")

# ── Step 4: .classpath ───────────────────────────────────────
print_step(4, "Writing .classpath ...")

classpath = f"""<?xml version="1.0" encoding="UTF-8"?>
<classpath>
    <classpathentry kind="src" path="src"/>
    <classpathentry kind="output" path="out"/>
    <classpathentry kind="lib" path="{JAR_FILENAME}"/>
    <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
</classpath>
"""

with open(CLASSPATH_FILE, "w") as f:
    f.write(classpath)

print_ok(f"Written: {CLASSPATH_FILE}")

# ── Step 5: Verify database ───────────────────────────────────
print_step(5, "Checking database ...")

os.makedirs("data", exist_ok=True)

if os.path.exists(DB_FILE) and os.path.getsize(DB_FILE) > 0:
    size_kb = os.path.getsize(DB_FILE) // 1024
    print_ok(f"Database ready: {DB_FILE}  ({size_kb} KB — sample data included)")
else:
    print("      ℹ️  Database will be auto-created with sample data on first run.")

# ── Done ──────────────────────────────────────────────────────
print("\n" + "═"*57)
print("  ✅  Setup complete! No extra JARs needed.")
print("═"*57)
print("""
  NEXT STEPS:
  ───────────
  1. Open the TimetableV3 folder in VS Code
     (File → Open Folder → select TimetableV3)

  2. Wait ~10 seconds for the Java Extension Pack to index
     (spinning icon disappears from the bottom status bar)

  3. Open  src/Main.java

  4. Click  ▶ Run  above the main() method

  The Admin Dashboard opens with 10 teachers, 10 subjects,
  and 6 class sections pre-loaded. Go to ⚙ Generate tab!
""")
