@Test
void testCheckKafka_TopicNamesNull_ShouldReturnFalse() throws Exception {
    HealthCheck healthCheck = new HealthCheck();

    // Mock AdminClient and ListTopicsResult
    AdminClient mockAdminClient = mock(AdminClient.class);
    ListTopicsResult mockTopicsResult = mock(ListTopicsResult.class);

    // Stub listTopics() to return mockTopicsResult
    when(mockAdminClient.listTopics()).thenReturn(mockTopicsResult);

    // Mock KafkaFuture for names()
    KafkaFuture<Set<String>> mockFuture = mock(KafkaFuture.class);
    when(mockTopicsResult.names()).thenReturn(mockFuture);

    // Stub .get() to return null
    when(mockFuture.get()).thenReturn(null);

    // Call method
    boolean result = healthCheck.checkKafka(mockAdminClient);

    // Assert
    assertFalse(result, "Expected false when topicNames is null");
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
