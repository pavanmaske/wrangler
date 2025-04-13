// Add these test methods to the existing test class
@Test
public void testByteSizeInGrammar() throws Exception {
    String recipe = "set-column :size 10MB";
    List<Directive> directives = new RecipeCompiler().compile(recipe);
    assertEquals(1, directives.size());
    
    // Verify the argument was parsed as a ByteSize token
    // This assumes you can inspect the directive's arguments
}

@Test
public void testTimeDurationInGrammar() throws Exception {
    String recipe = "set-column :timeout 500ms";
    List<Directive> directives = new RecipeCompiler().compile(recipe);
    assertEquals(1, directives.size());
    
    // Verify the argument was parsed as a TimeDuration token
}

@Test
public void testInvalidByteSizeInGrammar() {
    String recipe = "set-column :size 10XB";
    assertThrows(DirectiveParseException.class, () -> new RecipeCompiler().compile(recipe));
}

@Test
public void testInvalidTimeDurationInGrammar() {
    String recipe = "set-column :timeout 500ys";
    assertThrows(DirectiveParseException.class, () -> new RecipeCompiler().compile(recipe));
}