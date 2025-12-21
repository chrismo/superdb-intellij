package org.clabs.superdb;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.clabs.superdb.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SuperSQLFoldingBuilder extends FoldingBuilderEx implements DumbAware {

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        // Fold block comments
        Collection<PsiElement> comments = PsiTreeUtil.findChildrenOfType(root, PsiElement.class);
        for (PsiElement element : comments) {
            if (element.getNode().getElementType() == SuperSQLTypes.BLOCK_COMMENT) {
                TextRange range = element.getTextRange();
                if (range.getLength() > 4) { // /* */
                    descriptors.add(new FoldingDescriptor(
                            element.getNode(),
                            range,
                            FoldingGroup.newGroup("comment")
                    ));
                }
            }
        }

        // Fold record expressions { ... }
        Collection<SuperSQLRecordExpr> records = PsiTreeUtil.findChildrenOfType(root, SuperSQLRecordExpr.class);
        for (SuperSQLRecordExpr record : records) {
            TextRange range = record.getTextRange();
            if (range.getLength() > 2) {
                descriptors.add(new FoldingDescriptor(
                        record.getNode(),
                        range,
                        FoldingGroup.newGroup("record")
                ));
            }
        }

        // Fold array expressions [ ... ]
        Collection<SuperSQLArrayExpr> arrays = PsiTreeUtil.findChildrenOfType(root, SuperSQLArrayExpr.class);
        for (SuperSQLArrayExpr array : arrays) {
            TextRange range = array.getTextRange();
            if (range.getLength() > 2) {
                descriptors.add(new FoldingDescriptor(
                        array.getNode(),
                        range,
                        FoldingGroup.newGroup("array")
                ));
            }
        }

        // Fold CASE expressions
        Collection<SuperSQLCaseExpr> caseExprs = PsiTreeUtil.findChildrenOfType(root, SuperSQLCaseExpr.class);
        for (SuperSQLCaseExpr caseExpr : caseExprs) {
            TextRange range = caseExpr.getTextRange();
            if (range.getLength() > 10) {
                descriptors.add(new FoldingDescriptor(
                        caseExpr.getNode(),
                        range,
                        FoldingGroup.newGroup("case")
                ));
            }
        }

        // Fold scope bodies ( ... )
        Collection<SuperSQLScopeBody> scopes = PsiTreeUtil.findChildrenOfType(root, SuperSQLScopeBody.class);
        for (SuperSQLScopeBody scope : scopes) {
            TextRange range = scope.getTextRange();
            if (range.getLength() > 10) {
                descriptors.add(new FoldingDescriptor(
                        scope.getNode(),
                        range,
                        FoldingGroup.newGroup("scope")
                ));
            }
        }

        // Fold function declarations
        Collection<SuperSQLFuncDecl> funcs = PsiTreeUtil.findChildrenOfType(root, SuperSQLFuncDecl.class);
        for (SuperSQLFuncDecl func : funcs) {
            TextRange range = func.getTextRange();
            if (range.getLength() > 20) {
                descriptors.add(new FoldingDescriptor(
                        func.getNode(),
                        range,
                        FoldingGroup.newGroup("function")
                ));
            }
        }

        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        if (node.getElementType() == SuperSQLTypes.BLOCK_COMMENT) {
            return "/* ... */";
        }
        String text = node.getText();
        if (text.startsWith("{")) {
            return "{...}";
        }
        if (text.startsWith("[")) {
            return "[...]";
        }
        if (text.startsWith("(")) {
            return "(...)";
        }
        if (text.toUpperCase().startsWith("CASE")) {
            return "CASE...END";
        }
        if (text.toUpperCase().startsWith("FN")) {
            return "fn ...";
        }
        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
