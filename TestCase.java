@Test
void testCheckValidStatus_OldRecordStatusMatch_ProdProfile() throws Exception {
    // Arrange
    MergeMessageProcessor processor = new MergeMessageProcessor();

    // Set required private fields so no NPE in mailSent()
    ReflectionTestUtils.setField(processor, "activeProfile", "prod");
    ReflectionTestUtils.setField(processor, "mergeStatusSubject", "Test Subject");
    ReflectionTestUtils.setField(processor, "sendMailList", "test@example.com");

    // Create oldPersonForgotKeyVO with matching status code
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

    // Access the private method via reflection
    Method method = MergeMessageProcessor.class.getDeclaredMethod(
            "checkValidStatus",
            PersonForgotKeyVO.class,
            PersonForgotKeyVO.class,
            String[].class,
            PersonsRequestData.class
    );
    method.setAccessible(true);

    // Act
    boolean result = (boolean) method.invoke(processor, oldVO, newVO, statusArray, requestData);

    // Assert
    assertTrue(result, "Expected checkValidStatus to return true when status matches for old record");
}
