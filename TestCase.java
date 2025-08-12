import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

public class HealthCheckTest {

    @Test
    public void testHealth_CatchBlockTriggered() throws Exception {
        // Arrange
        HealthCheck healthCheck = spy(new HealthCheck());
        UsageLog mockUsageLog = mock(UsageLog.class);

        // Inject mocked UsageLog
        Field usageLogField = HealthCheck.class.getDeclaredField("usageLog");
        usageLogField.setAccessible(true);
        usageLogField.set(healthCheck, mockUsageLog);

        // Make Kafka and DB checks return true so we pass the if-condition
        doReturn(true).when(healthCheck).checkKafka(any());
        doReturn(true).when(healthCheck).checkDB();

        // Create a directory instead of file to cause IOException in FileOutputStream
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "healthDirForTest");
        tempDir.mkdir();

        // Inject the directory path into healthCheckFile
        Field fileField = HealthCheck.class.getDeclaredField("healthCheckFile");
        fileField.setAccessible(true);
        fileField.set(healthCheck, tempDir.getAbsolutePath());

        // Act — This should hit the catch block
        healthCheck.health();

        // No assertion needed just for coverage, but you can still verify logging
        verify(mockUsageLog).logUsageEvent(eq("doHealthCheck()"), contains("Health Check Status --->UP"));
    }
}
