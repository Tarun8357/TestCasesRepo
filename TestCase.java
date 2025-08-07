@Test
void testMergePerson_Scenario4_InsertCountZero_ShouldReturnNotSuccess() {
    // Arrange
    MergeMessageProcessor processor = new MergeMessageProcessor();

    PersonsRequestData request = new PersonsRequestData();
    request.setUdpid("NEW_ID");
    request.setNmlzclientid("NEW_CLIENT_ID");
    request.setOldudpid("OLD_ID");
    request.setOldnmlzclientid("OLD_CLIENT_ID");

    PersonLockVO oldPersonLock = mock(PersonLockVO.class);
    PersonLockVO newPersonLock = mock(PersonLockVO.class);

    when(oldPersonLock.getRowChangeTimestamp()).thenReturn(Timestamp.valueOf("2022-01-01 10:00:00"));
    when(newPersonLock.getRowChangeTimestamp()).thenReturn(Timestamp.valueOf("2023-01-01 10:00:00"));

    PersonLocksDao locksDao = mock(PersonLocksDao.class);
    DeleteMessageProcessor deleteProcessor = mock(DeleteMessageProcessor.class);
    ValidationUtil validationUtil = mock(ValidationUtil.class);

    when(validationUtil.insertPersonLockUsage(oldPersonLock, Constants.UDP_MERGE, "mergeKey")).thenReturn(0); // insertCount = 0

    // Inject dependencies
    injectPrivateField(processor, "personLocksDao", locksDao);
    injectPrivateField(processor, "deleteMessageProcessor", deleteProcessor);
    injectPrivateField(processor, "validationUtil", validationUtil);

    when(locksDao.getPersonLock("NEW_ID", "NEW_CLIENT_ID")).thenReturn(newPersonLock);
    when(locksDao.getPersonLock("OLD_ID", "OLD_CLIENT_ID")).thenReturn(oldPersonLock);

    // Act
    String result = processor.mergePerson(request, "mergeKey");

    // Assert
    assertEquals(Constants.LOCK_NOT_EXISTS, result); // Expected fallback response
}
