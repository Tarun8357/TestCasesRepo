import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class KafkaConfigTest {

    // Helper to set private fields using reflection
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testKafkaConfig_OuterIfOnly() throws Exception {
        try (MockedStatic<DockerSecretsUtil> mockSecrets = mockStatic(DockerSecretsUtil.class)) {
            mockSecrets.when(DockerSecretsUtil::load).thenReturn(new HashMap<>());

            MyKafkaClass kafka = new MyKafkaClass();
            setField(kafka, "consumerBootstrapServers", "localhost:9092");
            setField(kafka, "consumerSecurityProtocol", ""); // Will not hit 2nd if

            Properties props = kafka.kafkaConfig();

            assertEquals("localhost:9092", props.get("bootstrap.servers"));
            assertFalse(props.containsKey("security.protocol"));
        }
    }

    @Test
    void testKafkaConfig_OuterIf_SecondIfOnly() throws Exception {
        try (MockedStatic<DockerSecretsUtil> mockSecrets = mockStatic(DockerSecretsUtil.class)) {
            mockSecrets.when(DockerSecretsUtil::load).thenReturn(new HashMap<>());

            MyKafkaClass kafka = new MyKafkaClass();
            setField(kafka, "consumerBootstrapServers", "localhost:9092");
            setField(kafka, "consumerSecurityProtocol", "PLAINTEXT"); // Hits 2nd if but skips SSL/SASL

            Properties props = kafka.kafkaConfig();

            assertEquals("PLAINTEXT", props.get("security.protocol"));
            assertFalse(props.containsKey("ssl.truststore.location"));
        }
    }

    @Test
    void testKafkaConfig_WithSSLOnly() throws Exception {
        try (MockedStatic<DockerSecretsUtil> mockSecrets = mockStatic(DockerSecretsUtil.class)) {
            Map<String, String> secrets = Map.of("truststorePass", "pass123");
            mockSecrets.when(DockerSecretsUtil::load).thenReturn(secrets);

            MyKafkaClass kafka = new MyKafkaClass();
            setField(kafka, "consumerBootstrapServers", "localhost:9092");
            setField(kafka, "consumerSecurityProtocol", "SSL");
            setField(kafka, "consumerSslTruststoreLocation", "/path/truststore.jks");
            setField(kafka, "consumerSslTruststoreType", "JKS");
            setField(kafka, "consumerSslTruststorePasswordSecret", "truststorePass");
            setField(kafka, "consumerSaslMechanism", ""); // skip SASL

            Properties props = kafka.kafkaConfig();

            assertEquals("/path/truststore.jks", props.get("ssl.truststore.location"));
            assertEquals("JKS", props.get("ssl.truststore.type"));
            assertEquals("pass123", props.get("ssl.truststore.password"));
            assertFalse(props.containsKey("sasl.mechanism"));
        }
    }

    @Test
    void testKafkaConfig_WithSSLAndSASL() throws Exception {
        try (MockedStatic<DockerSecretsUtil> mockSecrets = mockStatic(DockerSecretsUtil.class)) {
            Map<String, String> secrets = Map.of(
                    "truststorePass", "pass123",
                    "jaasUser", "user",
                    "jaasPass", "pass"
            );
            mockSecrets.when(DockerSecretsUtil::load).thenReturn(secrets);

            MyKafkaClass kafka = new MyKafkaClass();
            setField(kafka, "consumerBootstrapServers", "localhost:9092");
            setField(kafka, "consumerSecurityProtocol", "SASL_SSL");
            setField(kafka, "consumerSslTruststoreLocation", "/path/truststore.jks");
            setField(kafka, "consumerSslTruststoreType", "JKS");
            setField(kafka, "consumerSslTruststorePasswordSecret", "truststorePass");
            setField(kafka, "consumerSaslMechanism", "PLAIN");
            setField(kafka, "consumerSaslJaasConfigUsernameSecret", "jaasUser");
            setField(kafka, "consumerSaslJaasConfigPasswordSecret", "jaasPass");

            Properties props = kafka.kafkaConfig();

            assertEquals("PLAIN", props.get("sasl.mechanism"));
            assertTrue(props.get("sasl.jaas.config").toString().contains("user"));
        }
    }
}
