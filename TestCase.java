@Test
public void testLogUsageEvent_WhenInfoEnabled() {
    Logger mockLogger = mock(Logger.class);
    when(mockLogger.isInfoEnabled()).thenReturn(true);

    UsageLog usageLogSpy = Mockito.spy(usageLog);

    // Stub buildLogMapFromMDC to return a predictable map
    Map<String, String> fakeMap = new HashMap<>();
    doReturn(fakeMap).when(usageLogSpy)
            .buildLogMapFromMDC(eq(mockLogger), any());

    usageLogSpy.logUsageEvent(mockLogger, "myMethod", "myMessage");

    // Verify buildLogMapFromMDC was called
    verify(usageLogSpy).buildLogMapFromMDC(eq(mockLogger), any());

    // Verify logEvent() was called with INFO severity
    verify(usageLogSpy).logEvent(eq(LogConstants.INFO_SEVERITY), eq(mockLogger), eq(fakeMap));

    // Check map modifications
    assertEquals("myMethod", fakeMap.get(LogAttributes.METHOD_ATTRIB));
    assertEquals("myMessage", fakeMap.get(LogAttributes.MESSAGE_ATTRIB));
}

@Test
public void testLogUsageEvent_WhenInfoDisabled() {
    Logger mockLogger = mock(Logger.class);
    when(mockLogger.isInfoEnabled()).thenReturn(false);

    UsageLog usageLogSpy = Mockito.spy(usageLog);

    usageLogSpy.logUsageEvent(mockLogger, "myMethod", "myMessage");

    // When info disabled, buildLogMapFromMDC and logEvent should never be called
    verify(usageLogSpy, never()).buildLogMapFromMDC(any(), any());
    verify(usageLogSpy, never()).logEvent(anyString(), any(), any());
}
