package org.clabs.superdb.lsp;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application-level settings for SuperSQL LSP configuration.
 */
@Service(Service.Level.APP)
@State(
    name = "SuperSQLLspSettings",
    storages = @Storage("supersql-lsp.xml")
)
public final class SuperSQLLspSettings implements PersistentStateComponent<SuperSQLLspSettings.State> {

    public static class State {
        /** Whether LSP is enabled */
        public boolean enabled = true;

        /** Custom path to the LSP binary (empty = use bundled/PATH) */
        public String customLspPath = "";

        /** Whether to show LSP status notifications */
        public boolean showNotifications = true;
    }

    private State myState = new State();

    public static SuperSQLLspSettings getInstance() {
        return ApplicationManager.getApplication().getService(SuperSQLLspSettings.class);
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public boolean isEnabled() {
        return myState.enabled;
    }

    public void setEnabled(boolean enabled) {
        myState.enabled = enabled;
    }

    public String getCustomLspPath() {
        return myState.customLspPath;
    }

    public void setCustomLspPath(String path) {
        myState.customLspPath = path;
    }

    public boolean isShowNotifications() {
        return myState.showNotifications;
    }

    public void setShowNotifications(boolean show) {
        myState.showNotifications = show;
    }
}
