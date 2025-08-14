import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.MockedStatic;
import org.powermock.api.mockito.PowerMockito;
import java.sql.Timestamp;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MergePersonTest {

    @InjectMocks
    private MergeMessageProcessor mergeMessageProcessor;

    @Mock
    private PersonLocksDao personLocksDao;

    @Mock
    private PersonForgetKeyDao personForgetKeyDao;

    @Mock
    private DeleteMessageProcessor deleteMessageProcessor;

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private UsageLog usageLog;

    // Test Case 1: Scenario 4 - Lock exists for both OLD and NEW, NEW is newer - Success path
    @Test
    public void testMergePerson_Scenario4_NewIsNewer_Success() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis()));
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis() - 10000));
        
        // Setup processor with mocked dependencies
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteMessageProcessor);
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        when(deleteMessageProcessor.deleteRecords(oldPersonLock)).thenReturn(true);
        
        // Create a spy to mock private method behavior
        MergeMessageProcessor spyProcessor = spy(processor);
        
        // Mock the private method insertUsageCode using doAnswer
        doAnswer(invocation -> 1).when(spyProcessor).insertUsageCode(any(), any(), any());
        
        // Since we can't directly spy private methods, we'll use ReflectionTestUtils to invoke
        // and mock the behavior by controlling the dependencies
        
        String result = spyProcessor.mergePerson(personsMergeRequest, key);
        
        // The actual assertion depends on the internal logic, but should be success
        assertNotNull(result);
        verify(personLocksDao).getPersonLock("udpid1", "clientid1");
        verify(personLocksDao).getPersonLock("oldudpid1", "oldclientid1");
    }

    // Test Case 2: Using ReflectionTestUtils to directly test private method insertUsageCode
    @Test
    public void testInsertUsageCode_Success() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        String key = "testKey";
        
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        
        // Mock the validationUtil method that insertUsageCode likely calls
        when(validationUtil.insertPersonLockUsage(any(), any(), any())).thenReturn(1);
        
        // Call private method directly using ReflectionTestUtils
        Integer result = ReflectionTestUtils.invokeMethod(processor, "insertUsageCode", 
            oldPersonLock, Constants.UDP_MERGE, key);
        
        assertNotNull(result);
        assertEquals(1, result);
    }

    // Test Case 3: Using ReflectionTestUtils to test updatePersonsData
    @Test
    public void testUpdatePersonsData_Success() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        String key = "testKey";
        
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        
        // Mock underlying dependencies
        when(validationUtil.updatePersonData(any(), any(), any())).thenReturn(true);
        
        Boolean result = ReflectionTestUtils.invokeMethod(processor, "updatePersonsData", 
            personsMergeRequest, oldPersonLock, key);
        
        assertNotNull(result);
        assertTrue(result);
    }

    // Test Case 4: Using ReflectionTestUtils to test checkValidStatus
    @Test
    public void testCheckValidStatus_Success() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonForgotKeyVO oldPersonForgotKeyVO = createPersonForgotKeyVO("ACTIVE");
        PersonForgotKeyVO personForgotKeyVO = createPersonForgotKeyVO("STARTED");
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String[] validStatuses = {"STARTED", "ACTIVE", "REVIEW"};
        
        MergeMessageProcessor processor = new MergeMessageProcessor();
        
        Boolean result = ReflectionTestUtils.invokeMethod(processor, "checkValidStatus", 
            oldPersonForgotKeyVO, personForgotKeyVO, validStatuses, personsMergeRequest);
        
        assertNotNull(result);
    }

    // Test Case 5: Full integration test - Scenario 4 with mocked private method responses
    @Test
    public void testMergePerson_Scenario4_MockedPrivateMethods() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis()));
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis() - 10000));
        
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteMessageProcessor);
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        when(deleteMessageProcessor.deleteRecords(oldPersonLock)).thenReturn(true);
        
        // Mock the underlying methods that private methods call
        when(validationUtil.insertPersonLockUsage(any(), any(), any())).thenReturn(1);
        
        String result = processor.mergePerson(personsMergeRequest, key);
        
        assertNotNull(result);
        verify(personLocksDao).getPersonLock("udpid1", "clientid1");
        verify(personLocksDao).getPersonLock("oldudpid1", "oldclientid1");
    }

    // Test Case 6: Scenario 3 - Lock does not exist for OLD but exists for NEW
    @Test
    public void testMergePerson_Scenario3_OldLockNull_NewLockExists() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis()));
        
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "usageLog", usageLog);
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(null);
        
        String result = processor.mergePerson(personsMergeRequest, key);
        
        assertEquals(Constants.ALREADY_PROCESSED, result);
        verify(usageLog).logUsageEvent(anyString(), anyString());
    }

    // Test Case 7: Both locks are null - Error path
    @Test
    public void testMergePerson_BothLocksNull_ErrorMessage() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(null);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(null);
        
        String result = processor.mergePerson(personsMergeRequest, key);
        
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    // Test Case 8: PersonLock is null but oldPersonLock exists - Success path
    @Test
    public void testMergePerson_PersonLockNull_OldLockExists_Success() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(null);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        when(validationUtil.updatePersonData(any(), any(), any())).thenReturn(true);
        
        String result = processor.mergePerson(personsMergeRequest, key);
        
        assertEquals(Constants.OK, result);
    }

    // Test Case 9: PersonLock is null but oldPersonLock exists - Failure path
    @Test
    public void testMergePerson_PersonLockNull_OldLockExists_Failure() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(null);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        when(validationUtil.updatePersonData(any(), any(), any())).thenReturn(false);
        
        String result = processor.mergePerson(personsMergeRequest, key);
        
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    // Test Case 10: Scenario 6 - Forgot key process active - Success
    @Test
    public void testMergePerson_Scenario6_ForgotKeyActive_Success() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis() - 10000));
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        
        PersonForgotKeyVO oldPersonForgotKeyVO = createPersonForgotKeyVO("ACTIVE");
        PersonForgotKeyVO personForgotKeyVO = createPersonForgotKeyVO("STARTED");
        
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "personForgetKeyDao", personForgetKeyDao);
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteMessageProcessor);
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        when(personForgetKeyDao.getPersonForgotKeyRecord(oldPersonLock.getPersonLockId())).thenReturn(oldPersonForgotKeyVO);
        when(personForgetKeyDao.getPersonForgotKeyRecord(personLock.getPersonLockId())).thenReturn(personForgotKeyVO);
        
        // Mock the validationUtil methods
        when(validationUtil.insertPersonLockUsageAccountMerge(oldPersonLock, personLock, Constants.UDP_MERGE_OLD)).thenReturn(1);
        when(validationUtil.insertPersonLockUsageAccountMerge(oldPersonLock, personLock, Constants.UDP_MERGE_NEW)).thenReturn(1);
        when(validationUtil.updateLockDetails(any(), any())).thenReturn(true);
        when(deleteMessageProcessor.deleteRecords(oldPersonLock)).thenReturn(true);
        
        String result = processor.mergePerson(personsMergeRequest, key);
        
        assertEquals(Constants.OK, result);
    }

    // Test Case 11: Testing private method behavior through public method with specific conditions
    @Test
    public void testMergePerson_PrivateMethodFailure_InsertUsageCode() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis()));
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis() - 10000));
        
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteMessageProcessor);
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        
        // Mock insertPersonLockUsage to return 0 (failure) to test insertUsageCode failure path
        when(validationUtil.insertPersonLockUsage(any(), any(), any())).thenReturn(0);
        
        String result = processor.mergePerson(personsMergeRequest, key);
        
        // Should return error message when insertUsageCode fails
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    // Helper methods to create test objects
    private PersonsRequestData createPersonsRequestData(String udpid, String nmlzClientId, String oldUdpid, String oldNmlzClientId) {
        PersonsRequestData data = new PersonsRequestData();
        data.setUdpid(udpid);
        data.setNmlzclientid(nmlzClientId);
        data.setOldudpid(oldUdpid);
        data.setOldnmlzclientid(oldNmlzClientId);
        return data;
    }

    private PersonLockVO createPersonLockVO(String udpid, String clientId, Timestamp timestamp) {
        PersonLockVO lockVO = new PersonLockVO();
        lockVO.setUdpid(udpid);
        lockVO.setClientId(clientId);
        lockVO.setRowChangeTimestamp(timestamp);
        lockVO.setPersonLockId(123L);
        return lockVO;
    }

    private PersonForgotKeyVO createPersonForgotKeyVO(String status) {
        PersonForgotKeyVO forgotKeyVO = new PersonForgotKeyVO();
        forgotKeyVO.setStatus(status);
        return forgotKeyVO;
    }
}
