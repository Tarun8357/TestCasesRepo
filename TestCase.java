@Test
void testHealthCheck_CatchIOException() throws Exception {
    HealthCheck healthCheck = new HealthCheck();

    // Inject a mock UsageLog so that it's not null
    UsageLog mockUsageLog = mock(UsageLog.class);
    Field usageLogField = HealthCheck.class.getDeclaredField("usageLog");
    usageLogField.setAccessible(true);
    usageLogField.set(healthCheck, mockUsageLog);

    // Make healthCheckFile point to a path that will cause IOException (unwritable location)
    Field fileField = HealthCheck.class.getDeclaredField("healthCheckFile");
    fileField.setAccessible(true);

    // Remove 'final' modifier
    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(fileField, fileField.getModifiers() & ~Modifier.FINAL);

    fileField.set(healthCheck, "/root/unwritable_path.txt"); // should cause IOException

    try (MockedStatic<ErrorLogEventHelper> mockedStatic = mockStatic(ErrorLogEventHelper.class)) {

        ArgumentCaptor<IOException> exceptionCaptor = ArgumentCaptor.forClass(IOException.class);

        // Stub the static method to capture the exception argument
        mockedStatic.when(() -> ErrorLogEventHelper.logErrorEvent(
                anyString(),
                anyString(),
                anyString(),
                exceptionCaptor.capture(),
                anyString(),
                anyInt()
        )).thenReturn(null);

        // Execute the method — should go into catch
        healthCheck.health();

        // Assert: IOException was indeed captured
        IOException captured = exceptionCaptor.getValue();
        assertNotNull(captured, "Expected IOException to be passed to logErrorEvent");
        assertTrue(captured instanceof IOException);
    }
}
