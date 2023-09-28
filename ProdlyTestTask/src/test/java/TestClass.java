import org.junit.jupiter.api.Test;
import org.prodly.Company;
import org.prodly.GitHubApiService;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Assertions;

import static org.prodly.CompanyGenerator.generateCompanyRecords;

public class TestClass {
    @Test
    public void testGitHubIntegration() throws IOException {
        // Generate n records of Company objects
        int n = 10; // You can set any integer value for 'n'
        List<Company> companies = generateCompanyRecords(n);

        // Insert records into CSV in the main branch
        GitHubApiService.insertRecordsToCSV(companies);

//                System.out.println(companies);

//
        // Merge main into the second branch
        GitHubApiService.mergeMainIntoSecondBranch();
//
        // Get records from the CSV in the second branch
        List<Company> secondBranchRecords = GitHubApiService.getRecordsFromSecondBranchCSV();
//
//        // Assert that both files contain the same number of records
        Assertions.assertEquals(companies.size(), secondBranchRecords.size());
//
//        // Assert that all fields contain the same values in both files
//        for (int i = 0; i < companies.size(); i++) {
//            Company original = companies.get(i);
//            Company retrieved = secondBranchRecords.get(i);
//
//            Assertions.assertEquals(original.getName(), retrieved.getName());
//            Assertions.assertEquals(original.getNumberOfEmployees(), retrieved.getNumberOfEmployees());
//            Assertions.assertEquals(original.getNumberOfCustomers(), retrieved.getNumberOfCustomers());
//            Assertions.assertEquals(original.getCountry(), retrieved.getCountry());
//        }
    }

}
