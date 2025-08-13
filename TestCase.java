@SuppressWarnings("unchecked")
private boolean invokeCheckValidStatus(
        MergeMessageProcessor processor,
        PersonForgotKeyVO oldVO,
        PersonForgotKeyVO newVO,
        String[] status,
        PersonsRequestData request
) throws Exception {
    Method method = MergeMessageProcessor.class.getDeclaredMethod(
            "checkValidStatus",
            PersonForgotKeyVO.class,
            PersonForgotKeyVO.class,
            String[].class,
            PersonsRequestData.class
    );
    method.setAccessible(true);
    return (boolean) method.invoke(processor, oldVO, newVO, status, request);
}


@Test
void testCheckValidStatus_OldRecordActiveStatusProd() throws Exception {
    MergeMessageProcessor processor = new MergeMessageProcessor();
    ReflectionTestUtils.setField(processor, "activeProfile", "prod");

    PersonForgotKeyVO oldVO = mock(PersonForgotKeyVO.class);
    when(oldVO.getForgotKeyStatusCode()).thenReturn("ACTIVE");
    when(oldVO.getForgotKeyWorkflow()).thenReturn("WF1");
    when(oldVO.getRowChangeTimestamp()).thenReturn("2025-08-13");

    PersonsRequestData request = new PersonsRequestData();
    request.setOldudpid("oldU");
    request.setOldnmlzclientid("oldC");
    request.setUdpid("newU");
    request.setNmlzclientid("newC");

    boolean result = invokeCheckValidStatus(processor, oldVO, null, new String[]{"ACTIVE"}, request);
    assertTrue(result);
}
