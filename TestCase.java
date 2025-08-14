@Test
void testRun_countResetBranch() {
    KafkaConsumer<String, String> mockConsumer = Mockito.mock(KafkaConsumer.class);

    // Track how many times poll() is called
    AtomicInteger pollCount = new AtomicInteger(0);
    Mockito.doNothing().when(mockConsumer).subscribe(Mockito.any());
    Mockito.when(mockConsumer.assignment()).thenReturn(Collections.emptySet());
    Mockito.when(mockConsumer.poll(Mockito.any())).thenAnswer(inv -> {
        if (pollCount.incrementAndGet() >= 101) {
            throw new WakeupException(); // exit after hitting count >= 100
        }
        return new ConsumerRecords<>(Collections.emptyMap());
    });

    KafkaConsumerThread thread = new KafkaConsumerThread(/* args */);
    ReflectionTestUtils.setField(thread, "kafkaConsumer", mockConsumer);
    ReflectionTestUtils.setField(thread, "topics", Arrays.asList("test-topic"));

    assertDoesNotThrow(thread::run);
}


yiuk
