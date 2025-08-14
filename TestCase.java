@Test
void testRun_forLoopCoverage() {
    KafkaConsumer<String, String> mockConsumer = Mockito.mock(KafkaConsumer.class);

    // Topic partitions
    Set<TopicPartition> partitions = new HashSet<>();
    TopicPartition tp = new TopicPartition("test-topic", 0);
    partitions.add(tp);

    OffsetAndMetadata committed = new OffsetAndMetadata(10L);

    // Stub subscribe
    Mockito.doNothing().when(mockConsumer).subscribe(Mockito.any());

    // Stub poll to return empty ConsumerRecords (needed to reach assignment())
    Mockito.when(mockConsumer.poll(Mockito.any()))
           .thenReturn(new ConsumerRecords<>(Collections.emptyMap()));

    // Stub assignment
    Mockito.when(mockConsumer.assignment()).thenReturn(partitions);

    // Stub committed and position
    Mockito.when(mockConsumer.committed(tp)).thenReturn(committed);
    Mockito.when(mockConsumer.position(tp)).thenReturn(20L);

    // Create KafkaConsumerThread and inject mock
    KafkaConsumerThread thread = new KafkaConsumerThread(/* your args here */);
    ReflectionTestUtils.setField(thread, "kafkaConsumer", mockConsumer);
    ReflectionTestUtils.setField(thread, "topics", Arrays.asList("test-topic"));

    // Run method
    thread.run();

    // Verify
    Mockito.verify(mockConsumer).assignment();
    Mockito.verify(mockConsumer).committed(tp);
    Mockito.verify(mockConsumer).position(tp);
}
