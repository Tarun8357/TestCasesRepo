@Test
    void testCheckKafka_TopicNamesNull_ShouldReturnFalse() throws Exception {
        ListTopicsResult mockListTopics = mock(ListTopicsResult.class);
        KafkaFuture<Set<String>> mockNamesFuture = mock(KafkaFuture.class);

        when(kafkaAdminClient.listTopics()).thenReturn(mockListTopics);
        when(mockListTopics.names()).thenReturn(mockNamesFuture);
        when(mockNamesFuture.get()).thenReturn(null); // triggers topicNames == null path

        boolean result = kafkaHealthCheck.checkKafka(kafkaAdminClient);

        assertFalse(result);
    }

    @Test
    void testCheckKafka_ExceptionThrown_ShouldReturnFalse() throws Exception {
        when(kafkaAdminClient.listTopics()).thenThrow(new RuntimeException("Kafka error"));

        boolean result = kafkaHealthCheck.checkKafka(kafkaAdminClient);

        assertFalse(result);
    }
}
