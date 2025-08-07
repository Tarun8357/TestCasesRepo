// All test cases for MergeMessageProcessor#mergePerson to cover all major scenarios

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MergeMessageProcessorTest {

    // Utility to inject private dependencies
    private void injectPrivateField(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    private MergeMessageProcessor prepareProcessor(PersonLocksDao locksDao,
                                                    DeleteMessageProcessor deleteProcessor,
                                                    ValidationUtil validationUtil,
                                                    PersonForgetKeyDao forgetKeyDao) throws Exception {
        MergeMessageProcessor processor = spy(new MergeMessageProcessor());
        injectPrivateField(processor, "personLocksDao", locksDao);
        injectPrivateField(processor, "deleteMessageProcessor", deleteProcessor);
        injectPrivateField(processor, "validationUtil", validationUtil);
        injectPrivateField(processor, "personForgetKeyDao", forgetKeyDao);
        return processor;
    }

    @Test
    public void testScenario4_NewIsNewer_isSuccessTrue() throws Exception {
        PersonsRequestData request = createRequest();
        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);

        when(oldLock.getRowChangeTimestamp()).thenReturn(new Timestamp(1000));
        when(newLock.getRowChangeTimestamp()).thenReturn(new Timestamp(2000));

        PersonLocksDao dao = mock(PersonLocksDao.class);
        when(dao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);
        when(dao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);

        ValidationUtil validationUtil = mock(ValidationUtil.class);
        when(validationUtil.insertPersonLockUsage(oldLock, Constants.UDP_MERGE, "key")).thenReturn(1);

        DeleteMessageProcessor deleteProcessor = mock(DeleteMessageProcessor.class);
        when(deleteProcessor.deleteRecords(oldLock)).thenReturn(true);

        MergeMessageProcessor processor = prepareProcessor(dao, deleteProcessor, validationUtil, null);

        String result = processor.mergePerson(request, "key");
        assertEquals(Constants.OK, result);
    }

    @Test
    public void testScenario4_NewIsNewer_insertFails_isSuccessFalse() throws Exception {
        PersonsRequestData request = createRequest();
        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);

        when(oldLock.getRowChangeTimestamp()).thenReturn(new Timestamp(1000));
        when(newLock.getRowChangeTimestamp()).thenReturn(new Timestamp(2000));

        PersonLocksDao dao = mock(PersonLocksDao.class);
        when(dao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);
        when(dao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);

        ValidationUtil validationUtil = mock(ValidationUtil.class);
        when(validationUtil.insertPersonLockUsage(oldLock, Constants.UDP_MERGE, "key")).thenReturn(0);

        DeleteMessageProcessor deleteProcessor = mock(DeleteMessageProcessor.class);
        when(deleteProcessor.deleteRecords(oldLock)).thenReturn(true);

        MergeMessageProcessor processor = prepareProcessor(dao, deleteProcessor, validationUtil, null);

        String result = processor.mergePerson(request, "key");
        assertNotEquals(Constants.OK, result);
    }

    @Test
    public void testScenario5_OldIsNewer_isSuccessTrue() throws Exception {
        PersonsRequestData request = createRequest();
        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);

        when(oldLock.getRowChangeTimestamp()).thenReturn(new Timestamp(2000));
        when(newLock.getRowChangeTimestamp()).thenReturn(new Timestamp(1000));

        PersonLocksDao dao = mock(PersonLocksDao.class);
        when(dao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);
        when(dao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);

        ValidationUtil validationUtil = mock(ValidationUtil.class);
        when(validationUtil.insertPersonLockUsage(oldLock, Constants.UDP_MERGE, "key")).thenReturn(1);

        DeleteMessageProcessor deleteProcessor = mock(DeleteMessageProcessor.class);
        when(deleteProcessor.deleteRecords(oldLock)).thenReturn(true);

        MergeMessageProcessor processor = prepareProcessor(dao, deleteProcessor, validationUtil, null);

        doReturn(true).when(processor).updateLockDetails(oldLock, newLock);

        String result = processor.mergePerson(request, "key");
        assertEquals(Constants.OK, result);
    }

    @Test
    public void testScenario3_OldIsNull_shouldCallUpdatePersonsData() throws Exception {
        PersonsRequestData request = createRequest();
        PersonLockVO newLock = mock(PersonLockVO.class);

        PersonLocksDao dao = mock(PersonLocksDao.class);
        when(dao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(null);
        when(dao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);

        MergeMessageProcessor processor = prepareProcessor(dao, null, null, null);

        doReturn(true).when(processor).updatePersonsData(eq(request), isNull(), eq("key"));

        String result = processor.mergePerson(request, "key");
        assertEquals(Constants.OK, result);
    }

    @Test
    public void testScenario6_ForgotKeyActive_shouldInsertBothUsages() throws Exception {
        PersonsRequestData request = createRequest();
        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);

        when(oldLock.getRowChangeTimestamp()).thenReturn(new Timestamp(2000));
        when(newLock.getRowChangeTimestamp()).thenReturn(new Timestamp(1000));

        PersonForgotKeyVO oldFK = mock(PersonForgotKeyVO.class);
        PersonForgotKeyVO newFK = mock(PersonForgotKeyVO.class);

        PersonLocksDao dao = mock(PersonLocksDao.class);
        when(dao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);
        when(dao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);

        PersonForgetKeyDao fkDao = mock(PersonForgetKeyDao.class);
        when(fkDao.getPersonForgotKeyRecord(anyLong())).thenReturn(oldFK).thenReturn(newFK);

        ValidationUtil validationUtil = mock(ValidationUtil.class);
        when(validationUtil.insertPersonLockUsageAccountMerge(eq(oldLock), eq(newLock), anyString())).thenReturn(1);

        DeleteMessageProcessor deleteProcessor = mock(DeleteMessageProcessor.class);
        when(deleteProcessor.deleteRecords(oldLock)).thenReturn(true);

        MergeMessageProcessor processor = prepareProcessor(dao, deleteProcessor, validationUtil, fkDao);

        doReturn(true).when(processor).checkValidStatus(any(), any(), any(), eq(request));
        doReturn(true).when(processor).updateLockDetails(oldLock, newLock);

        String result = processor.mergePerson(request, "key");
        assertEquals(Constants.OK, result);
    }

    private PersonsRequestData createRequest() {
        PersonsRequestData req = new PersonsRequestData();
        req.setUdpid("NEW_ID");
        req.setNmlzclientid("NEW_CLIENT");
        req.setOldudpid("OLD_ID");
        req.setOldnmlzclientid("OLD_CLIENT");
        return req;
    }
}
