package algorithm;

import model.*;
import db.DAO;
import java.util.*;

/**
 * ════════════════════════════════════════════════════════════════
 *  GeneticAlgorithm.java
 * ════════════════════════════════════════════════════════════════
 *
 *  GENETIC ALGORITHM — HOW IT WORKS
 *  ──────────────────────────────────────────────────────────────
 *  A Genetic Algorithm (GA) is a metaheuristic inspired by
 *  biological evolution. It finds good solutions to hard
 *  optimization problems (like timetabling) without checking
 *  every possible solution.
 *
 *  KEY CONCEPTS:
 *  ┌──────────────────────────────────────────────────────────┐
 *  │  Chromosome  = one complete timetable (5 days × 6 slots) │
 *  │  Gene        = one period assignment (subject + teacher)  │
 *  │  Population  = a set of chromosomes (candidate solutions) │
 *  │  Fitness     = score for how "good" a chromosome is      │
 *  │  Selection   = pick the best chromosomes to be "parents" │
 *  │  Crossover   = combine two parents to make a child       │
 *  │  Mutation    = randomly change one gene occasionally     │
 *  └──────────────────────────────────────────────────────────┘
 *
 *  ALGORITHM FLOW:
 *   1. Generate an initial population of random chromosomes.
 *   2. Evaluate fitness of each chromosome.
 *   3. Select the top chromosomes (elitism + tournament).
 *   4. Create new generation via crossover and mutation.
 *   5. Repeat steps 2–4 for MAX_GENERATIONS.
 *   6. Return the chromosome with the highest fitness.
 *
 *  FITNESS FUNCTION (higher = better):
 *   Start with 1000 points.  Subtract penalties for:
 *   • Same subject in consecutive periods      (-20 each)
 *   • Teacher double-booked in same period     (-50 each)
 *   • Subject count deviates from hours/week   (-10 per hour gap)
 *   • Same subject > 2 times in a day          (-15 each extra)
 *
 * ════════════════════════════════════════════════════════════════
 */
public class GeneticAlgorithm {

    // ── GA hyper-parameters ───────────────────────────────────
    private static final int    POPULATION_SIZE  = 80;   // chromosomes per generation
    private static final int    MAX_GENERATIONS  = 200;  // evolution cycles
    private static final double CROSSOVER_RATE   = 0.85; // probability of crossover
    private static final double MUTATION_RATE    = 0.08; // probability of mutation per gene
    private static final int    ELITE_COUNT      = 6;    // top N survive unchanged
    private static final int    TOURNAMENT_SIZE  = 5;    // contestants per tournament

    // ── Schedule constants ────────────────────────────────────
    public static final int DAYS    = 5;   // Mon–Fri
    public static final int PERIODS = 6;   // P1–P6
    public static final int SLOTS   = DAYS * PERIODS; // 30 total

    // ── Problem data ──────────────────────────────────────────
    private final ClassSection           classSection;
    private final List<Subject>          subjects;
    private final Map<Integer,List<Integer>> subjectTeacherMap; // subjectId → [teacherIds]
    private final Random                 rng;

    // Progress callback (called after each generation to update progress bar)
    private ProgressCallback progressCallback;

    @FunctionalInterface
    public interface ProgressCallback {
        void update(int generation, int maxGenerations, int bestFitness);
    }

    /**
     * @param classSection    the class this timetable is for
     * @param subjects        subjects to schedule
     * @param subjectTeacherMap  which teachers can teach each subject
     */
    public GeneticAlgorithm(ClassSection classSection,
                             List<Subject> subjects,
                             Map<Integer, List<Integer>> subjectTeacherMap) {
        this.classSection       = classSection;
        this.subjects           = subjects;
        this.subjectTeacherMap  = subjectTeacherMap;
        this.rng                = new Random();
    }

    public void setProgressCallback(ProgressCallback cb) {
        this.progressCallback = cb;
    }

    // ══════════════════════════════════════════════════════════
    //  PUBLIC RUN METHOD
    // ══════════════════════════════════════════════════════════

