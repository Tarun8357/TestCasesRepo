@Test
void testCheckValidStatus_OldRecordStatusMatch_ProdProfile() {
    MergeMessageProcessor processor = new MergeMessageProcessor();

    // Set fields so no NPE for mail list and subject
    ReflectionTestUtils.setField(processor, "activeProfile", "prod");
    ReflectionTestUtils.setField(processor, "mergeStatusSubject", "Test Subject");
    ReflectionTestUtils.setField(processor, "sendMailList", "test@example.com");

    // Mock the sendEmail dependency
    EmailSender mockEmailSender = Mockito.mock(EmailSender.class);
    ReflectionTestUtils.setField(processor, "sendEmail", mockEmailSender);

    // Old record with matching status
    PersonForgotKeyVO oldVO = new PersonForgotKeyVO();
    oldVO.setForgotKeyStatusCode("ACTIVE");
    oldVO.setForgotKeyWorkflow("TestWorkflow");
    oldVO.setRowChangeTimestamp("2025-08-13 10:00:00");

    PersonForgotKeyVO newVO = null;
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
