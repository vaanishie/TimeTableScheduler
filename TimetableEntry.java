package model;

/**
 * model/TimetableEntry.java — POJO for one slot in the generated timetable.
 *
 * day_of_week:   0 = Monday … 4 = Friday
 * period_number: 0 = P1 … 5 = P6
 */
public class TimetableEntry {
    private int     id;
    private int     classSectionId;
    private int     subjectId;
    private int     teacherId;
    private int     dayOfWeek;
    private int     periodNumber;

    // Denormalized fields (for display — filled by joins in DAO)
    private String  subjectName;
    private String  subjectCode;
    private String  teacherName;
    private String  className;

    public TimetableEntry() {}
    public TimetableEntry(int classSectionId, int subjectId,
                          int teacherId, int day, int period) {
        this.classSectionId = classSectionId;
        this.subjectId      = subjectId;
        this.teacherId      = teacherId;
        this.dayOfWeek      = day;
        this.periodNumber   = period;
    }

    // ── Getters ───────────────────────────────────────────
    public int    getId()              { return id; }
    public int    getClassSectionId()  { return classSectionId; }
    public int    getSubjectId()       { return subjectId; }
    public int    getTeacherId()       { return teacherId; }
    public int    getDayOfWeek()       { return dayOfWeek; }
    public int    getPeriodNumber()    { return periodNumber; }
    public String getSubjectName()     { return subjectName; }
    public String getSubjectCode()     { return subjectCode; }
    public String getTeacherName()     { return teacherName; }
    public String getClassName()       { return className; }

    // ── Setters ───────────────────────────────────────────
    public void setId(int id)                        { this.id = id; }
    public void setClassSectionId(int id)            { this.classSectionId = id; }
    public void setSubjectId(int id)                 { this.subjectId = id; }
    public void setTeacherId(int id)                 { this.teacherId = id; }
    public void setDayOfWeek(int d)                  { this.dayOfWeek = d; }
    public void setPeriodNumber(int p)               { this.periodNumber = p; }
    public void setSubjectName(String n)             { this.subjectName = n; }
    public void setSubjectCode(String c)             { this.subjectCode = c; }
    public void setTeacherName(String n)             { this.teacherName = n; }
    public void setClassName(String n)               { this.className = n; }
}
