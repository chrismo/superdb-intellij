package org.clabs.superdb;

import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.ParsingTestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests that all example files parse without errors.
 * This catches regressions in the parser/grammar.
 */
public class ExampleFilesParserTest extends ParsingTestCase {

    public ExampleFilesParserTest() {
        super("", "spq", new SuperSQLParserDefinition());
    }

    @Override
    protected String getTestDataPath() {
        return "examples";
    }

    /**
     * Parse all .spq files in examples/ and fail if any have parse errors.
     */
    public void testAllExampleFilesParse() throws IOException {
        Path examplesDir = Paths.get("examples");
        if (!Files.exists(examplesDir)) {
            fail("examples/ directory not found");
            return;
        }

        List<String> failures = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(examplesDir)) {
            List<Path> spqFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".spq"))
                    .collect(Collectors.toList());

            if (spqFiles.isEmpty()) {
                fail("No .spq files found in examples/");
                return;
            }

            for (Path spqFile : spqFiles) {
                String content = Files.readString(spqFile);
                String fileName = spqFile.getFileName().toString();

                PsiFile psiFile = createPsiFile(fileName, content);
                Collection<PsiErrorElement> errors = PsiTreeUtil.collectElementsOfType(psiFile, PsiErrorElement.class);

                if (!errors.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n").append(spqFile).append(":");
                    for (PsiErrorElement error : errors) {
                        int offset = error.getTextOffset();
                        String errorText = error.getErrorDescription();
                        sb.append("\n  - Line ").append(getLineNumber(content, offset))
                          .append(": ").append(errorText);
                    }
                    failures.add(sb.toString());
                }
            }
        }

        if (!failures.isEmpty()) {
            fail("Parse errors found in example files:" + String.join("", failures));
        }
    }

    private int getLineNumber(String content, int offset) {
        int line = 1;
        for (int i = 0; i < offset && i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }
}
