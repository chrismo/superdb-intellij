package org.clabs.superdb;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class SuperJSONDataFileType extends LanguageFileType {

    public static final SuperJSONDataFileType INSTANCE = new SuperJSONDataFileType();

    private SuperJSONDataFileType() {
        super(SuperSQLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "SuperJSON Data File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "SuperJSON data file";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "SuperJSON Data File";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "sup";
    }

    @Override
    public Icon getIcon() {
        return SuperSQLIcons.FILE;
    }

}
