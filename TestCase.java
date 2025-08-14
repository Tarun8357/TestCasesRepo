@Test
void testRun_forLoopCoverage() {
    // Mock KafkaConsumer
    KafkaConsumer<String, String> mockConsumer = Mockito.mock(KafkaConsumer.class);

    // Prepare test data
    Set<TopicPartition> partitions = new HashSet<>();
    TopicPartition tp = new TopicPartition("test-topic", 0);
    partitions.add(tp);

    OffsetAndMetadata committed = new OffsetAndMetadata(10L);

    // Stubbing behavior
    Mockito.when(mockConsumer.assignment()).thenReturn(partitions);
    Mockito.when(mockConsumer.committed(tp)).thenReturn(committed);
    Mockito.when(mockConsumer.position(tp)).thenReturn(20L);

    // Also mock methods before the loop
    Mockito.doNothing().when(mockConsumer).subscribe(Mockito.any());
    Mockito.when(mockConsumer.poll(Mockito.any())).thenReturn(null);

    // Create thread and inject consumer
    KafkaConsumerThread thread = new KafkaConsumerThread(/* your args here */);
    ReflectionTestUtils.setField(thread, "kafkaConsumer", mockConsumer);
    ReflectionTestUtils.setField(thread, "topics", Arrays.asList("test-topic"));

    // Run method
    thread.run();

    // Verify that loop interacted with mocked consumer
    Mockito.verify(mockConsumer).assignment();
    Mockito.verify(mockConsumer).committed(tp);
    Mockito.verify(mockConsumer).position(tp);
}
