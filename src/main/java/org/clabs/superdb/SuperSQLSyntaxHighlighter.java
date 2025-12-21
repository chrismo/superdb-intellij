package org.clabs.superdb;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.clabs.superdb.psi.SuperSQLTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class SuperSQLSyntaxHighlighter extends SyntaxHighlighterBase {

    // Color definitions
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("SUPERSQL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey OPERATOR_KEYWORD =
            createTextAttributesKey("SUPERSQL_OPERATOR_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey TYPE_KEYWORD =
            createTextAttributesKey("SUPERSQL_TYPE_KEYWORD", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("SUPERSQL_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("SUPERSQL_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey LINE_COMMENT =
            createTextAttributesKey("SUPERSQL_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT =
            createTextAttributesKey("SUPERSQL_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("SUPERSQL_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey FUNCTION_CALL =
            createTextAttributesKey("SUPERSQL_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey OPERATION_SIGN =
            createTextAttributesKey("SUPERSQL_OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey PARENTHESES =
            createTextAttributesKey("SUPERSQL_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACKETS =
            createTextAttributesKey("SUPERSQL_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey BRACES =
            createTextAttributesKey("SUPERSQL_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey COMMA =
            createTextAttributesKey("SUPERSQL_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey SEMICOLON =
            createTextAttributesKey("SUPERSQL_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey DOT =
            createTextAttributesKey("SUPERSQL_DOT", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey REGEX =
            createTextAttributesKey("SUPERSQL_REGEX", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey CONSTANT =
            createTextAttributesKey("SUPERSQL_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("SUPERSQL_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
    private static final TextAttributesKey[] OPERATOR_KEYWORD_KEYS = new TextAttributesKey[]{OPERATOR_KEYWORD};
    private static final TextAttributesKey[] TYPE_KEYWORD_KEYS = new TextAttributesKey[]{TYPE_KEYWORD};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
    private static final TextAttributesKey[] LINE_COMMENT_KEYS = new TextAttributesKey[]{LINE_COMMENT};
    private static final TextAttributesKey[] BLOCK_COMMENT_KEYS = new TextAttributesKey[]{BLOCK_COMMENT};
    private static final TextAttributesKey[] IDENTIFIER_KEYS = new TextAttributesKey[]{IDENTIFIER};
    private static final TextAttributesKey[] OPERATION_SIGN_KEYS = new TextAttributesKey[]{OPERATION_SIGN};
    private static final TextAttributesKey[] PARENTHESES_KEYS = new TextAttributesKey[]{PARENTHESES};
    private static final TextAttributesKey[] BRACKETS_KEYS = new TextAttributesKey[]{BRACKETS};
    private static final TextAttributesKey[] BRACES_KEYS = new TextAttributesKey[]{BRACES};
    private static final TextAttributesKey[] COMMA_KEYS = new TextAttributesKey[]{COMMA};
    private static final TextAttributesKey[] SEMICOLON_KEYS = new TextAttributesKey[]{SEMICOLON};
    private static final TextAttributesKey[] DOT_KEYS = new TextAttributesKey[]{DOT};
    private static final TextAttributesKey[] REGEX_KEYS = new TextAttributesKey[]{REGEX};
    private static final TextAttributesKey[] CONSTANT_KEYS = new TextAttributesKey[]{CONSTANT};
    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new SuperSQLLexerAdapter();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        // Comments
        if (tokenType.equals(SuperSQLTypes.LINE_COMMENT)) {
            return LINE_COMMENT_KEYS;
        }
        if (tokenType.equals(SuperSQLTypes.BLOCK_COMMENT)) {
            return BLOCK_COMMENT_KEYS;
        }

        // SQL Keywords
        if (tokenType.equals(SuperSQLTypes.SELECT) ||
            tokenType.equals(SuperSQLTypes.FROM) ||
            tokenType.equals(SuperSQLTypes.WHERE) ||
            tokenType.equals(SuperSQLTypes.GROUP) ||
            tokenType.equals(SuperSQLTypes.BY) ||
            tokenType.equals(SuperSQLTypes.HAVING) ||
            tokenType.equals(SuperSQLTypes.ORDER) ||
            tokenType.equals(SuperSQLTypes.LIMIT) ||
            tokenType.equals(SuperSQLTypes.OFFSET) ||
            tokenType.equals(SuperSQLTypes.UNION) ||
            tokenType.equals(SuperSQLTypes.ALL) ||
            tokenType.equals(SuperSQLTypes.DISTINCT) ||
            tokenType.equals(SuperSQLTypes.AS) ||
            tokenType.equals(SuperSQLTypes.ON) ||
            tokenType.equals(SuperSQLTypes.JOIN) ||
            tokenType.equals(SuperSQLTypes.LEFT) ||
            tokenType.equals(SuperSQLTypes.RIGHT) ||
            tokenType.equals(SuperSQLTypes.INNER) ||
            tokenType.equals(SuperSQLTypes.OUTER) ||
            tokenType.equals(SuperSQLTypes.FULL) ||
            tokenType.equals(SuperSQLTypes.CROSS) ||
            tokenType.equals(SuperSQLTypes.ANTI) ||
            tokenType.equals(SuperSQLTypes.WITH) ||
            tokenType.equals(SuperSQLTypes.VALUES) ||
            tokenType.equals(SuperSQLTypes.CASE) ||
            tokenType.equals(SuperSQLTypes.WHEN) ||
            tokenType.equals(SuperSQLTypes.THEN) ||
            tokenType.equals(SuperSQLTypes.ELSE) ||
            tokenType.equals(SuperSQLTypes.END) ||
            tokenType.equals(SuperSQLTypes.CAST) ||
            tokenType.equals(SuperSQLTypes.EXTRACT) ||
            tokenType.equals(SuperSQLTypes.SUBSTRING) ||
            tokenType.equals(SuperSQLTypes.DATE_KW) ||
            tokenType.equals(SuperSQLTypes.TIMESTAMP_KW) ||
            tokenType.equals(SuperSQLTypes.INTERVAL) ||
            tokenType.equals(SuperSQLTypes.FOR) ||
            tokenType.equals(SuperSQLTypes.EXISTS) ||
            tokenType.equals(SuperSQLTypes.BETWEEN) ||
            tokenType.equals(SuperSQLTypes.LIKE) ||
            tokenType.equals(SuperSQLTypes.IN) ||
            tokenType.equals(SuperSQLTypes.IS) ||
            tokenType.equals(SuperSQLTypes.ASC) ||
            tokenType.equals(SuperSQLTypes.DESC) ||
            tokenType.equals(SuperSQLTypes.NULLS) ||
            tokenType.equals(SuperSQLTypes.FIRST) ||
            tokenType.equals(SuperSQLTypes.LAST) ||
            tokenType.equals(SuperSQLTypes.AND) ||
            tokenType.equals(SuperSQLTypes.OR) ||
            tokenType.equals(SuperSQLTypes.NOT)) {
            return KEYWORD_KEYS;
        }

        // Pipe Operators (special styling)
        if (tokenType.equals(SuperSQLTypes.FORK) ||
            tokenType.equals(SuperSQLTypes.SWITCH) ||
            tokenType.equals(SuperSQLTypes.SEARCH) ||
            tokenType.equals(SuperSQLTypes.ASSERT) ||
            tokenType.equals(SuperSQLTypes.SORT) ||
            tokenType.equals(SuperSQLTypes.TOP) ||
            tokenType.equals(SuperSQLTypes.CUT) ||
            tokenType.equals(SuperSQLTypes.DROP) ||
            tokenType.equals(SuperSQLTypes.HEAD) ||
            tokenType.equals(SuperSQLTypes.TAIL) ||
            tokenType.equals(SuperSQLTypes.SKIP_KW) ||
            tokenType.equals(SuperSQLTypes.UNIQ) ||
            tokenType.equals(SuperSQLTypes.PUT) ||
            tokenType.equals(SuperSQLTypes.RENAME) ||
            tokenType.equals(SuperSQLTypes.FUSE) ||
            tokenType.equals(SuperSQLTypes.SHAPES) ||
            tokenType.equals(SuperSQLTypes.PASS) ||
            tokenType.equals(SuperSQLTypes.EXPLODE) ||
            tokenType.equals(SuperSQLTypes.MERGE) ||
            tokenType.equals(SuperSQLTypes.UNNEST) ||
            tokenType.equals(SuperSQLTypes.LOAD) ||
            tokenType.equals(SuperSQLTypes.OUTPUT) ||
            tokenType.equals(SuperSQLTypes.DEBUG) ||
            tokenType.equals(SuperSQLTypes.COUNT) ||
            tokenType.equals(SuperSQLTypes.CALL) ||
            tokenType.equals(SuperSQLTypes.AGGREGATE) ||
            tokenType.equals(SuperSQLTypes.SUMMARIZE) ||
            tokenType.equals(SuperSQLTypes.DEFAULT)) {
            return OPERATOR_KEYWORD_KEYS;
        }

        // Declaration Keywords
        if (tokenType.equals(SuperSQLTypes.CONST) ||
            tokenType.equals(SuperSQLTypes.FN) ||
            tokenType.equals(SuperSQLTypes.LET) ||
            tokenType.equals(SuperSQLTypes.LAMBDA) ||
            tokenType.equals(SuperSQLTypes.OP) ||
            tokenType.equals(SuperSQLTypes.PRAGMA) ||
            tokenType.equals(SuperSQLTypes.TYPE_KW)) {
            return KEYWORD_KEYS;
        }

        // Type Keywords
        if (tokenType.equals(SuperSQLTypes.ERROR) ||
            tokenType.equals(SuperSQLTypes.ENUM) ||
            tokenType.equals(SuperSQLTypes.UINT8) ||
            tokenType.equals(SuperSQLTypes.UINT16) ||
            tokenType.equals(SuperSQLTypes.UINT32) ||
            tokenType.equals(SuperSQLTypes.UINT64) ||
            tokenType.equals(SuperSQLTypes.INT8) ||
            tokenType.equals(SuperSQLTypes.INT16) ||
            tokenType.equals(SuperSQLTypes.INT32) ||
            tokenType.equals(SuperSQLTypes.INT64) ||
            tokenType.equals(SuperSQLTypes.FLOAT16) ||
            tokenType.equals(SuperSQLTypes.FLOAT32) ||
            tokenType.equals(SuperSQLTypes.FLOAT64) ||
            tokenType.equals(SuperSQLTypes.BOOL) ||
            tokenType.equals(SuperSQLTypes.STRING_TYPE) ||
            tokenType.equals(SuperSQLTypes.DURATION_TYPE) ||
            tokenType.equals(SuperSQLTypes.TIME_TYPE) ||
            tokenType.equals(SuperSQLTypes.BYTES_TYPE) ||
            tokenType.equals(SuperSQLTypes.IP_TYPE) ||
            tokenType.equals(SuperSQLTypes.NET_TYPE) ||
            tokenType.equals(SuperSQLTypes.BIGINT) ||
            tokenType.equals(SuperSQLTypes.BOOLEAN) ||
            tokenType.equals(SuperSQLTypes.BYTEA) ||
            tokenType.equals(SuperSQLTypes.CHAR) ||
            tokenType.equals(SuperSQLTypes.CIDR) ||
            tokenType.equals(SuperSQLTypes.INTEGER) ||
            tokenType.equals(SuperSQLTypes.INET) ||
            tokenType.equals(SuperSQLTypes.REAL) ||
            tokenType.equals(SuperSQLTypes.SMALLINT) ||
            tokenType.equals(SuperSQLTypes.TEXT) ||
            tokenType.equals(SuperSQLTypes.VARCHAR)) {
            return TYPE_KEYWORD_KEYS;
        }

        // Constants
        if (tokenType.equals(SuperSQLTypes.TRUE) ||
            tokenType.equals(SuperSQLTypes.FALSE) ||
            tokenType.equals(SuperSQLTypes.NULL) ||
            tokenType.equals(SuperSQLTypes.NAN_LIT) ||
            tokenType.equals(SuperSQLTypes.INF_LIT)) {
            return CONSTANT_KEYS;
        }

        // Strings
        if (tokenType.equals(SuperSQLTypes.DOUBLE_QUOTED_STRING) ||
            tokenType.equals(SuperSQLTypes.SINGLE_QUOTED_STRING) ||
            tokenType.equals(SuperSQLTypes.BACKTICK_STRING) ||
            tokenType.equals(SuperSQLTypes.RAW_STRING) ||
            tokenType.equals(SuperSQLTypes.FSTRING)) {
            return STRING_KEYS;
        }

        // Numbers
        if (tokenType.equals(SuperSQLTypes.INT_LIT) ||
            tokenType.equals(SuperSQLTypes.FLOAT_LIT) ||
            tokenType.equals(SuperSQLTypes.HEX_LIT) ||
            tokenType.equals(SuperSQLTypes.DURATION_LIT) ||
            tokenType.equals(SuperSQLTypes.TIMESTAMP_LIT) ||
            tokenType.equals(SuperSQLTypes.IP4_LIT) ||
            tokenType.equals(SuperSQLTypes.IP6_LIT) ||
            tokenType.equals(SuperSQLTypes.IP4_NET_LIT) ||
            tokenType.equals(SuperSQLTypes.IP6_NET_LIT)) {
            return NUMBER_KEYS;
        }

        // Regex
        if (tokenType.equals(SuperSQLTypes.REGEX)) {
            return REGEX_KEYS;
        }

        // Identifier
        if (tokenType.equals(SuperSQLTypes.IDENTIFIER)) {
            return IDENTIFIER_KEYS;
        }

        // Operators
        if (tokenType.equals(SuperSQLTypes.PIPE_ARROW) ||
            tokenType.equals(SuperSQLTypes.PIPE) ||
            tokenType.equals(SuperSQLTypes.CONCAT) ||
            tokenType.equals(SuperSQLTypes.CAST_OP) ||
            tokenType.equals(SuperSQLTypes.ASSIGN) ||
            tokenType.equals(SuperSQLTypes.SPREAD) ||
            tokenType.equals(SuperSQLTypes.EQ) ||
            tokenType.equals(SuperSQLTypes.NEQ) ||
            tokenType.equals(SuperSQLTypes.LE) ||
            tokenType.equals(SuperSQLTypes.GE) ||
            tokenType.equals(SuperSQLTypes.LT) ||
            tokenType.equals(SuperSQLTypes.GT) ||
            tokenType.equals(SuperSQLTypes.MATCH) ||
            tokenType.equals(SuperSQLTypes.PLUS) ||
            tokenType.equals(SuperSQLTypes.MINUS) ||
            tokenType.equals(SuperSQLTypes.STAR) ||
            tokenType.equals(SuperSQLTypes.SLASH) ||
            tokenType.equals(SuperSQLTypes.PERCENT) ||
            tokenType.equals(SuperSQLTypes.BANG) ||
            tokenType.equals(SuperSQLTypes.QUESTION) ||
            tokenType.equals(SuperSQLTypes.COLON) ||
            tokenType.equals(SuperSQLTypes.AT) ||
            tokenType.equals(SuperSQLTypes.AMP) ||
            tokenType.equals(SuperSQLTypes.EQUALS)) {
            return OPERATION_SIGN_KEYS;
        }

        // Parentheses
        if (tokenType.equals(SuperSQLTypes.LPAREN) ||
            tokenType.equals(SuperSQLTypes.RPAREN)) {
            return PARENTHESES_KEYS;
        }

        // Brackets
        if (tokenType.equals(SuperSQLTypes.LBRACKET) ||
            tokenType.equals(SuperSQLTypes.RBRACKET) ||
            tokenType.equals(SuperSQLTypes.SET_LBRACKET) ||
            tokenType.equals(SuperSQLTypes.SET_RBRACKET)) {
            return BRACKETS_KEYS;
        }

        // Braces
        if (tokenType.equals(SuperSQLTypes.LBRACE) ||
            tokenType.equals(SuperSQLTypes.RBRACE) ||
            tokenType.equals(SuperSQLTypes.MAP_LBRACE) ||
            tokenType.equals(SuperSQLTypes.MAP_RBRACE)) {
            return BRACES_KEYS;
        }

        // Comma
        if (tokenType.equals(SuperSQLTypes.COMMA)) {
            return COMMA_KEYS;
        }

        // Semicolon
        if (tokenType.equals(SuperSQLTypes.SEMICOLON)) {
            return SEMICOLON_KEYS;
        }

        // Dot
        if (tokenType.equals(SuperSQLTypes.DOT)) {
            return DOT_KEYS;
        }

        // Bad character
        if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        }

        return EMPTY_KEYS;
    }
}
