import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HealthCheckTest {

    @Test
    public void testHealthCheck_HealthUp() throws Exception {
        // Arrange
        HealthCheck healthCheck = Mockito.spy(new HealthCheck());

        // Mock dependencies
        UsageLog mockUsageLog = mock(UsageLog.class);
        healthCheck.getClass().getDeclaredField("usageLog").setAccessible(true);
        healthCheck.getClass().getDeclaredField("usageLog").set(healthCheck, mockUsageLog);

        // Mock the file location to avoid actual disk writes
        healthCheck.getClass().getDeclaredField("healthCheckFile").setAccessible(true);
        healthCheck.getClass().getDeclaredField("healthCheckFile").set(healthCheck, File.createTempFile("health", ".txt"));

        // Force both checks to pass
        doReturn(true).when(healthCheck).checkKafka(any());
        doReturn(true).when(healthCheck).checkDB();

        // Act
        healthCheck.health();

        // Assert
        verify(mockUsageLog).logUsageEvent(
                eq("doHealthCheck()"),
                eq("Health Check Status --->" + Constants.HEALTH_UP)
        );
    }

    @Test
    public void testHealthCheck_HealthDown() throws Exception {
        // Arrange
        HealthCheck healthCheck = Mockito.spy(new HealthCheck());

        // Mock dependencies
        UsageLog mockUsageLog = mock(UsageLog.class);
        healthCheck.getClass().getDeclaredField("usageLog").setAccessible(true);
        healthCheck.getClass().getDeclaredField("usageLog").set(healthCheck, mockUsageLog);

        // Mock the file location to avoid actual disk writes
        healthCheck.getClass().getDeclaredField("healthCheckFile").setAccessible(true);
        healthCheck.getClass().getDeclaredField("healthCheckFile").set(healthCheck, File.createTempFile("health", ".txt"));

        // Force one check to fail
        doReturn(false).when(healthCheck).checkKafka(any());
        doReturn(true).when(healthCheck).checkDB();

        // Act
        healthCheck.health();

        // Assert
        verify(mockUsageLog).logUsageEvent(
                eq("doHealthCheck()"),
                eq("Health Check Status --->" + Constants.HEALTH_DOWN)
        );
    }
}
