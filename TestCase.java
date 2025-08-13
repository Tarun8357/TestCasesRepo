import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UsageLogTest {

    @Test
    void testLogUsageEvent_InfoEnabled_ReflectionOnly() throws Exception {
        // Mock logger to return true for info enabled
        Logger mockLogger = mock(Logger.class);
        when(mockLogger.isInfoEnabled()).thenReturn(true);

        UsageLog usageLog = new UsageLog();

        // Get reference to buildLogMapFromMDC method
        Method buildLogMapMethod = UsageLog.class.getDeclaredMethod(
                "buildLogMapFromMDC", Logger.class, String[].class);
        buildLogMapMethod.setAccessible(true);

        // Get reference to logEvent method
        Method logEventMethod = UsageLog.class.getDeclaredMethod(
                "logEvent", String.class, Logger.class, Map.class);
        logEventMethod.setAccessible(true);

        // Wrap logEvent to intercept arguments
        final Object[] capturedArgs = new Object[3];
        Method finalLogEventMethod = logEventMethod;
        UsageLog interceptingUsageLog = new UsageLog() {
            @Override
            protected void logEvent(String severity, Logger logger, Map<String, String> logMap) {
                capturedArgs[0] = severity;
                capturedArgs[1] = logger;
                capturedArgs[2] = logMap;
            }
        };

        // Invoke logUsageEvent
        interceptingUsageLog.logUsageEvent(mockLogger, "testMethod", "testMessage");

        // Assertions on captured arguments
        assertEquals(LogConstants.INFO_SEVERITY, capturedArgs[0]);
        assertEquals(mockLogger, capturedArgs[1]);
        Map<String, String> logMap = (Map<String, String>) capturedArgs[2];
        assertEquals("testMethod", logMap.get(LogAttributes.METHOD_ATTRIB));
        assertEquals("testMessage", logMap.get(LogAttributes.MESSAGE_ATTRIB));
    }

    @Test
    void testLogUsageEvent_InfoDisabled_ReflectionOnly() throws Exception {
        Logger mockLogger = mock(Logger.class);
        when(mockLogger.isInfoEnabled()).thenReturn(false);

        // Same interception trick
        final boolean[] called = {false};
        UsageLog interceptingUsageLog = new UsageLog() {
            @Override
            protected void logEvent(String severity, Logger logger, Map<String, String> logMap) {
                called[0] = true;
            }
        };

        interceptingUsageLog.logUsageEvent(mockLogger, "testMethod", "testMessage");

        // Should never call logEvent if info disabled
        assertEquals(false, called[0]);
    }
}
