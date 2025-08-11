import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class HealthCheckTest {

    @Test
    public void testHealth_LogsHealthUp_WhenKafkaAndDbAreUp() throws Exception {
        // Spy the class so we can stub the health checks
        HealthCheck healthCheck = spy(new HealthCheck());

        // Mock usageLog
        UsageLog mockUsageLog = mock(UsageLog.class);
        var logField = HealthCheck.class.getDeclaredField("usageLog");
        logField.setAccessible(true);
        logField.set(healthCheck, mockUsageLog);

        // Stub check methods to both return true
        doReturn(true).when(healthCheck).checkKafka(any());
        doReturn(true).when(healthCheck).checkDB();

        // Prevent file writing from interfering — point to temp file
        var fileField = HealthCheck.class.getDeclaredField("healthCheckFile");
        fileField.setAccessible(true);
        fileField.set(healthCheck, "test-health-file.txt");

        // Call method
        healthCheck.health();

        // Verify that HEALTH_UP was logged
        verify(mockUsageLog).logUsageEvent(
                eq("doHealthCheck()"),
                contains(Constants.HEALTH_UP)
        );
    }
}
