import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;
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
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        
        // Prepare test data
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis()));
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis() - 10000)); // Older timestamp
        
        // Mock DAO calls
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        
        // Mock private method insertUsageCode using ReflectionTestUtils
        MergeMessageProcessor spyProcessor = spy(mergeMessageProcessor);
        doReturn(1).when(spyProcessor).insertUsageCode(oldPersonLock, Constants.UDP_MERGE, key);
        
        // Use ReflectionTestUtils to inject the spy if needed
        ReflectionTestUtils.setField(this, "mergeMessageProcessor", spyProcessor);
        
        // Mock deleteRecords to return true
        when(deleteMessageProcessor.deleteRecords(oldPersonLock)).thenReturn(true);
        
        // Execute the method using the spy
        String result = spyProcessor.mergePerson(personsMergeRequest, key);
        
        // Verify result
        assertEquals(Constants.OK, result);
        
        // Verify interactions
        verify(personLocksDao).getPersonLock("udpid1", "clientid1");
        verify(personLocksDao).getPersonLock("oldudpid1", "oldclientid1");
        verify(deleteMessageProcessor).deleteRecords(oldPersonLock);
    }

    // Test Case 2: Scenario 4 - Lock exists for both OLD and NEW, NEW is newer - Failure path
    @Test
    public void testMergePerson_Scenario4_NewIsNewer_Failure() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis()));
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis() - 10000));
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        
        // Mock insertUsageCode to return failure using spy and ReflectionTestUtils
        MergeMessageProcessor spyProcessor = spy(mergeMessageProcessor);
        doReturn(0).when(spyProcessor).insertUsageCode(oldPersonLock, Constants.UDP_MERGE, key);
        
        String result = spyProcessor.mergePerson(personsMergeRequest, key);
        
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    // Test Case 3: Scenario 6 - Forgot key process active - Success path
    @Test
    public void testMergePerson_Scenario6_ForgotKeyActive_Success() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis() - 10000)); // Older
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis())); // Newer
        
        PersonForgotKeyVO oldPersonForgotKeyVO = createPersonForgotKeyVO("ACTIVE");
        PersonForgotKeyVO personForgotKeyVO = createPersonForgotKeyVO("STARTED");
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        when(personForgetKeyDao.getPersonForgotKeyRecord(oldPersonLock.getPersonLockId())).thenReturn(oldPersonForgotKeyVO);
        when(personForgetKeyDao.getPersonForgotKeyRecord(personLock.getPersonLockId())).thenReturn(personForgotKeyVO);
        
        // Create spy and mock private methods using ReflectionTestUtils approach
        MergeMessageProcessor spyProcessor = spy(mergeMessageProcessor);
        
        // Mock checkValidStatus method using spy
        doReturn(true).when(spyProcessor).checkValidStatus(
            eq(oldPersonForgotKeyVO), 
            eq(personForgotKeyVO), 
            any(String[].class), 
            eq(personsMergeRequest)
        );
        
        // Mock updateLockDetails method using spy
        doReturn(true).when(spyProcessor).updateLockDetails(oldPersonLock, personLock);
        
        when(deleteMessageProcessor.deleteRecords(oldPersonLock)).thenReturn(true);
        
        String result = spyProcessor.mergePerson(personsMergeRequest, key);
        
        assertEquals(Constants.OK, result);
    }

    // Test Case 4: Scenario 6 - Forgot key process active - Failure path
    @Test
    public void testMergePerson_Scenario6_ForgotKeyActive_Failure() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis() - 10000));
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        
        PersonForgotKeyVO oldPersonForgotKeyVO = createPersonForgotKeyVO("ACTIVE");
        PersonForgotKeyVO personForgotKeyVO = createPersonForgotKeyVO("STARTED");
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        when(personForgetKeyDao.getPersonForgotKeyRecord(oldPersonLock.getPersonLockId())).thenReturn(oldPersonForgotKeyVO);
        when(personForgetKeyDao.getPersonForgotKeyRecord(personLock.getPersonLockId())).thenReturn(personForgotKeyVO);
        
        // Create spy and mock private methods
        MergeMessageProcessor spyProcessor = spy(mergeMessageProcessor);
        
        doReturn(true).when(spyProcessor).checkValidStatus(
            eq(oldPersonForgotKeyVO), 
            eq(personForgotKeyVO), 
            any(String[].class), 
            eq(personsMergeRequest)
        );
        
        // Mock failure scenario - insertCountOld fails
        when(validationUtil.insertPersonLockUsageAccountMerge(oldPersonLock, personLock, Constants.UDP_MERGE_OLD)).thenReturn(0);
        when(validationUtil.insertPersonLockUsageAccountMerge(oldPersonLock, personLock, Constants.UDP_MERGE_NEW)).thenReturn(1);
        
        String result = spyProcessor.mergePerson(personsMergeRequest, key);
        
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    // Test Case 5: Scenario 5 - OLD is newer, no forgot key process - Success
    @Test
    public void testMergePerson_Scenario5_OldIsNewer_Success() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis() - 10000));
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        
        PersonForgotKeyVO oldPersonForgotKeyVO = createPersonForgotKeyVO("INACTIVE");
        PersonForgotKeyVO personForgotKeyVO = createPersonForgotKeyVO("INACTIVE");
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        when(personForgetKeyDao.getPersonForgotKeyRecord(oldPersonLock.getPersonLockId())).thenReturn(oldPersonForgotKeyVO);
        when(personForgetKeyDao.getPersonForgotKeyRecord(personLock.getPersonLockId())).thenReturn(personForgotKeyVO);
        
        // Create spy and mock private methods using ReflectionTestUtils approach
        MergeMessageProcessor spyProcessor = spy(mergeMessageProcessor);
        
        doReturn(false).when(spyProcessor).checkValidStatus(
            eq(oldPersonForgotKeyVO), 
            eq(personForgotKeyVO), 
            any(String[].class), 
            eq(personsMergeRequest)
        );
        
        doReturn(1).when(spyProcessor).insertUsageCode(oldPersonLock, Constants.UDP_MERGE, key);
        doReturn(true).when(spyProcessor).updateLockDetails(oldPersonLock, personLock);
        
        when(deleteMessageProcessor.deleteRecords(oldPersonLock)).thenReturn(true);
        
        String result = spyProcessor.mergePerson(personsMergeRequest, key);
        
        assertEquals(Constants.OK, result);
    }

    // Test Case 6: Scenario 5 - OLD is newer, no forgot key process - Failure
    @Test
    public void testMergePerson_Scenario5_OldIsNewer_Failure() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis() - 10000));
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        
        PersonForgotKeyVO oldPersonForgotKeyVO = createPersonForgotKeyVO("INACTIVE");
        PersonForgotKeyVO personForgotKeyVO = createPersonForgotKeyVO("INACTIVE");
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        when(personForgetKeyDao.getPersonForgotKeyRecord(oldPersonLock.getPersonLockId())).thenReturn(oldPersonForgotKeyVO);
        when(personForgetKeyDao.getPersonForgotKeyRecord(personLock.getPersonLockId())).thenReturn(personForgotKeyVO);
        
        // Create spy and mock private methods
        MergeMessageProcessor spyProcessor = spy(mergeMessageProcessor);
        
        doReturn(false).when(spyProcessor).checkValidStatus(
            eq(oldPersonForgotKeyVO), 
            eq(personForgotKeyVO), 
            any(String[].class), 
            eq(personsMergeRequest)
        );
        
        doReturn(0).when(spyProcessor).insertUsageCode(oldPersonLock, Constants.UDP_MERGE, key); // Failure
        
        String result = spyProcessor.mergePerson(personsMergeRequest, key);
        
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    // Test Case 7: Scenario 2 - personLock is null, oldPersonLock exists - Success
    @Test
    public void testMergePerson_Scenario2_PersonLockNull_Success() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(null);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        
        // Create spy and mock private method updatePersonsData
        MergeMessageProcessor spyProcessor = spy(mergeMessageProcessor);
        doReturn(true).when(spyProcessor).updatePersonsData(personsMergeRequest, oldPersonLock, key);
        
        String result = spyProcessor.mergePerson(personsMergeRequest, key);
        
        assertEquals(Constants.OK, result);
    }

    // Test Case 8: Scenario 2 - personLock is null, oldPersonLock exists - Failure
    @Test
    public void testMergePerson_Scenario2_PersonLockNull_Failure() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(null);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(oldPersonLock);
        
        // Create spy and mock private method updatePersonsData to return false
        MergeMessageProcessor spyProcessor = spy(mergeMessageProcessor);
        doReturn(false).when(spyProcessor).updatePersonsData(personsMergeRequest, oldPersonLock, key);
        
        String result = spyProcessor.mergePerson(personsMergeRequest, key);
        
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    // Test Case 9: Scenario 3 - Lock does not exist for OLD but exists for NEW
    @Test
    public void testMergePerson_Scenario3_OldLockNull_NewLockExists() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        PersonLockVO personLock = createPersonLockVO("udpid1", "clientid1", new Timestamp(System.currentTimeMillis()));
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(personLock);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(null);
        
        String result = mergeMessageProcessor.mergePerson(personsMergeRequest, key);
        
        assertEquals(Constants.ALREADY_PROCESSED, result);
        verify(usageLog).logUsageEvent("mergePerson()", "Lock does not exists for OLD but exists for NEW");
    }

    // Test Case 10: Both locks are null
    @Test
    public void testMergePerson_BothLocksNull() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(null);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(null);
        
        String result = mergeMessageProcessor.mergePerson(personsMergeRequest, key);
        
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    // Additional test case demonstrating ReflectionTestUtils for setting field values
    @Test
    public void testMergePerson_UsingReflectionTestUtilsForFields() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonsRequestData personsMergeRequest = createPersonsRequestData("udpid1", "clientid1", "oldudpid1", "oldclientid1");
        String key = "testKey";
        
        // Create a new instance and use ReflectionTestUtils to inject dependencies
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "personLocksDao", personLocksDao);
        ReflectionTestUtils.setField(processor, "personForgetKeyDao", personForgetKeyDao);
        ReflectionTestUtils.setField(processor, "deleteMessageProcessor", deleteMessageProcessor);
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        ReflectionTestUtils.setField(processor, "usageLog", usageLog);
        
        // Mock the DAO responses
        when(personLocksDao.getPersonLock("udpid1", "clientid1")).thenReturn(null);
        when(personLocksDao.getPersonLock("oldudpid1", "oldclientid1")).thenReturn(null);
        
        // Call the method using ReflectionTestUtils
        String result = (String) ReflectionTestUtils.invokeMethod(processor, "mergePerson", personsMergeRequest, key);
        
        assertEquals("Lock does not exists for old and new and history data also not found", result);
    }

    // Test case using ReflectionTestUtils to call private methods directly
    @Test
    public void testPrivateMethodsDirectly_UsingReflectionTestUtils() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        PersonLockVO oldPersonLock = createPersonLockVO("oldudpid1", "oldclientid1", new Timestamp(System.currentTimeMillis()));
        String key = "testKey";
        
        // Setup processor with dependencies
        MergeMessageProcessor processor = new MergeMessageProcessor();
        ReflectionTestUtils.setField(processor, "validationUtil", validationUtil);
        
        // Mock validationUtil behavior
        when(validationUtil.insertPersonLockUsage(any(), any(), any())).thenReturn(1);
        
        // Call private method directly using ReflectionTestUtils
        Integer result = ReflectionTestUtils.invokeMethod(processor, "insertUsageCode", oldPersonLock, Constants.UDP_MERGE, key);
        
        // Since we're mocking the underlying call, we expect the method to work
        assertNotNull(result);
    }
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
        lockVO.setPersonLockId(123L); // Mock ID
        return lockVO;
    }

    private PersonForgotKeyVO createPersonForgotKeyVO(String status) {
        PersonForgotKeyVO forgotKeyVO = new PersonForgotKeyVO();
        forgotKeyVO.setStatus(status);
        return forgotKeyVO;
    }
}
