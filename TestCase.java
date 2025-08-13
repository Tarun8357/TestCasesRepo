import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class KafkaConfigTest {

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testKafkaConfig_WithSSLAndSASL() throws Exception {
        try (MockedStatic<DockerSecretsUtil> mockSecrets = mockStatic(DockerSecretsUtil.class)) {
            Map<String, String> secrets = new HashMap<>();
            secrets.put("truststorePasswordSecret", "pass123");
            secrets.put("jaasUserSecret", "user");
            secrets.put("jaasPassSecret", "pass");
            mockSecrets.when(DockerSecretsUtil::load).thenReturn(secrets);

            MyKafkaClass kafka = new MyKafkaClass();
            setField(kafka, "consumerBootstrapServers", "localhost:9092");
            setField(kafka, "consumerSecurityProtocol", "SASL_SSL");
            setField(kafka, "consumerSslTruststoreLocation", "/path/truststore.jks");
            setField(kafka, "consumerSslTruststoreType", "JKS");
            setField(kafka, "consumerSslTruststorePasswordSecret", "truststorePasswordSecret");
            setField(kafka, "consumerSaslMechanism", "PLAIN");
            setField(kafka, "consumerSaslJaasConfigUsernameSecret", "jaasUserSecret");
            setField(kafka, "consumerSaslJaasConfigPasswordSecret", "jaasPassSecret");

            Properties props = kafka.kafkaConfig();

            assertEquals("localhost:9092", props.get("bootstrap.servers"));
            assertEquals("SASL_SSL", props.get("security.protocol"));
            assertEquals("/path/truststore.jks", props.get("ssl.truststore.location"));
            assertEquals("JKS", props.get("ssl.truststore.type"));
            assertEquals("pass123", props.get("ssl.truststore.password"));
            assertEquals("PLAIN", props.get("sasl.mechanism"));
        }
    }

    @Test
    void testKafkaConfig_OnlyBootstrapAndSecurityProtocol() throws Exception {
        try (MockedStatic<DockerSecretsUtil> mockSecrets = mockStatic(DockerSecretsUtil.class)) {
            mockSecrets.when(DockerSecretsUtil::load).thenReturn(new HashMap<>());

            MyKafkaClass kafka = new MyKafkaClass();
            setField(kafka, "consumerBootstrapServers", "localhost:9092");
            setField(kafka, "consumerSecurityProtocol", "PLAINTEXT");

            Properties props = kafka.kafkaConfig();

            assertEquals("localhost:9092", props.get("bootstrap.servers"));
            assertEquals("PLAINTEXT", props.get("security.protocol"));
        }
    }

    @Test
    void testKafkaConfig_NoBootstrap_NoProps() throws Exception {
        try (MockedStatic<DockerSecretsUtil> mockSecrets = mockStatic(DockerSecretsUtil.class)) {
            mockSecrets.when(DockerSecretsUtil::load).thenReturn(new HashMap<>());

            MyKafkaClass kafka = new MyKafkaClass();
            setField(kafka, "consumerBootstrapServers", "");

            Properties props = kafka.kafkaConfig();

            // No bootstrap.servers should be set
            assertEquals(null, props.get("bootstrap.servers"));
        }
    }
}
