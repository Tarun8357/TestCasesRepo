import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonDataServiceTest {

    // Custom subclass to simulate exception
    static class ExceptionThrowingPersonDataService extends PersonDataService {
        @Override
        protected String getPersonTokenWithFeignClient(String globalPersonIdentifier, String normalizedClientId) {
            throw new RuntimeException("Simulated failure"); // triggers catch block
        }
    }

    @Test
    void test_getPlatformInternalIdAndSchema_exception_handling() {
        // Arrange
        PersonDataService service = new ExceptionThrowingPersonDataService();

        // Act
        List<PersonData> result = service.getPlatformInternalIdAndSchema("GPI123", "CLIENT123");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // ✅ we expect empty list on exception
    }
}
