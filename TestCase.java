@Test
void testCheckValidStatus_OldRecordStatusMatch_ProdProfile() {
    // Arrange
    MergeMessageProcessor processor = new MergeMessageProcessor();

    // Set required private fields so no NPE in mailSent()
    ReflectionTestUtils.setField(processor, "activeProfile", "prod");
    ReflectionTestUtils.setField(processor, "mergeStatusSubject", "Test Subject");
    ReflectionTestUtils.setField(processor, "sendMailList", "test@example.com");

    // Mock dependencies to avoid actual side effects
    MergeMessageProcessor spyProcessor = Mockito.spy(processor);
    doNothing().when(spyProcessor).mailSent(anyString(), anyString(), anyString());

    // Create oldPersonForgotKeyVO with matching status code
    PersonForgotKeyVO oldVO = new PersonForgotKeyVO();
    oldVO.setForgotKeyStatusCode("ACTIVE");
    oldVO.setForgotKeyWorkflow("TestWorkflow");
    oldVO.setRowChangeTimestamp("2025-08-13 10:00:00");

    // No new person record
    PersonForgotKeyVO newVO = null;

    // Status array that contains a match for oldVO
    String[] statusArray = {"ACTIVE", "INACTIVE"};

    // Request object
    PersonsRequestData requestData = new PersonsRequestData();
    requestData.setOldudpid("oldId");
    requestData.setOldnmlzclientid("oldClient");
    requestData.setUdpid("newId");
    requestData.setNmlzclientid("newClient");

    // Act
    boolean result = spyProcessor.checkValidStatus(oldVO, newVO, statusArray, requestData);

    // Assert
    assertTrue(result, "Expected checkValidStatus to return true when status matches for old record");
    verify(spyProcessor, times(1))
            .mailSent(anyString(), eq("Test Subject"), eq("test@example.com"));
}
