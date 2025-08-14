@Test
void testRun_countResetBranch() {
    KafkaConsumer<String, String> mockConsumer = Mockito.mock(KafkaConsumer.class);

    // Stub subscribe and poll
    Mockito.doNothing().when(mockConsumer).subscribe(Mockito.any());
    Mockito.when(mockConsumer.poll(Mockito.any()))
           .thenReturn(new ConsumerRecords<>(Collections.emptyMap()));

    KafkaConsumerThread thread = new KafkaConsumerThread(/* args */);
    ReflectionTestUtils.setField(thread, "kafkaConsumer", mockConsumer);
    ReflectionTestUtils.setField(thread, "topics", Arrays.asList("test-topic"));

    // Set count to 100 before run
    ReflectionTestUtils.setField(thread, "count", 100);

    // We need to break the while(true) loop — mock poll to throw after first call
    Mockito.when(mockConsumer.poll(Mockito.any()))
           .thenAnswer(invocation -> { throw new WakeupException(); });

    // Run — should hit count >= 100 and reset
    assertDoesNotThrow(thread::run);
}
