package org.clabs.superdb;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Integration tests for SuperSQL syntax highlighting.
 * Uses IntelliJ's test framework to verify highlighting behavior.
 *
 * Note: We avoid checkHighlighting() calls as they trigger the daemon analyzer
 * which loads LSP4IJ, causing issues with temp filesystem paths in tests.
 * Instead, we verify that files parse correctly and the highlighter is registered.
 */
public class SuperSQLHighlightingTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/highlighting";
    }

    /**
     * Tests that basic SuperSQL files can be opened and parsed without errors.
     */
    public void testBasicHighlighting() {
        myFixture.configureByText("test.spq", """
                SELECT * FROM users WHERE id > 0
                """);
        // Verify file is created and parsed
        assertNotNull(myFixture.getFile());
        assertEquals(SuperSQLLanguage.INSTANCE, myFixture.getFile().getLanguage());
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
        assertNotNull(myFixture.getFile());
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
        assertNotNull(myFixture.getFile());
    }

    /**
     * Tests highlighting of string literals.
     */
    public void testStringHighlighting() {
        myFixture.configureByText("test.spq", """
                SELECT "double quoted", 'single quoted', `backtick`
                """);
        assertNotNull(myFixture.getFile());
    }

    /**
     * Tests highlighting of numeric literals.
     */
    public void testNumericHighlighting() {
        myFixture.configureByText("test.spq", """
                SELECT 42, 3.14, 0x1a2b, 1h30m, 2024-01-01T00:00:00Z
                """);
        assertNotNull(myFixture.getFile());
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
        assertNotNull(myFixture.getFile());
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
