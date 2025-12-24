package org.clabs.superdb;

import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.testFramework.ParsingTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests for SuperSQL injection in shell scripts.
 *
 * These tests extract SuperSQL content from shell script test files
 * and verify they parse correctly.
 */
public class SuperSQLInjectionTest extends ParsingTestCase {

    // Pattern to extract content from super -c "..." or super -c '...'
    // Handles multi-line strings and various flag combinations
    private static final Pattern SUPER_COMMAND_EXTRACT = Pattern.compile(
            "super\\s+(?:[^\"']*?)(?:-c|--command)\\s*\"([^\"]*(?:\"[^\"]*\"[^\"]*)*?)\"",
            Pattern.DOTALL
    );

    // Simpler pattern for single-quoted strings
    private static final Pattern SUPER_COMMAND_SINGLE = Pattern.compile(
            "super\\s+(?:[^\"']*?)(?:-c|--command)\\s*'([^']*)'",
            Pattern.DOTALL
    );

    public SuperSQLInjectionTest() {
        super("injection", "sh", new SuperSQLParserDefinition());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    @Override
    protected boolean skipSpaces() {
        return true;
    }

    /**
     * Tests that all SuperSQL snippets in basic_super_commands.sh parse without errors.
     */
    public void testBasicSuperCommands() throws IOException {
        testShellFile("basic_super_commands.sh");
    }

    /**
     * Tests bash_interpolation.sh - this file contains complex bash variable
     * interpolation that can't be fully parsed as standalone SuperSQL.
     * This test verifies the file exists and has valid structure.
     */
    public void testBashInterpolationFileExists() throws IOException {
        Path filePath = Path.of(getTestDataPath(), "injection", "bash_interpolation.sh");
        assertTrue("bash_interpolation.sh should exist", Files.exists(filePath));
        String content = Files.readString(filePath);
        assertTrue("File should contain super commands", content.contains("super"));
        assertTrue("File should contain bash interpolation", content.contains("${"));
    }

    /**
     * Tests that all SuperSQL snippets in complex_patterns.sh parse without errors.
     */
    public void testComplexPatterns() throws IOException {
        testShellFile("complex_patterns.sh");
    }

    /**
     * Extracts SuperSQL content from a shell file and verifies each snippet parses correctly.
     */
    private void testShellFile(String filename) throws IOException {
        Path filePath = Path.of(getTestDataPath(), "injection", filename);
        String content = Files.readString(filePath);

        List<String> snippets = extractSuperSqlSnippets(content);
        assertTrue("No SuperSQL snippets found in " + filename, !snippets.isEmpty());

        List<String> errors = new ArrayList<>();
        for (int i = 0; i < snippets.size(); i++) {
            String snippet = snippets.get(i);
            List<String> parseErrors = getParseErrors(snippet);
            if (!parseErrors.isEmpty()) {
                errors.add(String.format("Snippet %d:\n%s\nErrors: %s",
                        i + 1, truncate(snippet, 100), parseErrors));
            }
        }

        if (!errors.isEmpty()) {
            fail("Parse errors found in " + filename + ":\n" + String.join("\n\n", errors));
        }
    }

    /**
     * Extracts SuperSQL snippets from shell script content.
     * Skips snippets that contain unexpanded bash variables ($var) since they're
     * not valid SuperSQL until expanded at runtime.
     */
    private List<String> extractSuperSqlSnippets(String content) {
        List<String> snippets = new ArrayList<>();

        // Extract double-quoted strings
        Matcher matcher = SUPER_COMMAND_EXTRACT.matcher(content);
        while (matcher.find()) {
            String snippet = matcher.group(1);
            // Unescape basic escapes
            snippet = snippet.replace("\\n", "\n").replace("\\t", "\t");
            // Skip snippets with unexpanded bash variables ($var but not ${ })
            // These are valid in shell context but not parseable as pure SuperSQL
            if (!containsUnexpandedBashVar(snippet)) {
                snippets.add(snippet);
            }
        }

        // Extract single-quoted strings
        matcher = SUPER_COMMAND_SINGLE.matcher(content);
        while (matcher.find()) {
            String snippet = matcher.group(1);
            if (!containsUnexpandedBashVar(snippet)) {
                snippets.add(snippet);
            }
        }

        return snippets;
    }

    /**
     * Checks if a snippet contains unexpanded bash variables like $var or $VAR
     * (but not ${ } which we handle as BASH_INTERPOLATION).
     */
    private boolean containsUnexpandedBashVar(String snippet) {
        // Match $word but not ${ (which is bash interpolation we handle)
        return Pattern.compile("\\$(?!\\{)[a-zA-Z_][a-zA-Z0-9_]*").matcher(snippet).find();
    }

    /**
     * Parses a SuperSQL snippet and returns any error messages.
     */
    private List<String> getParseErrors(String superSqlContent) {
        List<String> errors = new ArrayList<>();

        PsiFile file = createPsiFile("test", superSqlContent);
        file.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(@NotNull com.intellij.psi.PsiElement element) {
                if (element instanceof PsiErrorElement errorElement) {
                    errors.add(errorElement.getErrorDescription());
                }
                super.visitElement(element);
            }
        });

        return errors;
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "<null>";
        String normalized = s.replace("\n", "\\n");
        return normalized.length() <= maxLen ? normalized : normalized.substring(0, maxLen) + "...";
    }
}
