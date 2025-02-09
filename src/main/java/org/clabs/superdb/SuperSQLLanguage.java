package org.clabs.superdb;

import com.intellij.lang.Language;

public class SuperSQLLanguage extends Language {

    public static final SuperSQLLanguage INSTANCE = new SuperSQLLanguage();

    private SuperSQLLanguage() {
        super("SuperSQL");
    }

}