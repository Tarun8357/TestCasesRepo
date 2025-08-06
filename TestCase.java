@Test
    public void testUpdatePersonsData_WhenPersonHistoryListIsNull_ShouldReturnFalse() throws Exception {
        // Arrange
        MergeMessageProcessor processor = new MergeMessageProcessor();

        // Mock dependencies
        PersonLockHistoryDao mockHistoryDao = mock(PersonLockHistoryDao.class);
        PersonLocksDao mockLocksDao = mock(PersonLocksDao.class);
        UsageLog mockUsageLog = mock(UsageLog.class);

        injectPrivateField(processor, "personLockHistoryDao", mockHistoryDao);
        injectPrivateField(processor, "personLocksDao", mockLocksDao);
        injectPrivateField(processor, "usageLog", mockUsageLog);

        // Prepare input
        PersonsRequestData mockRequest = mock(PersonsRequestData.class);
        when(mockRequest.getUdpid()).thenReturn("NEW-UDP");
        when(mockRequest.getNmlzclientid()).thenReturn("NEW-CLIENT");
        when(mockRequest.getOldudpid()).thenReturn("OLD-UDP");
        when(mockRequest.getOldnmlzclientid()).thenReturn("OLD-CLIENT");

        when(mockHistoryDao.getPersonLockHistoryList("OLD-UDP", "OLD-CLIENT")).thenReturn(null);

        // Access private method via reflection
        Method method = MergeMessageProcessor.class.getDeclaredMethod(
                "updatePersonsData", PersonsRequestData.class, PersonLockVO.class, String.class);
        method.setAccessible(true);

        // Act
        boolean result = (boolean) method.invoke(processor, mockRequest, null, "someKey");

        // Assert
        assertFalse(result);
    }

    @Test
    public void testUpdatePersonsData_WhenPersonHistoryListIsNotNull_ShouldEnterLoop() throws Exception {
        // Arrange
        MergeMessageProcessor processor = new MergeMessageProcessor();

        PersonLockHistoryDao mockHistoryDao = mock(PersonLockHistoryDao.class);
        PersonLocksDao mockLocksDao = mock(PersonLocksDao.class);
        UsageLog mockUsageLog = mock(UsageLog.class);

        injectPrivateField(processor, "personLockHistoryDao", mockHistoryDao);
        injectPrivateField(processor, "personLocksDao", mockLocksDao);
        injectPrivateField(processor, "usageLog", mockUsageLog);

        // Prepare input
        PersonsRequestData mockRequest = mock(PersonsRequestData.class);
        when(mockRequest.getUdpid()).thenReturn("NEW-UDP");
        when(mockRequest.getNmlzclientid()).thenReturn("NEW-CLIENT");
        when(mockRequest.getOldudpid()).thenReturn("OLD-UDP");
        when(mockRequest.getOldnmlzclientid()).thenReturn("OLD-CLIENT");

        PersonLockVO historyItem = new PersonLockVO(101L);
        when(mockHistoryDao.getPersonLockHistoryList("OLD-UDP", "OLD-CLIENT"))
                .thenReturn(List.of(historyItem));

        when(mockLocksDao.updatePersonLock(anyString(), anyList())).thenReturn(1);

        // Access private method
        Method method = MergeMessageProcessor.class.getDeclaredMethod(
                "updatePersonsData", PersonsRequestData.class, PersonLockVO.class, String.class);
        method.setAccessible(true);

        // Act
        boolean result = (boolean) method.invoke(processor, mockRequest, null, "someKey");

        // Assert
        assertTrue(result); // Based on updateCount > 0 → isUpdated = true
    }

    // Helper to inject private fields
    private void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
