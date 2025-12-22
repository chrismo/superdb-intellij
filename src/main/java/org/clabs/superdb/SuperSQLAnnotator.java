package org.clabs.superdb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.clabs.superdb.psi.SuperSQLTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Annotator for SuperSQL that provides semantic highlighting.
 * Currently highlights built-in function calls with special styling.
 *
 * Built-in function names are extracted from brimdata/super:
 * - runtime/sam/expr/function/function.go (scalar functions)
 * - runtime/sam/expr/agg/agg.go (aggregate functions)
 */
public class SuperSQLAnnotator implements Annotator {

    /**
     * Built-in scalar functions from brimdata/super.
     * These are registered in runtime/sam/expr/function/function.go
     */
    private static final Set<String> SCALAR_FUNCTIONS = Set.of(
            "abs",
            "base64",
            "bucket",
            "ceil",
            "cidr_match",
            "coalesce",
            "compare",
            "date_part",
            "fields",
            "flatten",
            "floor",
            "grep",
            "grok",
            "has",
            "has_error",
            "hex",
            "is_error",
            "join",
            "kind",
            "ksuid",
            "len",
            "length",
            "levenshtein",
            "log",
            "lower",
            "max",
            "min",
            "missing",
            "nameof",
            "nest_dotted",
            "network_of",
            "now",
            "nullif",
            "parse_sup",
            "parse_uri",
            "position",
            "pow",
            "quiet",
            "regexp",
            "regexp_replace",
            "replace",
            "round",
            "split",
            "sqrt",
            "strftime",
            "trim",
            "typename",
            "typeof",
            "under",
            "unflatten",
            "upper"
    );

    /**
     * Built-in aggregate functions from brimdata/super.
     * These are registered in runtime/sam/expr/agg/agg.go
     * Note: count, fuse, union, and, or are already lexer keywords
     */
    private static final Set<String> AGGREGATE_FUNCTIONS = Set.of(
            "any",
            "avg",
            "dcount",
            "sum",
            "collect",
            "collect_map"
    );

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        IElementType elementType = element.getNode().getElementType();

        // Only process identifiers
        if (!elementType.equals(SuperSQLTypes.IDENTIFIER)) {
            return;
        }

        String name = element.getText().toLowerCase();

        // Check if it's a built-in function
        if (SCALAR_FUNCTIONS.contains(name) || AGGREGATE_FUNCTIONS.contains(name)) {
            // Check if followed by '(' to confirm it's a function call
            PsiElement nextSibling = element.getNextSibling();
            while (nextSibling != null && nextSibling.getNode().getElementType().toString().equals("WHITE_SPACE")) {
                nextSibling = nextSibling.getNextSibling();
            }

            if (nextSibling != null && nextSibling.getNode().getElementType().equals(SuperSQLTypes.LPAREN)) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .textAttributes(SuperSQLSyntaxHighlighter.FUNCTION_CALL)
                        .create();
            }
        }
    }
}
