@Test
void testRun_runtimeExceptionAfterPolling() {
    KafkaConsumer<String, String> mockConsumer = Mockito.mock(KafkaConsumer.class);
    ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");
    ConsumerRecords<String, String> records = new ConsumerRecords<>(
        Collections.singletonMap(new TopicPartition("topic", 0), List.of(record))
    );

    Mockito.when(mockConsumer.poll(Mockito.any())).thenReturn(records);

    KafkaConsumerThread thread = new KafkaConsumerThread(/* args */) {
        @Override
        protected void processRecord(ConsumerRecord<String, String> r) {
            throw new RuntimeException("After polling");
        }
    };

    ReflectionTestUtils.setField(thread, "kafkaConsumer", mockConsumer);
    ReflectionTestUtils.setField(thread, "topics", Arrays.asList("test-topic"));

    assertDoesNotThrow(thread::run);
}
