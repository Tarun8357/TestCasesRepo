@Test
void testCheckKafka_TopicNamesNull_ShouldReturnFalse() throws Exception {
    // Arrange
    when(akafkaAdminClient.listTopics()).thenReturn(listTopicsResult);
    CompletableFuture<Set<String>> future = CompletableFuture.completedFuture(null);
    when(listTopicsResult.names()).thenReturn(future);

    // Act
    boolean result = healthCheck.checkKafka(akafkaAdminClient);

    // Assert
    assertFalse(result);
}

@Test
void testCheckKafka_TopicNamesNotNull_ShouldReturnTrue() throws Exception {
    // Arrange
    when(akafkaAdminClient.listTopics()).thenReturn(listTopicsResult);
    Set<String> topicNames = new HashSet<>();
    topicNames.add("test-topic");
    CompletableFuture<Set<String>> future = CompletableFuture.completedFuture(topicNames);
    when(listTopicsResult.names()).thenReturn(future);

    // Act
    boolean result = healthCheck.checkKafka(akafkaAdminClient);

    // Assert
    assertTrue(result);
}
