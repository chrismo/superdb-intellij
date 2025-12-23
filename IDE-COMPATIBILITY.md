# IDE Compatibility & Plugin Maintenance

This document explains how to keep the SuperDB plugin working across JetBrains IDE versions.

## Version Constraints

The plugin uses `sinceBuild` without `untilBuild` in `build.gradle.kts`:

```kotlin
patchPluginXml {
    sinceBuild.set("241")
    // No untilBuild - allow all future IDE versions
}
```

This means the plugin will load on any IDE from 2024.1 onwards, indefinitely.

## When Recompilation Is NOT Needed

Most of the time, nothing breaks. JetBrains maintains backward compatibility for stable APIs across:

- Patch versions (2024.1.1 → 2024.1.7)
- Minor versions (2024.1 → 2024.2 → 2024.3)
- Often even major versions (2024.x → 2025.x)

Your existing plugin build will continue to load and work as long as the APIs it uses still exist.

## When Recompilation IS Needed

1. **API Deprecation/Removal** - JetBrains deprecates APIs, then removes them ~2 major versions later
2. **Extension Point Changes** - Changes to plugin.xml schema or required attributes
3. **Platform Changes** - Major internal refactors (rare)

## Plugin Verifier

The plugin verifier checks compatibility against multiple IDE versions. It's configured in `build.gradle.kts`:

```kotlin
runPluginVerifier {
    ideVersions.set(listOf("2024.1", "2024.2", "2024.3", "2025.1", "2025.2", "2025.3"))
}
```

Run locally:
```bash
./gradlew runPluginVerifier
```

This downloads each IDE version and validates the plugin against it, reporting:
- Deprecated API usage (warnings)
- Removed API usage (errors)
- Binary compatibility issues

## CI Integration

The verifier runs automatically in CI (`.github/workflows/ci.yml`):

- **verify job**: Runs `runPluginVerifier` on every push/PR
- **compatibility job**: Tests against multiple IDE versions in a matrix

## Maintenance Workflow

### Reactive (Minimum Effort)

1. Do nothing until users report issues with a new IDE version
2. When issues arise: fix the specific API changes, bump version, release

### Proactive (Recommended)

1. When a new major IDE version releases (e.g., 2026.1), add it to:
   - `runPluginVerifier.ideVersions` in `build.gradle.kts`
   - `compatibility.matrix.ide-version` in `.github/workflows/ci.yml`
2. Run the verifier locally or check CI results
3. If it passes, no action needed
4. If it fails, fix deprecated/removed API usage before users hit issues

### Staying Ahead

- Subscribe to [JetBrains Platform Updates](https://blog.jetbrains.com/platform/)
- Check [IntelliJ Platform SDK docs](https://plugins.jetbrains.com/docs/intellij/api-changes-list.html) for API changes
- Run verifier against EAP/RC builds before major releases

## This Plugin's Risk Profile

| Dependency | Risk | Notes |
|------------|------|-------|
| Grammar-Kit generated code | Low | Stable, rarely changes |
| LSP4IJ plugin | Medium | Third-party, monitor their releases |
| BashSupport Pro reflection | Low | Graceful degradation if classes renamed |
| Core IntelliJ APIs | Low | Using stable, well-established APIs |

## Quick Reference

```bash
# Check compatibility locally
./gradlew runPluginVerifier

# Build distributable
./build.sh dist

# Full verification
./gradlew buildPlugin verifyPlugin runPluginVerifier
```
