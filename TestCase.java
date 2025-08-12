import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HealthCheckTest {

    private HealthCheck createHealthCheckWithMocks(String tempFilePath, UsageLog mockUsageLog) throws Exception {
        HealthCheck healthCheck = Mockito.spy(new HealthCheck());

        // Inject mock UsageLog
        Field usageLogField = healthCheck.getClass().getDeclaredField("usageLog");
        usageLogField.setAccessible(true);
        usageLogField.set(healthCheck, mockUsageLog);

        // Inject temp file path
        Field fileField = healthCheck.getClass().getDeclaredField("healthCheckFile");
        fileField.setAccessible(true);
        fileField.set(healthCheck, tempFilePath);

        return healthCheck;
    }

    @Test
    public void testIfCondition_TrueTrue() throws Exception {
        String tempFilePath = File.createTempFile("health", ".txt").getAbsolutePath();
        UsageLog mockUsageLog = mock(UsageLog.class);
        HealthCheck healthCheck = createHealthCheckWithMocks(tempFilePath, mockUsageLog);

        doReturn(true).when(healthCheck).checkKafka(any());
        doReturn(true).when(healthCheck).checkDB();

        healthCheck.health();

        String fileContent = Files.readString(Paths.get(tempFilePath));
        Assertions.assertEquals(Constants.HEALTH_UP, fileContent);
        verify(mockUsageLog).logUsageEvent(eq("doHealthCheck()"), contains(Constants.HEALTH_UP));
    }

    @Test
    public void testIfCondition_FalseTrue() throws Exception {
        String tempFilePath = File.createTempFile("health", ".txt").getAbsolutePath();
        UsageLog mockUsageLog = mock(UsageLog.class);
        HealthCheck healthCheck = createHealthCheckWithMocks(tempFilePath, mockUsageLog);

        doReturn(false).when(healthCheck).checkKafka(any());
        doReturn(true).when(healthCheck).checkDB();

        healthCheck.health();

        String fileContent = Files.readString(Paths.get(tempFilePath));
        Assertions.assertEquals(Constants.HEALTH_DOWN, fileContent);
        verify(mockUsageLog).logUsageEvent(eq("doHealthCheck()"), contains(Constants.HEALTH_DOWN));
    }

    @Test
    public void testIfCondition_TrueFalse() throws Exception {
        String tempFilePath = File.createTempFile("health", ".txt").getAbsolutePath();
        UsageLog mockUsageLog = mock(UsageLog.class);
        HealthCheck healthCheck = createHealthCheckWithMocks(tempFilePath, mockUsageLog);

        doReturn(true).when(healthCheck).checkKafka(any());
        doReturn(false).when(healthCheck).checkDB();

        healthCheck.health();

        String fileContent = Files.readString(Paths.get(tempFilePath));
        Assertions.assertEquals(Constants.HEALTH_DOWN, fileContent);
        verify(mockUsageLog).logUsageEvent(eq("doHealthCheck()"), contains(Constants.HEALTH_DOWN));
    }

    @Test
    public void testIfCondition_FalseFalse() throws Exception {
        String tempFilePath = File.createTempFile("health", ".txt").getAbsolutePath();
        UsageLog mockUsageLog = mock(UsageLog.class);
        HealthCheck healthCheck = createHealthCheckWithMocks(tempFilePath, mockUsageLog);

        doReturn(false).when(healthCheck).checkKafka(any());
        doReturn(false).when(healthCheck).checkDB();

        healthCheck.health();

        String fileContent = Files.readString(Paths.get(tempFilePath));
        Assertions.assertEquals(Constants.HEALTH_DOWN, fileContent);
        verify(mockUsageLog).logUsageEvent(eq("doHealthCheck()"), contains(Constants.HEALTH_DOWN));
    }
}
