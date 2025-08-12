@Test
public void testLogUsageLifecycle_WhenLoggingEnabled() {
    ReflectionTestUtils.setField(usageLog, "loggingEnabled", "true");
    ReflectionTestUtils.setField(usageLog, "activeProfile", "DEMO");

    try (MockedStatic<LogUtils> logUtilsMock = Mockito.mockStatic(LogUtils.class)) {

        usageLog.logUsageLifecycle("myMethod", "myMessage");

        logUtilsMock.verify(() ->
            LogUtils.setLogAttribute(LogAttributes.LIFECYCLE_ATTRIB, "]DEMO]"));
        logUtilsMock.verify(() ->
            LogUtils.setLogAttribute(LogAttributes.SERVICE_NAME_ATTRIB, Constants.ACCOUNTLOCK_LISTENER_PROCESS));
    }
}

@Test
public void testLogUsageLifecycle_WhenLoggingDisabled() {
    ReflectionTestUtils.setField(usageLog, "loggingEnabled", "false");
    ReflectionTestUtils.setField(usageLog, "activeProfile", "DEMO");

    try (MockedStatic<LogUtils> logUtilsMock = Mockito.mockStatic(LogUtils.class)) {

        usageLog.logUsageLifecycle("myMethod", "myMessage");

        logUtilsMock.verifyNoInteractions();
    }
}
