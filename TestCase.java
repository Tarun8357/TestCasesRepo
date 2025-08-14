@Test
void testRun_runtimeExceptionAfterFetch() {
    KafkaConsumer<String, String> mockConsumer = Mockito.mock(KafkaConsumer.class);

    Mockito.when(mockConsumer.poll(Mockito.any()))
           .thenThrow(new RuntimeException("Test after fetch"));

    KafkaConsumerThread thread = new KafkaConsumerThread(/* args */);
    ReflectionTestUtils.setField(thread, "kafkaConsumer", mockConsumer);
    ReflectionTestUtils.setField(thread, "topics", Arrays.asList("test-topic"));

    assertDoesNotThrow(thread::run);
}
