package org.prodly;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GitHubApiService {
    private static final String BASE_URL = "https://api.github.com";
    private static final String REPO_OWNER = "Santiors";
    private static final String REPO_NAME = "Prodly-Test-Task";
    private static final String AUTH_TOKEN = "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";
    private static final String CSV_FILE_PATH = "src/main/resources/TestData.csv";

    public static void insertRecordsToCSV(List<Company> companies) {
        try (FileWriter fileWriter = new FileWriter(CSV_FILE_PATH);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                     .withHeader("Name", "NumberOfEmployees", "NumberOfCustomers", "Country"))) {

            for (Company company : companies) {
                csvPrinter.printRecord(
                        company.getName(),
                        company.getNumberOfEmployees(),
                        company.getNumberOfCustomers(),
                        company.getCountry()
                );
            }
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }

    public static void mergeMainIntoSecondBranch() {
        try {
            // Define the Git merge command to merge 'main' into 'second' branch
            String[] gitCommand = {"git", "checkout", "SecondBranch", "&&", "git", "merge", "main"};

            // Create a new process builder for executing the Git command
            ProcessBuilder processBuilder = new ProcessBuilder(gitCommand);

            // Set the working directory to the root directory of your Git repository
            processBuilder.directory(new File("D:/Projects/Prodly-Test-Task"));

            // Start the process
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the merge was successful
            if (exitCode == 0) {
                System.out.println("Merge successful.");
            } else {
                System.err.println("Merge failed. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }

    public static List<Company> getRecordsFromSecondBranchCSV() {
        // Implement logic to make a GET request to fetch records from the CSV in the second branch.
        return null; // Replace with the retrieved records.
    }
}
