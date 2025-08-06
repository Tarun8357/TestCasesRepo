import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KafkaConfigTest {

    @Test
    public void testKafkaConfig_FullConfig_ReturnsExpectedProperties() {
        // Arrange
        KafkaConfigBuilder builder = new KafkaConfigBuilder();
        builder.consumerBootstrapServers = "localhost:9092";
        builder.consumerSecurityProtocol = "SASL_SSL";
        builder.consumerSslTruststoreLocation = "/etc/kafka/secrets/truststore.jks";
        builder.consumerSslTruststoreType = "JKS";
        builder.consumerSslTruststorePasswordSecret = "truststore-pass";
        builder.consumerSaslMechanism = "PLAIN";
        builder.consumerSasJaasConfigUsernameSecret = "username-key";
        builder.consumerSasJaasConfigPasswordSecret = "password-key";

        Map<String, String> secrets = new HashMap<>();
        secrets.put("truststore-pass", "secretPass");
        secrets.put("username-key", "testUser");
        secrets.put("password-key", "testPass");

        try (MockedStatic<DockerSecretsUtil> dockerSecrets = mockStatic(DockerSecretsUtil.class)) {
            dockerSecrets.when(DockerSecretsUtil::load).thenReturn(secrets);

            // Act
            Properties props = builder.kafkaConfig();

            // Assert
            assertEquals("localhost:9092", props.get("bootstrap.servers"));
            assertEquals("SASL_SSL", props.get("security.protocol"));
            assertEquals("/etc/kafka/secrets/truststore.jks", props.get("ssl.truststore.location"));
            assertEquals("JKS", props.get("ssl.truststore.type"));
            assertEquals("secretPass", props.get("ssl.truststore.password"));
            assertEquals("PLAIN", props.get("sasl.mechanism"));
            assertEquals(
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"testUser\" password=\"testPass\";",
                props.get("sasl.jaas.config")
            );
        }
    }
}
