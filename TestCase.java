@Test
void testHealth_UpCondition() throws Exception {
    // Arrange
    HealthCheck healthCheck = Mockito.spy(new HealthCheck());
    healthCheck.usageLog = mock(UsageLog.class);

    // Make sure file is writeable to avoid IOException in this case
    Field fileField = HealthCheck.class.getDeclaredField("healthCheckFile");
    fileField.setAccessible(true);
    fileField.set(healthCheck, "test-health-file.txt");

    doReturn(true).when(healthCheck).checkKafka(any());
    doReturn(true).when(healthCheck).checkDB();

    // Act
    healthCheck.health();

    // Assert
    verify(healthCheck).checkKafka(any());
    verify(healthCheck).checkDB();
    verify(healthCheck.usageLog).logUsageEvent(
        eq("doHealthCheck()"),
        contains(Constants.HEALTH_UP)
    );
}
