# 🗓 Timetable Generator v3

**B.Tech CS Project  •  Java Swing  +  SQLite  +  Genetic Algorithm**

A full-stack desktop scheduling application with an Admin Dashboard,
SQLite-backed persistence, and a Genetic Algorithm (GA) that evolves
conflict-free timetables over 200 generations.

---

## 📁 Project Structure

```
TimetableV3/
├── src/
│   ├── Main.java                        ← Entry point
│   │
│   ├── model/                           ← Data model (POJOs)
│   │   ├── Teacher.java
│   │   ├── Subject.java
│   │   ├── ClassSection.java
│   │   └── TimetableEntry.java
│   │
│   ├── db/                              ← Database layer
│   │   ├── DatabaseManager.java         ← SQLite connection (Singleton)
│   │   ├── SeedData.java                ← Sample data inserter
│   │   └── DAO.java                     ← All CRUD operations
│   │
│   ├── algorithm/                       ← Genetic Algorithm
│   │   ├── Gene.java                    ← One period assignment
│   │   ├── Chromosome.java              ← One full timetable solution
│   │   ├── FitnessEvaluator.java        ← Constraint scoring (0–1000)
│   │   └── GeneticAlgorithm.java        ← GA runner (init/select/crossover/mutate)
│   │
│   └── ui/                              ← Swing GUI
│       ├── Theme.java                   ← Colours, fonts, factory methods
│       ├── AdminDashboard.java          ← Main tabbed window
│       ├── DashboardPanel.java          ← Home tab with stats
│       ├── TeacherPanel.java            ← CRUD for teachers
│       ├── SubjectPanel.java            ← CRUD for subjects
│       ├── ClassPanel.java              ← CRUD for class sections
│       └── GeneratePanel.java           ← GA runner + timetable display
│
├── data/
│   └── timetable.db                     ← SQLite database (auto-created)
│
├── lib/
│   └── sqlite-jdbc-3.45.1.0.jar         ← Downloaded by setup.py
│
├── .vscode/
│   ├── settings.json                    ← Classpath config for VS Code
│   └── launch.json                      ← Run config for VS Code
│
├── setup.py                             ← ⬅ RUN THIS FIRST
├── run.bat                              ← Windows command-line launcher
├── run.sh                               ← Linux/macOS command-line launcher
└── README.md
```

---

## ▶️ How to Run

### Step 1 — Download the SQLite driver (one time only)

```bash
python setup.py
```

This downloads `sqlite-jdbc-3.45.1.0.jar` into `lib/` and configures
`.vscode/settings.json` automatically.

### Step 2A — VS Code

1. Open the `TimetableV3` folder in VS Code.
2. Wait for the Java Extension Pack to finish indexing
   (spinning icon disappears from the bottom status bar).
3. Open `src/Main.java`.
4. Click **▶ Run** above the `main()` method.

### Step 2B — Command Line

```bash
# Linux / macOS
chmod +x run.sh && ./run.sh

# Windows
run.bat
```

---

## 🧠 Genetic Algorithm — Explained

The timetable scheduling problem is an **NP-hard combinatorial
optimisation problem** — there are billions of possible schedules.
A Genetic Algorithm (GA) finds a near-optimal solution efficiently
by mimicking biological evolution.

### Representation

| Term         | In This Project                                     |
|--------------|-----------------------------------------------------|
| Gene         | One period assignment: (subjectId, teacherId)       |
| Chromosome   | Full 5-day × 6-period timetable = 30 genes          |
| Population   | 80 candidate chromosomes per generation             |
| Fitness      | Score 0–1000 (higher = fewer constraint violations) |

### Algorithm Flow

```
1. INITIALISATION
   └─ Create 80 random chromosomes (random subject/teacher per slot)

2. EVALUATION  (repeat for up to 200 generations)
   └─ Score each chromosome using FitnessEvaluator

3. SELECTION
   └─ Tournament selection: pick 5 random, keep the best
      (gives fitter chromosomes better odds without monopoly)

4. CROSSOVER  (85% probability)
   └─ Single-point crossover:
        Parent1: [A A A A | B B B B]
        Parent2: [C C C C | D D D D]
        Child:   [A A A A | D D D D]

5. MUTATION   (8% probability per gene)
   └─ Randomly replace a gene with a new (subject, teacher) pair
      (prevents premature convergence to local optima)

6. ELITISM
   └─ Top 6 chromosomes survive unchanged to next generation
      (ensures the best solution never gets lost)

7. REPEAT from step 2 until 200 generations or fitness = 1000
```

### Fitness Function

Starting score: **1000 points**. Subtract penalties for:

| Constraint           | Type | Penalty |
|----------------------|------|---------|
| Teacher double-booked in same period | Hard | −50 each |
| Invalid subject/teacher assignment   | Hard | −30 each |
| Same subject in consecutive periods  | Soft | −20 each |
| Subject exceeds 2× in one day        | Soft | −15 each |
| Weekly hours deviate from target     | Soft | −10 per hour gap |

---

## 🗄️ Database Schema

```sql
teachers         (id, name, email, specialization)
subjects         (id, name, code, hours_per_week)
class_sections   (id, name, year, branch, strength)
teacher_subjects (teacher_id, subject_id)          ← many-to-many
timetable_entries(id, class_section_id, subject_id,
                  teacher_id, day_of_week, period_number)
```

Sample data included: 10 teachers, 10 subjects, 6 class sections.

---

## 🎨 Features

| Feature | Detail |
|---------|--------|
| Admin Dashboard | Tabbed Swing window with sidebar navigation |
| Full CRUD | Add/edit/delete teachers, subjects, class sections |
| Custom subjects | Any subject name, code, and weekly hours |
| Teacher assignment | Checkboxes to assign which subjects a teacher can teach |
| GA with progress | Real-time progress bar + generation counter + fitness display |
| Multi-class | Generate and save separate timetables for every class section |
| Colour-coded grid | Each subject gets a distinct colour in the output table |
| SQLite persistence | All data and generated timetables saved to `data/timetable.db` |
| Load saved | Reload any previously generated timetable from the database |

---

## 📚 OOP & Design Patterns Used

| Pattern | Where |
|---------|-------|
| **Singleton** | `DatabaseManager` — one DB connection throughout the app |
| **DAO pattern** | `DAO.java` — separates data access from business logic |
| **MVC** | Models (`model/`), View (`ui/`), Controller logic in panels |
| **Observer** | `ProgressCallback` functional interface for GA→UI updates |
| **SwingWorker** | GA runs on background thread; EDT only updates UI |
| **Strategy** | `FitnessEvaluator` is swappable without changing the GA |

---

## 🔧 Requirements

- Java **11** or newer (Java 17/21 recommended)
- Python **3.6+** (for `setup.py` only)
- Internet connection (for `setup.py` to download the JAR)
- VS Code with **Extension Pack for Java** (Microsoft)
