@Test
void testProcessMessage_mismatchedSizes_shouldNotCallMergePerson() {
    Body mockBody = mock(Body.class);
    FullMerge mockFullMerge = mock(FullMerge.class);
    Delete mockDelete = mock(Delete.class);
    Update mockUpdate = mock(Update.class);
    After mockAfter = mock(After.class);

    // Prepare mocked response
    when(mockBody.getFullMerge()).thenReturn(mockFullMerge);
    when(mockFullMerge.getSource().getDelete()).thenReturn(mockDelete);
    when(mockFullMerge.getTarget().getUpdate().getAfter()).thenReturn(mockAfter);

    IdMapping id1 = new IdMapping("old1");
    IdMapping id2 = new IdMapping("old2");
    IdMapping id3 = new IdMapping("new1");

    when(mockDelete.getIdMapping()).thenReturn(List.of(id1, id2)); // size 2
    when(mockAfter.getIdMapping()).thenReturn(List.of(id3));       // size 1

    // Setup GPI mocks
    when(mockDelete.getGlobalPersonIdentifier()).thenReturn("oldGPI");
    when(mockAfter.getGlobalPersonIdentifier()).thenReturn("newGPI");

    // System under test
    MyService service = spy(new MyService());
    doReturn("messageX").when(service).mergePerson(any(), any());

    String result = service.processMessage(mockBody, "key123");

    // Assert
    assertNull(result); // loop doesn't run
    verify(service, never()).mergePerson(any(), any());
}
