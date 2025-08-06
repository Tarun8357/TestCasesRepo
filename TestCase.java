    private MergeMessageProcessor setupProcessor(int updateCount, int insertCount, int updateHistoryCount) throws Exception {
        MergeMessageProcessor processor = new MergeMessageProcessor();

        // Mocks
        PersonLockHistoryDao mockHistoryDao = mock(PersonLockHistoryDao.class);
        PersonLocksDao mockLocksDao = mock(PersonLocksDao.class);
        UsageLog mockUsageLog = mock(UsageLog.class);

        injectPrivateField(processor, "personLockHistoryDao", mockHistoryDao);
        injectPrivateField(processor, "personLocksDao", mockLocksDao);
        injectPrivateField(processor, "usageLog", mockUsageLog);

        // Mock personHistoryList with one item
        PersonLockVO historyItem = new PersonLockVO(1L);
        when(mockHistoryDao.getPersonLockHistoryList(anyString(), anyString()))
                .thenReturn(List.of(historyItem));

        // Mock update of history
        when(mockLocksDao.updatePersonLock(eq("UPDATE_UDP_AND_CLIENT_ID_HISTORY"), anyList()))
                .thenReturn(updateHistoryCount);

        // Mock update of oldPersonLock
        when(mockLocksDao.updatePersonLock(eq("UPDATE_UDP_AND_CLIENT_ID"), anyList()))
                .thenReturn(updateCount);

        // Hardcoded insertCount via mocked insertUsageCode (simulate behavior)
        injectPrivateField(processor, "insertCount", insertCount); // Simulated

        return processor;
    }

    @Test
    public void testIsUpdated_AllGreaterThanZero_ShouldReturnTrue() throws Exception {
        MergeMessageProcessor processor = setupProcessor(1, 1, 1);
        boolean result = invokeUpdatePersonsData(processor);
        assertTrue(result);
    }

    @Test
    public void testIsUpdated_UpdateCountZero_ShouldReturnFalse() throws Exception {
        MergeMessageProcessor processor = setupProcessor(0, 1, 1);
        boolean result = invokeUpdatePersonsData(processor);
        assertFalse(result);
    }

    @Test
    public void testIsUpdated_TwoZero_ShouldReturnFalse() throws Exception {
        MergeMessageProcessor processor = setupProcessor(0, 1, 0);
        boolean result = invokeUpdatePersonsData(processor);
        assertFalse(result);
    }

    @Test
    public void testIsUpdated_AllZero_ShouldReturnFalse() throws Exception {
        MergeMessageProcessor processor = setupProcessor(0, 0, 0);
        boolean result = invokeUpdatePersonsData(processor);
        assertFalse(result);
    }

    private boolean invokeUpdatePersonsData(MergeMessageProcessor processor) throws Exception {
        // Create a mock request
        PersonsRequestData mockRequest = mock(PersonsRequestData.class);
        when(mockRequest.getUdpid()).thenReturn("UDP1");
        when(mockRequest.getNmlzclientid()).thenReturn("CLNT1");
        when(mockRequest.getOldudpid()).thenReturn("OLD_UDP");
        when(mockRequest.getOldnmlzclientid()).thenReturn("OLD_CLNT");

        PersonLockVO oldLock = new PersonLockVO(99L);

        // Reflect and call the method
        Method method = MergeMessageProcessor.class.getDeclaredMethod("updatePersonsData",
                PersonsRequestData.class, PersonLockVO.class, String.class);
        method.setAccessible(true);

        return (boolean) method.invoke(processor, mockRequest, oldLock, "testKey");
    }

    private void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException e) {
            // Simulate insertUsageCode result
            if ("insertCount".equals(fieldName)) {
                Method m = target.getClass().getDeclaredMethod("setInsertCountForTest", int.class);
                m.setAccessible(true);
                m.invoke(target, value);
            } else {
                throw e;
            }
        }
    }





    888888888888888888888888888888888888888888888888888888888888888888888

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
}


@Test
public void testIsUpdated_AllGreaterThanZero_ShouldReturnTrue() throws Exception {
    // Spy on real object
    MergeMessageProcessor processor = spy(new MergeMessageProcessor());

    // Inject dependencies
    PersonLocksDao mockLocksDao = mock(PersonLocksDao.class);
    PersonLockHistoryDao mockHistoryDao = mock(PersonLockHistoryDao.class);
    UsageLog mockUsageLog = mock(UsageLog.class);

    setPrivateField(processor, "personLocksDao", mockLocksDao);
    setPrivateField(processor, "personLockHistoryDao", mockHistoryDao);
    setPrivateField(processor, "usageLog", mockUsageLog);

    // Setup mocks
    when(mockHistoryDao.getPersonLockHistoryList(anyString(), anyString()))
        .thenReturn(List.of(new PersonLockVO(1L)));

    when(mockLocksDao.updatePersonLock(eq("UPDATE_UDP_AND_CLIENT_ID_HISTORY"), anyList()))
        .thenReturn(1); // updateHistoryCount

    when(mockLocksDao.updatePersonLock(eq("UPDATE_UDP_AND_CLIENT_ID"), anyList()))
        .thenReturn(1); // updateCount

    // Spy on insertUsageCode method to return 1
    doReturn(1).when(processor).insertUsageCode(any(), any(), any());

    // Create inputs
    PersonsRequestData mockRequest = mock(PersonsRequestData.class);
    when(mockRequest.getUdpid()).thenReturn("UDP1");
    when(mockRequest.getNmlzclientid()).thenReturn("CLNT1");
    when(mockRequest.getOldudpid()).thenReturn("OLD_UDP");
    when(mockRequest.getOldnmlzclientid()).thenReturn("OLD_CLNT");

    PersonLockVO oldLock = new PersonLockVO(99L);

    // Reflect and call the method
    Method method = MergeMessageProcessor.class.getDeclaredMethod("updatePersonsData",
            PersonsRequestData.class, PersonLockVO.class, String.class);
    method.setAccessible(true);

    boolean result = (boolean) method.invoke(processor, mockRequest, oldLock, "testKey");
    assertTrue(result);
}

