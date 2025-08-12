import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class HealthCheckTest {

    @Test
    public void testLogUsageLifecycle_WhenLoggingEnabled() {
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.loggingEnabled = "true";
        healthCheck.activeProfile = "dev";

        try (MockedStatic<LogUtils> logUtilsMock = mockStatic(LogUtils.class)) {
            try (MockedStatic<UsageLog> usageLogMock = mockStatic(UsageLog.class)) {
                
                // Act
                healthCheck.logUsageLifecycle("methodX", "messageX");

                // Assert — verify static calls
                logUtilsMock.verify(() -> 
                    LogUtils.setLogAttribute(LogAttributes.LIFECYCLE_ATTRIB, "]dev]"));
                logUtilsMock.verify(() -> 
                    LogUtils.setLogAttribute(LogAttributes.SERVICE_NAME_ATTRIB, Constants.ACCOUNTLOCK_LISTENER_PROCESS));
                
                usageLogMock.verify(() -> 
                    UsageLog.logUsageEvent(any(), eq("methodX"), eq("messageX")));
            }
        }
    }

    @Test
    public void testLogUsageLifecycle_WhenLoggingDisabled() {
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.loggingEnabled = "false"; // Will skip the if-branch
        healthCheck.activeProfile = "dev";

        try (MockedStatic<LogUtils> logUtilsMock = mockStatic(LogUtils.class)) {
            try (MockedStatic<UsageLog> usageLogMock = mockStatic(UsageLog.class)) {

                // Act
                healthCheck.logUsageLifecycle("methodX", "messageX");

                // Assert — nothing should be called
                logUtilsMock.verifyNoInteractions();
                usageLogMock.verifyNoInteractions();
            }
        }
    }
}