    /**
     * Runs the GA and returns the best chromosome found.
     * May take a few seconds (run on a background SwingWorker thread).
     */
    public Chromosome run() {
        // Step 1 — Initialise population
        List<Chromosome> population = initialisePopulation();

        Chromosome bestEver = null;

        for (int gen = 0; gen < MAX_GENERATIONS; gen++) {

            // Step 2 — Evaluate fitness
            for (Chromosome c : population) {
                c.setFitness(FitnessEvaluator.evaluate(c, subjects, subjectTeacherMap));
            }

            // Sort descending by fitness
            population.sort((a, b) -> b.getFitness() - a.getFitness());

            // Track best solution ever seen
            if (bestEver == null || population.get(0).getFitness() > bestEver.getFitness()) {
                bestEver = population.get(0).deepCopy();
            }

            // Notify UI of progress
            if (progressCallback != null) {
                progressCallback.update(gen + 1, MAX_GENERATIONS, bestEver.getFitness());
            }

            // Early exit if perfect solution found
            if (bestEver.getFitness() >= 1000) break;

            // Step 3 — Build next generation
            List<Chromosome> nextGen = new ArrayList<>();

            // Elitism: top ELITE_COUNT survive unchanged
            for (int i = 0; i < ELITE_COUNT && i < population.size(); i++) {
                nextGen.add(population.get(i).deepCopy());
            }

            // Step 4 — Fill rest with crossover + mutation
            while (nextGen.size() < POPULATION_SIZE) {
                Chromosome parent1 = tournamentSelect(population);
                Chromosome parent2 = tournamentSelect(population);

                Chromosome child;
                if (rng.nextDouble() < CROSSOVER_RATE) {
                    child = crossover(parent1, parent2);
                } else {
                    child = parent1.deepCopy();
                }

                mutate(child);
                nextGen.add(child);
            }

            population = nextGen;
        }

        return bestEver;
    }

    // ══════════════════════════════════════════════════════════
    //  STEP 1: INITIALISATION
    // ══════════════════════════════════════════════════════════

    /**
     * Creates POPULATION_SIZE random chromosomes.
     * Each chromosome is a random valid assignment of
     * (subject, teacher) pairs to 30 time slots.
     */
    private List<Chromosome> initialisePopulation() {
        List<Chromosome> pop = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            pop.add(randomChromosome());
        }
        return pop;
    }

    /** Creates one random chromosome. */
    private Chromosome randomChromosome() {
        Gene[] genes = new Gene[SLOTS];
        for (int slot = 0; slot < SLOTS; slot++) {
            genes[slot] = randomGene();
        }
        return new Chromosome(genes, classSection.getId());
    }

    /** Picks a random (subject, teacher) pair from the available data. */
    private Gene randomGene() {
        if (subjects.isEmpty()) return new Gene(0, 0);

        Subject subj = subjects.get(rng.nextInt(subjects.size()));
        List<Integer> teacherIds = subjectTeacherMap.getOrDefault(
            subj.getId(), Collections.emptyList());

        int teacherId = teacherIds.isEmpty() ? 0
            : teacherIds.get(rng.nextInt(teacherIds.size()));

        return new Gene(subj.getId(), teacherId);
    }

    // ══════════════════════════════════════════════════════════
    //  STEP 3: SELECTION — Tournament
    // ══════════════════════════════════════════════════════════

    /**
     * Tournament selection: pick TOURNAMENT_SIZE random chromosomes,
     * return the one with the highest fitness.
     * This gives fitter chromosomes a better (but not guaranteed)
     * chance of being selected — maintaining diversity.
     */
    private Chromosome tournamentSelect(List<Chromosome> population) {
        Chromosome best = null;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            Chromosome candidate = population.get(rng.nextInt(population.size()));
            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        return best;
    }

    // ══════════════════════════════════════════════════════════
    //  STEP 4a: CROSSOVER — Single-point
    // ══════════════════════════════════════════════════════════

    /**
     * Single-point crossover:
     *   Pick a random crossover point k.
     *   Child takes genes [0..k] from parent1 and [k+1..end] from parent2.
     *
     *   Parent1: [A A A A A | B B B B B]
     *   Parent2: [C C C C C | D D D D D]
     *   Child:   [A A A A A | D D D D D]
     *                      ↑ crossover point k
     */
    private Chromosome crossover(Chromosome parent1, Chromosome parent2) {
        int crossoverPoint = 1 + rng.nextInt(SLOTS - 2); // avoid 0 and SLOTS-1
        Gene[] childGenes  = new Gene[SLOTS];

        for (int i = 0; i < SLOTS; i++) {
            childGenes[i] = (i <= crossoverPoint)
                ? parent1.getGene(i).copy()
                : parent2.getGene(i).copy();
        }

        return new Chromosome(childGenes, classSection.getId());
    }

    // ══════════════════════════════════════════════════════════
    //  STEP 4b: MUTATION
    // ══════════════════════════════════════════════════════════

    /**
     * For each gene, with probability MUTATION_RATE,
     * replace it with a random (subject, teacher) pair.
     *
     * Mutation prevents the population from converging too
     * quickly to a local optimum (getting "stuck").
     */
    private void mutate(Chromosome chromosome) {
        for (int i = 0; i < SLOTS; i++) {
            if (rng.nextDouble() < MUTATION_RATE) {
                chromosome.setGene(i, randomGene());
            }
        }
    }
}
