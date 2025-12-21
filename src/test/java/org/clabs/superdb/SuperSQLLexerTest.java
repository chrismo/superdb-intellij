package org.clabs.superdb;

import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import org.clabs.superdb.psi.SuperSQLTypes;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the SuperSQL lexer.
 * Tests tokenization of various SuperSQL language constructs.
 */
public class SuperSQLLexerTest {

    private Lexer createLexer() {
        return new SuperSQLLexerAdapter();
    }

    private List<TokenInfo> tokenize(String text) {
        Lexer lexer = createLexer();
        lexer.start(text);
        List<TokenInfo> tokens = new ArrayList<>();
        while (lexer.getTokenType() != null) {
            tokens.add(new TokenInfo(
                    lexer.getTokenType(),
                    lexer.getTokenText()
            ));
            lexer.advance();
        }
        return tokens;
    }

    private void assertTokenTypes(String text, IElementType... expectedTypes) {
        List<TokenInfo> tokens = tokenize(text);
        List<IElementType> actualTypes = tokens.stream()
                .map(t -> t.type)
                .filter(t -> !t.toString().equals("WHITE_SPACE"))
                .toList();

        Assert.assertEquals(
                "Token count mismatch for: " + text,
                expectedTypes.length,
                actualTypes.size()
        );

        for (int i = 0; i < expectedTypes.length; i++) {
            Assert.assertEquals(
                    "Token type mismatch at position " + i + " for: " + text,
                    expectedTypes[i],
                    actualTypes.get(i)
            );
        }
    }

    // === Keyword Tests ===

    @Test
    public void testSQLKeywords() {
        assertTokenTypes("SELECT", SuperSQLTypes.SELECT);
        assertTokenTypes("FROM", SuperSQLTypes.FROM);
        assertTokenTypes("WHERE", SuperSQLTypes.WHERE);
        assertTokenTypes("GROUP BY", SuperSQLTypes.GROUP, SuperSQLTypes.BY);
        assertTokenTypes("ORDER BY", SuperSQLTypes.ORDER, SuperSQLTypes.BY);
        assertTokenTypes("LIMIT", SuperSQLTypes.LIMIT);
        assertTokenTypes("OFFSET", SuperSQLTypes.OFFSET);
    }

    @Test
    public void testSQLKeywordsCaseInsensitive() {
        assertTokenTypes("select", SuperSQLTypes.SELECT);
        assertTokenTypes("Select", SuperSQLTypes.SELECT);
        assertTokenTypes("SELECT", SuperSQLTypes.SELECT);
        assertTokenTypes("from", SuperSQLTypes.FROM);
        assertTokenTypes("From", SuperSQLTypes.FROM);
    }

    @Test
    public void testJoinKeywords() {
        assertTokenTypes("JOIN", SuperSQLTypes.JOIN);
        assertTokenTypes("LEFT JOIN", SuperSQLTypes.LEFT, SuperSQLTypes.JOIN);
        assertTokenTypes("RIGHT JOIN", SuperSQLTypes.RIGHT, SuperSQLTypes.JOIN);
        assertTokenTypes("INNER JOIN", SuperSQLTypes.INNER, SuperSQLTypes.JOIN);
        assertTokenTypes("CROSS JOIN", SuperSQLTypes.CROSS, SuperSQLTypes.JOIN);
        assertTokenTypes("ANTI JOIN", SuperSQLTypes.ANTI, SuperSQLTypes.JOIN);
    }

    @Test
    public void testPipeOperatorKeywords() {
        assertTokenTypes("fork", SuperSQLTypes.FORK);
        assertTokenTypes("switch", SuperSQLTypes.SWITCH);
        assertTokenTypes("sort", SuperSQLTypes.SORT);
        assertTokenTypes("top", SuperSQLTypes.TOP);
        assertTokenTypes("head", SuperSQLTypes.HEAD);
        assertTokenTypes("tail", SuperSQLTypes.TAIL);
        assertTokenTypes("cut", SuperSQLTypes.CUT);
        assertTokenTypes("drop", SuperSQLTypes.DROP);
        assertTokenTypes("put", SuperSQLTypes.PUT);
        assertTokenTypes("rename", SuperSQLTypes.RENAME);
        assertTokenTypes("uniq", SuperSQLTypes.UNIQ);
        assertTokenTypes("fuse", SuperSQLTypes.FUSE);
        assertTokenTypes("search", SuperSQLTypes.SEARCH);
    }

