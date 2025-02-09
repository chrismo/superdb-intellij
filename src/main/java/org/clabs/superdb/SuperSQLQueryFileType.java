package org.clabs.superdb;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class SuperSQLQueryFileType extends LanguageFileType {

    public static final SuperSQLQueryFileType INSTANCE = new SuperSQLQueryFileType();

    private SuperSQLQueryFileType() {
        super(SuperSQLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "SuperSQL Query File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "SuperSQL Query language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "spq";
    }

    @Override
    public Icon getIcon() {
        return SuperSQLIcons.FILE;
    }

}