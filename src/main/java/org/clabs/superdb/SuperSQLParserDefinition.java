package org.clabs.superdb;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.clabs.superdb.parser.SuperSQLParser;
import org.clabs.superdb.psi.SuperSQLFile;
import org.clabs.superdb.psi.SuperSQLTypes;
import org.jetbrains.annotations.NotNull;

public class SuperSQLParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(SuperSQLLanguage.INSTANCE);

    public static final TokenSet COMMENTS = TokenSet.create(
            SuperSQLTypes.LINE_COMMENT,
            SuperSQLTypes.BLOCK_COMMENT
    );

    public static final TokenSet STRINGS = TokenSet.create(
            SuperSQLTypes.DOUBLE_QUOTED_STRING,
            SuperSQLTypes.SINGLE_QUOTED_STRING,
            SuperSQLTypes.BACKTICK_STRING,
            SuperSQLTypes.RAW_STRING,
            SuperSQLTypes.FSTRING
    );

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new SuperSQLLexerAdapter();
    }

    @NotNull
    @Override
    public PsiParser createParser(Project project) {
        return new SuperSQLParser();
    }

    @NotNull
    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return STRINGS;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        return SuperSQLTypes.Factory.createElement(node);
    }

    @NotNull
    @Override
    public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new SuperSQLFile(viewProvider);
    }
}
