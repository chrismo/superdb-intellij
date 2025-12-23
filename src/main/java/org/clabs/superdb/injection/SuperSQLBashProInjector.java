package org.clabs.superdb.injection;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.clabs.superdb.SuperSQLLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Injects SuperDB language into BashSupport Pro strings following "super -c" pattern.
 * <p>
 * This injector provides syntax highlighting, code completion, and other language
 * features for SuperDB queries embedded in shell scripts like:
 * <pre>
 * super -c "from data.json | where status == 'active'"
 * </pre>
 * <p>
 * Note: This class uses reflection to avoid compile-time dependencies on BashSupport Pro,
 * which is a paid plugin. The injector will only activate when BashSupport Pro is installed.
 * <p>
 * For the built-in Shell plugin, users should use comment-based injection:
 * {@code # language=SuperDB}
 */
public class SuperSQLBashProInjector implements MultiHostInjector {

    private static final String BASH_STRING_CLASS = "com.ansorgit.plugins.bash.lang.psi.api.BashString";
    private static final String BASH_COMMAND_CLASS = "com.ansorgit.plugins.bash.lang.psi.api.command.BashCommand";

    private Class<?> bashStringClass;
    private Class<?> bashCommandClass;
    private boolean initialized = false;
    private boolean available = false;

    /**
     * Lazily initialize reflection classes.
     */
    private synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            bashStringClass = Class.forName(BASH_STRING_CLASS);
            bashCommandClass = Class.forName(BASH_COMMAND_CLASS);
            available = true;
        } catch (ClassNotFoundException e) {
            // BashSupport Pro not available
            available = false;
        }
    }

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar,
                                     @NotNull PsiElement context) {
        ensureInitialized();
        if (!available || bashStringClass == null) {
            return;
        }

        if (!bashStringClass.isInstance(context)) {
            return;
        }

        // Check if this string is a SuperDB query (follows "super -c" or "super --command")
        if (!isSuperDbQuery(context)) {
            return;
        }

        // Get the text range excluding the quotes
        TextRange innerRange = getInnerStringRange(context);
        if (innerRange == null || innerRange.isEmpty()) {
            return;
        }

        // Inject SuperSQL language
        registrar.startInjecting(SuperSQLLanguage.INSTANCE)
                .addPlace(null, null, (PsiLanguageInjectionHost) context, innerRange)
                .doneInjecting();
    }

    @Override
    public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        ensureInitialized();
        if (!available || bashStringClass == null) {
            return Collections.emptyList();
        }
        @SuppressWarnings("unchecked")
        Class<? extends PsiElement> clazz = (Class<? extends PsiElement>) bashStringClass;
        return List.of(clazz);
    }

    /**
     * Checks if the given string literal is a SuperDB query argument.
     * Looks for patterns like: super -c "..." or super --command "..."
     */
    private boolean isSuperDbQuery(PsiElement bashString) {
        // Try PSI-based detection first
        if (isSuperDbQueryViaPsi(bashString)) {
            return true;
        }

        // Fallback: text-based detection
        return isSuperDbQueryViaText(bashString);
    }

    /**
     * PSI-based detection of super -c pattern.
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
     * Text-based fallback detection of super -c pattern.
     * Looks at the text preceding the string to find "super -c" or "super --command".
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
