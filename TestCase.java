import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UsageLogTest {

    @Test
    void testLogUsageEvent_InfoEnabled() {
        // Mock logger
        Logger mockLogger = mock(Logger.class);
        when(mockLogger.isInfoEnabled()).thenReturn(true);

        // Spy UsageLog so we can stub protected methods
        UsageLog usageLogSpy = Mockito.spy(new UsageLog());

        // Stub the protected method buildLogMapFromMDC
        Map<String, String> fakeMap = new HashMap<>();
        doReturn(fakeMap).when(usageLogSpy)
                .buildLogMapFromMDC(eq(mockLogger), any());

        // Stub logEvent (protected)
        doNothing().when(usageLogSpy)
                .logEvent(anyString(), eq(mockLogger), anyMap());

        // Call the method under test
        usageLogSpy.logUsageEvent(mockLogger, "testMethod", "testMessage");

        // Verify the protected methods were called
        verify(usageLogSpy).buildLogMapFromMDC(eq(mockLogger), any());
        verify(usageLogSpy).logEvent(eq(LogConstants.INFO_SEVERITY), eq(mockLogger), eq(fakeMap));

        // Verify map values were populated
        assertEquals("testMethod", fakeMap.get(LogAttributes.METHOD_ATTRIB));
        assertEquals("testMessage", fakeMap.get(LogAttributes.MESSAGE_ATTRIB));
    }

    @Test
    void testLogUsageEvent_InfoDisabled() {
        Logger mockLogger = mock(Logger.class);
        when(mockLogger.isInfoEnabled()).thenReturn(false);

        UsageLog usageLogSpy = Mockito.spy(new UsageLog());

        usageLogSpy.logUsageEvent(mockLogger, "testMethod", "testMessage");

        // Ensure protected methods are NOT called
        verify(usageLogSpy, never()).buildLogMapFromMDC(any(), any());
        verify(usageLogSpy, never()).logEvent(anyString(), any(), any());
    }
}
