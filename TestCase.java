@Test
void testDeleteRecords_FirstIfConditionFalse() {
    // Arrange
    DeleteMessageProcessor processor = new DeleteMessageProcessor();

    PersonUnlockDao mockUnlockDao = mock(PersonUnlockDao.class);
    PersonFileDAO mockFileDao = mock(PersonFileDAO.class);
    PersonForgetKeyDao mockForgetDao = mock(PersonForgetKeyDao.class);
    PersonFormDAD mockFormDao = mock(PersonFormDAD.class);
    PersonLocksDao mockLocksDao = mock(PersonLocksDao.class);

    // Inject mocks to avoid NPE in later if conditions
    ReflectionTestUtils.setField(processor, "personUnlockDao", mockUnlockDao);
    ReflectionTestUtils.setField(processor, "personFileDAO", mockFileDao);
    ReflectionTestUtils.setField(processor, "personForgetKeyDao", mockForgetDao);
    ReflectionTestUtils.setField(processor, "personFormDAD", mockFormDao);
    ReflectionTestUtils.setField(processor, "personLocksDao", mockLocksDao);

    // PersonLock input
    PersonLockVO mockPersonLock = mock(PersonLockVO.class);
    when(mockPersonLock.getPersonLockId()).thenReturn("lock123");

    // First if → false
    when(mockUnlockDao.getPersonUnlockRecordBoolean("lock123")).thenReturn(false);

    // Act
    boolean result = processor.deleteRecords(mockPersonLock);

    // Assert
    assertFalse(result);
    verify(mockUnlockDao).getPersonUnlockRecordBoolean("lock123");
    verify(mockUnlockDao, never()).deletePersonUnlockRecord(any());
}
