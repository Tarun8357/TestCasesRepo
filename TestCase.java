@Test
void testDeleteRecords_FirstIfConditionTrue() {
    // Arrange
    DeleteMessageProcessor processor = new DeleteMessageProcessor();

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

    // First if → true
    when(mockUnlockDao.getPersonUnlockRecordBoolean("lock123")).thenReturn(true);
    when(mockUnlockDao.deletePersonUnlockRecord("lock123")).thenReturn(true);

    // Later if's → false so they won't execute
    when(mockFileDao.getPersonFileCountBoolean(any())).thenReturn(false);
    when(mockForgetDao.isRecordPresent(any())).thenReturn(false);
    when(mockFormDao.getPersonFormCountBoolean(any())).thenReturn(false);

    // Act
    processor.deleteRecords(mockPersonLock);

    // Assert
    verify(mockUnlockDao).getPersonUnlockRecordBoolean("lock123");
    verify(mockUnlockDao).deletePersonUnlockRecord("lock123");
}
