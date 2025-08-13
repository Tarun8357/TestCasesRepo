import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeleteMessageProcessorTest {

    @Test
    void testProcessMessage_IdMappingListWithClientIds() {
        // Arrange
        DeleteMessageProcessor processor = Mockito.spy(new DeleteMessageProcessor());
        UsageLog mockUsageLog = mock(UsageLog.class);
        ReflectionTestUtils.setField(processor, "usageLog", mockUsageLog);

        doReturn("mockResponse").when(processor).deletePerson(any(), any());

        IdMapping mapping1 = mock(IdMapping.class);
        when(mapping1.getNormalizedClientId()).thenReturn("clientA");

        Delete delete = mock(Delete.class);
        when(delete.getIdMapping()).thenReturn(Arrays.asList(mapping1));
        when(delete.getGlobalPersonIdentifier()).thenReturn("udp123");

        Body body = mock(Body.class);
        when(body.getDelete()).thenReturn(delete);

        // Act
        processor.processMessage(body, "someKey");

        // Assert
        verify(processor).deletePerson(any(), any());
    }

    @Test
    void testProcessMessage_IdMappingListIsNull() {
        // Arrange
        DeleteMessageProcessor processor = Mockito.spy(new DeleteMessageProcessor());
        UsageLog mockUsageLog = mock(UsageLog.class);
        ReflectionTestUtils.setField(processor, "usageLog", mockUsageLog);

        Delete delete = mock(Delete.class);
        when(delete.getIdMapping()).thenReturn(null);
        when(delete.getGlobalPersonIdentifier()).thenReturn("udp456");

        Body body = mock(Body.class);
        when(body.getDelete()).thenReturn(delete);

        // Act
        processor.processMessage(body, "key2");

        // Assert
        verify(processor, never()).deletePerson(any(), any());
    }

    @Test
    void testProcessMessage_IdMappingListWithNullClientIds() {
        // Arrange
        DeleteMessageProcessor processor = Mockito.spy(new DeleteMessageProcessor());
        UsageLog mockUsageLog = mock(UsageLog.class);
        ReflectionTestUtils.setField(processor, "usageLog", mockUsageLog);

        IdMapping mapping1 = mock(IdMapping.class);
        when(mapping1.getNormalizedClientId()).thenReturn(null);

        Delete delete = mock(Delete.class);
        when(delete.getIdMapping()).thenReturn(Collections.singletonList(mapping1));
        when(delete.getGlobalPersonIdentifier()).thenReturn("udp789");

        Body body = mock(Body.class);
        when(body.getDelete()).thenReturn(delete);

        // Act
        processor.processMessage(body, "key3");

        // Assert
        verify(processor, never()).deletePerson(any(), any());
    }
}
