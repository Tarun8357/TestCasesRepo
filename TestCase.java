import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PersonLockServiceTest {

    @Test
    public void testUpdateLockDetails_ReturnsTrue_WhenUpdateCountGreaterThanZero() {
        // Arrange
        PersonLocksDao mockDao = mock(PersonLocksDao.class);
        PersonLockService service = new PersonLockService(mockDao); // assuming DI via constructor

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setLockKeyValue("key1");
        oldLock.setLockTypeCode("TYPE1");
        oldLock.setFailedKeyAttempt(2);
        oldLock.setLastFailedKeyTimestamp(new Timestamp(System.currentTimeMillis()));

        PersonLockVO newLock = new PersonLockVO();
        newLock.setPersonLockId(123L);

        when(mockDao.updatePersonLock(eq("UPDATE_LOCK_DETAILS"), anyList())).thenReturn(1);

        // Act
        boolean result = service.updateLockDetails(oldLock, newLock);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testUpdateLockDetails_ReturnsFalse_WhenUpdateCountIsZero() {
        // Arrange
        PersonLocksDao mockDao = mock(PersonLocksDao.class);
        PersonLockService service = new PersonLockService(mockDao);

        PersonLockVO oldLock = new PersonLockVO();
        oldLock.setLockKeyValue("key2");
        oldLock.setLockTypeCode("TYPE2");
        oldLock.setFailedKeyAttempt(1);
        oldLock.setLastFailedKeyTimestamp(new Timestamp(System.currentTimeMillis()));

        PersonLockVO newLock = new PersonLockVO();
        newLock.setPersonLockId(456L);

        when(mockDao.updatePersonLock(eq("UPDATE_LOCK_DETAILS"), anyList())).thenReturn(0);

        // Act
        boolean result = service.updateLockDetails(oldLock, newLock);

        // Assert
        assertFalse(result);
    }
}
