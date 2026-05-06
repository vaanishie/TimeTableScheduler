package model;

/**
 * model/ClassSection.java — POJO for the class_sections table.
 */
public class ClassSection {
    private int    id;
    private String name;
    private int    year;
    private String branch;
    private int    strength;

    public ClassSection() {}
    public ClassSection(int id, String name, int year, String branch, int strength) {
        this.id       = id;
        this.name     = name;
        this.year     = year;
        this.branch   = branch;
        this.strength = strength;
    }

    public int    getId()       { return id; }
    public String getName()     { return name; }
    public int    getYear()     { return year; }
    public String getBranch()   { return branch; }
    public int    getStrength() { return strength; }

    public void setId(int id)             { this.id = id; }
    public void setName(String name)      { this.name = name; }
    public void setYear(int year)         { this.year = year; }
    public void setBranch(String branch)  { this.branch = branch; }
    public void setStrength(int s)        { this.strength = s; }

    @Override public String toString() {
        return "Year " + year + " – " + name + " (" + branch + ")";
    }
}
