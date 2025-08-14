@ExtendWith(MockitoExtension.class)
class MergeMessageProcessorTest {

    @InjectMocks
    MergeMessageProcessor mergeMessageProcessor;

    @Mock PersonLocksDao personLocksDao;
    @Mock PersonForgetKeyDao personForgetKeyDao;
    @Mock ValidationUtil validationUtil;
    @Mock DeleteMessageProcessor deleteMessageProcessor;

    PersonsRequestData request = new PersonsRequestData();
    PersonLockVO oldLock = new PersonLockVO();
    PersonLockVO newLock = new PersonLockVO();

    @BeforeEach
    void setup() {
        request.setUdpid("udp1");
        request.setNmlzclientid("nmlz1");
        request.setOldudpid("udp2");
        request.setOldnmlzclientid("nmlz2");

        oldLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-01 00:00:00"));
        newLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-02 00:00:00"));
    }

    @Test
    void testScenario4_newIsNewer() {
        when(personLocksDao.getPersonLock("udp1", "nmlz1")).thenReturn(newLock);
        when(personLocksDao.getPersonLock("udp2", "nmlz2")).thenReturn(oldLock);
        when(validationUtil.insertUsageCode(any(), any(), anyString())).thenReturn(1);
        when(deleteMessageProcessor.deleteRecords(any())).thenReturn(true);

        String result = mergeMessageProcessor.mergePerson(request, "key");

        assertEquals(Constants.OK, result);
    }

    @Test
    void testScenario6_oldIsNewerAndCheckValidStatusTrue() {
        oldLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-02 00:00:00"));
        newLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-01 00:00:00"));

        when(personLocksDao.getPersonLock("udp1", "nmlz1")).thenReturn(newLock);
        when(personLocksDao.getPersonLock("udp2", "nmlz2")).thenReturn(oldLock);
        doReturn(true).when(spy(mergeMessageProcessor)).checkValidStatus(any(), any(), any(), any());
        when(validationUtil.insertPersonLockUsageAccountMerge(any(), any(), any())).thenReturn(1);
        when(deleteMessageProcessor.deleteRecords(any())).thenReturn(true);

        String result = mergeMessageProcessor.mergePerson(request, "key");

        assertEquals(Constants.OK, result);
    }

    @Test
    void testScenario5_oldIsNewerAndCheckValidStatusFalse() {
        oldLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-02 00:00:00"));
        newLock.setRowChangeTimestamp(Timestamp.valueOf("2024-01-01 00:00:00"));

        when(personLocksDao.getPersonLock("udp1", "nmlz1")).thenReturn(newLock);
        when(personLocksDao.getPersonLock("udp2", "nmlz2")).thenReturn(oldLock);
        doReturn(false).when(spy(mergeMessageProcessor)).checkValidStatus(any(), any(), any(), any());
        when(validationUtil.insertUsageCode(any(), any(), anyString())).thenReturn(1);
        when(deleteMessageProcessor.deleteRecords(any())).thenReturn(true);

        String result = mergeMessageProcessor.mergePerson(request, "key");

        assertEquals(Constants.OK, result);
    }

    @Test
    void testScenario2_personLockNull() {
        when(personLocksDao.getPersonLock("udp1", "nmlz1")).thenReturn(null);
        when(personLocksDao.getPersonLock("udp2", "nmlz2")).thenReturn(oldLock);
        when(mergeMessageProcessor.updatePersonsData(any(), any(), anyString())).thenReturn(true);

        String result = mergeMessageProcessor.mergePerson(request, "key");

        assertEquals(Constants.OK, result);
    }

    @Test
    void testScenario3_oldLockNullNewExists() {
        when(personLocksDao.getPersonLock("udp1", "nmlz1")).thenReturn(newLock);
        when(personLocksDao.getPersonLock("udp2", "nmlz2")).thenReturn(null);

        String result = mergeMessageProcessor.mergePerson(request, "key");

        assertEquals(Constants.ALREADY_PROCESSED, result);
    }

    @Test
    void testScenario1_noLocksFound() {
        when(personLocksDao.getPersonLock(any(), any())).thenReturn(null);

        String result = mergeMessageProcessor.mergePerson(request, "key");

        assertTrue(result.contains("Lock does not exists for old and new"));
    }
}
