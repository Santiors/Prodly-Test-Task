package org.prodly;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CompanyGenerator {

    // Generate 'n' random Company records
    public static List<Company> generateCompanyRecords(int n) {
        List<Company> companies = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String name = "Company" + (i + 1);
            int numberOfEmployees = getRandomNumber(10, 500);
            int numberOfCustomers = getRandomNumber(50, 1000);
            String country = getRandomCountry();

            Company company = new Company(name, numberOfEmployees, numberOfCustomers, country);
            companies.add(company);
        }

        return companies;
    }

    // Method to generate a random number within a specified range
    private static int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    // Method to get a random country (you can add more countries as needed)
    private static String getRandomCountry() {
        String[] countries = {"USA", "Canada", "UK", "Germany", "France", "Australia", "Japan"};
        Random random = new Random();
        int index = random.nextInt(countries.length);
        return countries[index];
    }

}