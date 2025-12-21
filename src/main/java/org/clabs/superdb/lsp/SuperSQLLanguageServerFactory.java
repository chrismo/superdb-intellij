package org.clabs.superdb.lsp;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.redhat.devtools.lsp4ij.LanguageServerFactory;
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider;
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Factory for creating connections to the SuperSQL Language Server.
 * <p>
 * This factory supports multiple ways to locate the LSP binary:
 * 1. Bundled binary extracted from plugin resources
 * 2. Binary in system PATH (super-lsp)
 * 3. Custom path via system property (supersql.lsp.path)
 */
public class SuperSQLLanguageServerFactory implements LanguageServerFactory {

    private static final Logger LOG = Logger.getInstance(SuperSQLLanguageServerFactory.class);
    private static final String LSP_BINARY_NAME = "super-lsp";
    private static final String LSP_PATH_PROPERTY = "supersql.lsp.path";

    @Override
    public @NotNull StreamConnectionProvider createConnectionProvider(@NotNull Project project) {
        String lspPath = findLspBinary();
        List<String> commands = Collections.singletonList(lspPath);

        LOG.info("Starting SuperSQL LSP: " + lspPath);

        ProcessStreamConnectionProvider provider = new ProcessStreamConnectionProvider(commands);
        provider.setWorkingDirectory(project.getBasePath());
        return provider;
    }

    /**
     * Finds the LSP binary using the following precedence:
     * 1. System property supersql.lsp.path
     * 2. Bundled binary in plugin resources
     * 3. Binary in system PATH
     */
    private String findLspBinary() {
        // 1. Check system property
        String customPath = System.getProperty(LSP_PATH_PROPERTY);
        if (customPath != null && !customPath.isEmpty()) {
            File customFile = new File(customPath);
            if (customFile.exists() && customFile.canExecute()) {
                LOG.info("Using custom LSP path: " + customPath);
                return customPath;
            } else {
                LOG.warn("Custom LSP path not found or not executable: " + customPath);
            }
        }

        // 2. Try bundled binary
        try {
            String bundledPath = extractBundledBinary();
            if (bundledPath != null) {
                LOG.info("Using bundled LSP binary: " + bundledPath);
                return bundledPath;
            }
        } catch (IOException e) {
            LOG.warn("Failed to extract bundled LSP binary", e);
        }

        // 3. Fall back to system PATH
        LOG.info("Using system PATH for LSP binary: " + LSP_BINARY_NAME);
        return LSP_BINARY_NAME;
    }

    /**
     * Extracts the bundled LSP binary for the current platform.
     */
    private String extractBundledBinary() throws IOException {
        String resourceName = getBundledResourceName();
        if (resourceName == null) {
            return null;
        }

        // Check if binary exists in resources
        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            if (is == null) {
                LOG.info("No bundled LSP binary found at: " + resourceName);
                return null;
            }

            // Extract to temp directory
            Path tempDir = Files.createTempDirectory("supersql-lsp");
            String binaryName = SystemInfo.isWindows ? LSP_BINARY_NAME + ".exe" : LSP_BINARY_NAME;
            Path binaryPath = tempDir.resolve(binaryName);

            Files.copy(is, binaryPath, StandardCopyOption.REPLACE_EXISTING);

            // Make executable on Unix systems
            if (!SystemInfo.isWindows) {
                Set<PosixFilePermission> perms = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OTHERS_EXECUTE
                );
                Files.setPosixFilePermissions(binaryPath, perms);
            }

            // Mark for cleanup on exit
            binaryPath.toFile().deleteOnExit();
            tempDir.toFile().deleteOnExit();

            return binaryPath.toString();
        }
    }

    /**
     * Gets the resource path for the bundled binary based on current platform.
     */
    private String getBundledResourceName() {
        String os;
        String arch;

        if (SystemInfo.isWindows) {
            os = "windows";
        } else if (SystemInfo.isMac) {
            os = "darwin";
        } else if (SystemInfo.isLinux) {
            os = "linux";
        } else {
            LOG.warn("Unsupported OS: " + SystemInfo.OS_NAME);
            return null;
        }

        // Detect architecture
        String osArch = System.getProperty("os.arch", "").toLowerCase();
        if (osArch.contains("amd64") || osArch.contains("x86_64")) {
            arch = "amd64";
        } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            arch = "arm64";
        } else {
            LOG.warn("Unsupported architecture: " + osArch);
            return null;
        }

        String suffix = SystemInfo.isWindows ? ".exe" : "";
        return "/lsp/super-lsp-" + os + "-" + arch + suffix;
    }
}
