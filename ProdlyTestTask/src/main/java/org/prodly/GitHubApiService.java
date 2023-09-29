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
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GitHubApiService {
    private static final String BASE_URL = "https://api.github.com";
    private static final String REPO_OWNER = "Santiors";
    private static final String REPO_NAME = "Prodly-Test-Task";
    private static final String AUTH_TOKEN = "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";
    private static final String CSV_FILE_PATH = "ProdlyTestTask/src/main/resources/TestData.csv";

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

            String existingFileSha = existingFileResponse.jsonPath().getString("sha");

            // Convert the list of companies to CSV content
            String csvContent = convertToCSV(companies);

            // Encode the new CSV content in Base64
            String encodedContent = Base64.getEncoder().encodeToString(csvContent.getBytes());

            // Prepare the Rest Assured request to update the CSV file
            RequestSpecification request = RestAssured.given()
                    .baseUri(BASE_URL)
                    .header("Authorization", "token " + AUTH_TOKEN)
                    .contentType("application/json")
                    .body(buildRequestBody(encodedContent, existingFileSha));

            // Make a PUT request to update the CSV file
            Response updateResponse = request.put(apiUrl);

            // Handle the update response
            int statusCode = updateResponse.getStatusCode();
            if (statusCode == 200) {
                System.out.println("CSV file updated successfully.");
            } else {
                System.err.println("Failed to update CSV file. Status code: " + statusCode);
                System.err.println("Response body: " + updateResponse.getBody().asString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mergeMainIntoSecondBranch() {
        try {
            // Prepare the GitHub API URL for merging 'main' into 'testSecond' branch
            String apiUrl = BASE_URL + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/merges";

            // Define the merge request body
            String requestBody = "{\n" +
                    "  \"base\": \"testSecond\",\n" +
                    "  \"head\": \"main\",\n" +
                    "  \"commit_message\": \"Merge main into second \"\n" +
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

    public static List<Company> getRecordsFromSecondBranchCSV() {
        try {
            // Construct the GitHub API URL for fetching the raw CSV file from the 'testSecond' branch
            String apiUrl = BASE_URL + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + CSV_FILE_PATH;

            // Prepare the Rest Assured request
            RequestSpecification request = RestAssured.given()
                    .baseUri(apiUrl) // Set the base URL without specifying the branch in the URL
                    .header("Authorization", "token " + AUTH_TOKEN)
                    .accept(ContentType.JSON);

            // Specify the branch using the "ref" query parameter
            request.queryParam("ref", "testSecond");

            // Make a GET request to retrieve the raw CSV content
            Response response = request.get();

            // Handle the response
            int statusCode = response.getStatusCode();
            if (statusCode == 200) {
                String csvContent = response.jsonPath().get("content");
                csvContent = csvContent.replace("\n", "").replace(" ", "");
                return parseBase64EncodedCSV(csvContent);
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

    public static List<Company> parseBase64EncodedCSV(String base64EncodedCSV) {
        List<Company> companies = new ArrayList<>();

        try {
            // Decode the base64-encoded CSV content
            byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedCSV);

            // Convert the decoded bytes to a ByteArrayInputStream
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);

            // Define the CSV format
            CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();

            try (CSVParser csvParser = CSVParser.parse(byteArrayInputStream, StandardCharsets.UTF_8, csvFormat)) {
                // Iterate through CSV records and convert them to Company objects
                for (CSVRecord csvRecord : csvParser) {
                    String name = csvRecord.get("Name");
                    int numberOfEmployees = Integer.parseInt(csvRecord.get("NumberOfEmployees"));
                    int numberOfCustomers = Integer.parseInt(csvRecord.get("NumberOfCustomers"));
                    String country = csvRecord.get("Country");

                    Company company = new Company(name, numberOfEmployees, numberOfCustomers, country);
                    companies.add(company);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return companies;
    }

    private static String buildRequestBody(String encodedContent, String existingFileSha) {
        return "{\n" +
                "  \"message\": \"Update CSV file\",\n" +
                "  \"content\": \"" + encodedContent + "\",\n" +
                "  \"sha\": \"" + existingFileSha + "\"\n" +
                "}";
    }

}
