import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.prodly.Company;
import org.prodly.GitHubApiService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.prodly.CompanyGenerator.generateCompanyRecords;

public class SuccessfulCSVCheckTest {
    private static int NUMBER_OF_RECORDS = 10;
    // Generate n records of Company objects
    private static List<Company> companies = generateCompanyRecords(NUMBER_OF_RECORDS);

    @BeforeAll
    private static void setUp() {
        GitHubApiService.insertRecordsToCSV(companies);
        GitHubApiService.mergeMainIntoSecondBranch();
    }
    @Test
    public void twoCSVFilesShouldBeEqualsWhenMergeMainIntoSecondBranch() {
        List<Company> secondBranchRecords = GitHubApiService.getRecordsFromSecondBranchCSV();
        assertThat("Size are not equals",companies.size(),is(equalTo(secondBranchRecords.size())));
        // Assert that all fields contain the same values in both files
        for (int i = 0; i < companies.size(); i++) {
            Company original = companies.get(i);
            Company retrieved = secondBranchRecords.get(i);
            assertThat("Names are not equal",original.getName(),
                    is(equalTo(retrieved.getName())));
            assertThat("Number Of Employees are not equal",original.getNumberOfEmployees(),
                    is(equalTo(retrieved.getNumberOfEmployees())));
            assertThat("Number Of Customers are not equal",original.getNumberOfCustomers(),
                    is(equalTo(retrieved.getNumberOfCustomers())));
            assertThat("Countries are not equal",original.getCountry(),
                    is(equalTo(retrieved.getCountry())));
        }
    }

}
