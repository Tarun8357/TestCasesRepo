import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.sql.Timestamp;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MergeMessageProcessorTest {

    @Test
    public void testMergePerson_Scenario4_NewIsNewer_isSuccessTrue() throws Exception {
        // Arrange
        MergeMessageProcessor processor = new MergeMessageProcessor();
        MergeMessageProcessor spyProcessor = spy(processor);

        // Mock dependencies
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteMessageProcessor = mock(DeleteMessageProcessor.class);
        setPrivateField(spyProcessor, "personLocksDao", personLocksDao);
        setPrivateField(spyProcessor, "deleteMessageProcessor", deleteMessageProcessor);

        // Request data
        PersonsRequestData request = new PersonsRequestData();
        request.setUdpid("NEW_ID");
        request.setNmlzclientid("NEW_CLIENT");
        request.setOldudpid("OLD_ID");
        request.setOldnmlzclientid("OLD_CLIENT");

        // Mock person locks
        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);
        when(oldLock.getRowChangeTimestamp()).thenReturn(new Timestamp(1000));
        when(newLock.getRowChangeTimestamp()).thenReturn(new Timestamp(2000));
        when(personLocksDao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);
        when(personLocksDao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);

        // Control internal method behavior
        doReturn(1).when(spyProcessor).insertUsageCode(oldLock, Constants.UDP_MERGE, "testKey");
        when(deleteMessageProcessor.deleteRecords(oldLock)).thenReturn(true);

        // Act
        String result = spyProcessor.mergePerson(request, "testKey");

        // Assert
        assertEquals(Constants.OK, result);
    }

    // Utility method to inject private fields via reflection
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
