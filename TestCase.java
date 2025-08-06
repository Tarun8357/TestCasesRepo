
    @Test
    public void testUpdateLockDetails_ReturnsTrue_WhenUpdateCountIsGreaterThanZero() throws Exception {
        // Arrange
        MergeMessageProcessor processor = new MergeMessageProcessor();

        // Mock personLocksDao
        PersonLocksDao mockDao = mock(PersonLocksDao.class);
        injectPrivateField(processor, "personLocksDao", mockDao);

        // Create dummy inputs
        PersonLockVO oldLock = new PersonLockVO("key1", "typeA", 2, new Timestamp(System.currentTimeMillis()));
        PersonLockVO newLock = new PersonLockVO(1001L);

        // Mock DAO behavior
        when(mockDao.updatePersonLock(anyString(), anyList())).thenReturn(1);

        // Access private method
        Method method = MergeMessageProcessor.class.getDeclaredMethod("updateLockDetails", PersonLockVO.class, PersonLockVO.class);
        method.setAccessible(true);

        // Act
        boolean result = (boolean) method.invoke(processor, oldLock, newLock);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testUpdateLockDetails_ReturnsFalse_WhenUpdateCountIsZero() throws Exception {
        // Arrange
        MergeMessageProcessor processor = new MergeMessageProcessor();

        PersonLocksDao mockDao = mock(PersonLocksDao.class);
        injectPrivateField(processor, "personLocksDao", mockDao);

        PersonLockVO oldLock = new PersonLockVO("key2", "typeB", 0, new Timestamp(System.currentTimeMillis()));
        PersonLockVO newLock = new PersonLockVO(2002L);

        when(mockDao.updatePersonLock(anyString(), anyList())).thenReturn(0);

        Method method = MergeMessageProcessor.class.getDeclaredMethod("updateLockDetails", PersonLockVO.class, PersonLockVO.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(processor, oldLock, newLock);

        assertFalse(result);
    }

    // Utility to inject a private field (mocked DAO)
    private void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }