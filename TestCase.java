import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

public class HealthCheckTest {

    private HealthCheck createHealthCheckWithMocks(String filePath, UsageLog mockUsageLog) throws Exception {
        HealthCheck healthCheck = spy(new HealthCheck());

        Field usageLogField = HealthCheck.class.getDeclaredField("usageLog");
        usageLogField.setAccessible(true);
        usageLogField.set(healthCheck, mockUsageLog);

        Field fileField = HealthCheck.class.getDeclaredField("healthCheckFile");
        fileField.setAccessible(true);
        fileField.set(healthCheck, filePath);

        return healthCheck;
    }

    @Test
    public void testCatchBlock_IOExceptionWithAssertThrows() throws Exception {
        // Arrange: directory instead of file to trigger IOException
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "healthDir");
        tempDir.mkdir();

        UsageLog mockUsageLog = mock(UsageLog.class);
        HealthCheck healthCheck = createHealthCheckWithMocks(tempDir.getAbsolutePath(), mockUsageLog);

        doReturn(true).when(healthCheck).checkKafka(any());
        doReturn(true).when(healthCheck).checkDB();

        // Act + Assert
        assertThrows(RuntimeException.class, () -> {
            // Force a RuntimeException inside catch block
            doThrow(new RuntimeException("Forced failure")).when(healthCheck).checkDB();
            healthCheck.health();
        });
    }
}
