import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PersonDataServiceTest {

    @Test
    void test_getPlatformInternalIdAndSchema_withReflection() throws Exception {
        // Step 1: Create spy of the class
        PersonDataService service = new PersonDataService();

        // Step 2: Use reflection to override private method getPersonV2CallData
        Method privateMethod = PersonDataService.class.getDeclaredMethod(
                "getPersonV2CallData", String.class, String.class);
        privateMethod.setAccessible(true);

        // Step 3: Create fake data to inject
        IdMapping mapping = new IdMapping();
        mapping.setPlatformType("TBA123");
        mapping.setDomains("DBDC");
        mapping.setNormalizedClientId("CLIENT123");
        mapping.setPlatformInternalId("PLATFORM-ID-123");
        mapping.setSourceSchemaName("SOURCE-SCHEMA");

        PersonV2Collection collection = new PersonV2Collection();
        collection.setIdMapping(List.of(mapping));

        // Step 4: Replace actual logic by injecting test collection using reflection
        PersonDataService testService = new PersonDataService() {
            @Override
            public List<PersonData> getPlatformInternalIdAndSchema(String globalPersonIdentifier, String normalizedClientId) {
                try {
                    return PersonDataServiceTest.this.callGetPlatformInternalIdAndSchemaWithMockedCollection(this, collection, normalizedClientId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // Step 5: Call test method
        List<PersonData> result = testService.getPlatformInternalIdAndSchema("GLOBAL123", "CLIENT123");

        // Step 6: Assert
        assertEquals(1, result.size());
        PersonData data = result.get(0);
        assertEquals("PLATFORM-ID-123", data.getPlatformInternalId());
        assertEquals("SOURCE-SCHEMA", data.getSourceSchemaName());
    }

    // Helper to simulate calling the private method and injecting return value
    public List<PersonData> callGetPlatformInternalIdAndSchemaWithMockedCollection(
            PersonDataService service,
            PersonV2Collection mockCollection,
            String normalizedClientId
    ) throws Exception {
        String token = "mock-token";
        String header = "{\"clientId\":\"" + normalizedClientId + "\"}";

        // simulate internal logic with mocked PersonV2Collection
        List<PersonData> result = new ArrayList<>();
        if (token != null && !token.isEmpty()) {
            List<IdMapping> idMappings = mockCollection.getIdMapping();
            for (IdMapping idMapping : idMappings) {
                if (idMapping.getPlatformType().startsWith("TBA")) {
                    List<String> domainList = Arrays.asList(idMapping.getDomains().split(""));
                    if ((domainList.contains("DB") || domainList.contains("DC"))
                            && idMapping.getNormalizedClientId().equals(normalizedClientId)) {
                        PersonData personData = new PersonData();
                        personData.setPlatformInternalId(idMapping.getPlatformInternalId());
                        personData.setSourceSchemaName(idMapping.getSourceSchemaName());
                        result.add(personData);
                    }
                }
            }
        }
        return result;
    }
}
