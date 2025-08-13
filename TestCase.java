@Test
void testCheckValidStatus_anyMatchTrue() {
    // Arrange
    MergeMessageProcessor processor = new MergeMessageProcessor();

    // Inject required dependencies or fields
    ReflectionTestUtils.setField(processor, "activeProfile", "prod");
    ReflectionTestUtils.setField(processor, "mailList", "test@example.com"); // avoid NPE in mailSent()

    PersonForgotKeyVO oldPerson = new PersonForgotKeyVO();
    oldPerson.setForgotKeyStatusCode("ACTIVE"); // value to match

    PersonsRequestData requestData = new PersonsRequestData();
    requestData.setOldudpid("oldUdpid");
    requestData.setOldnmlzclientid("oldClientId");
    requestData.setUdpid("newUdpid");
    requestData.setNmlzclientid("newClientId");

    // status array that will match the oldPerson's status
    String[] statusArray = {"active", "inactive"}; // 'active' matches 'ACTIVE' ignoring case

    // Spy to avoid sending real mail
    MergeMessageProcessor spyProcessor = Mockito.spy(processor);
    doNothing().when(spyProcessor)
               .mailSent(anyString(), anyString(), anyString(), anyString(), anyString());

    // Act
    boolean result = ReflectionTestUtils.invokeMethod(
            spyProcessor,
            "checkValidStatus",
            oldPerson,
            null,               // personForgotKeyVO = null
            statusArray,
            requestData
    );

    // Assert
    assertTrue(result, "Expected isValid to be true when status matches");
}
