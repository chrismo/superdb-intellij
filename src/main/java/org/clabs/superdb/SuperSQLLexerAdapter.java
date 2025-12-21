package org.clabs.superdb;

import com.intellij.lexer.FlexAdapter;

public class SuperSQLLexerAdapter extends FlexAdapter {

    public SuperSQLLexerAdapter() {
        super(new SuperSQLLexer(null));
    }
}
