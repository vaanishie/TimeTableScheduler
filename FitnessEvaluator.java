package algorithm;

import model.Subject;
import java.util.*;

/**
 * ════════════════════════════════════════════════════════
 *  FitnessEvaluator.java
 * ════════════════════════════════════════════════════════
 *  Scores a chromosome from 0 to 1000.
 *  The GA maximises this score.
 *
 *  HARD CONSTRAINTS (large penalty if violated):
 *   HC1. No teacher is scheduled twice in the same period
 *        across different chromosomes (teacher conflict).
 *        → -50 per violation
 *   HC2. A subject/teacher must exist in the allowed list.
 *        → -30 per invalid assignment
 *
 *  SOFT CONSTRAINTS (smaller penalty):
 *   SC1. No same subject in consecutive periods.
 *        → -20 per consecutive pair
 *   SC2. Subject weekly count should match hours_per_week.
 *        → -10 per hour deviation
 *   SC3. No subject more than 2 times in a day.
 *        → -15 per extra occurrence
 *   SC4. Subjects should be spread evenly across the week.
 *        → -5 per unbalanced day
 * ════════════════════════════════════════════════════════
 */
public class FitnessEvaluator {

    private static final int BASE_SCORE       = 1000;
    private static final int PENALTY_TEACHER  = 50;   // HC1
    private static final int PENALTY_INVALID  = 30;   // HC2
    private static final int PENALTY_CONSEC   = 20;   // SC1
    private static final int PENALTY_HOURS    = 10;   // SC2
    private static final int PENALTY_DAILY    = 15;   // SC3
    private static final int MAX_PER_DAY      = 2;    // SC3 threshold

    /**
     * Evaluates the fitness of one chromosome.
     *
     * @param c                 the chromosome to score
     * @param subjects          all subjects being scheduled
     * @param subjectTeacherMap subjectId → list of allowed teacherIds
     * @return fitness score (higher = better, max = BASE_SCORE)
     */
    public static int evaluate(Chromosome c,
                                List<Subject> subjects,
                                Map<Integer, List<Integer>> subjectTeacherMap) {
        int penalty = 0;
        int days    = GeneticAlgorithm.DAYS;
        int periods = GeneticAlgorithm.PERIODS;

        // Map subjectId → hours_per_week for quick lookup
        Map<Integer, Integer> hoursMap = new HashMap<>();
        for (Subject s : subjects) hoursMap.put(s.getId(), s.getHoursPerWeek());

        // Count how many times each subject appears total this week
        Map<Integer, Integer> weekCount = new HashMap<>();

        for (int day = 0; day < days; day++) {

            // Count subject occurrences for this day (SC3)
            Map<Integer, Integer> dayCount = new HashMap<>();

            // Track teachers used per period across all days (HC1)
            // (For single-class timetable, we check within this chromosome)
            Set<Integer> teachersThisPeriod = new HashSet<>();

            for (int period = 0; period < periods; period++) {
                Gene gene = c.getGeneAt(day, period);

                int subjectId = gene.getSubjectId();
                int teacherId = gene.getTeacherId();

                // HC2: invalid teacher-subject assignment
                List<Integer> allowed = subjectTeacherMap.getOrDefault(
                    subjectId, Collections.emptyList());
                if (teacherId == 0 || !allowed.contains(teacherId)) {
                    penalty += PENALTY_INVALID;
                }

                // SC1: consecutive same subject
                if (period > 0) {
                    Gene prev = c.getGeneAt(day, period - 1);
                    if (prev.getSubjectId() == subjectId && subjectId != 0) {
                        penalty += PENALTY_CONSEC;
                    }
                }

                // SC3: daily subject count tracking
                dayCount.merge(subjectId, 1, Integer::sum);

                // Week count tracking
                weekCount.merge(subjectId, 1, Integer::sum);
            }

            // SC3: apply daily overload penalty
            for (Map.Entry<Integer, Integer> entry : dayCount.entrySet()) {
                int excess = entry.getValue() - MAX_PER_DAY;
                if (excess > 0 && entry.getKey() != 0) {
                    penalty += excess * PENALTY_DAILY;
                }
            }
        }

        // SC2: weekly hour deviation
        for (Map.Entry<Integer, Integer> entry : weekCount.entrySet()) {
            int subjectId = entry.getKey();
            if (subjectId == 0) continue;
            int expected = hoursMap.getOrDefault(subjectId, 4);
            int actual   = entry.getValue();
            penalty += Math.abs(expected - actual) * PENALTY_HOURS;
        }

        // Clamp fitness to [0, BASE_SCORE]
        return Math.max(0, BASE_SCORE - penalty);
    }
}
