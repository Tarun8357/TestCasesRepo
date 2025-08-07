@Test
public void testProcessMessage_WhenListSizesMatch_ShouldCallMergePerson() {
    // Arrange
    MergeMessageProcessor processor = spy(new MergeMessageProcessor());

    Body mockBody = mock(Body.class);
    FullMerge mockFullMerge = mock(FullMerge.class);
    Source mockSource = mock(Source.class);
    Target mockTarget = mock(Target.class);
    Update mockUpdate = mock(Update.class);
    Delete mockDelete = mock(Delete.class);
    After mockAfter = mock(After.class);

    IdMapping mapping1 = new IdMapping("CLIENT_1");
    IdMapping mapping2 = new IdMapping("CLIENT_2");

    // Setup
    when(mockBody.getFullMerge()).thenReturn(mockFullMerge);
    when(mockFullMerge.getSource()).thenReturn(mockSource);
    when(mockSource.getDelete()).thenReturn(mockDelete);
    when(mockDelete.getGlobalPersonIdentifier()).thenReturn("OLD_ID");
    when(mockDelete.getIdMapping()).thenReturn(List.of(mapping1, mapping2));

    when(mockFullMerge.getTarget()).thenReturn(mockTarget);
    when(mockTarget.getUpdate()).thenReturn(mockUpdate);
    when(mockUpdate.getAfter()).thenReturn(mockAfter);
    when(mockAfter.getGlobalPersonIdentifier()).thenReturn("NEW_ID");
    when(mockAfter.getIdMapping()).thenReturn(List.of(
            new IdMapping("CLIENT_1_NEW"),
            new IdMapping("CLIENT_2_NEW")
    ));

    doReturn("merged").when(processor).mergePerson(any(), any());

    // Act
    String result = processor.processMessage(mockBody, "key123");

    // Assert
    assertEquals("merged", result);
    verify(processor, times(2)).mergePerson(any(), eq("key123")); // Called twice for 2 mappings
}



@Test
public void testProcessMessage_WhenListSizesDiffer_ShouldNotCallMergePerson() {
    // Arrange
    MergeMessageProcessor processor = spy(new MergeMessageProcessor());

    Body mockBody = mock(Body.class);
    FullMerge mockFullMerge = mock(FullMerge.class);
    Source mockSource = mock(Source.class);
    Target mockTarget = mock(Target.class);
    Update mockUpdate = mock(Update.class);
    Delete mockDelete = mock(Delete.class);
    After mockAfter = mock(After.class);

    IdMapping mapping1 = new IdMapping("CLIENT_1");

    // Setup
    when(mockBody.getFullMerge()).thenReturn(mockFullMerge);
    when(mockFullMerge.getSource()).thenReturn(mockSource);
    when(mockSource.getDelete()).thenReturn(mockDelete);
    when(mockDelete.getGlobalPersonIdentifier()).thenReturn("OLD_ID");
    when(mockDelete.getIdMapping()).thenReturn(List.of(mapping1));

    when(mockFullMerge.getTarget()).thenReturn(mockTarget);
    when(mockTarget.getUpdate()).thenReturn(mockUpdate);
    when(mockUpdate.getAfter()).thenReturn(mockAfter);
    when(mockAfter.getGlobalPersonIdentifier()).thenReturn("NEW_ID");
    when(mockAfter.getIdMapping()).thenReturn(List.of(
            new IdMapping("CLIENT_1_NEW"),
            new IdMapping("CLIENT_2_NEW")
    )); // Size mismatch: 1 vs 2

    // Act
    String result = processor.processMessage(mockBody, "key123");

    // Assert
    assertNull(result);
    verify(processor, never()).mergePerson(any(), any());
}



