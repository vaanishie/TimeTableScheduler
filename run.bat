@echo off
setlocal

echo.
echo  =========================================
echo   Timetable Generator v3  ^|  Windows
echo  =========================================
echo.

:: ── Check Java ──────────────────────────────────────────
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo  ERROR: Java not found on PATH.
    echo  Install Java 11+ from https://adoptium.net
    pause & exit /b 1
)

where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo  ERROR: javac ^(JDK^) not found. You need the JDK, not just the JRE.
    echo  Install from https://adoptium.net
    pause & exit /b 1
)

:: ── Check sqlite-jdbc.jar ───────────────────────────────
if not exist "lib\sqlite-jdbc-3.36.0.3.jar" (
    echo  sqlite-jdbc.jar not found. Running setup.py first...
    python setup.py
    if %errorlevel% neq 0 ( pause & exit /b 1 )
)

:: ── Compile ─────────────────────────────────────────────
echo  [1/2] Compiling...
if not exist out mkdir out

javac -cp "lib\sqlite-jdbc-3.36.0.3.jar" -d out ^
    src\Main.java ^
    src\model\Teacher.java ^
    src\model\Subject.java ^
    src\model\ClassSection.java ^
    src\model\TimetableEntry.java ^
    src\db\DatabaseManager.java ^
    src\db\SeedData.java ^
    src\db\DAO.java ^
    src\algorithm\Gene.java ^
    src\algorithm\Chromosome.java ^
    src\algorithm\FitnessEvaluator.java ^
    src\algorithm\GeneticAlgorithm.java ^
    src\ui\Theme.java ^
    src\ui\DashboardPanel.java ^
    src\ui\TeacherPanel.java ^
    src\ui\SubjectPanel.java ^
    src\ui\ClassPanel.java ^
    src\ui\GeneratePanel.java ^
    src\ui\AdminDashboard.java

if %errorlevel% neq 0 (
    echo.
    echo  COMPILATION FAILED. Check error messages above.
    pause & exit /b 1
)

echo  Compilation successful.
echo.

:: ── Run ─────────────────────────────────────────────────
echo  [2/2] Launching application...
java -cp "out;lib\sqlite-jdbc-3.36.0.3.jar" Main

pause
