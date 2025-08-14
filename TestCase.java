import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MergeMessageProcessorTest {

    @Test
    void testScenario4_NewIsNewer_isSuccessTrue() {
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteMessageProcessor = mock(DeleteMessageProcessor.class);
        MergeMessageProcessor processor = new MergeMessageProcessor(
                personLocksDao, /* other deps mocked */ deleteMessageProcessor, /* etc */
        );

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setRowChangeTimestamp(Timestamp.from(Instant.now().minusSeconds(60)));
        PersonLockVO newLock = new PersonLockVO();
        newLock.setRowChangeTimestamp(Timestamp.from(Instant.now()));

        when(personLocksDao.getPersonLock(any(), any())).thenReturn(newLock).thenReturn(oldLock);
        when(processor.insertUsageCode(any(), anyString(), anyString())).thenReturn(1);
        when(deleteMessageProcessor.deleteRecords(oldLock)).thenReturn(true);

        PersonsRequestData req = new PersonsRequestData();
        String resp = processor.mergePerson(req, "key");

        assertEquals(Constants.OK, resp);
    }

    @Test
    void testScenario4_NewIsNewer_isSuccessFalse() {
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteMessageProcessor = mock(DeleteMessageProcessor.class);
        MergeMessageProcessor processor = new MergeMessageProcessor(
                personLocksDao, /* other deps mocked */ deleteMessageProcessor
        );

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setRowChangeTimestamp(Timestamp.from(Instant.now().minusSeconds(60)));
        PersonLockVO newLock = new PersonLockVO();
        newLock.setRowChangeTimestamp(Timestamp.from(Instant.now()));

        when(personLocksDao.getPersonLock(any(), any())).thenReturn(newLock).thenReturn(oldLock);
        when(processor.insertUsageCode(any(), anyString(), anyString())).thenReturn(0);
        when(deleteMessageProcessor.deleteRecords(oldLock)).thenReturn(false);

        PersonsRequestData req = new PersonsRequestData();
        String resp = processor.mergePerson(req, "key");

        // Expect not OK
        assertEquals(Constants.ALREADY_PROCESSED, resp);
    }

    @Test
    void testScenario6_ForgotKeyActive_isSuccessTrue() {
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        PersonForgetKeyDao forgetKeyDao = mock(PersonForgetKeyDao.class);
        ValidationUtil validationUtil = mock(ValidationUtil.class);
        DeleteMessageProcessor deleteMessageProcessor = mock(DeleteMessageProcessor.class);

        MergeMessageProcessor processor = new MergeMessageProcessor(
                personLocksDao, forgetKeyDao, validationUtil, deleteMessageProcessor
        );

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setRowChangeTimestamp(Timestamp.from(Instant.now()));
        PersonLockVO newLock = new PersonLockVO();
        newLock.setRowChangeTimestamp(Timestamp.from(Instant.now().minusSeconds(60)));

        when(personLocksDao.getPersonLock(any(), any())).thenReturn(newLock).thenReturn(oldLock);
        when(forgetKeyDao.getPersonForgotKeyRecord(any())).thenReturn(new PersonForgotKeyVO());
        when(validationUtil.insertPersonLockUsageAccountMerge(any(), any(), anyString())).thenReturn(1);
        when(deleteMessageProcessor.deleteRecords(oldLock)).thenReturn(true);
        when(processor.checkValidStatus(any(), any(), any(), any())).thenReturn(true);

        PersonsRequestData req = new PersonsRequestData();
        String resp = processor.mergePerson(req, "key");

        assertEquals(Constants.OK, resp);
    }

    @Test
    void testScenario5_OldIsNewer_isSuccessTrue() {
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteMessageProcessor = mock(DeleteMessageProcessor.class);
        MergeMessageProcessor processor = new MergeMessageProcessor(
                personLocksDao, /* other deps mocked */ deleteMessageProcessor
        );

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setRowChangeTimestamp(Timestamp.from(Instant.now()));
        PersonLockVO newLock = new PersonLockVO();
        newLock.setRowChangeTimestamp(Timestamp.from(Instant.now().minusSeconds(60)));

        when(personLocksDao.getPersonLock(any(), any())).thenReturn(newLock).thenReturn(oldLock);
        when(processor.insertUsageCode(any(), anyString(), anyString())).thenReturn(1);
        when(processor.updateLockDetails(any(), any())).thenReturn(true);
        when(deleteMessageProcessor.deleteRecords(oldLock)).thenReturn(true);

        PersonsRequestData req = new PersonsRequestData();
        String resp = processor.mergePerson(req, "key");

        assertEquals(Constants.OK, resp);
    }

    @Test
    void testScenario_personLockNull() {
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        MergeMessageProcessor processor = new MergeMessageProcessor(personLocksDao /* etc mocks */);

        PersonLockVO oldLock = new PersonLockVO();
        when(personLocksDao.getPersonLock(any(), any())).thenReturn(null).thenReturn(oldLock);
        when(processor.updatePersonsData(any(), any(), anyString())).thenReturn(true);

        PersonsRequestData req = new PersonsRequestData();
        String resp = processor.mergePerson(req, "key");

        assertEquals(Constants.OK, resp);
    }

    @Test
    void testScenario3_OldNull_NewExists() {
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        MergeMessageProcessor processor = new MergeMessageProcessor(personLocksDao /* etc mocks */);

        PersonLockVO newLock = new PersonLockVO();
        when(personLocksDao.getPersonLock(any(), any())).thenReturn(newLock).thenReturn(null);

        PersonsRequestData req = new PersonsRequestData();
        String resp = processor.mergePerson(req, "key");

        assertEquals(Constants.ALREADY_PROCESSED, resp);
    }
}
