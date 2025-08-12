@Test
    void testCheckKafka_TopicNamesNull_ShouldReturnFalse() throws Exception {
        // Arrange
        when(akafkaAdminClient.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        boolean result = healthCheck.checkKafka(akafkaAdminClient);

        // Assert
        assertFalse(result, "Expected false when topicNames is null");
    }

    @Test
    void testCheckKafka_TopicNamesNotNull_ShouldReturnTrue() throws Exception {
        // Arrange
        when(akafkaAdminClient.listTopics()).thenReturn(listTopicsResult);
        Set<String> topicNames = new HashSet<>();
        topicNames.add("test-topic");
        when(listTopicsResult.names()).thenReturn(CompletableFuture.completedFuture(topicNames));

        // Act
        boolean result = healthCheck.checkKafka(akafkaAdminClient);

        // Assert
        assertTrue(result, "Expected true when topicNames is not null");
    }
