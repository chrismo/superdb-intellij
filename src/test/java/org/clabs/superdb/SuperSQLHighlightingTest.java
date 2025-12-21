package org.clabs.superdb;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Integration tests for SuperSQL syntax highlighting.
 * Uses IntelliJ's test framework to verify highlighting behavior.
 */
public class SuperSQLHighlightingTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/highlighting";
    }

    /**
     * Tests that basic SuperSQL files can be opened and highlighted without errors.
     */
    public void testBasicHighlighting() {
        myFixture.configureByText("test.spq", """
                SELECT * FROM users WHERE id > 0
                """);
        // If highlighting causes errors, this will fail
        myFixture.checkHighlighting(false, false, false);
    }

    /**
     * Tests highlighting of pipe operators.
     */
    public void testPipeOperatorHighlighting() {
        myFixture.configureByText("test.spq", """
                from data
                | where x > 0
                | head 10
                """);
        myFixture.checkHighlighting(false, false, false);
    }

    /**
     * Tests highlighting of comments.
     */
    public void testCommentHighlighting() {
        myFixture.configureByText("test.spq", """
                -- line comment
                SELECT * FROM users
                /* block
                   comment */
                """);
        myFixture.checkHighlighting(false, false, false);
    }

    /**
     * Tests highlighting of string literals.
     */
    public void testStringHighlighting() {
        myFixture.configureByText("test.spq", """
                SELECT "double quoted", 'single quoted', `backtick`
                """);
        myFixture.checkHighlighting(false, false, false);
    }

    /**
     * Tests highlighting of numeric literals.
     */
    public void testNumericHighlighting() {
        myFixture.configureByText("test.spq", """
                SELECT 42, 3.14, 0x1a2b, 1h30m, 2024-01-01T00:00:00Z
                """);
        myFixture.checkHighlighting(false, false, false);
    }

    /**
     * Tests highlighting of type expressions.
     */
    public void testTypeHighlighting() {
        myFixture.configureByText("test.spq", """
                type User = {
                    id: int64,
                    name: string,
                    score: float64
                }
                """);
        myFixture.checkHighlighting(false, false, false);
    }

    /**
     * Tests that the file type is correctly recognized.
     */
    public void testFileTypeRecognition() {
        myFixture.configureByText("query.spq", "SELECT 1");
        assertEquals(SuperSQLQueryFileType.INSTANCE, myFixture.getFile().getFileType());
    }

    /**
     * Tests that the language is correctly recognized.
     */
    public void testLanguageRecognition() {
        myFixture.configureByText("query.spq", "SELECT 1");
        assertEquals(SuperSQLLanguage.INSTANCE, myFixture.getFile().getLanguage());
    }
}