    @Test
    public void testDeclarationKeywords() {
        assertTokenTypes("const", SuperSQLTypes.CONST);
        assertTokenTypes("fn", SuperSQLTypes.FN);
        assertTokenTypes("let", SuperSQLTypes.LET);
        assertTokenTypes("op", SuperSQLTypes.OP);
        assertTokenTypes("type", SuperSQLTypes.TYPE_KW);
        assertTokenTypes("lambda", SuperSQLTypes.LAMBDA);
    }

    @Test
    public void testBooleanKeywords() {
        assertTokenTypes("true", SuperSQLTypes.TRUE);
        assertTokenTypes("false", SuperSQLTypes.FALSE);
        assertTokenTypes("null", SuperSQLTypes.NULL);
        assertTokenTypes("and", SuperSQLTypes.AND);
        assertTokenTypes("or", SuperSQLTypes.OR);
        assertTokenTypes("not", SuperSQLTypes.NOT);
    }

    // === Operator Tests ===

    @Test
    public void testPipeOperators() {
        assertTokenTypes("|", SuperSQLTypes.PIPE);
        assertTokenTypes("|>", SuperSQLTypes.PIPE_ARROW);
    }

    @Test
    public void testComparisonOperators() {
        assertTokenTypes("==", SuperSQLTypes.EQ);
        assertTokenTypes("!=", SuperSQLTypes.NEQ);
        assertTokenTypes("<>", SuperSQLTypes.NEQ);
        assertTokenTypes("<", SuperSQLTypes.LT);
        assertTokenTypes(">", SuperSQLTypes.GT);
        assertTokenTypes("<=", SuperSQLTypes.LE);
        assertTokenTypes(">=", SuperSQLTypes.GE);
    }

    @Test
    public void testArithmeticOperators() {
        assertTokenTypes("+", SuperSQLTypes.PLUS);
        assertTokenTypes("-", SuperSQLTypes.MINUS);
        assertTokenTypes("*", SuperSQLTypes.STAR);
        assertTokenTypes("/", SuperSQLTypes.SLASH);
        assertTokenTypes("%", SuperSQLTypes.PERCENT);
    }

    @Test
    public void testSpecialOperators() {
        assertTokenTypes("::", SuperSQLTypes.CAST_OP);
        assertTokenTypes(":=", SuperSQLTypes.ASSIGN);
        assertTokenTypes("...", SuperSQLTypes.SPREAD);
        assertTokenTypes("||", SuperSQLTypes.CONCAT);
    }

    // === Literal Tests ===

    @Test
    public void testIntegerLiterals() {
        assertTokenTypes("42", SuperSQLTypes.INT_LIT);
        assertTokenTypes("0", SuperSQLTypes.INT_LIT);
        assertTokenTypes("12345", SuperSQLTypes.INT_LIT);
    }

    @Test
    public void testFloatLiterals() {
        assertTokenTypes("3.14", SuperSQLTypes.FLOAT_LIT);
        assertTokenTypes(".5", SuperSQLTypes.FLOAT_LIT);
        assertTokenTypes("1.0e10", SuperSQLTypes.FLOAT_LIT);
    }

    @Test
    public void testHexLiterals() {
        assertTokenTypes("0x1a2b", SuperSQLTypes.HEX_LIT);
        assertTokenTypes("0xDEADBEEF", SuperSQLTypes.HEX_LIT);
    }

    @Test
    public void testSpecialNumericLiterals() {
        assertTokenTypes("NaN", SuperSQLTypes.NAN_LIT);
        assertTokenTypes("+Inf", SuperSQLTypes.INF_LIT);
        assertTokenTypes("-Inf", SuperSQLTypes.INF_LIT);
    }

    @Test
    public void testStringLiterals() {
        assertTokenTypes("\"hello\"", SuperSQLTypes.DOUBLE_QUOTED_STRING);
        assertTokenTypes("'hello'", SuperSQLTypes.SINGLE_QUOTED_STRING);
        assertTokenTypes("`hello`", SuperSQLTypes.BACKTICK_STRING);
    }

