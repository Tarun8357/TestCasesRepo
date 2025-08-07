@Test
void testUpdateLockDetails_ShouldReturnTrue_WhenUpdateCountIsGreaterThanZero() throws Exception {
    MergeMessageProcessor processor = new MergeMessageProcessor();
    
    PersonLockVO oldLock = mock(PersonLockVO.class);
    PersonLockVO newLock = mock(PersonLockVO.class);
    
    when(oldLock.getLockKeyValue()).thenReturn("key");
    when(oldLock.getLockTypeCode()).thenReturn("type");
    when(oldLock.getFailedKeyAttempt()).thenReturn(1);
    when(oldLock.getLastFailedKeyTimestamp()).thenReturn(Timestamp.valueOf("2023-01-01 00:00:00"));
    when(newLock.getPersonLockId()).thenReturn(123L);

    PersonLocksDao mockDao = mock(PersonLocksDao.class);
    when(mockDao.updatePersonLock(anyString(), anyList())).thenReturn(1);
    
    Field daoField = MergeMessageProcessor.class.getDeclaredField("personLocksDao");
    daoField.setAccessible(true);
    daoField.set(processor, mockDao);

    Method method = MergeMessageProcessor.class.getDeclaredMethod("updateLockDetails", PersonLockVO.class, PersonLockVO.class);
    method.setAccessible(true);

    boolean result = (boolean) method.invoke(processor, oldLock, newLock);

    assertTrue(result);
}
