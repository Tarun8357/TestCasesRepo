@Test
public void testHealthCheck_IfCondition() throws Exception {
    // Spy the class so we can mock only specific methods
    HealthCheck healthCheck = Mockito.spy(new HealthCheck());

    // Inject mock UsageLog
    UsageLog mockUsageLog = mock(UsageLog.class);
    Field usageLogField = healthCheck.getClass().getDeclaredField("usageLog");
    usageLogField.setAccessible(true);
    usageLogField.set(healthCheck, mockUsageLog);

    // Create temp file for healthCheckFile
    Field fileField = healthCheck.getClass().getDeclaredField("healthCheckFile");
    fileField.setAccessible(true);
    String tempFilePath = File.createTempFile("health", ".txt").getAbsolutePath();
    fileField.set(healthCheck, tempFilePath);

    // Mock dependencies to force if-condition
    doReturn(true).when(healthCheck).checkKafka(any());
    doReturn(true).when(healthCheck).checkDB();

    // Act
    healthCheck.health();

    // Assert file content matches expected health status
    String fileContent = Files.readString(Paths.get(tempFilePath));
    Assertions.assertEquals(Constants.HEALTH_UP, fileContent);

    // Verify log message
    verify(mockUsageLog).logUsageEvent(
            eq("doHealthCheck()"),
            contains(Constants.HEALTH_UP)
    );
}
