@Test
    void testCheckKafka_ShouldReturnFalse_WhenExceptionThrown() throws Exception {
        // Arrange
        when(akafkaAdminClient.listTopics())
                .thenThrow(new RuntimeException("Kafka not reachable"));

        // Act
        boolean result = healthCheck.checkKafka(akafkaAdminClient);

        // Assert
        assertFalse(result, "Expected checkKafka to return false when Kafka throws exception");
    }
