package com.personal.esttimeconverter.roster;

public class Person {

    private String name;
    private String email;
    private PersonRole role;

    public Person() {
    }

    public Person(String name, String email, PersonRole role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PersonRole getRole() {
        return role;
    }

    public void setRole(PersonRole role) {
        this.role = role;
    }
}
