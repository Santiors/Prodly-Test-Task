package org.prodly;
import lombok.*;

public class Company {
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private int numberOfEmployees;
    @Setter
    @Getter
    private int numberOfCustomers;
    @Setter
    @Getter
    private String country;

    public Company(String name, int numberOfEmployees, int numberOfCustomers, String country) {
        this.name = name;
        this.numberOfEmployees = numberOfEmployees;
        this.numberOfCustomers = numberOfCustomers;
        this.country = country;
    }

    //add toString methods for easier debugging and logging processes
    @Override
    public String toString() {
        return "Company{" +
                "name='" + name + '\'' +
                ", numberOfEmployees=" + numberOfEmployees +
                ", numberOfCustomers=" + numberOfCustomers +
                ", country='" + country + '\'' +
                '}';
    }

}