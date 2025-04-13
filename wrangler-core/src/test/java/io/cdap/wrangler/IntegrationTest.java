// Add this test to an existing integration test class or create a new one
@Test
public void testAggregateStatsInPipeline() throws Exception {
    String[] directives = new String[] {
        "parse-as-csv :body ','",
        "drop :body",
        "aggregate-stats :bytes :duration total_bytes total_duration 1MB 1s"
    };

    List<Row> rows = Arrays.asList(
        new Row("body", "1024KB,500ms"),
        new Row("body", "1MB,1s"),
        new Row("body", "512KB,1500ms")
    );

    List<Row> results = TestingRig.execute(directives, rows);
    assertEquals(1, results.size());
    
    Row result = results.get(0);
    // 1MB + 1MB + 0.5MB = 2.5MB
    assertEquals(2.5, (Double) result.getValue("total_bytes"), 0.001);
    // 0.5 + 1 + 1.5 = 3 seconds
    assertEquals(3.0, (Double) result.getValue("total_duration"), 0.001);
}