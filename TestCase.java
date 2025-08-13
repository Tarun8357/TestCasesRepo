import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UsageLogTest {

    @Test
    void testLogUsageEvent_InfoEnabled_Reflection() throws Exception {
        Logger mockLogger = mock(Logger.class);
        when(mockLogger.isInfoEnabled()).thenReturn(true);

        UsageLog usageLog = new UsageLog();

        // Use reflection to call the protected buildLogMapFromMDC
        Method buildLogMapMethod = UsageLog.class.getDeclaredMethod("buildLogMapFromMDC", Logger.class, String[].class);
        buildLogMapMethod.setAccessible(true);
        Map<String, String> logMap = (Map<String, String>) buildLogMapMethod.invoke(usageLog, mockLogger, new String[]{});
        logMap = new HashMap<>(logMap); // Make sure we can modify

        // Spy on UsageLog to intercept logEvent only
        UsageLog spyUsageLog = spy(usageLog);
        doNothing().when(spyUsageLog).logEvent(anyString(), any(), any());

        // Call the public method
        spyUsageLog.logUsageEvent(mockLogger, "testMethod", "testMessage");

        // Use reflection to check that logMap contains expected entries
        assertEquals("testMethod", logMap.get(LogAttributes.METHOD_ATTRIB));
        assertEquals("testMessage", logMap.get(LogAttributes.MESSAGE_ATTRIB));

        // Verify logEvent was called with INFO severity
        verify(spyUsageLog).logEvent(eq(LogConstants.INFO_SEVERITY), eq(mockLogger), anyMap());
    }

    @Test
    void testLogUsageEvent_InfoDisabled_Reflection() throws Exception {
        Logger mockLogger = mock(Logger.class);
        when(mockLogger.isInfoEnabled()).thenReturn(false);

        UsageLog usageLog = new UsageLog();

        // Spy to verify logEvent never called
        UsageLog spyUsageLog = spy(usageLog);

        spyUsageLog.logUsageEvent(mockLogger, "testMethod", "testMessage");

        verify(spyUsageLog, never()).logEvent(anyString(), any(), any());
    }
}
