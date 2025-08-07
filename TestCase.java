private void mockUpdateLockDetails(MergeMessageProcessor processor, boolean returnValue) throws Exception {
    Method method = MergeMessageProcessor.class.getDeclaredMethod("updateLockDetails", PersonLockVO.class, PersonLockVO.class);
    method.setAccessible(true);

    MergeMessageProcessor spyProcessor = Mockito.spy(processor);
    doReturn(returnValue).when(spyProcessor).updateLockDetails(any(), any());
    
    injectPrivateField(spyProcessor, "updateLockDetails", method);
}


@Test
void testMergePerson_Scenario5_AllConditionsTrue_ShouldReturnSuccess() throws Exception {
    MergeMessageProcessor processor = Mockito.spy(new MergeMessageProcessor());

    PersonsRequestData request = new PersonsRequestData();
    request.setUdpid("NEW_ID");
    request.setNmlzclientid("NEW_CLIENT");
    request.setOldudpid("OLD_ID");
    request.setOldnmlzclientid("OLD_CLIENT");

    PersonLockVO oldLock = mock(PersonLockVO.class);
    PersonLockVO newLock = mock(PersonLockVO.class);

    when(oldLock.getRowChangeTimestamp()).thenReturn(Timestamp.valueOf("2023-01-01 10:00:00")); // OLD is newer
    when(newLock.getRowChangeTimestamp()).thenReturn(Timestamp.valueOf("2022-01-01 10:00:00"));

    injectPrivateField(processor, "personLocksDao", mock(PersonLocksDao.class));
    injectPrivateField(processor, "validationUtil", mock(ValidationUtil.class));
    injectPrivateField(processor, "deleteMessageProcessor", mock(DeleteMessageProcessor.class));

    when(processor.personLocksDao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);
    when(processor.personLocksDao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);

    doReturn(1).when(processor).insertUsageCode(oldLock, Constants.UDP_MERGE, "KEY");
    doReturn(true).when(processor).updateLockDetails(oldLock, newLock);
    when(processor.deleteMessageProcessor.deleteRecords(oldLock)).thenReturn(true);

    String result = processor.mergePerson(request, "KEY");

    assertEquals(Constants.OK, result);
}



@Test
void testMergePerson_Scenario5_InsertCountZero_ShouldReturnFailure() throws Exception {
    MergeMessageProcessor processor = Mockito.spy(new MergeMessageProcessor());

    PersonsRequestData request = new PersonsRequestData();
    request.setUdpid("NEW_ID");
    request.setNmlzclientid("NEW_CLIENT");
    request.setOldudpid("OLD_ID");
    request.setOldnmlzclientid("OLD_CLIENT");

    PersonLockVO oldLock = mock(PersonLockVO.class);
    PersonLockVO newLock = mock(PersonLockVO.class);

    when(oldLock.getRowChangeTimestamp()).thenReturn(Timestamp.valueOf("2023-01-01 10:00:00")); // OLD is newer
    when(newLock.getRowChangeTimestamp()).thenReturn(Timestamp.valueOf("2022-01-01 10:00:00"));

    injectPrivateField(processor, "personLocksDao", mock(PersonLocksDao.class));
    injectPrivateField(processor, "validationUtil", mock(ValidationUtil.class));
    injectPrivateField(processor, "deleteMessageProcessor", mock(DeleteMessageProcessor.class));

    when(processor.personLocksDao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);
    when(processor.personLocksDao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);

    doReturn(0).when(processor).insertUsageCode(oldLock, Constants.UDP_MERGE, "KEY");

    String result = processor.mergePerson(request, "KEY");

    assertEquals(Constants.LOCK_NOT_EXISTS, result);
}


@Test
void testMergePerson_Scenario6_ValidStatusAndAllTrue_ShouldReturnSuccess() throws Exception {
    MergeMessageProcessor processor = Mockito.spy(new MergeMessageProcessor());

    PersonsRequestData request = new PersonsRequestData();
    request.setUdpid("NEW_ID");
    request.setNmlzclientid("NEW_CLIENT");
    request.setOldudpid("OLD_ID");
    request.setOldnmlzclientid("OLD_CLIENT");

    PersonLockVO oldLock = mock(PersonLockVO.class);
    PersonLockVO newLock = mock(PersonLockVO.class);

    when(oldLock.getRowChangeTimestamp()).thenReturn(Timestamp.valueOf("2023-01-01 10:00:00")); // OLD is newer
    when(newLock.getRowChangeTimestamp()).thenReturn(Timestamp.valueOf("2022-01-01 10:00:00"));

    PersonForgotKeyVO oldForgotKey = mock(PersonForgotKeyVO.class);
    PersonForgotKeyVO newForgotKey = mock(PersonForgotKeyVO.class);

    injectPrivateField(processor, "personLocksDao", mock(PersonLocksDao.class));
    injectPrivateField(processor, "validationUtil", mock(ValidationUtil.class));
    injectPrivateField(processor, "deleteMessageProcessor", mock(DeleteMessageProcessor.class));
    injectPrivateField(processor, "personForgetKeyDao", mock(PersonForgetKeyDao.class));

    when(processor.personLocksDao.getPersonLock("OLD_ID", "OLD_CLIENT")).thenReturn(oldLock);
    when(processor.personLocksDao.getPersonLock("NEW_ID", "NEW_CLIENT")).thenReturn(newLock);
    when(processor.personForgetKeyDao.getPersonForgotKeyRecord(oldLock.getPersonLockId())).thenReturn(oldForgotKey);
    when(processor.personForgetKeyDao.getPersonForgotKeyRecord(newLock.getPersonLockId())).thenReturn(newForgotKey);

    doReturn(true).when(processor).checkValidStatus(eq(oldForgotKey), eq(newForgotKey), any(), eq(request));
    when(processor.validationUtil.insertPersonLockUsageAccountMerge(any(), any(), eq(Constants.UDP_MERGE_OLD))).thenReturn(1);
    when(processor.validationUtil.insertPersonLockUsageAccountMerge(any(), any(), eq(Constants.UDP_MERGE_NEW))).thenReturn(1);
    doReturn(true).when(processor).updateLockDetails(any(), any());
    when(processor.deleteMessageProcessor.deleteRecords(oldLock)).thenReturn(true);

    String result = processor.mergePerson(request, "KEY");

    assertEquals(Constants.OK, result);
}
