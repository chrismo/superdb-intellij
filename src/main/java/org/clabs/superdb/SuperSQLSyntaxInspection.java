package org.clabs.superdb;

import com.intellij.codeInspection.*;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Inspection that reports SuperSQL syntax errors under the "SuperDB" inspection group.
 * This wraps parser errors (PsiErrorElement) so they appear in a dedicated category
 * instead of the generic "General > Annotator" category.
 *
 * Works for both standalone .spq/.sup files and injected SuperSQL in shell scripts.
 */
public class SuperSQLSyntaxInspection extends LocalInspectionTool {

    @Override
    public @NotNull String getGroupDisplayName() {
        return "SuperDB";
    }

    @Override
    public @NotNull String getShortName() {
        return "SuperSQLSyntax";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Syntax errors";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public ProblemDescriptor @NotNull [] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        // Check if file is SuperDB language (handles both standalone and injected)
        Language language = file.getLanguage();
        if (!"SuperDB".equals(language.getID())) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }

        List<ProblemDescriptor> problems = new ArrayList<>();

        file.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof PsiErrorElement errorElement) {
                    String errorDescription = errorElement.getErrorDescription();
                    PsiElement highlightElement = errorElement;

                    // Try to highlight the previous sibling if error element has no text
                    if (errorElement.getTextLength() == 0 && errorElement.getPrevSibling() != null) {
                        highlightElement = errorElement.getPrevSibling();
                    }

                    problems.add(manager.createProblemDescriptor(
                            highlightElement,
                            errorDescription,
                            (LocalQuickFix) null,
                            ProblemHighlightType.ERROR,
                            isOnTheFly
                    ));
                }
                super.visitElement(element);
            }
        });

        return problems.toArray(ProblemDescriptor.EMPTY_ARRAY);
    }
}
