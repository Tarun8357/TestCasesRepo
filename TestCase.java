import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MergeMessageProcessorTest {

    @Test
    public void testMergePerson_Scenario4_NewIsNewer_isSuccessTrue() throws Exception {
        // Create spy of the class under test
        MergeMessageProcessor processor = spy(new MergeMessageProcessor());

        // Mock dependencies
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteMessageProcessor = mock(DeleteMessageProcessor.class);

        // Inject mocks using reflection
        injectPrivateField(processor, "personLocksDao", personLocksDao);
        injectPrivateField(processor, "deleteMessageProcessor", deleteMessageProcessor);

        // Create request
        PersonsRequestData request = new PersonsRequestData();
        request.setUdpid("NEW_ID");
        request.setNmlzclientid("NEW_CLIENT");
        request.setOldudpid("OLD_ID");
        request.setOldnmlzclientid("OLD_CLIENT");

        // Create mock PersonLockVO objects
        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);

        // RowChangeTimestamp: new is newer
        when(oldLock.getRowChangeTimestamp()).thenReturn(new Timestamp(1000));
        when(newLock.getRowChangeTimestamp()).thenReturn(new Timestamp(2000));

        // Stub DAO calls
        when(personLocksDao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);
        when(personLocksDao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);

        // Stub delete success
        when(deleteMessageProcessor.deleteRecords(oldLock)).thenReturn(true);

        // Stub insertUsageCode (private method) via reflection
        Method insertUsageMethod = MergeMessageProcessor.class.getDeclaredMethod(
                "insertUsageCode", PersonLockVO.class, String.class, String.class
        );
        insertUsageMethod.setAccessible(true);

        // Replace behavior by spying method call
        doReturn(1).when(processor).insertUsageCode(oldLock, Constants.UDP_MERGE, "testKey");

        // Call the method
        String result = processor.mergePerson(request, "testKey");

        // Assert success
        assertEquals(Constants.OK, result);
    }

    // Utility to inject private field
    private void injectPrivateField(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }
}
