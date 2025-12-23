package org.clabs.superdb;

import com.intellij.testFramework.ParsingTestCase;
/**
 * Parser tests for SuperSQL.
 * Uses IntelliJ's ParsingTestCase which compares parsed PSI trees
 * against expected output files in testData/parser/.
 *
 * For each test method named testXxx, it looks for:
 * - testData/parser/Xxx.spq (input)
 * - testData/parser/Xxx.txt (expected PSI tree)
 *
 * To regenerate expected output files (.txt), run tests with:
 * -Didea.tests.overwrite.data=true
 */
public class SuperSQLParserTest extends ParsingTestCase {

    public SuperSQLParserTest() {
        super("parser", "spq", new SuperSQLParserDefinition());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    @Override
    protected boolean skipSpaces() {
        return true;
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }

    // === Basic Statement Tests ===

    public void testSimpleSelect() {
        doTest(true);
    }

    public void testSelectWithWhere() {
        doTest(true);
    }

    public void testSelectWithGroupBy() {
        doTest(true);
    }

    public void testSelectWithOrderBy() {
        doTest(true);
    }

    public void testSelectWithJoin() {
        doTest(true);
    }

    public void testJoinUsing() {
        doTest(true);
    }

    public void testRecursiveCTE() {
        doTest(true);
    }

    public void testUnnestOrdinality() {
        doTest(true);
    }

    // === Pipe Operator Tests ===

    public void testPipeSequence() {
        doTest(true);
    }

    public void testForkOperator() {
        doTest(true);
    }

    public void testSwitchOperator() {
        doTest(true);
    }

    public void testSortOperator() {
        doTest(true);
    }

    public void testAggregation() {
        doTest(true);
    }

    // === Declaration Tests ===

    public void testConstDeclaration() {
        doTest(true);
    }

    public void testFunctionDeclaration() {
        doTest(true);
    }

    public void testTypeDeclaration() {
        doTest(true);
    }

    // === Expression Tests ===

    public void testArithmeticExpression() {
        doTest(true);
    }

    public void testComparisonExpression() {
        doTest(true);
    }

    public void testCaseExpression() {
        doTest(true);
    }

    public void testRecordLiteral() {
        doTest(true);
    }

    public void testArrayLiteral() {
        doTest(true);
    }

    // === Comment Tests ===

    public void testComments() {
        doTest(true);
    }

    // === Values Tests ===

    public void testValues1() {
        doTest(true);
    }

    public void testBareNumber() {
        doTest(true);
    }

    public void testBareIdentifier() {
        doTest(true);
    }

    public void testSimpleAddition() {
        doTest(true);
    }

    // === Error Recovery Tests ===

    public void testIncompleteSelect() {
        doTest(true);
    }

    public void testMissingParenthesis() {
        doTest(true);
    }
}
