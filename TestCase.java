
    @Test
    public void testMergePerson_Scenario4_NewIsNewer_isSuccessTrue() throws Exception {
        // Create spy of the class under test
        MergeMessageProcessor processor = spy(new MergeMessageProcessor());

        // Create mocks for dependencies
        PersonLocksDao personLocksDao = mock(PersonLocksDao.class);
        DeleteMessageProcessor deleteMessageProcessor = mock(DeleteMessageProcessor.class);
        ValidationUtil validationUtil = mock(ValidationUtil.class);

        // Inject mocks via reflection
        injectPrivateField(processor, "personLocksDao", personLocksDao);
        injectPrivateField(processor, "deleteMessageProcessor", deleteMessageProcessor);
        injectPrivateField(processor, "validationUtil", validationUtil);

        // Prepare test data
        PersonsRequestData request = new PersonsRequestData();
        request.setUdpid("NEW_ID");
        request.setNmlzclientid("NEW_CLIENT");
        request.setOldudpid("OLD_ID");
        request.setOldnmlzclientid("OLD_CLIENT");

        PersonLockVO oldLock = mock(PersonLockVO.class);
        PersonLockVO newLock = mock(PersonLockVO.class);

        // Row change timestamps: new is newer
        when(oldLock.getRowChangeTimestamp()).thenReturn(new Timestamp(1000));
        when(newLock.getRowChangeTimestamp()).thenReturn(new Timestamp(2000));

        // Stubbing DAO methods
        when(personLocksDao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);
        when(personLocksDao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);

        // insertUsageCode indirectly returns value from this:
        when(validationUtil.insertPersonLockUsage(oldLock, Constants.UDP_MERGE, "testKey"))
                .thenReturn(1);

        when(deleteMessageProcessor.deleteRecords(oldLock)).thenReturn(true);

        // Run method under test
        String result = processor.mergePerson(request, "testKey");

        // Assert success
        assertEquals(Constants.OK, result);
    }
