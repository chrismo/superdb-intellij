package org.clabs.superdb.injection;

import com.intellij.lang.injection.general.Injection;
import com.intellij.lang.injection.general.LanguageInjectionContributor;
import com.intellij.lang.injection.general.SimpleInjection;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.clabs.superdb.SuperSQLLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-level language injection contributor for SuperDB/SuperSQL.
 * <p>
 * This contributor provides automatic injection of SuperSQL into shell script strings
 * that follow the "super -c" or "super --command" pattern, with any options before.
 * <p>
 * Unlike MultiHostInjector, this approach works through IntelliLang's higher-level API
 * and doesn't require knowing the exact PSI class names (which are obfuscated in BashSupport Pro).
 */
public class SuperSQLInjectionContributor implements LanguageInjectionContributor {

    private static final Logger LOG = Logger.getInstance(SuperSQLInjectionContributor.class);
    private static final String BASHPRO_LANGUAGE_ID = "BashSupport Pro Shell Script";

    // Pattern to match "super" followed by any options, ending with "-c" or "--command"
    // Examples: "super -c", "super -s -c", "super -f line -c", "super --command"
    // Uses non-greedy match to find the last -c/--command before the string
    private static final Pattern SUPER_COMMAND_PATTERN = Pattern.compile(
            "\\bsuper\\s+[^\\n]*?(-c|--command)\\s*$"
    );

    // Pattern for heredocs: super [options] -c <<["']?MARKER["']?\n
    // Matches heredocs following super -c command
    private static final Pattern SUPER_HEREDOC_PATTERN = Pattern.compile(
            "\\bsuper\\s+[^\\n]*?(-c|--command)\\s*<<[\"']?\\w+[\"']?\\s*$",
            Pattern.MULTILINE
    );

    // Pattern for marker-based heredocs (inject regardless of command)
    // Matches: <<["']?(SUPERSQL|SUPER|SPQ|ZQ|SUPERDB)["']?
    private static final Pattern MARKER_HEREDOC_PATTERN = Pattern.compile(
            "<<[\"']?(SUPERSQL|SUPER|SPQ|ZQ|SUPERDB)[\"']?\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    @Override
    public @Nullable Injection getInjection(@NotNull PsiElement context) {
        // Only process injection host elements
        if (!(context instanceof PsiLanguageInjectionHost)) {
            return null;
        }

        // Check if we're in a BashSupport Pro file
        PsiFile file = context.getContainingFile();
        if (file == null) {
            return null;
        }

        String languageId = file.getLanguage().getID();
        if (!BASHPRO_LANGUAGE_ID.equals(languageId) && !languageId.contains("Bash") && !languageId.contains("Shell")) {
            return null;
        }

        // Check if this is a super -c query
        if (isSuperDbQuery(context)) {
            LOG.debug("SuperSQLInjectionContributor: Injecting SuperSQL into: " + truncate(context.getText(), 50));
            return new SimpleInjection(SuperSQLLanguage.INSTANCE, "", "", null);
        }

        return null;
    }

    /**
     * Checks if the given element is a SuperDB query argument.
     * Supports:
     * - String arguments: super -c "...", super -s -c "...", super --command "..."
     * - Heredocs after super -c: super -c <<"EOF" ... EOF
     * - Marker-based heredocs: <<SUPERSQL, <<SUPER, <<SPQ, <<ZQ, <<SUPERDB
     */
    private boolean isSuperDbQuery(PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file == null) {
            return false;
        }

        String fileText = file.getText();
        int elementStart = element.getTextOffset();

        // Get text before the element (up to 150 chars to allow for heredoc markers and options)
        int lookbackStart = Math.max(0, elementStart - 150);
        String precedingText = fileText.substring(lookbackStart, elementStart);

        // Check for string argument: super [options] -c "..."
        if (SUPER_COMMAND_PATTERN.matcher(precedingText).find()) {
            LOG.debug("Matched SUPER_COMMAND_PATTERN");
            return true;
        }

        // Check for heredoc after super -c: super [options] -c <<"EOF"\n
        if (SUPER_HEREDOC_PATTERN.matcher(precedingText).find()) {
            LOG.debug("Matched SUPER_HEREDOC_PATTERN");
            return true;
        }

        // Check for marker-based heredocs: <<"SUPERSQL", <<"SUPER", etc.
        if (MARKER_HEREDOC_PATTERN.matcher(precedingText).find()) {
            LOG.debug("Matched MARKER_HEREDOC_PATTERN");
            return true;
        }

        return false;
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "<null>";
        s = s.replace("\n", "\\n");
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
