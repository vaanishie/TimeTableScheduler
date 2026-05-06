package algorithm;

/**
 * ════════════════════════════════════════════════════════
 *  Gene.java  —  One slot in the timetable chromosome
 * ════════════════════════════════════════════════════════
 *  A gene represents the assignment for ONE period:
 *    which subject is being taught, and by which teacher.
 *
 *  30 genes (5 days × 6 periods) make one Chromosome.
 */
public class Gene {
    private int subjectId;
    private int teacherId;

    public Gene(int subjectId, int teacherId) {
        this.subjectId = subjectId;
        this.teacherId = teacherId;
    }

    public int getSubjectId() { return subjectId; }
    public int getTeacherId() { return teacherId; }

    public void setSubjectId(int id) { this.subjectId = id; }
    public void setTeacherId(int id) { this.teacherId = id; }

    /** Deep copy for crossover / elitism. */
    public Gene copy() {
        return new Gene(subjectId, teacherId);
    }

    @Override
    public String toString() {
        return "Gene[sub=" + subjectId + ", teach=" + teacherId + "]";
    }
}
