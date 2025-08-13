@Test
void testCheckValidStatus_OldRecordActiveStatusProd() {
    MergeMessageProcessor processor = new MergeMessageProcessor();
    ReflectionTestUtils.setField(processor, "activeProfile", "prod");

    // Mock OLD record with active status
    PersonForgotKeyVO oldVO = mock(PersonForgotKeyVO.class);
    when(oldVO.getForgotKeyStatusCode()).thenReturn("ACTIVE");
    when(oldVO.getForgotKeyWorkflow()).thenReturn("WORKFLOW1");
    when(oldVO.getRowChangeTimestamp()).thenReturn("2025-08-13");

    // NEW record is null
    PersonForgotKeyVO newVO = null;

    // Status list contains "ACTIVE"
    String[] status = {"ACTIVE"};

    PersonsRequestData request = new PersonsRequestData();
    request.setOldudpid("oldU");
    request.setOldnmlzclientid("oldC");
    request.setUdpid("newU");
    request.setNmlzclientid("newC");

    boolean result = processor.checkValidStatus(oldVO, newVO, status, request);

    assertTrue(result);
}
