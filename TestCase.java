@Test
void testCheckKafka_TopicNamesNull_ShouldReturnFalse() throws Exception {
    HealthCheck healthCheck = new HealthCheck();

    // Mock AdminClient and ListTopicsResult
    AdminClient mockAdminClient = mock(AdminClient.class);
    ListTopicsResult mockTopicsResult = mock(ListTopicsResult.class);
    KafkaFuture<Set<String>> mockFuture = mock(KafkaFuture.class);

    when(mockAdminClient.listTopics()).thenReturn(mockTopicsResult);
    when(mockTopicsResult.names()).thenReturn(mockFuture);
    when(mockFuture.get()).thenReturn(null);

    // Inject mock AdminClient into private field
    Field adminClientField = HealthCheck.class.getDeclaredField("kafkaAdminClient");
    adminClientField.setAccessible(true);
    adminClientField.set(healthCheck, mockAdminClient);

    boolean result = healthCheck.checkKafka(mockAdminClient);

    assertFalse(result);
}


@Test
void testCheckKafka_Exception_ShouldReturnFalse() throws Exception {
    HealthCheck healthCheck = new HealthCheck();

    // Mock AdminClient and ListTopicsResult
    AdminClient mockAdminClient = mock(AdminClient.class);
    ListTopicsResult mockTopicsResult = mock(ListTopicsResult.class);

    // Stub listTopics() to return mockTopicsResult
    when(mockAdminClient.listTopics()).thenReturn(mockTopicsResult);

    // Mock KafkaFuture for names()
    KafkaFuture<Set<String>> mockFuture = mock(KafkaFuture.class);
    when(mockTopicsResult.names()).thenReturn(mockFuture);

    // Stub .get() to throw exception
    when(mockFuture.get()).thenThrow(new RuntimeException("Kafka failure"));

    // Call method
    boolean result = healthCheck.checkKafka(mockAdminClient);

    // Assert
    assertFalse(result, "Expected false when exception occurs");
}
