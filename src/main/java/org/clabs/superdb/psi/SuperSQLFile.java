package org.clabs.superdb.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.clabs.superdb.SuperSQLLanguage;
import org.clabs.superdb.SuperSQLQueryFileType;
import org.jetbrains.annotations.NotNull;

public class SuperSQLFile extends PsiFileBase {

    public SuperSQLFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, SuperSQLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return SuperSQLQueryFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "SuperSQL File";
    }
}
