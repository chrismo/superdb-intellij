package org.clabs.superdb;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class SuperSQLColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Keyword", SuperSQLSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Operator keyword", SuperSQLSyntaxHighlighter.OPERATOR_KEYWORD),
            new AttributesDescriptor("Type", SuperSQLSyntaxHighlighter.TYPE_KEYWORD),
            new AttributesDescriptor("String", SuperSQLSyntaxHighlighter.STRING),
            new AttributesDescriptor("Number", SuperSQLSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("Line comment", SuperSQLSyntaxHighlighter.LINE_COMMENT),
            new AttributesDescriptor("Block comment", SuperSQLSyntaxHighlighter.BLOCK_COMMENT),
            new AttributesDescriptor("Identifier", SuperSQLSyntaxHighlighter.IDENTIFIER),
            new AttributesDescriptor("Function call", SuperSQLSyntaxHighlighter.FUNCTION_CALL),
            new AttributesDescriptor("Operator", SuperSQLSyntaxHighlighter.OPERATION_SIGN),
            new AttributesDescriptor("Parentheses", SuperSQLSyntaxHighlighter.PARENTHESES),
            new AttributesDescriptor("Brackets", SuperSQLSyntaxHighlighter.BRACKETS),
            new AttributesDescriptor("Braces", SuperSQLSyntaxHighlighter.BRACES),
            new AttributesDescriptor("Comma", SuperSQLSyntaxHighlighter.COMMA),
            new AttributesDescriptor("Semicolon", SuperSQLSyntaxHighlighter.SEMICOLON),
            new AttributesDescriptor("Dot", SuperSQLSyntaxHighlighter.DOT),
            new AttributesDescriptor("Regex", SuperSQLSyntaxHighlighter.REGEX),
            new AttributesDescriptor("Constant", SuperSQLSyntaxHighlighter.CONSTANT),
            new AttributesDescriptor("Bad character", SuperSQLSyntaxHighlighter.BAD_CHARACTER),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return SuperSQLIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new SuperSQLSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return """
-- SuperSQL Query Example
/* This is a block comment
   spanning multiple lines */

const MAX_RESULTS = 100
fn double(x): x * 2

FROM 'https://data.example.com/events.json'
| WHERE timestamp >= 2024-01-01T00:00:00Z
| SELECT
    user_id,
    event_type,
    count(*) AS event_count
| GROUP BY user_id, event_type
| HAVING event_count > 10
| ORDER BY event_count DESC
| LIMIT MAX_RESULTS

-- Pipe operators example
from data.log
| search /error|warning/
| sort -r timestamp
| head 50
| put severity := CASE
    WHEN message ~ /error/ THEN "high"
    WHEN message ~ /warning/ THEN "medium"
    ELSE "low"
  END
| fork (
    count() by severity
  ) (
    distinct message
  )

-- Type expressions
type UserRecord = {
    id: int64,
    name: string,
    email: string,
    score: float64 | null,
    tags: [string],
    metadata: |{string: string}|
}

-- Aggregation with built-in functions
summarize
    total := <func>sum</func>(amount),
    avg_amount := <func>avg</func>(amount),
    user_count := count(distinct user_id)
by region, category
with -limit 1000

-- Built-in functions
<func>len</func>(name)
<func>lower</func>(email)
<func>coalesce</func>(value, 0)
<func>typeof</func>(data)
<func>cidr_match</func>(ip, 10.0.0.0/8)
<func>split</func>(path, "/")
<func>trim</func>(input)

-- Special literals
192.168.1.0/24
10.0.0.1
::1
2001:db8::1/32
0x1a2b3c
1.5h
30s
true
false
null
NaN
+Inf
""";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return Map.of("func", SuperSQLSyntaxHighlighter.FUNCTION_CALL);
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "SuperSQL";
    }
}
