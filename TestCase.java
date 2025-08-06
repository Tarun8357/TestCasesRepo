import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JaasConfigBuilderTest {

    @Test
    public void testBuildJaasConfig() {
        // Arrange
        Map<String, String> secrets = new HashMap<>();
        secrets.put("username-key", "testUser");
        secrets.put("password-key", "testPass");

        String expected = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"testUser\" password=\"testPass\";";
        
        // Act
        String actual = buildJaasConfig(secrets, "username-key", "password-key");

        // Assert
        assertEquals(expected, actual);
    }

    // Copied from your implementation (can also be in a separate class)
    public String buildJaasConfig(Map<String, String> secrets, String saslJaasConfigUsernameSecret, String saslJaasConfigPasswordSecret) {
        StringBuilder jaasTemplate = new StringBuilder(128);
        jaasTemplate.append("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"")
                .append(secrets.get(saslJaasConfigUsernameSecret).trim())
                .append("\" password=\"")
                .append(secrets.get(saslJaasConfigPasswordSecret).trim())
                .append("\";");
        return jaasTemplate.toString();
    }
}
