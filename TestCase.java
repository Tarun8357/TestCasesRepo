package com.alight.upoint.listener.healthcheck;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class HealthCheckTest {

    @Test
    public void testHealthCheck_HealthUp() throws Exception {
        // Spy the HealthCheck so we can mock checkKafka and checkDB
        HealthCheck healthCheck = Mockito.spy(new HealthCheck());

        // Mock usageLog dependency
        UsageLog mockUsageLog = mock(UsageLog.class);

        // Inject mockUsageLog into private field
        Field usageLogField = healthCheck.getClass().getDeclaredField("usageLog");
        usageLogField.setAccessible(true);
        usageLogField.set(healthCheck, mockUsageLog);

        // Mock the final String healthCheckFile with a temp file path
        Field fileField = healthCheck.getClass().getDeclaredField("healthCheckFile");
        fileField.setAccessible(true);

        // Remove 'final' modifier for testing
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(fileField, fileField.getModifiers() & ~Modifier.FINAL);

        // Assign a temporary file path string
        String tempFilePath = File.createTempFile("health", ".txt").getAbsolutePath();
        fileField.set(healthCheck, tempFilePath);

        // Mock methods to force if condition (isUp && isUpDB) == true
        doReturn(true).when(healthCheck).checkKafka(any());
        doReturn(true).when(healthCheck).checkDB();

        // Execute the method
        healthCheck.health();

        // Verify log usage event was called with correct values
        verify(mockUsageLog).logUsageEvent(
                eq("doHealthCheck()"),
                eq("Health Check Status --->" + Constants.HEALTH_UP)
        );

        // Optional: check the file contains HEALTH_UP
        String fileContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(tempFilePath)));
        org.junit.jupiter.api.Assertions.assertEquals(Constants.HEALTH_UP, fileContent);
    }
}
