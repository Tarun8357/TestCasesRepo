@Test
    void testCheckDB_ShouldReturnTrue_WhenResultsEmpty() {
        // Arrange: results.isEmpty() = true
        when(template.query(anyString(), any(SingleColumnRowMapper.class)))
                .thenReturn(Collections.emptyList());

        // Act
        boolean result = healthCheck.checkDB();

        // Assert
        assertTrue(result, "Expected checkDB to return true when results are empty");
    }

    @Test
    void testCheckDB_ShouldReturnFalse_WhenDataAccessExceptionThrown() {
        // Arrange: trigger catch block
        when(template.query(anyString(), any(SingleColumnRowMapper.class)))
                .thenThrow(new DataAccessResourceFailureException("DB connection error"));

        // Act
        boolean result = healthCheck.checkDB();

        // Assert
        assertFalse(result, "Expected checkDB to return false when exception is thrown");
    }
