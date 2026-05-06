#!/usr/bin/env bash
set -e

echo ""
echo " ========================================="
echo "  Timetable Generator v3  |  Linux/macOS"
echo " ========================================="
echo ""

# ── Check Java ─────────────────────────────────────────────
if ! command -v java &>/dev/null; then
    echo " ERROR: java not found."
    echo " Install Java 11+ from https://adoptium.net"
    exit 1
fi

if ! command -v javac &>/dev/null; then
    echo " ERROR: javac (JDK) not found. You need the full JDK, not just the JRE."
    echo " Install from https://adoptium.net"
    exit 1
fi

# ── Check JAR ──────────────────────────────────────────────
JAR="lib/sqlite-jdbc-3.36.0.3.jar"

if [ ! -f "$JAR" ]; then
    echo " sqlite-jdbc.jar not found. Running setup.py first..."
    python3 setup.py
fi

# ── Compile ────────────────────────────────────────────────
echo " [1/2] Compiling..."
mkdir -p out

javac -cp "$JAR" -d out \
    src/Main.java \
    src/model/Teacher.java \
    src/model/Subject.java \
    src/model/ClassSection.java \
    src/model/TimetableEntry.java \
    src/db/DatabaseManager.java \
    src/db/SeedData.java \
    src/db/DAO.java \
    src/algorithm/Gene.java \
    src/algorithm/Chromosome.java \
    src/algorithm/FitnessEvaluator.java \
    src/algorithm/GeneticAlgorithm.java \
    src/ui/Theme.java \
    src/ui/DashboardPanel.java \
    src/ui/TeacherPanel.java \
    src/ui/SubjectPanel.java \
    src/ui/ClassPanel.java \
    src/ui/GeneratePanel.java \
    src/ui/AdminDashboard.java

echo " Compilation successful."
echo ""

# ── Run ────────────────────────────────────────────────────
echo " [2/2] Launching application..."
java -cp "out:$JAR" Main
