package org.clabs.superdb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.clabs.superdb.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * Annotator that flags operators and declarations as errors in SuperJSON (.sup) data files.
 * SUP files should only contain data values, not query operators or declarations.
 */
public class SuperJSONOperatorAnnotator implements Annotator {

    private static final String ERROR_MESSAGE = "Operators and declarations are not allowed in SuperJSON data files";

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // Only check .sup files
        if (!isSupFile(element)) {
            return;
        }

        // Check if this element is an operator or declaration that shouldn't be in a data file
        if (isDisallowedElement(element)) {
            holder.newAnnotation(HighlightSeverity.ERROR, ERROR_MESSAGE)
                    .create();
        }
    }

    private boolean isSupFile(@NotNull PsiElement element) {
        PsiFile psiFile = element.getContainingFile();
        if (psiFile == null) {
            return false;
        }
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            return false;
        }
        String extension = virtualFile.getExtension();
        return "sup".equals(extension);
    }

    private boolean isDisallowedElement(@NotNull PsiElement element) {
        // Declarations are not allowed in data files
        if (element instanceof SuperSQLDeclaration) {
            return true;
        }

        // All operators are not allowed - check for the operator rule
        if (element instanceof SuperSQLOperator) {
            return true;
        }

        // Specific operator types that might not be caught by SuperSQLOperator
        if (element instanceof SuperSQLSqlOp ||
            element instanceof SuperSQLForkOp ||
            element instanceof SuperSQLSwitchOp ||
            element instanceof SuperSQLSearchOp ||
            element instanceof SuperSQLAssertOp ||
            element instanceof SuperSQLSortOp ||
            element instanceof SuperSQLTopOp ||
            element instanceof SuperSQLCutOp ||
            element instanceof SuperSQLDistinctOp ||
            element instanceof SuperSQLDropOp ||
            element instanceof SuperSQLHeadOp ||
            element instanceof SuperSQLTailOp ||
            element instanceof SuperSQLSkipOp ||
            element instanceof SuperSQLWhereOp ||
            element instanceof SuperSQLUniqOp ||
            element instanceof SuperSQLPutOp ||
            element instanceof SuperSQLRenameOp ||
            element instanceof SuperSQLFuseOp ||
            element instanceof SuperSQLJoinOp ||
            element instanceof SuperSQLShapesOp ||
            element instanceof SuperSQLFromOp ||
            element instanceof SuperSQLPassOp ||
            element instanceof SuperSQLExplodeOp ||
            element instanceof SuperSQLMergeOp ||
            element instanceof SuperSQLUnnestOp ||
            element instanceof SuperSQLValuesOp ||
            element instanceof SuperSQLLoadOp ||
            element instanceof SuperSQLOutputOp ||
            element instanceof SuperSQLDebugOp ||
            element instanceof SuperSQLCallOp ||
            element instanceof SuperSQLCountOp) {
            return true;
        }

        // Aggregations are not allowed
        if (element instanceof SuperSQLAggregation) {
            return true;
        }

        return false;
    }
}
