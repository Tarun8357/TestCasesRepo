import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

import java.lang.reflect.Field;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class HealthCheckTest {

    @Test
    void testHealthCheck_CatchBlock() throws Exception {
        // Create a spy so we can mock protected/private methods like checkKafka, checkDB
        HealthCheck healthCheck = spy(new HealthCheck());

        // ✅ Mock checkKafka and checkDB so they don't throw NPE
        doReturn(true).when(healthCheck).checkKafka(any());
        doReturn(true).when(healthCheck).checkDB();

        // Mock usageLog to avoid NPE when logging
        UsageLog mockUsageLog = mock(UsageLog.class);
        Field usageLogField = HealthCheck.class.getDeclaredField("usageLog");
        usageLogField.setAccessible(true);
        usageLogField.set(healthCheck, mockUsageLog);

        // Set healthCheckFile to an invalid path to force IOException
        Field fileField = HealthCheck.class.getDeclaredField("healthCheckFile");
        fileField.setAccessible(true);
        fileField.set(healthCheck, "/root/invalid/path/healthCheck.txt");

        // Mock static ErrorLogEventHelper
        try (MockedStatic<ErrorLogEventHelper> mockedStatic = mockStatic(ErrorLogEventHelper.class)) {

            // Execute method — should hit catch block
            healthCheck.health();

            // Verify static log call
            mockedStatic.verify(() -> ErrorLogEventHelper.logErrorEvent(
                    eq(HealthCheck.class.getName()),
                    eq("Error while writing the Kafka health check output file"),
                    eq("health()"),
                    any(IOException.class),
                    eq(""),
                    eq(ErrorLogEvent.ERROR_SEVERITY)
            ));
        }
    }
}
