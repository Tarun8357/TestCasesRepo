import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MergeMessageProcessorCheckValidStatusCoverageTest {

    private MergeMessageProcessor createScenario6Processor(
            PersonForgotKeyVO oldForgot,
            PersonForgotKeyVO newForgot
    ) {
        MergeMessageProcessor processor = Mockito.spy(new MergeMessageProcessor());

        // Mock OLD and NEW locks: OLD newer than NEW
        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);
        when(oldLock.getRowChangeTimestamp()).thenReturn(2000L);
        when(newLock.getRowChangeTimestamp()).thenReturn(1000L);

        // Return locks in correct sequence
        doReturn(newLock)   // for getPersonLock(udpId, clientId) - NEW
            .doReturn(oldLock) // for getPersonLock(oldUdpId, oldClientId) - OLD
            .when(processor).getPersonLock(any(), any());

        // ForgotKey records
        doReturn(oldForgot).when(processor).getPersonForgotKeyRecord(oldLock.getPersonLockId());
        doReturn(newForgot).when(processor).getPersonForgotKeyRecord(newLock.getPersonLockId());

        // Mock insert/update/delete so Scenario 6 completes
        doReturn(1).when(processor).insertPersonLockUsageAccountMerge(any(), any(), any());
        doReturn(true).when(processor).updateLockDetails(any(), any());
        doReturn(true).when(processor).deleteRecords(any());

        // Force prod profile
        ReflectionTestUtils.setField(processor, "activeProfile", "prod");

        return processor;
    }

    @Test
    void testScenario6_OldMatchStatus_Prod() {
        PersonForgotKeyVO oldForgot = new PersonForgotKeyVO();
        oldForgot.setForgotKeyStatusCode("ACTIVE");

        MergeMessageProcessor processor = createScenario6Processor(oldForgot, null);

        PersonsRequestData request = mock(PersonsRequestData.class);
        String response = (String) ReflectionTestUtils.invokeMethod(processor, "mergePerson", request, "key");

        assertTrue(response.contains("OK"));
    }

    @Test
    void testScenario6_OldNoMatchStatus_Prod() {
        PersonForgotKeyVO oldForgot = new PersonForgotKeyVO();
        oldForgot.setForgotKeyStatusCode("EXPIRED"); // not matching

        MergeMessageProcessor processor = createScenario6Processor(oldForgot, null);

        PersonsRequestData request = mock(PersonsRequestData.class);
        String response = (String) ReflectionTestUtils.invokeMethod(processor, "mergePerson", request, "key");

        assertNotNull(response); // should still execute without errors
    }

    @Test
    void testScenario6_NewMatchStatus_Prod() {
        PersonForgotKeyVO newForgot = new PersonForgotKeyVO();
        newForgot.setForgotKeyStatusCode("ACTIVE");

        MergeMessageProcessor processor = createScenario6Processor(null, newForgot);

        PersonsRequestData request = mock(PersonsRequestData.class);
        String response = (String) ReflectionTestUtils.invokeMethod(processor, "mergePerson", request, "key");

        assertTrue(response.contains("OK"));
    }

    @Test
    void testScenario6_NewNoMatchStatus_Prod() {
        PersonForgotKeyVO newForgot = new PersonForgotKeyVO();
        newForgot.setForgotKeyStatusCode("CLOSED");

        MergeMessageProcessor processor = createScenario6Processor(null, newForgot);

        PersonsRequestData request = mock(PersonsRequestData.class);
        String response = (String) ReflectionTestUtils.invokeMethod(processor, "mergePerson", request, "key");

        assertNotNull(response);
    }

    @Test
    void testScenario6_NoForgotKeyRecord() {
        MergeMessageProcessor processor = createScenario6Processor(null, null);

        PersonsRequestData request = mock(PersonsRequestData.class);
        String response = (String) ReflectionTestUtils.invokeMethod(processor, "mergePerson", request, "key");

        assertNotNull(response);
    }
}
