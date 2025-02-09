package org.clabs.superdb;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SuperSQLTokenType extends IElementType {

    public SuperSQLTokenType(@NotNull @NonNls String debugName) {
        super(debugName, SuperSQLLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "SuperSQLTokenType." + super.toString();
    }

}