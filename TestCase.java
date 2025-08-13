@Test
void testCheckValidStatus_NewRecordStatusMatch_ProdProfile() throws Exception {
    MergeMessageProcessor processor = new MergeMessageProcessor();

    ReflectionTestUtils.setField(processor, "activeProfile", "prod");
    ReflectionTestUtils.setField(processor, "mergeStatusSubject", "Test Subject");
    ReflectionTestUtils.setField(processor, "sendMailList", "test@example.com");

    EmailSender mockEmailSender = Mockito.mock(EmailSender.class);
    ReflectionTestUtils.setField(processor, "sendEmail", mockEmailSender);

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

    // Call private method via Reflection
    Method method = MergeMessageProcessor.class
            .getDeclaredMethod("checkValidStatus", PersonForgotKeyVO.class, PersonForgotKeyVO.class, String[].class, PersonsRequestData.class);
    method.setAccessible(true);

    boolean result = (boolean) method.invoke(processor, oldVO, newVO, statusArray, requestData);

    assertTrue(result);
    verify(mockEmailSender, times(1)).sendMessage(any(Mail.class));
}
