@Test
void testCheckValidStatus_NewRecordStatusMatch_ProdProfile() {
    MergeMessageProcessor processor = new MergeMessageProcessor();

    // Set test values
    ReflectionTestUtils.setField(processor, "activeProfile", "prod");
    ReflectionTestUtils.setField(processor, "mergeStatusSubject", "Test Subject");
    ReflectionTestUtils.setField(processor, "sendMailList", "test@example.com");

    // Mock the sendEmail dependency
    EmailSender mockEmailSender = Mockito.mock(EmailSender.class);
    ReflectionTestUtils.setField(processor, "sendEmail", mockEmailSender);

    // OLD is null, NEW record has matching status
    PersonForgotKeyVO oldVO = null;
    PersonForgotKeyVO newVO = new PersonForgotKeyVO();
    newVO.setForgotKeyStatusCode("ACTIVE");
    newVO.setForgotKeyWorkflow("NewWorkflow");
    newVO.setRowChangeTimestamp("2025-08-13 11:00:00");

    String[] statusArray = {"ACTIVE", "INACTIVE"};

    PersonsRequestData requestData = new PersonsRequestData();
    requestData.setOldudpid("oldId");
    requestData.setOldnmlzclientid("oldClient");
    requestData.setUdpid("newId");
    requestData.setNmlzclientid("newClient");

    // Act
    boolean result = processor.checkValidStatus(oldVO, newVO, statusArray, requestData);

    // Assert
    assertTrue(result);
    verify(mockEmailSender, times(1)).sendMessage(any(Mail.class));
}
