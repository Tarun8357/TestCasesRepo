@Test
    void testCheckDB_ShouldReturnTrue_WhenResultsEmpty() {
        // Arrange: empty results
        when(template.query(anyString(), any(SingleColumnRowMapper.class)))
                .thenReturn(Collections.emptyList());

        // Act
        boolean result = healthCheck.checkDB();

        // Assert
        assertTrue(result, "Expected checkDB to return true when results are empty");
    }

    @Test
    void testCheckDB_ShouldReturnFalse_WhenResultsNotEmpty() {
        // Arrange: non-empty results
        when(template.query(anyString(), any(SingleColumnRowMapper.class)))
                .thenReturn(Collections.singletonList(1));

        // Act
        boolean result = healthCheck.checkDB();

        // Assert
        assertFalse(result, "Expected checkDB to return false when results are not empty");
    }
