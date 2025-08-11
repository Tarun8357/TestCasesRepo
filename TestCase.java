@Test
public void testHealthCheck_WhenIOExceptionOccurs_ShouldLogError() throws Exception {
    // Spy the HealthCheck so we can mock checkKafka and checkDB
    HealthCheck healthCheck = Mockito.spy(new HealthCheck());

    // Mock usageLog to avoid null pointer in if/else
    UsageLog mockUsageLog = mock(UsageLog.class);
    Field usageLogField = healthCheck.getClass().getDeclaredField("usageLog");
    usageLogField.setAccessible(true);
    usageLogField.set(healthCheck, mockUsageLog);

    // Set healthCheckFile to a directory (not a file) to cause IOException
    File tempDir = java.nio.file.Files.createTempDirectory("unwritableDir").toFile();
    Field fileField = healthCheck.getClass().getDeclaredField("healthCheckFile");
    fileField.setAccessible(true);
    fileField.set(healthCheck, tempDir.getAbsolutePath());

    // Mock checkKafka and checkDB so we enter the if branch first
    doReturn(true).when(healthCheck).checkKafka(any());
    doReturn(true).when(healthCheck).checkDB();

    // Mock ErrorLogEventHelper (static) using Mockito's mockStatic
    try (MockedStatic<ErrorLogEventHelper> mockedStatic = Mockito.mockStatic(ErrorLogEventHelper.class)) {
        healthCheck.health();

        mockedStatic.verify(() -> ErrorLogEventHelper.logErrorEvent(
                eq(HealthCheck.class.getName()),
                eq("Error while writing the Kafka health check output file"),
                eq("health()"),
                any(IOException.class),
                eq(""),
                eq(ErrorLogEvent.ERROR_SEVERITY)
        ));
    }
}
