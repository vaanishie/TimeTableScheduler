package model;

/**
 * ════════════════════════════════════════════════════════
 *  model/Teacher.java
 * ════════════════════════════════════════════════════════
 *  Plain Old Java Object (POJO) representing a teacher row.
 */
public class Teacher {
    private int    id;
    private String name;
    private String email;
    private String specialization;

    public Teacher() {}
    public Teacher(int id, String name, String email, String specialization) {
        this.id             = id;
        this.name           = name;
        this.email          = email;
        this.specialization = specialization;
    }

    public int    getId()              { return id; }
    public String getName()            { return name; }
    public String getEmail()           { return email; }
    public String getSpecialization()  { return specialization; }

    public void setId(int id)                          { this.id = id; }
    public void setName(String name)                   { this.name = name; }
    public void setEmail(String email)                 { this.email = email; }
    public void setSpecialization(String spec)         { this.specialization = spec; }

    @Override public String toString() { return name; }   // shown in JComboBox / JTable
}
