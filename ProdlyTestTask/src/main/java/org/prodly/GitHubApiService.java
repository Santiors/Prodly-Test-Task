package org.prodly;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.http.ContentType;
import java.io.*;
import java.util.*;

public class GitHubApiService {
    private static final String BASE_URL = "https://api.github.com";
    private static final String REPO_OWNER = "Santiors";
    private static final String REPO_NAME = "Prodly-Test-Task";
    private static final String AUTH_TOKEN = "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";
    private static final String CSV_FILE_PATH = "src/main/resources/TestData.csv";

    public static void insertRecordsToCSV(List<Company> companies) {
        try {
            // Prepare the GitHub API URL for fetching the existing CSV file
            String apiUrl = BASE_URL + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + CSV_FILE_PATH;

            // Fetch the existing file content and SHA
            Response existingFileResponse = RestAssured.given()
                    .baseUri(BASE_URL)
                    .header("Authorization", "token " + AUTH_TOKEN)
                    .accept(ContentType.JSON)
                    .get(apiUrl);

            // Check if the existing file was found
            if (existingFileResponse.getStatusCode() != 200) {
                System.err.println("Existing CSV file not found. Status code: " + existingFileResponse.getStatusCode());
                return;
            }

            String existingFileContent = existingFileResponse.jsonPath().getString("content");
            String existingFileSha = existingFileResponse.jsonPath().getString("sha");

            // Convert the list of companies to CSV content
            String csvContent = convertToCSV(companies);

            // Encode the new CSV content in Base64
            String encodedContent = encodeBase64(csvContent);

            // Prepare the Rest Assured request to update the CSV file
            RequestSpecification request = RestAssured.given()
                    .baseUri(BASE_URL)
                    .header("Authorization", "token " + AUTH_TOKEN)
                    .contentType(ContentType.JSON)
                    .body(buildRequestBody(encodedContent, existingFileSha));

            // Make a PUT request to update the CSV file
            Response updateResponse = request.put(apiUrl);

            // Handle the update response
            int statusCode = updateResponse.getStatusCode();
            if (statusCode == 200) {
                System.out.println("CSV file updated successfully.");
            } else {
                System.err.println("Failed to update CSV file. Status code: " + statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void mergeMainIntoSecondBranch() {
        try {
            // Prepare the GitHub API URL for merging 'main' into 'second' branch
            String apiUrl = BASE_URL + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/merges";

            // Define the merge request body (specify 'main' as the base branch and 'second' as the head branch)
            String requestBody = "{\n" +
                    "  \"base\": \"main\",\n" +
                    "  \"head\": \"second\"\n" +
                    "}";

            // Prepare the Rest Assured request
            RequestSpecification request = RestAssured.given()
                    .baseUri(BASE_URL)
                    .header("Authorization", "token " + AUTH_TOKEN)
                    .contentType(ContentType.JSON)
                    .body(requestBody);

            // Make a POST request to perform the merge
            Response response = request.post(apiUrl);

            // Handle the response
            int statusCode = response.getStatusCode();
            if (statusCode == 201 || statusCode == 204) {
                System.out.println("Merge successful.");
            } else {
                System.err.println("Merge failed. Status code: " + statusCode);
                System.err.println(response.getBody().asString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Company> getRecordsFromSecondBranchCSV() throws IOException {
        try {
            // Construct the GitHub API URL for fetching the raw CSV file from the 'second' branch
            String apiUrl = BASE_URL + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + CSV_FILE_PATH;

            // Prepare the Rest Assured request
            RequestSpecification request = RestAssured.given()
                    .baseUri(apiUrl) // Set the base URL without specifying the branch in the URL
                    .header("Authorization", "token " + AUTH_TOKEN)
                    .accept(ContentType.JSON);

            // Specify the branch using the "ref" query parameter
            request.queryParam("ref", "second");

            // Make a GET request to retrieve the raw CSV content
            Response response = request.get();

            // Handle the response
            int statusCode = response.getStatusCode();
            if (statusCode == 200) {
                String csvContent = response.jsonPath().get("content");
                return parseCSV(csvContent);
            } else {
                System.err.println("Failed to fetch CSV content. Status code: " + statusCode);
                System.err.println(response.getBody().asString());
                return new ArrayList<>(); // Return an empty list or handle the error accordingly
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return an empty list or handle the error accordingly
        }
    }

    private static String convertToCSV(List<Company> companies) throws IOException {
        try (StringWriter writer = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Name", "NumberOfEmployees", "NumberOfCustomers", "Country"))) {

            for (Company company : companies) {
                csvPrinter.printRecord(
                        company.getName(),
                        company.getNumberOfEmployees(),
                        company.getNumberOfCustomers(),
                        company.getCountry()
                );
            }

            return writer.toString();
        }
    }

    public static List<Company> parseCSV(String csvContent) throws IOException {
        List<Company> companies = new ArrayList<>();

        try (Reader reader = new StringReader(csvContent);
             CSVParser csvParser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            for (String header : headerMap.keySet()) {
                System.out.println("Header: " + header);
            }

            for (CSVRecord csvRecord : csvParser) {
                String name = csvRecord.get("Name");
                int numberOfEmployees = Integer.parseInt(csvRecord.get("NumberOfEmployees"));
                int numberOfCustomers = Integer.parseInt(csvRecord.get("NumberOfCustomers"));
                String country = csvRecord.get("Country");

                Company company = new Company(name, numberOfEmployees, numberOfCustomers, country);
                companies.add(company);
            }
        }

        return companies;
    }
    // Helper method to check if headers match the expected format
    private static boolean areHeadersValid(String[] headers) {
        return headers.length == 4 &&
                "Name".equals(headers[0]) &&
                "NumberOfEmployees".equals(headers[1]) &&
                "NumberOfCustomers".equals(headers[2]) &&
                "Country".equals(headers[3]);
    }

    private static String buildRequestBody(String encodedContent, String existingFileSha) {
        return "{\n" +
                "  \"message\": \"Update CSV file\",\n" +
                "  \"content\": \"" + encodedContent + "\",\n" +
                "  \"sha\": \"" + existingFileSha + "\"\n" +
                "}";
    }

    private static String encodeBase64(String content) {
        return java.util.Base64.getEncoder().encodeToString(content.getBytes());
    }


}
