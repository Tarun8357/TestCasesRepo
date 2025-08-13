import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class UsageLogTest {

    @Test
    void testGetLogger_PublisherBlank() {
        try (MockedStatic<LogUtils> logUtilsMock = mockStatic(LogUtils.class)) {
            logUtilsMock.when(() -> LogUtils.getLogAttribute(LogAttributes.PUBLISHER_ID_ATTRIB))
                        .thenReturn("");

            UsageLog usageLog = new UsageLog();
            Logger logger = usageLog.getLogger();

            assertEquals(LoggerFactory.getLogger("USAGE").getName(), logger.getName());
        }
    }

    @Test
    void testGetLogger_PublisherNotBlank() {
        try (MockedStatic<LogUtils> logUtilsMock = mockStatic(LogUtils.class)) {
            logUtilsMock.when(() -> LogUtils.getLogAttribute(LogAttributes.PUBLISHER_ID_ATTRIB))
                        .thenReturn("PublisherX");

            UsageLog usageLog = new UsageLog();
            Logger logger = usageLog.getLogger();

            assertEquals(LoggerFactory.getLogger("USAGE.PublisherX").getName(), logger.getName());
        }
    }
}
