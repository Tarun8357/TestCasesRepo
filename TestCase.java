import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KafkaConfigServiceTest {

    private KafkaConfigService kafkaConfigService;

    @BeforeEach
    void setUp() {
        kafkaConfigService = new KafkaConfigService();

        // Set required field values manually
        kafkaConfigService.consumerBootstrapServers = "localhost:9092";
        kafkaConfigService.consumerSecurityProtocol = "SASL_SSL";
        kafkaConfigService.consumerSslTruststoreLocation = "/path/to/truststore";
        kafkaConfigService.consumerSslTruststoreType = "JKS";
        kafkaConfigService.consumerSslTruststorePasswordSecret = "truststore-password-secret";
        kafkaConfigService.consumerSaslMechanism = "PLAIN";
        kafkaConfigService.consumerSaslJaasConfigUsernameSecret = "jaas-user";
        kafkaConfigService.consumerSaslJaasConfigPasswordSecret = "jaas-pass";
    }

    @Test
    void testKafkaConfig_WithSASL_SSL() {
        Map<String, String> secrets = new HashMap<>();
        secrets.put("truststore-password-secret", "password");
        secrets.put("jaas-user", "user");
        secrets.put("jaas-pass", "pass");

        // Mock DockerSecretsUtil.load()
        try (MockedStatic<DockerSecretsUtil> mockedSecrets = mockStatic(DockerSecretsUtil.class)) {
            mockedSecrets.when(DockerSecretsUtil::load).thenReturn(secrets);

            // Stub buildJaasConfig to return dummy JAAS config
            KafkaConfigService spyService = spy(kafkaConfigService);
            doReturn("JAAS_CONFIG_STRING").when(spyService)
                    .buildJaasConfig(secrets, "jaas-user", "jaas-pass");

            // Act
            Properties props = spyService.kafkaConfig();

            // Assert
            assertEquals("localhost:9092", props.getProperty("bootstrap.servers"));
            assertEquals("SASL_SSL", props.getProperty("security.protocol"));
            assertEquals("/path/to/truststore", props.getProperty("ssl.truststore.location"));
            assertEquals("JKS", props.getProperty("ssl.truststore.type"));
            assertEquals("password", props.getProperty("ssl.truststore.password"));
            assertEquals("PLAIN", props.getProperty("sasl.mechanism"));
            assertEquals("JAAS_CONFIG_STRING", props.getProperty("sasl.jaas.config"));
        }
    }

    @Test
    void testKafkaConfig_WithMinimalConfig() {
        // Setup minimal config
        kafkaConfigService.consumerBootstrapServers = "localhost:9092";
        kafkaConfigService.consumerSecurityProtocol = null;
        kafkaConfigService.consumerSaslMechanism = null;

        Map<String, String> secrets = new HashMap<>();

        try (MockedStatic<DockerSecretsUtil> mockedSecrets = mockStatic(DockerSecretsUtil.class)) {
            mockedSecrets.when(DockerSecretsUtil::load).thenReturn(secrets);

            Properties props = kafkaConfigService.kafkaConfig();

            assertEquals("localhost:9092", props.getProperty("bootstrap.servers"));
            assertNull(props.getProperty("security.protocol"));
            assertNull(props.getProperty("sasl.mechanism"));
            assertNull(props.getProperty("sasl.jaas.config"));
        }
    }
}
