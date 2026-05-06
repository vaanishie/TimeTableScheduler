package model;

/**
 * model/Subject.java — POJO for the subjects table.
 */
public class Subject {
    private int    id;
    private String name;
    private String code;
    private int    hoursPerWeek;

    public Subject() {}
    public Subject(int id, String name, String code, int hoursPerWeek) {
        this.id           = id;
        this.name         = name;
        this.code         = code;
        this.hoursPerWeek = hoursPerWeek;
    }

    public int    getId()           { return id; }
    public String getName()         { return name; }
    public String getCode()         { return code; }
    public int    getHoursPerWeek() { return hoursPerWeek; }

    public void setId(int id)                   { this.id = id; }
    public void setName(String name)             { this.name = name; }
    public void setCode(String code)             { this.code = code; }
    public void setHoursPerWeek(int h)           { this.hoursPerWeek = h; }

    @Override public String toString() { return code + " – " + name; }
}
