package org.clabs.superdb;

import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.psi.PsiErrorElement;
import org.jetbrains.annotations.NotNull;

/**
 * Filters out default parser error highlighting for SuperDB files.
 * This prevents duplicate error reporting since we use SuperSQLSyntaxInspection
 * to report errors under the "SuperDB" inspection group instead of "General".
 */
public class SuperSQLHighlightErrorFilter extends HighlightErrorFilter {

    @Override
    public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement element) {
        // Don't highlight parser errors through default mechanism for SuperDB
        // Our SuperSQLSyntaxInspection handles them under the "SuperDB" group
        String languageId = element.getLanguage().getID();
        if ("SuperDB".equals(languageId)) {
            return false;
        }
        return true;
    }
}
