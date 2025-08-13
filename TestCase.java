@Test
void testDeletePerson_ActiveProfileProdBranch() {
    // Arrange
    DeleteMessageProcessor processor = new DeleteMessageProcessor();

    // Mocks for dependencies
    PersonLocksDao mockLocksDao = mock(PersonLocksDao.class);
    PersonForgetKeyDao mockForgetKeyDao = mock(PersonForgetKeyDao.class);
    MergeMessageProcessor mockMergeProcessor = mock(MergeMessageProcessor.class);

    // Inject mocks
    ReflectionTestUtils.setField(processor, "personLocksDao", mockLocksDao);
    ReflectionTestUtils.setField(processor, "personForgetKeyDao", mockForgetKeyDao);
    ReflectionTestUtils.setField(processor, "mergeMessageProcessor", mockMergeProcessor);
    ReflectionTestUtils.setField(processor, "activeProfile", "prod");

    // Mock data for request
    PersonsRequestData request = new PersonsRequestData();
    request.setUdpid("udp123");
    request.setNmlzclientid("client123");

    // Mock PersonLockVO
    PersonLockVO mockLock = mock(PersonLockVO.class);
    when(mockLock.getPersonLockId()).thenReturn("lock123");
    when(mockLock.getUdpGlobalPersonId()).thenReturn("udpGID");
    when(mockLock.getNormalizedClientId()).thenReturn("nmlzCID");
    when(mockLocksDao.getPersonLock("udp123", "client123")).thenReturn(mockLock);

    // Mock PersonForgotKeyVO
    PersonForgotKeyVO mockForgotKey = mock(PersonForgotKeyVO.class);
    when(mockForgotKey.getForgotKeyWorkflow()).thenReturn("workflow1");
    when(mockForgotKey.getForgotKeyStatusCode()).thenReturn("status1");
    when(mockForgotKey.getRowChangeTimestamp()).thenReturn("timestamp1");
    when(mockForgetKeyDao.getPersonForgotKeyRecord("lock123")).thenReturn(mockForgotKey);

    // Act
    String result = processor.deletePerson(request, "someKey");

    // Assert
    assertNotNull(result);
    verify(mockLocksDao).getPersonLock("udp123", "client123");
    verify(mockForgetKeyDao).getPersonForgotKeyRecord("lock123");
    verify(mockMergeProcessor).mailSent(anyString(), anyString(), anyString());
}
