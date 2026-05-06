package algorithm;

/**
 * ════════════════════════════════════════════════════════
 *  Chromosome.java  —  One complete timetable solution
 * ════════════════════════════════════════════════════════
 *  A chromosome is an array of 30 genes (5×6 grid).
 *
 *  Gene index mapping:
 *    index = day * PERIODS + period
 *    day   = index / PERIODS
 *    period= index % PERIODS
 *
 *  e.g.  index 0  = Monday   P1
 *         index 5  = Monday   P6
 *         index 6  = Tuesday  P1
 *         index 29 = Friday   P6
 */
public class Chromosome {

    private final Gene[] genes;         // array of 30 genes
    private final int    classSectionId;
    private int          fitness;       // set by FitnessEvaluator

    public Chromosome(Gene[] genes, int classSectionId) {
        this.genes          = genes;
        this.classSectionId = classSectionId;
        this.fitness        = 0;
    }

    // ── Accessors ─────────────────────────────────────────
    public Gene   getGene(int index)         { return genes[index]; }
    public void   setGene(int index, Gene g) { genes[index] = g; }
    public int    getFitness()               { return fitness; }
    public void   setFitness(int f)          { this.fitness = f; }
    public int    getClassSectionId()        { return classSectionId; }
    public int    length()                   { return genes.length; }

    /**
     * Helper: get the gene for a specific (day, period) coordinate.
     * day    = 0–4  (Mon–Fri)
     * period = 0–5  (P1–P6)
     */
    public Gene getGeneAt(int day, int period) {
        return genes[day * GeneticAlgorithm.PERIODS + period];
    }

    /** Full deep copy — used for elitism and crossover. */
    public Chromosome deepCopy() {
        Gene[] copy = new Gene[genes.length];
        for (int i = 0; i < genes.length; i++) {
            copy[i] = genes[i].copy();
        }
        return new Chromosome(copy, classSectionId);
    }

    @Override
    public String toString() {
        return "Chromosome[fitness=" + fitness + ", classId=" + classSectionId + "]";
    }
}
