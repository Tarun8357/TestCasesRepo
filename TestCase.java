private void mockPrivateMethod(MergeMessageProcessor processor, String methodName, boolean returnValue) throws Exception {
    Method method = MergeMessageProcessor.class.getDeclaredMethod(methodName, PersonLockVO.class, PersonLockVO.class);
    method.setAccessible(true);

    // Use a spy if method needs to be mocked dynamically (e.g., using PowerMockito)
    MergeMessageProcessor spyProcessor = spy(processor);
    doReturn(returnValue).when(spyProcessor).getClass().getDeclaredMethod(methodName, PersonLockVO.class, PersonLockVO.class);
}

@Test
void testMergePerson_Scenario5_ShouldReturnOk_WhenOldIsNewer() throws Exception {
    MergeMessageProcessor processor = new MergeMessageProcessor();

    // Mocks
    PersonLockVO oldLock = mock(PersonLockVO.class);
    PersonLockVO newLock = mock(PersonLockVO.class);
    PersonLocksDAO personLocksDao = mock(PersonLocksDAO.class);
    DeleteMessageProcessor deleteProcessor = mock(DeleteMessageProcessor.class);
    ValidationUtil validationUtil = mock(ValidationUtil.class);

    // PersonsRequestData
    PersonsRequestData requestData = new PersonsRequestData();
    requestData.setOldudpid("oldUdp");
    requestData.setOldnmlzclientid("oldClientId");
    requestData.setUdpid("newUdp");
    requestData.setNmlzclientid("newClientId");

    // Mocks setup
    when(oldLock.getRowChangeTimestamp()).thenReturn(Timestamp.valueOf("2024-01-02 00:00:00"));
    when(newLock.getRowChangeTimestamp()).thenReturn(Timestamp.valueOf("2023-01-01 00:00:00"));

    when(personLocksDao.getPersonLock("newUdp", "newClientId")).thenReturn(newLock);
    when(personLocksDao.getPersonLock("oldUdp", "oldClientId")).thenReturn(oldLock);

    // insertUsageCode will return 1
    when(validationUtil.insertPersonLockUsage(oldLock, Constants.UDP_MERGE, "key")).thenReturn(1);

    // updateLockDetails returns true
    injectPrivateField(processor, "personLocksDao", personLocksDao);
    injectPrivateField(processor, "validationUtil", validationUtil);
    injectPrivateField(processor, "deleteMessageProcessor", deleteProcessor);
    mockPrivateMethod(processor, "updateLockDetails", true);

    when(deleteProcessor.deleteRecords(oldLock)).thenReturn(true);

    String result = processor.mergePerson(requestData, "key");

    assertEquals(Constants.OK, result);
}
