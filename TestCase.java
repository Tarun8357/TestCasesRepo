import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class HealthCheckTest {

    @Test
    void testHealthCheck_CatchBlock() throws Exception {
        // Create instance of HealthCheck
        HealthCheck healthCheck = new HealthCheck();

        // 1️⃣ Mock usageLog (not relevant here but prevents null pointers if accessed)
        UsageLog mockUsageLog = mock(UsageLog.class);
        Field usageLogField = HealthCheck.class.getDeclaredField("usageLog");
        usageLogField.setAccessible(true);
        usageLogField.set(healthCheck, mockUsageLog);

        // 2️⃣ Force a bad path so FileOutputStream throws IOException
        Field fileField = HealthCheck.class.getDeclaredField("healthCheckFile");
        fileField.setAccessible(true);
        // Point to an invalid path so writing will fail
        fileField.set(healthCheck, "/root/invalid/path/healthCheck.txt");

        // 3️⃣ Mock static ErrorLogEventHelper
        try (MockedStatic<ErrorLogEventHelper> mockedStatic = mockStatic(ErrorLogEventHelper.class)) {

            // Execute method (this should go into the catch block)
            healthCheck.health();

            // 4️⃣ Verify EXACT arguments
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
