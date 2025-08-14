@Test
void testRun_catchThrowableBranch() {
    // Arrange
    KafkaConsumerThread thread = Mockito.spy(new KafkaConsumerThread(/* ctor args */));

    // Make kafkaConsumer mock so it won't NPE on close()
    KafkaConsumer<?, ?> mockConsumer = Mockito.mock(KafkaConsumer.class);
    ReflectionTestUtils.setField(thread, "kafkaConsumer", mockConsumer);

    // Force messageProcessor() to throw
    Mockito.doThrow(new RuntimeException("Test exception"))
           .when(thread).messageProcessor();

    // Act
    thread.run();

    // Assert
    Mockito.verify(mockConsumer).close();
    // Optionally verify log call if you have a log captor
}
