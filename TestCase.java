import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class MergeMessageProcessorFullTest {

    @Test
    void testScenario4_NewIsNewer_isSuccessTrue() {
        MergeMessageProcessor processor = new MergeMessageProcessor();

        // Mock dependencies
        PersonLocksDao personLocksDao = Mockito.mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteMessageProcessor = Mockito.mock(DeleteMessageProcessor.class);
        ValidationUtil validationUtil = Mockito.mock(ValidationUtil.class);
        PersonForgetKeyDao personForgetKeyDao = Mockito.mock(PersonForgetKeyDao.class);

        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteMessageProcessor);
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        ReflectionTestUtils.setField(processor, "personForgetKeyDao", personForgetKeyDao);

        // Locks
        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setRowChangeTimestamp(Timestamp.valueOf("2023-01-01 10:00:00"));

        PersonLockVO newLock = new PersonLockVO();
        newLock.setRowChangeTimestamp(Timestamp.valueOf("2023-01-02 10:00:00"));

        Mockito.when(personLocksDao.getPersonLock(any(), any()))
                .thenReturn(newLock) // 1st call
                .thenReturn(oldLock); // 2nd call

        Mockito.when(validationUtil.insertUsageCode(any(), any(), any())).thenReturn(1);
        Mockito.when(deleteMessageProcessor.deleteRecords(any())).thenReturn(true);

        String result = processor.mergePerson(new PersonsRequestData(), "key");
        assertEquals(Constants.OK, result);
    }

    @Test
    void testScenario4_NewIsNewer_isSuccessFalse() {
        MergeMessageProcessor processor = new MergeMessageProcessor();

        PersonLocksDao personLocksDao = Mockito.mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteMessageProcessor = Mockito.mock(DeleteMessageProcessor.class);
        ValidationUtil validationUtil = Mockito.mock(ValidationUtil.class);

        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteMessageProcessor);
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setRowChangeTimestamp(Timestamp.valueOf("2023-01-01 10:00:00"));

        PersonLockVO newLock = new PersonLockVO();
        newLock.setRowChangeTimestamp(Timestamp.valueOf("2023-01-02 10:00:00"));

        Mockito.when(personLocksDao.getPersonLock(any(), any()))
                .thenReturn(newLock)
                .thenReturn(oldLock);

        Mockito.when(validationUtil.insertUsageCode(any(), any(), any())).thenReturn(0);
        Mockito.when(deleteMessageProcessor.deleteRecords(any())).thenReturn(false);

        String result = processor.mergePerson(new PersonsRequestData(), "key");
        assertNotEquals(Constants.OK, result);
    }

    @Test
    void testScenario6_OldIsNewer_ForgotKeyActive_isSuccessTrue() {
        MergeMessageProcessor processor = new MergeMessageProcessor();

        PersonLocksDao personLocksDao = Mockito.mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteMessageProcessor = Mockito.mock(DeleteMessageProcessor.class);
        ValidationUtil validationUtil = Mockito.mock(ValidationUtil.class);
        PersonForgetKeyDao personForgetKeyDao = Mockito.mock(PersonForgetKeyDao.class);

        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteMessageProcessor);
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        ReflectionTestUtils.setField(processor, "personForgetKeyDao", personForgetKeyDao);
        ReflectionTestUtils.setField(processor, "activeProfile", "prod");

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setPersonLockId(1);
        oldLock.setRowChangeTimestamp(Timestamp.valueOf("2023-01-02 10:00:00"));

        PersonLockVO newLock = new PersonLockVO();
        newLock.setPersonLockId(2);
        newLock.setRowChangeTimestamp(Timestamp.valueOf("2023-01-01 10:00:00"));

        Mockito.when(personLocksDao.getPersonLock(any(), any()))
                .thenReturn(newLock) // first call
                .thenReturn(oldLock); // second call

        PersonForgotKeyVO oldForgot = new PersonForgotKeyVO();
        oldForgot.setForgotKeyStatusCode("ACTIVE");

        Mockito.when(personForgetKeyDao.getPersonForgotKeyRecord(eq(1))).thenReturn(oldForgot);
        Mockito.when(personForgetKeyDao.getPersonForgotKeyRecord(eq(2))).thenReturn(null);

        Mockito.when(validationUtil.insertPersonLockUsageAccountMerge(any(), any(), any())).thenReturn(1);
        Mockito.when(deleteMessageProcessor.deleteRecords(any())).thenReturn(true);
        Mockito.when(processor.updateLockDetails(any(), any())).thenReturn(true);

        String result = processor.mergePerson(new PersonsRequestData(), "key");
        assertEquals(Constants.OK, result);
    }

    @Test
    void testScenario3_NoOldLock_AlreadyProcessed() {
        MergeMessageProcessor processor = new MergeMessageProcessor();

        PersonLocksDao personLocksDao = Mockito.mock(PersonLocksDao.class);
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);

        PersonLockVO newLock = new PersonLockVO();
        newLock.setRowChangeTimestamp(Timestamp.valueOf("2023-01-02 10:00:00"));

        Mockito.when(personLocksDao.getPersonLock(any(), any()))
                .thenReturn(null) // old lock
                .thenReturn(newLock); // new lock

        String result = processor.mergePerson(new PersonsRequestData(), "key");
        assertEquals(Constants.ALREADY_PROCESSED, result);
    }

    @Test
    void testNoLocks_ErrorMessageBranch() {
        MergeMessageProcessor processor = new MergeMessageProcessor();

        PersonLocksDao personLocksDao = Mockito.mock(PersonLocksDao.class);
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);

        Mockito.when(personLocksDao.getPersonLock(any(), any())).thenReturn(null);

        String result = processor.mergePerson(new PersonsRequestData(), "key");
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }
}
