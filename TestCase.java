import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

class DeleteMessageProcessorTest {

    @Test
    void testDeleteRecords_FirstIfConditionTrue() {
        // Arrange
        DeleteMessageProcessor processor = new DeleteMessageProcessor();

        // Mocks for DAOs
        PersonUnlockDao mockUnlockDao = mock(PersonUnlockDao.class);
        PersonFileDAO mockFileDao = mock(PersonFileDAO.class);
        PersonForgetKeyDao mockForgetDao = mock(PersonForgetKeyDao.class);
        PersonFormDAD mockFormDao = mock(PersonFormDAD.class);
        PersonLocksDao mockLocksDao = mock(PersonLocksDao.class);

        // Inject mocks
        ReflectionTestUtils.setField(processor, "personUnlockDao", mockUnlockDao);
        ReflectionTestUtils.setField(processor, "personFileDAO", mockFileDao);
        ReflectionTestUtils.setField(processor, "personForgetKeyDao", mockForgetDao);
        ReflectionTestUtils.setField(processor, "personFormDAD", mockFormDao);
        ReflectionTestUtils.setField(processor, "personLocksDao", mockLocksDao);

        // PersonLock input
        PersonLockVO mockPersonLock = mock(PersonLockVO.class);
        when(mockPersonLock.getPersonLockId()).thenReturn("lock123");

        // First IF condition returns true
        when(mockUnlockDao.getPersonUnlockRecordBoolean("lock123")).thenReturn(true);

        // Stub out DAO delete calls
        when(mockUnlockDao.deletePersonUnlockRecord("lock123")).thenReturn(true);

        // Act
        processor.deleteRecords(mockPersonLock);

        // Assert
        verify(mockUnlockDao).getPersonUnlockRecordBoolean("lock123");
        verify(mockUnlockDao).deletePersonUnlockRecord("lock123");
    }

    @Test
    void testDeleteRecords_FirstIfConditionFalse() {
        // Arrange
        DeleteMessageProcessor processor = new DeleteMessageProcessor();

        PersonUnlockDao mockUnlockDao = mock(PersonUnlockDao.class);
        ReflectionTestUtils.setField(processor, "personUnlockDao", mockUnlockDao);

        PersonLockVO mockPersonLock = mock(PersonLockVO.class);
        when(mockPersonLock.getPersonLockId()).thenReturn("lock456");

        // First IF returns false
        when(mockUnlockDao.getPersonUnlockRecordBoolean("lock456")).thenReturn(false);

        // Act
        boolean result = processor.deleteRecords(mockPersonLock);

        // Assert
        verify(mockUnlockDao).getPersonUnlockRecordBoolean("lock456");
        verify(mockUnlockDao, never()).deletePersonUnlockRecord(any());
        org.junit.jupiter.api.Assertions.assertFalse(result);
    }
}