    @Test
    public void testDurationLiterals() {
        assertTokenTypes("1h", SuperSQLTypes.DURATION_LIT);
        assertTokenTypes("30s", SuperSQLTypes.DURATION_LIT);
        assertTokenTypes("1h30m", SuperSQLTypes.DURATION_LIT);
        assertTokenTypes("500ms", SuperSQLTypes.DURATION_LIT);
    }

    // === Comment Tests ===

    @Test
    public void testLineComment() {
        assertTokenTypes("-- this is a comment", SuperSQLTypes.LINE_COMMENT);
    }

    @Test
    public void testBlockComment() {
        assertTokenTypes("/* block comment */", SuperSQLTypes.BLOCK_COMMENT);
    }

    // === Bracket Tests ===

    @Test
    public void testBrackets() {
        assertTokenTypes("(", SuperSQLTypes.LPAREN);
        assertTokenTypes(")", SuperSQLTypes.RPAREN);
        assertTokenTypes("[", SuperSQLTypes.LBRACKET);
        assertTokenTypes("]", SuperSQLTypes.RBRACKET);
        assertTokenTypes("{", SuperSQLTypes.LBRACE);
        assertTokenTypes("}", SuperSQLTypes.RBRACE);
    }

    @Test
    public void testSpecialBrackets() {
        assertTokenTypes("|[", SuperSQLTypes.SET_LBRACKET);
        assertTokenTypes("]|", SuperSQLTypes.SET_RBRACKET);
        assertTokenTypes("|{", SuperSQLTypes.MAP_LBRACE);
        assertTokenTypes("}|", SuperSQLTypes.MAP_RBRACE);
    }

    // === Type Tests ===

    @Test
    public void testPrimitiveTypes() {
        assertTokenTypes("int64", SuperSQLTypes.INT64);
        assertTokenTypes("float64", SuperSQLTypes.FLOAT64);
        assertTokenTypes("string", SuperSQLTypes.STRING_TYPE);
        assertTokenTypes("bool", SuperSQLTypes.BOOL);
        assertTokenTypes("duration", SuperSQLTypes.DURATION_TYPE);
        assertTokenTypes("time", SuperSQLTypes.TIME_TYPE);
        assertTokenTypes("ip", SuperSQLTypes.IP_TYPE);
        assertTokenTypes("net", SuperSQLTypes.NET_TYPE);
    }

    // === Identifier Tests ===

    @Test
    public void testIdentifiers() {
        assertTokenTypes("foo", SuperSQLTypes.IDENTIFIER);
        assertTokenTypes("_bar", SuperSQLTypes.IDENTIFIER);
        assertTokenTypes("user_id", SuperSQLTypes.IDENTIFIER);
        assertTokenTypes("$special", SuperSQLTypes.IDENTIFIER);
    }

    // === Complex Expression Tests ===

    @Test
    public void testSimpleSelect() {
        List<TokenInfo> tokens = tokenize("SELECT * FROM users");
        Assert.assertTrue(tokens.size() >= 4);
    }

    @Test
    public void testPipeExpression() {
        List<TokenInfo> tokens = tokenize("from data | where x > 0 | head 10");
        Assert.assertTrue(tokens.stream().anyMatch(t -> t.type == SuperSQLTypes.PIPE));
        Assert.assertTrue(tokens.stream().anyMatch(t -> t.type == SuperSQLTypes.FROM));
        Assert.assertTrue(tokens.stream().anyMatch(t -> t.type == SuperSQLTypes.WHERE));
        Assert.assertTrue(tokens.stream().anyMatch(t -> t.type == SuperSQLTypes.HEAD));
    }

    @Test
    public void testRecordLiteral() {
        List<TokenInfo> tokens = tokenize("{name: \"John\", age: 30}");
        Assert.assertTrue(tokens.stream().anyMatch(t -> t.type == SuperSQLTypes.LBRACE));
        Assert.assertTrue(tokens.stream().anyMatch(t -> t.type == SuperSQLTypes.RBRACE));
        Assert.assertTrue(tokens.stream().anyMatch(t -> t.type == SuperSQLTypes.COLON));
    }

    // === Helper Class ===

    private record TokenInfo(IElementType type, String text) {
        @Override
        public String toString() {
            return type + ":'" + text + "'";
        }
    }
}
