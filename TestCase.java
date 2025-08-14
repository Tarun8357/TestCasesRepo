import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MergeMessageProcessorTest {

    @Test
    void scenario1_noLocksFound() {
        MergeMessageProcessor processor = new MergeMessageProcessor();
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);

        PersonsRequestData req = new PersonsRequestData();
        req.setUdpid("udp1");
        req.setNmlzclientid("nmlz1");
        req.setOldudpid("udp2");
        req.setOldnmlzclientid("nmlz2");

        when(personLocksDao.getPersonLock(anyString(), anyString())).thenReturn(null);

        String result = processor.mergePerson(req, "key");
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    @Test
    void scenario3_oldLockNull_newExists() {
        MergeMessageProcessor processor = new MergeMessageProcessor();
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);

        PersonsRequestData req = new PersonsRequestData();
        req.setUdpid("udp1");
        req.setNmlzclientid("nmlz1");
        req.setOldudpid("udp2");
        req.setOldnmlzclientid("nmlz2");

        PersonLockVO newLock = new PersonLockVO();
        when(personLocksDao.getPersonLock("udp1", "nmlz1")).thenReturn(newLock);
        when(personLocksDao.getPersonLock("udp2", "nmlz2")).thenReturn(null);

        String result = processor.mergePerson(req, "key");
        assertEquals(Constants.ALREADY_PROCESSED, result);
    }

    @Test
    void scenario2_personLockNull_oldLockExists() {
        MergeMessageProcessor processor = new MergeMessageProcessor();
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);

        // Use reflection to mock private method updatePersonsData
        ReflectionTestUtils.setField(processor, "updatePersonsData", (UpdatePersonsDataHandler) (r, l, k) -> true);

        PersonsRequestData req = new PersonsRequestData();
        req.setUdpid("udp1");
        req.setNmlzclientid("nmlz1");
        req.setOldudpid("udp2");
        req.setOldnmlzclientid("nmlz2");

        PersonLockVO oldLock = new PersonLockVO();
        when(personLocksDao.getPersonLock("udp1", "nmlz1")).thenReturn(null);
        when(personLocksDao.getPersonLock("udp2", "nmlz2")).thenReturn(oldLock);

        String result = processor.mergePerson(req, "key");
        assertEquals(Constants.OK, result);
    }

    @Test
    void scenario4_newIsNewer() {
        MergeMessageProcessor processor = new MergeMessageProcessor();
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteProcessor = mock(DeleteMessageProcessor.class);
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteProcessor);

        // reflection stub private insertUsageCode
        ReflectionTestUtils.setField(processor, "insertUsageCode", (InsertUsageCodeHandler) (l, c, k) -> 1);

        PersonsRequestData req = new PersonsRequestData();
        req.setUdpid("udp1");
        req.setNmlzclientid("nmlz1");
        req.setOldudpid("udp2");
        req.setOldnmlzclientid("nmlz2");

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-01 00:00:00"));
        PersonLockVO newLock = new PersonLockVO();
        newLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-02 00:00:00"));

        when(personLocksDao.getPersonLock("udp1", "nmlz1")).thenReturn(newLock);
        when(personLocksDao.getPersonLock("udp2", "nmlz2")).thenReturn(oldLock);
        when(deleteProcessor.deleteRecords(any())).thenReturn(true);

        String result = processor.mergePerson(req, "key");
        assertEquals(Constants.OK, result);
    }

    @Test
    void scenario6_oldIsNewer_checkValidStatusTrue() {
        MergeMessageProcessor processor = new MergeMessageProcessor();
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        PersonForgetKeyDao forgetKeyDao = mock(PersonForgetKeyDao.class);
        DeleteMessageProcessor deleteProcessor = mock(DeleteMessageProcessor.class);
        ValidationUtil validationUtil = mock(ValidationUtil.class);

        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "personForgetKeyDao", forgetKeyDao);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteProcessor);
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);

        // reflection stub private checkValidStatus
        ReflectionTestUtils.setField(processor, "checkValidStatus", (CheckValidStatusHandler) (o, n, s, r) -> true);

        PersonsRequestData req = new PersonsRequestData();
        req.setUdpid("udp1");
        req.setNmlzclientid("nmlz1");
        req.setOldudpid("udp2");
        req.setOldnmlzclientid("nmlz2");

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-02 00:00:00"));
        PersonLockVO newLock = new PersonLockVO();
        newLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-01 00:00:00"));

        when(personLocksDao.getPersonLock("udp1", "nmlz1")).thenReturn(newLock);
        when(personLocksDao.getPersonLock("udp2", "nmlz2")).thenReturn(oldLock);
        when(validationUtil.insertPersonLockUsageAccountMerge(any(), any(), anyString())).thenReturn(1);
        when(deleteProcessor.deleteRecords(any())).thenReturn(true);

        String result = processor.mergePerson(req, "key");
        assertEquals(Constants.OK, result);
    }

    @Test
    void scenario5_oldIsNewer_checkValidStatusFalse() {
        MergeMessageProcessor processor = new MergeMessageProcessor();
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteProcessor = mock(DeleteMessageProcessor.class);

        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteProcessor);

        // reflection stub private checkValidStatus
        ReflectionTestUtils.setField(processor, "checkValidStatus", (CheckValidStatusHandler) (o, n, s, r) -> false);
        // reflection stub private insertUsageCode
        ReflectionTestUtils.setField(processor, "insertUsageCode", (InsertUsageCodeHandler) (l, c, k) -> 1);
        // reflection stub private updateLockDetails
        ReflectionTestUtils.setField(processor, "updateLockDetails", (UpdateLockDetailsHandler) (o, n) -> true);

        PersonsRequestData req = new PersonsRequestData();
        req.setUdpid("udp1");
        req.setNmlzclientid("nmlz1");
        req.setOldudpid("udp2");
        req.setOldnmlzclientid("nmlz2");

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-02 00:00:00"));
        PersonLockVO newLock = new PersonLockVO();
        newLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-01 00:00:00"));

        when(personLocksDao.getPersonLock("udp1", "nmlz1")).thenReturn(newLock);
        when(personLocksDao.getPersonLock("udp2", "nmlz2")).thenReturn(oldLock);
        when(deleteProcessor.deleteRecords(any())).thenReturn(true);

        String result = processor.mergePerson(req, "key");
        assertEquals(Constants.OK, result);
    }

    @Test
    void finalElseIf_and_responseMsgNull() {
        MergeMessageProcessor processor = new MergeMessageProcessor();
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);

        // This is more of a default fallthrough case
        PersonsRequestData req = new PersonsRequestData();
        req.setUdpid("udp1");
        req.setNmlzclientid("nmlz1");
        req.setOldudpid("udp2");
        req.setOldnmlzclientid("nmlz2");

        PersonLockVO lock1 = new PersonLockVO();
        PersonLockVO lock2 = new PersonLockVO();

        when(personLocksDao.getPersonLock(anyString(), anyString())).thenReturn(lock1).thenReturn(lock2);

        String result = processor.mergePerson(req, "key");
        // fallback text should be hit if no earlier branch sets message
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    // Functional interfaces to allow lambda stubbing of private methods
    @FunctionalInterface
    private interface UpdatePersonsDataHandler {
        boolean apply(Object req, Object lock, String key);
    }

    @FunctionalInterface
    private interface InsertUsageCodeHandler {
        int apply(Object lock, String code, String key);
    }

    @FunctionalInterface
    private interface UpdateLockDetailsHandler {
        boolean apply(Object oldLock, Object newLock);
    }

    @FunctionalInterface
    private interface CheckValidStatusHandler {
        boolean apply(Object o, Object n, Object arr, Object req);
    }
}
