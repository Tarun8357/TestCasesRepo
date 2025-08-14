import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MergeMessageProcessorTest {

    @Test
    void testScenario4_NewIsNewer_IsSuccessTrue() {
        MergeMessageProcessor processor = Mockito.spy(new MergeMessageProcessor());

        // Mock NEW lock with newer timestamp
        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);
        when(oldLock.getRowChangeTimestamp()).thenReturn(1000L);
        when(newLock.getRowChangeTimestamp()).thenReturn(2000L);

        doReturn(newLock).when(processor).getPersonLock(any(), any());
        doReturn(oldLock).when(processor).getPersonLock(any(), any());

        doReturn(1).when(processor).insertUsageCode(any(), any(), any());
        doReturn(true).when(processor).deleteRecords(any());

        PersonsRequestData request = mock(PersonsRequestData.class);

        String response = (String) ReflectionTestUtils.invokeMethod(
                processor, "mergePerson", request, "key"
        );

        assertTrue(response.contains("OK"));
    }

    @Test
    void testScenario5_OldIsNewer_IsSuccessTrue() {
        MergeMessageProcessor processor = Mockito.spy(new MergeMessageProcessor());

        // OLD lock newer
        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);
        when(oldLock.getRowChangeTimestamp()).thenReturn(2000L);
        when(newLock.getRowChangeTimestamp()).thenReturn(1000L);

        doReturn(1).when(processor).insertUsageCode(any(), any(), any());
        doReturn(true).when(processor).updateLockDetails(any(), any());
        doReturn(true).when(processor).deleteRecords(any());

        PersonsRequestData request = mock(PersonsRequestData.class);

        String response = (String) ReflectionTestUtils.invokeMethod(
                processor, "mergePerson", request, "key"
        );

        assertTrue(response.contains("OK"));
    }

    @Test
    void testScenario6_OldIsNewer_ForgotKeyActive() {
        MergeMessageProcessor processor = Mockito.spy(new MergeMessageProcessor());

        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);
        when(oldLock.getRowChangeTimestamp()).thenReturn(2000L);
        when(newLock.getRowChangeTimestamp()).thenReturn(1000L);

        // Simulate Forgot Key active
        doReturn(true).when(processor).checkValidStatus(any(), any(), any());

        doReturn(1).when(processor).insertPersonLockUsageAccountMerge(any(), any(), any());
        doReturn(true).when(processor).updateLockDetails(any(), any());
        doReturn(true).when(processor).deleteRecords(any());

        PersonsRequestData request = mock(PersonsRequestData.class);

        String response = (String) ReflectionTestUtils.invokeMethod(
                processor, "mergePerson", request, "key"
        );

        assertTrue(response.contains("OK"));
    }

    @Test
    void testScenario3_NoOldLock_NewLockExists() {
        MergeMessageProcessor processor = Mockito.spy(new MergeMessageProcessor());

        doReturn(null).when(processor).getPersonLock(any(), any()); // OLD lock
        doReturn(mock(PersonLockVO.class)).when(processor).getPersonLock(any(), any()); // NEW lock

        PersonsRequestData request = mock(PersonsRequestData.class);

        String response = (String) ReflectionTestUtils.invokeMethod(
                processor, "mergePerson", request, "key"
        );

        assertEquals(Constants.ALREADY_PROCESSED, response);
    }

    @Test
    void testIsSuccessFalsePath() {
        MergeMessageProcessor processor = Mockito.spy(new MergeMessageProcessor());

        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);

        doReturn(oldLock).when(processor).getPersonLock(any(), any());
        doReturn(newLock).when(processor).getPersonLock(any(), any());

        doReturn(0).when(processor).insertUsageCode(any(), any(), any()); // Force fail

        PersonsRequestData request = mock(PersonsRequestData.class);

        String response = (String) ReflectionTestUtils.invokeMethod(
                processor, "mergePerson", request, "key"
        );

        assertNotNull(response);
    }

    @Test
    void testFinalElseIf_ErrorMessage() {
        MergeMessageProcessor processor = Mockito.spy(new MergeMessageProcessor());

        doReturn(null).when(processor).getPersonLock(any(), any()); // No locks found

        PersonsRequestData request = mock(PersonsRequestData.class);

        String response = (String) ReflectionTestUtils.invokeMethod(
                processor, "mergePerson", request, "key"
        );

        assertEquals(
                "Lock does not exists for old and new and history data also not found",
                response
        );
    }
}
