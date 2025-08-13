@Test
void testDeletePerson_ActiveProfileNotProdBranch() {
    // Arrange
    DeleteMessageProcessor processor = new DeleteMessageProcessor();

    // Mock dependencies
    PersonLocksDao mockLocksDao = mock(PersonLocksDao.class);
    PersonForgetKeyDao mockForgetKeyDao = mock(PersonForgetKeyDao.class);
    MergeMessageProcessor mockMergeProcessor = mock(MergeMessageProcessor.class);

    // Inject mocks
    ReflectionTestUtils.setField(processor, "personLocksDao", mockLocksDao);
    ReflectionTestUtils.setField(processor, "personForgetKeyDao", mockForgetKeyDao);
    ReflectionTestUtils.setField(processor, "mergeMessageProcessor", mockMergeProcessor);
    ReflectionTestUtils.setField(processor, "activeProfile", "dev"); // NOT prod

    // Mock request
    PersonsRequestData request = new PersonsRequestData();
    request.setUdpid("udp123");
    request.setNmlzclientid("client123");

    // Mock PersonLockVO
    PersonLockVO mockLock = mock(PersonLockVO.class);
    when(mockLock.getPersonLockId()).thenReturn("lock123");
    when(mockLocksDao.getPersonLock("udp123", "client123")).thenReturn(mockLock);

    // Act
    String result = processor.deletePerson(request, "someKey");

    // Assert
    assertNotNull(result);
    verify(mockLocksDao).getPersonLock("udp123", "client123");
    // Ensure we did NOT call the prod-specific logic
    verify(mockForgetKeyDao, never()).getPersonForgotKeyRecord(anyString());
    verify(mockMergeProcessor, never()).mailSent(anyString(), anyString(), anyString());
}
