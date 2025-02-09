package org.clabs.superdb;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SuperSQLElementType extends IElementType {

    public SuperSQLElementType(@NotNull @NonNls String debugName) {
        super(debugName, SuperSQLLanguage.INSTANCE);
    }

}