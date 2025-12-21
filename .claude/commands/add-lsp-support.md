# Add LSP Support

Add Language Server Protocol (LSP) support to this plugin using LSP4IJ.

## Prerequisites

Before running this command, ensure:
1. The `super-lsp` binary exists and is working
2. You know where the LSP binary will be located (bundled or external)

## Instructions

### Step 1: Update build.gradle.kts

Add LSP4IJ dependency:

```kotlin
intellij {
    plugins.set(listOf(
        "com.redhat.devtools.lsp4ij:x.y.z"  // Check for latest version
    ))
}
```

### Step 2: Create Language Server Factory

Create `src/main/java/org/clabs/superdb/lsp/SuperSQLLanguageServerFactory.java`:

```java
package org.clabs.superdb.lsp;

import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider;
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider;
import com.redhat.devtools.lsp4ij.LanguageServerFactory;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SuperSQLLanguageServerFactory implements LanguageServerFactory {

    @Override
    public @NotNull StreamConnectionProvider createConnectionProvider(@NotNull Project project) {
        // Option 1: Bundled binary
        // String binaryPath = PluginManagerCore.getPlugin(PluginId.getId("org.clabs.superdb"))
        //     .getPluginPath().resolve("bin/super-lsp").toString();

        // Option 2: System PATH
        List<String> commands = Arrays.asList("super-lsp");

        return new ProcessStreamConnectionProvider(commands);
    }
}
```

### Step 3: Create Server Definition

Create `src/main/java/org/clabs/superdb/lsp/SuperSQLServerDefinition.java`:

```java
package org.clabs.superdb.lsp;

import com.redhat.devtools.lsp4ij.LanguageServerDefinition;

public class SuperSQLServerDefinition extends LanguageServerDefinition {

    public SuperSQLServerDefinition() {
        super("SuperSQL Language Server", "supersql-lsp");
    }
}
```

### Step 4: Update plugin.xml

Add LSP4IJ extensions:

```xml
<!-- LSP4IJ dependency -->
<depends>com.redhat.devtools.lsp4ij</depends>

<extensions defaultExtensionNs="com.redhat.devtools.lsp4ij">
    <!-- Language Server -->
    <server id="supersql"
            name="SuperSQL Language Server"
            factoryClass="org.clabs.superdb.lsp.SuperSQLLanguageServerFactory">
        <description><![CDATA[
            SuperSQL language server providing code intelligence for .spq files.
        ]]></description>
    </server>

    <!-- Language Mapping -->
    <languageMapping language="SuperSQL" serverId="supersql"/>
</extensions>
```

### Step 5: Configure Server Capabilities

Create `src/main/java/org/clabs/superdb/lsp/SuperSQLLanguageClient.java` if custom client behavior is needed.

### Step 6: Add Fallback Logic (Hybrid Mode)

The native syntax highlighting should continue to work even if LSP fails.

In `SuperSQLParserDefinition.java` or create a new class:
- Check if LSP is available
- Log if LSP connection fails
- Native features remain functional

### Step 7: Update Tests

Add LSP-related tests:
- Test that plugin works without LSP (fallback mode)
- Test LSP initialization (mock server)

### Step 8: Update Documentation

1. Update `EDITOR_TOOLING_SPEC.md` to reflect LSP integration
2. Add setup instructions for users to install `super-lsp`

### Step 9: Bundle LSP Binary (Optional)

If bundling the binary:

1. Create `src/main/resources/bin/` directory
2. Add platform-specific binaries:
   - `bin/super-lsp-linux`
   - `bin/super-lsp-macos`
   - `bin/super-lsp-windows.exe`
3. Add extraction logic on plugin load
4. Update `SuperSQLLanguageServerFactory` to use bundled binary

## Output

After adding LSP support, verify:

1. Plugin builds successfully with LSP4IJ dependency
2. Native features still work without LSP running
3. LSP features work when server is available
4. Graceful degradation on LSP errors
