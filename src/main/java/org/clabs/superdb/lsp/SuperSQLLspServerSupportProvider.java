package org.clabs.superdb.lsp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.lsp4ij.LanguageServerEnablementSupport;
import org.jetbrains.annotations.NotNull;

/**
 * Provides LSP server support configuration for SuperSQL.
 * This class determines when the LSP should be active and provides
 * the server definition.
 */
public class SuperSQLLspServerSupportProvider implements LanguageServerEnablementSupport {

    private static final String SERVER_ID = "supersql-lsp";

    @Override
    public boolean isEnabled(@NotNull Project project) {
        // LSP is enabled by default - can be made configurable later
        return true;
    }

    @Override
    public boolean isEnabled(@NotNull Project project, @NotNull VirtualFile file) {
        // Enable LSP for .spq files
        String extension = file.getExtension();
        return "spq".equalsIgnoreCase(extension);
    }
}
