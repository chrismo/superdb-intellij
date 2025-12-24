package org.clabs.superdb.injection;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.clabs.superdb.SuperSQLLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.diagnostic.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comprehensive SuperDB language injector for BashSupport Pro.
 * <p>
 * Injects SuperSQL syntax highlighting and code intelligence into:
 * <ul>
 *   <li>Strings following "super -c" or "super --command" pattern</li>
 *   <li>Heredocs following "super -c" or "super --command" pattern</li>
 *   <li>Heredocs with recognized markers (SUPERSQL, SUPER, SPQ, ZQ, SUPERDB)</li>
 * </ul>
 * <p>
 * Examples:
 * <pre>
 * # Automatic injection (BashSupport Pro only):
 * super -c "from data.json | where status == 'active'"
 *
 * super -c &lt;&lt;EOF
 * from data.json
 * | where status == 'active'
 * EOF
 *
 * # Marker-based injection (no command needed):
 * cat &lt;&lt;SUPERSQL | super
 * from data.json
 * | count()
 * SUPERSQL
 * </pre>
 * <p>
 * Note: This class uses reflection to avoid compile-time dependencies on BashSupport Pro,
 * which is a paid plugin. The injector only activates when BashSupport Pro is installed.
 * <p>
 * For the built-in Shell plugin, users should use comment-based injection:
 * {@code # language=SuperDB}
 */
public class SuperSQLBashProInjector implements MultiHostInjector {

    private static final Logger LOG = Logger.getInstance(SuperSQLBashProInjector.class);

    // BashSupport Pro class names
    private static final String BASH_STRING_CLASS = "com.ansorgit.plugins.bash.lang.psi.api.BashString";
    private static final String BASH_HEREDOC_CLASS = "com.ansorgit.plugins.bash.lang.psi.api.heredoc.BashHereDoc";
    private static final String BASH_COMMAND_CLASS = "com.ansorgit.plugins.bash.lang.psi.api.command.BashCommand";
    private static final String BASH_HEREDOC_START_MARKER_CLASS = "com.ansorgit.plugins.bash.lang.psi.api.heredoc.BashHereDocStartMarker";
    private static final String BASH_HEREDOC_END_MARKER_CLASS = "com.ansorgit.plugins.bash.lang.psi.api.heredoc.BashHereDocEndMarker";

    // Heredoc markers that trigger SuperSQL injection regardless of command
    private static final Set<String> SUPERSQL_HEREDOC_MARKERS = Set.of(
            "SUPERSQL", "SUPER", "SPQ", "ZQ", "SUPERDB",
            "supersql", "super", "spq", "zq", "superdb"
    );

    // Pattern to detect super command in text before heredoc
    private static final Pattern SUPER_HEREDOC_PATTERN = Pattern.compile(
            "super\\s+(?:-c|--command)\\s*<<-?['\"]?\\w*['\"]?\\s*$"
    );

    // Reflection caches
    private Class<?> bashStringClass;
    private Class<?> bashHeredocClass;
    private Class<?> bashCommandClass;
    private Class<?> bashHeredocStartMarkerClass;
    private Class<?> bashHeredocEndMarkerClass;
    private boolean initialized = false;
    private boolean available = false;

    /**
     * Lazily initialize reflection classes using BashSupport Pro's classloader.
     */
    private synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            // Get BashSupport Pro's classloader - required due to plugin classloader isolation
            IdeaPluginDescriptor bashProPlugin = PluginManagerCore.getPlugin(PluginId.getId("pro.bashsupport"));
            if (bashProPlugin == null) {
                available = false;
                LOG.info("SuperSQLBashProInjector: BashSupport Pro plugin not found");
                return;
            }

            ClassLoader bashProClassLoader = bashProPlugin.getClassLoader();
            if (bashProClassLoader == null) {
                available = false;
                LOG.info("SuperSQLBashProInjector: BashSupport Pro classloader not available");
                return;
            }

            bashStringClass = Class.forName(BASH_STRING_CLASS, true, bashProClassLoader);
            bashHeredocClass = Class.forName(BASH_HEREDOC_CLASS, true, bashProClassLoader);
            bashCommandClass = Class.forName(BASH_COMMAND_CLASS, true, bashProClassLoader);
            // These might not exist in all versions, so we catch individually
            try {
                bashHeredocStartMarkerClass = Class.forName(BASH_HEREDOC_START_MARKER_CLASS, true, bashProClassLoader);
            } catch (ClassNotFoundException ignored) {}
            try {
                bashHeredocEndMarkerClass = Class.forName(BASH_HEREDOC_END_MARKER_CLASS, true, bashProClassLoader);
            } catch (ClassNotFoundException ignored) {}
            available = true;
            LOG.info("SuperSQLBashProInjector initialized successfully. BashString=" + bashStringClass +
                    ", BashHereDoc=" + bashHeredocClass + ", BashCommand=" + bashCommandClass);
        } catch (ClassNotFoundException e) {
            // BashSupport Pro classes not found
            available = false;
            LOG.info("SuperSQLBashProInjector: BashSupport Pro classes not available - " + e.getMessage());
        }
    }

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar,
                                     @NotNull PsiElement context) {
        ensureInitialized();
        if (!available) {
            return;
        }

        // Check if it's a string literal
        if (bashStringClass != null && bashStringClass.isInstance(context)) {
            boolean isQuery = isSuperDbQueryString(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug("SuperSQLBashProInjector: BashString found, text='" +
                        truncate(context.getText(), 50) + "', isSuperDbQuery=" + isQuery);
            }
            if (isQuery) {
                injectIntoString(registrar, context);
                LOG.debug("SuperSQLBashProInjector: Injected SuperSQL into string");
            }
            return;
        }

        // Check if it's a heredoc
        if (bashHeredocClass != null && bashHeredocClass.isInstance(context)) {
            boolean isQuery = isSuperDbHeredoc(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug("SuperSQLBashProInjector: BashHereDoc found, text='" +
                        truncate(context.getText(), 50) + "', isSuperDbQuery=" + isQuery);
            }
            if (isQuery) {
                injectIntoHeredoc(registrar, context);
                LOG.debug("SuperSQLBashProInjector: Injected SuperSQL into heredoc");
            }
        }
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "<null>";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }

    @Override
    public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        ensureInitialized();
        if (!available) {
            return Collections.emptyList();
        }

        List<Class<? extends PsiElement>> classes = new ArrayList<>();

        if (bashStringClass != null) {
            @SuppressWarnings("unchecked")
            Class<? extends PsiElement> stringClazz = (Class<? extends PsiElement>) bashStringClass;
            classes.add(stringClazz);
        }

        if (bashHeredocClass != null) {
            @SuppressWarnings("unchecked")
            Class<? extends PsiElement> heredocClazz = (Class<? extends PsiElement>) bashHeredocClass;
            classes.add(heredocClazz);
        }

        return classes;
    }

    // ========== String Injection ==========

    /**
     * Checks if the given string literal is a SuperDB query argument.
     * Looks for patterns like: super -c "..." or super --command "..."
     */
    private boolean isSuperDbQueryString(PsiElement bashString) {
        // Try PSI-based detection first
        if (isSuperDbQueryViaPsi(bashString)) {
            return true;
        }

        // Fallback: text-based detection
        return isSuperDbQueryViaText(bashString);
    }

    /**
     * PSI-based detection of super -c pattern for strings.
     */
    private boolean isSuperDbQueryViaPsi(PsiElement bashString) {
        // Walk up to find the containing BashCommand
        PsiElement parent = bashString.getParent();
        while (parent != null && !bashCommandClass.isInstance(parent)) {
            parent = parent.getParent();
        }

        if (parent == null) {
            return false;
        }

        // Get the command name via reflection
        String commandName = getReferencedCommandName(parent);
        if (!"super".equals(commandName)) {
            return false;
        }

        // Get all parameters and find our string's position
        List<PsiElement> params = getParameters(parent);
        if (params == null || params.size() < 2) {
            return false;
        }

        // Find the position of our string in the parameters
        int stringIndex = -1;
        for (int i = 0; i < params.size(); i++) {
            if (isOrContains(params.get(i), bashString)) {
                stringIndex = i;
                break;
            }
        }

        if (stringIndex < 1) {
            return false; // Need at least one param before the string (-c)
        }

        // Check if the previous parameter is -c or --command
        String prevText = params.get(stringIndex - 1).getText();
        return "-c".equals(prevText) || "--command".equals(prevText);
    }

    /**
     * Text-based fallback detection of super -c pattern for strings.
     */
    private boolean isSuperDbQueryViaText(PsiElement bashString) {
        PsiElement file = bashString.getContainingFile();
        if (file == null) {
            return false;
        }

        String fileText = file.getText();
        int stringStart = bashString.getTextOffset();

        // Get text before the string (up to 50 chars should be enough)
        int lookbackStart = Math.max(0, stringStart - 50);
        String precedingText = fileText.substring(lookbackStart, stringStart).trim();

        // Check for "super -c" or "super --command" pattern
        return precedingText.endsWith("super -c") ||
               precedingText.endsWith("super --command");
    }

    /**
     * Inject SuperSQL into a string element.
     */
    private void injectIntoString(MultiHostRegistrar registrar, PsiElement bashString) {
        TextRange innerRange = getInnerStringRange(bashString);
        if (innerRange == null || innerRange.isEmpty()) {
            return;
        }

        registrar.startInjecting(SuperSQLLanguage.INSTANCE)
                .addPlace(null, null, (PsiLanguageInjectionHost) bashString, innerRange)
                .doneInjecting();
    }

    // ========== Heredoc Injection ==========

    /**
     * Checks if the given heredoc should have SuperSQL injected.
     * Returns true if:
     * - The heredoc follows a "super -c" or "super --command" pattern
     * - The heredoc marker is a recognized SuperSQL marker (SUPERSQL, SUPER, SPQ, etc.)
     */
    private boolean isSuperDbHeredoc(PsiElement heredoc) {
        // First check if the heredoc is a valid injection host
        if (!isValidHeredocHost(heredoc)) {
            return false;
        }

        // Check for marker-based detection (highest priority - works without command)
        String markerName = getHeredocMarkerName(heredoc);
        if (markerName != null && SUPERSQL_HEREDOC_MARKERS.contains(markerName)) {
            return true;
        }

        // Check for command-based detection
        return isSuperDbHeredocViaCommand(heredoc);
    }

    /**
     * Check if heredoc follows a super -c command.
     */
    private boolean isSuperDbHeredocViaCommand(PsiElement heredoc) {
        // Try PSI-based detection
        if (isSuperDbHeredocViaPsi(heredoc)) {
            return true;
        }

        // Fallback: text-based detection
        return isSuperDbHeredocViaText(heredoc);
    }

    /**
     * PSI-based detection for heredocs following super -c.
     */
    private boolean isSuperDbHeredocViaPsi(PsiElement heredoc) {
        // Walk up to find the containing BashCommand
        PsiElement parent = heredoc.getParent();
        while (parent != null && !bashCommandClass.isInstance(parent)) {
            parent = parent.getParent();
        }

        if (parent == null) {
            return false;
        }

        // Get the command name
        String commandName = getReferencedCommandName(parent);
        if (!"super".equals(commandName)) {
            return false;
        }

        // Check for -c or --command parameter
        List<PsiElement> params = getParameters(parent);
        if (params == null) {
            return false;
        }

        for (PsiElement param : params) {
            String text = param.getText();
            if ("-c".equals(text) || "--command".equals(text)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Text-based detection for heredocs following super -c.
     */
    private boolean isSuperDbHeredocViaText(PsiElement heredoc) {
        PsiElement file = heredoc.getContainingFile();
        if (file == null) {
            return false;
        }

        String fileText = file.getText();
        int heredocStart = heredoc.getTextOffset();

        // Look back for super -c <<MARKER pattern (up to 100 chars)
        int lookbackStart = Math.max(0, heredocStart - 100);
        String precedingText = fileText.substring(lookbackStart, heredocStart);

        Matcher matcher = SUPER_HEREDOC_PATTERN.matcher(precedingText);
        return matcher.find();
    }

    /**
     * Gets the heredoc marker name (e.g., "EOF", "SUPERSQL").
     */
    @Nullable
    private String getHeredocMarkerName(PsiElement heredoc) {
        // Try to find the start marker
        PsiElement startMarker = findHeredocStartMarker(heredoc);
        if (startMarker != null) {
            String markerText = startMarker.getText();
            // Strip quotes if present: "EOF" -> EOF, 'EOF' -> EOF
            if (markerText.length() >= 2) {
                char first = markerText.charAt(0);
                char last = markerText.charAt(markerText.length() - 1);
                if ((first == '"' || first == '\'') && first == last) {
                    markerText = markerText.substring(1, markerText.length() - 1);
                }
            }
            return markerText;
        }

        // Fallback: try to get from end marker (usually last sibling)
        PsiElement endMarker = findHeredocEndMarker(heredoc);
        if (endMarker != null) {
            return endMarker.getText().trim();
        }

        return null;
    }

    /**
     * Find the start marker element for a heredoc.
     */
    @Nullable
    private PsiElement findHeredocStartMarker(PsiElement heredoc) {
        // Check previous siblings
        PsiElement sibling = heredoc.getPrevSibling();
        while (sibling != null) {
            if (bashHeredocStartMarkerClass != null && bashHeredocStartMarkerClass.isInstance(sibling)) {
                return sibling;
            }
            // Also check children of siblings
            for (PsiElement child : sibling.getChildren()) {
                if (bashHeredocStartMarkerClass != null && bashHeredocStartMarkerClass.isInstance(child)) {
                    return child;
                }
            }
            sibling = sibling.getPrevSibling();
        }
        return null;
    }

    /**
     * Find the end marker element for a heredoc.
     */
    @Nullable
    private PsiElement findHeredocEndMarker(PsiElement heredoc) {
        // Check next siblings
        PsiElement sibling = heredoc.getNextSibling();
        while (sibling != null) {
            if (bashHeredocEndMarkerClass != null && bashHeredocEndMarkerClass.isInstance(sibling)) {
                return sibling;
            }
            sibling = sibling.getNextSibling();
        }
        return null;
    }

    /**
     * Check if heredoc is a valid injection host.
     * BashSupport Pro only supports injection in heredocs that don't evaluate variables
     * (i.e., quoted markers like <<"EOF").
     */
    private boolean isValidHeredocHost(PsiElement heredoc) {
        // First check if it implements PsiLanguageInjectionHost
        if (!(heredoc instanceof PsiLanguageInjectionHost)) {
            return false;
        }

        // Check isValidHost() via reflection
        try {
            Method isValidHostMethod = heredoc.getClass().getMethod("isValidHost");
            Boolean result = (Boolean) isValidHostMethod.invoke(heredoc);
            return result != null && result;
        } catch (Exception e) {
            // If we can't determine, try to check isEvaluatingVariables
            try {
                Method method = bashHeredocClass.getMethod("isEvaluatingVariables");
                Boolean isEvaluating = (Boolean) method.invoke(heredoc);
                // Valid host if NOT evaluating variables
                return isEvaluating != null && !isEvaluating;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    /**
     * Inject SuperSQL into a heredoc element.
     */
    private void injectIntoHeredoc(MultiHostRegistrar registrar, PsiElement heredoc) {
        // Get the full text range of the heredoc content
        String text = heredoc.getText();
        if (text == null || text.isEmpty()) {
            return;
        }

        // The entire heredoc content is injectable (no quotes to strip)
        TextRange range = new TextRange(0, text.length());

        registrar.startInjecting(SuperSQLLanguage.INSTANCE)
                .addPlace(null, null, (PsiLanguageInjectionHost) heredoc, range)
                .doneInjecting();
    }

    // ========== Utility Methods ==========

    /**
     * Gets the command name via reflection.
     */
    @Nullable
    private String getReferencedCommandName(PsiElement command) {
        try {
            Method method = bashCommandClass.getMethod("getReferencedCommandName");
            return (String) method.invoke(command);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets parameters via reflection.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private List<PsiElement> getParameters(PsiElement command) {
        try {
            Method method = bashCommandClass.getMethod("parameters");
            return (List<PsiElement>) method.invoke(command);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if element is the target or contains it.
     */
    private boolean isOrContains(PsiElement element, PsiElement target) {
        if (element == target) {
            return true;
        }
        for (PsiElement child : element.getChildren()) {
            if (isOrContains(child, target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the text range of the string content, excluding the quotes.
     */
    @Nullable
    private TextRange getInnerStringRange(PsiElement bashString) {
        String text = bashString.getText();
        if (text == null || text.length() < 2) {
            return null;
        }

        // Determine quote type and strip accordingly
        char firstChar = text.charAt(0);
        char lastChar = text.charAt(text.length() - 1);

        int start = 0;
        int end = text.length();

        // Handle different string types
        if (firstChar == '"' || firstChar == '\'') {
            start = 1;
            if (lastChar == firstChar) {
                end = text.length() - 1;
            }
        } else if (text.startsWith("$'")) {
            // ANSI-C quoting: $'...'
            start = 2;
            if (lastChar == '\'') {
                end = text.length() - 1;
            }
        } else if (text.startsWith("$\"")) {
            // Localized string: $"..."
            start = 2;
            if (lastChar == '"') {
                end = text.length() - 1;
            }
        }

        if (start >= end) {
            return null;
        }

        return new TextRange(start, end);
    }
}
