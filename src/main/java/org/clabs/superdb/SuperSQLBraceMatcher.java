package org.clabs.superdb;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.clabs.superdb.psi.SuperSQLTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuperSQLBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(SuperSQLTypes.LPAREN, SuperSQLTypes.RPAREN, false),
            new BracePair(SuperSQLTypes.LBRACKET, SuperSQLTypes.RBRACKET, false),
            new BracePair(SuperSQLTypes.LBRACE, SuperSQLTypes.RBRACE, true),
            new BracePair(SuperSQLTypes.SET_LBRACKET, SuperSQLTypes.SET_RBRACKET, false),
            new BracePair(SuperSQLTypes.MAP_LBRACE, SuperSQLTypes.MAP_RBRACE, true),
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
