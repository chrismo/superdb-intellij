package org.clabs.superdb;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import org.clabs.superdb.psi.SuperSQLTypes;

public class SuperSQLQuoteHandler extends SimpleTokenSetQuoteHandler {

    public SuperSQLQuoteHandler() {
        super(
                SuperSQLTypes.DOUBLE_QUOTED_STRING,
                SuperSQLTypes.SINGLE_QUOTED_STRING,
                SuperSQLTypes.BACKTICK_STRING
        );
    }
}
