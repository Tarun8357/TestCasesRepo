@Test
public void testScenario4_insertCountPositive_deleteRecordsTrue_shouldReturnTrue() throws Exception {
    MergeMessageProcessor processor = new MergeMessageProcessor();

    PersonLockVO oldLock = new PersonLockVO();
    oldLock.setRowChangeTimestamp(Timestamp.valueOf("2023-01-01 00:00:00"));

    PersonLockVO newLock = new PersonLockVO();
    newLock.setRowChangeTimestamp(Timestamp.valueOf("2023-02-01 00:00:00"));

    // Mocks
    MergeMessageProcessor spy = Mockito.spy(processor);
    Mockito.doReturn(1).when(spy).insertUsageCode(Mockito.eq(oldLock), Mockito.anyString(), Mockito.anyString());

    DeleteMessageProcessor deleteMock = Mockito.mock(DeleteMessageProcessor.class);
    Mockito.when(deleteMock.deleteRecords(oldLock)).thenReturn(true);

    // Inject mock
    Field deleteField = MergeMessageProcessor.class.getDeclaredField("deleteMessageProcessor");
    deleteField.setAccessible(true);
    deleteField.set(spy, deleteMock);

    // Invoke private method using reflection
    Method method = MergeMessageProcessor.class.getDeclaredMethod("mergePerson", PersonsRequestData.class, String.class);
    method.setAccessible(true);

    // Setup input
    PersonsRequestData req = new PersonsRequestData();
    req.setUdpid("NEW_ID");
    req.setNmlzclientid("NEW_CLIENT");
    req.setOldudpid("OLD_ID");
    req.setOldnmlzclientid("OLD_CLIENT");

    // Mock DAO
    PersonLocksDao dao = Mockito.mock(PersonLocksDao.class);
    Field daoField = MergeMessageProcessor.class.getDeclaredField("personLocksDao");
    daoField.setAccessible(true);
    daoField.set(spy, dao);
    Mockito.when(dao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);
    Mockito.when(dao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);

    String result = (String) method.invoke(spy, req, "testKey");
    assertEquals(Constants.OK, result);
}
