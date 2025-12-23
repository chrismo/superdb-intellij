package org.clabs.superdb;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.clabs.superdb.psi.SuperSQLTypes.*;

%%

%{
  public SuperSQLLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class SuperSQLLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

// Whitespace
EOL=\R
WHITE_SPACE=\s+
LINE_WS=[\ \t\f]

// Comments
LINE_COMMENT="--"[^\r\n]*
BLOCK_COMMENT="/*"([^*]|\*+[^*/])*\*+"/"

// Numbers
DIGIT=[0-9]
HEX_DIGIT=[0-9a-fA-F]
INT={DIGIT}+
FLOAT=({DIGIT}+"."{DIGIT}*|"."{DIGIT}+)([eE][+-]?{DIGIT}+)?
HEX="0x"{HEX_DIGIT}+
NAN="NaN"
INF=[+-]?"Inf"

// Identifiers
LETTER=[a-zA-Z_$]
ID_CHAR={LETTER}|{DIGIT}
IDENTIFIER={LETTER}{ID_CHAR}*

// Strings
DOUBLE_QUOTED_STRING=\"([^\"\\\n\r]|\\.)*\"
SINGLE_QUOTED_STRING='([^'\\\n\r]|\\.)*'
BACKTICK_STRING=`([^`\\]|\\.)*`
RAW_STRING_SINGLE=r'[^']*'
RAW_STRING_DOUBLE=r\"[^\"]*\"
FSTRING_DOUBLE=f\"([^\"\\\n\r{]|\\.|"{"[^}]*"}")*\"
FSTRING_SINGLE=f'([^'\\\n\r{]|\\.|"{"[^}]*"}")*'

// Regex and Glob
REGEX="/"[^/\n\r]+"/"

// Time and Duration
TIME_UNIT=(ns|us|ms|s|m|h|d|w|y)
DURATION="-"?({DIGIT}+("."{DIGIT}+)?{TIME_UNIT})+
DATE={DIGIT}{4}"-"{DIGIT}{2}"-"{DIGIT}{2}
TIME_PART={DIGIT}{2}":"{DIGIT}{2}":"{DIGIT}{2}("."{DIGIT}+)?
TIME_OFFSET=("Z"|[+-]{DIGIT}{2}":"{DIGIT}{2}("."{DIGIT}+)?)
TIMESTAMP={DATE}"T"{TIME_PART}{TIME_OFFSET}

// IP Addresses
IP4={DIGIT}+"."{DIGIT}+"."{DIGIT}+"."{DIGIT}+
IP4_NET={IP4}"/"{DIGIT}+
IP6_PART={HEX_DIGIT}+
// IPv6: require at least one colon-separated pair, or :: with at least one colon-part following
// This prevents ::f from matching (would conflict with :: cast operator + float64)
IP6_FULL=({IP6_PART}":")+{IP6_PART}
IP6_COMPRESSED_START="::"{IP6_PART}(":"{IP6_PART})+
IP6_COMPRESSED_END=({IP6_PART}":")+"::"({IP6_PART}(":"{IP6_PART})*)?
IP6_LOOPBACK="::1"
IP6={IP6_FULL}|{IP6_COMPRESSED_START}|{IP6_COMPRESSED_END}|{IP6_LOOPBACK}
IP6_NET={IP6}"/"{DIGIT}+

%state STRING_STATE

%%

<YYINITIAL> {
  {WHITE_SPACE}                       { return WHITE_SPACE; }
  {LINE_COMMENT}                      { return LINE_COMMENT; }
  {BLOCK_COMMENT}                     { return BLOCK_COMMENT; }

  // Operators and Punctuation
  "|>"                                { return PIPE_ARROW; }
  "|"                                 { return PIPE; }
  "||"                                { return CONCAT; }
  "::"                                { return CAST_OP; }
  ":="                                { return ASSIGN; }
  "..."                               { return SPREAD; }
  "=="                                { return EQ; }
  "!="                                { return NEQ; }
  "<>"                                { return NEQ; }
  "<="                                { return LE; }
  ">="                                { return GE; }
  "<"                                 { return LT; }
  ">"                                 { return GT; }
  "~"                                 { return MATCH; }
  "+"                                 { return PLUS; }
  "-"                                 { return MINUS; }
  "*"                                 { return STAR; }
  "/"                                 { return SLASH; }
  "%"                                 { return PERCENT; }
  "!"                                 { return BANG; }
  "?"                                 { return QUESTION; }
  ":"                                 { return COLON; }
  ";"                                 { return SEMICOLON; }
  ","                                 { return COMMA; }
  "."                                 { return DOT; }
  "@"                                 { return AT; }
  "&"                                 { return AMP; }
  "="                                 { return EQUALS; }
  "("                                 { return LPAREN; }
  ")"                                 { return RPAREN; }
  "["                                 { return LBRACKET; }
  "]"                                 { return RBRACKET; }
  "{"                                 { return LBRACE; }
  "}"                                 { return RBRACE; }
  "|["                                { return SET_LBRACKET; }
  "]|"                                { return SET_RBRACKET; }
  "|{"                                { return MAP_LBRACE; }
  "}|"                                { return MAP_RBRACE; }

  // SQL Keywords (case insensitive)
  [Ss][Ee][Ll][Ee][Cc][Tt]           { return SELECT; }
  [Ff][Rr][Oo][Mm]                   { return FROM; }
  [Ww][Hh][Ee][Rr][Ee]               { return WHERE; }
  [Gg][Rr][Oo][Uu][Pp]               { return GROUP; }
  [Bb][Yy]                           { return BY; }
  [Hh][Aa][Vv][Ii][Nn][Gg]           { return HAVING; }
  [Oo][Rr][Dd][Ee][Rr]               { return ORDER; }
  [Ll][Ii][Mm][Ii][Tt]               { return LIMIT; }
  [Oo][Ff][Ff][Ss][Ee][Tt]           { return OFFSET; }
  [Uu][Nn][Ii][Oo][Nn]               { return UNION; }
  [Aa][Ll][Ll]                       { return ALL; }
  [Dd][Ii][Ss][Tt][Ii][Nn][Cc][Tt]   { return DISTINCT; }
  [Aa][Ss]                           { return AS; }
  [Aa][Tt]                           { return AT_KW; }
  [Oo][Nn]                           { return ON; }
  [Jj][Oo][Ii][Nn]                   { return JOIN; }
  [Uu][Ss][Ii][Nn][Gg]               { return USING; }
  [Ll][Ee][Ff][Tt]                   { return LEFT; }
  [Rr][Ii][Gg][Hh][Tt]               { return RIGHT; }
  [Ii][Nn][Nn][Ee][Rr]               { return INNER; }
  [Oo][Uu][Tt][Ee][Rr]               { return OUTER; }
  [Ff][Uu][Ll][Ll]                   { return FULL; }
  [Cc][Rr][Oo][Ss][Ss]               { return CROSS; }
  [Aa][Nn][Tt][Ii]                   { return ANTI; }
  [Ww][Ii][Tt][Hh]                   { return WITH; }
  [Rr][Ee][Cc][Uu][Rr][Ss][Ii][Vv][Ee] { return RECURSIVE; }
  [Mm][Aa][Tt][Ee][Rr][Ii][Aa][Ll][Ii][Zz][Ee][Dd] { return MATERIALIZED; }
  [Oo][Rr][Dd][Ii][Nn][Aa][Ll][Ii][Tt][Yy] { return ORDINALITY; }
  [Vv][Aa][Ll][Uu][Ee]               { return VALUE; }
  [Vv][Aa][Ll][Uu][Ee][Ss]           { return VALUES; }
  [Cc][Aa][Ss][Ee]                   { return CASE; }
  [Ww][Hh][Ee][Nn]                   { return WHEN; }
  [Tt][Hh][Ee][Nn]                   { return THEN; }
  [Ee][Ll][Ss][Ee]                   { return ELSE; }
  [Ee][Nn][Dd]                       { return END; }
  [Cc][Aa][Ss][Tt]                   { return CAST; }
  [Ee][Xx][Tt][Rr][Aa][Cc][Tt]       { return EXTRACT; }
  [Ss][Uu][Bb][Ss][Tt][Rr][Ii][Nn][Gg] { return SUBSTRING; }
  [Pp][Oo][Ss][Ii][Tt][Ii][Oo][Nn]   { return POSITION; }
  [Dd][Aa][Tt][Ee]                   { return DATE_KW; }
  [Tt][Ii][Mm][Ee][Ss][Tt][Aa][Mm][Pp] { return TIMESTAMP_KW; }
  [Ii][Nn][Tt][Ee][Rr][Vv][Aa][Ll]   { return INTERVAL; }
  [Ff][Oo][Rr]                       { return FOR; }
  [Ee][Xx][Ii][Ss][Tt][Ss]           { return EXISTS; }
  [Bb][Ee][Tt][Ww][Ee][Ee][Nn]       { return BETWEEN; }
  [Ll][Ii][Kk][Ee]                   { return LIKE; }
  [Ii][Nn]                           { return IN; }
  [Ii][Ss]                           { return IS; }
  [Aa][Ss][Cc]                       { return ASC; }
  [Dd][Ee][Ss][Cc]                   { return DESC; }
  [Nn][Uu][Ll][Ll][Ss]               { return NULLS; }
  [Ff][Ii][Rr][Ss][Tt]               { return FIRST; }
  [Ll][Aa][Ss][Tt]                   { return LAST; }

  // Boolean and Logic Keywords
  [Aa][Nn][Dd]                       { return AND; }
  [Oo][Rr]                           { return OR; }
  [Nn][Oo][Tt]                       { return NOT; }
  [Tt][Rr][Uu][Ee]                   { return TRUE; }
  [Ff][Aa][Ll][Ss][Ee]               { return FALSE; }
  [Nn][Uu][Ll][Ll]                   { return NULL; }

  // SuperSQL Pipe Operators
  [Ff][Oo][Rr][Kk]                   { return FORK; }
  [Ss][Ww][Ii][Tt][Cc][Hh]           { return SWITCH; }
  [Ss][Ee][Aa][Rr][Cc][Hh]           { return SEARCH; }
  [Aa][Ss][Ss][Ee][Rr][Tt]           { return ASSERT; }
  [Ss][Oo][Rr][Tt]                   { return SORT; }
  [Tt][Oo][Pp]                       { return TOP; }
  [Cc][Uu][Tt]                       { return CUT; }
  [Dd][Rr][Oo][Pp]                   { return DROP; }
  [Hh][Ee][Aa][Dd]                   { return HEAD; }
  [Tt][Aa][Ii][Ll]                   { return TAIL; }
  [Ss][Kk][Ii][Pp]                   { return SKIP_KW; }
  [Uu][Nn][Ii][Qq]                   { return UNIQ; }
  [Pp][Uu][Tt]                       { return PUT; }
  [Rr][Ee][Nn][Aa][Mm][Ee]           { return RENAME; }
  [Ff][Uu][Ss][Ee]                   { return FUSE; }
  [Ss][Hh][Aa][Pp][Ee][Ss]           { return SHAPES; }
  [Ss][Hh][Aa][Pp][Ee]               { return SHAPE; }
  [Pp][Aa][Ss][Ss]                   { return PASS; }
  [Ee][Xx][Pp][Ll][Oo][Dd][Ee]       { return EXPLODE; }
  [Mm][Ee][Rr][Gg][Ee]               { return MERGE; }
  [Uu][Nn][Nn][Ee][Ss][Tt]           { return UNNEST; }
  [Ll][Oo][Aa][Dd]                   { return LOAD; }
  [Oo][Uu][Tt][Pp][Uu][Tt]           { return OUTPUT; }
  [Dd][Ee][Bb][Uu][Gg]               { return DEBUG; }
  [Cc][Oo][Uu][Nn][Tt]               { return COUNT; }
  [Cc][Aa][Ll][Ll]                   { return CALL; }

  // Declaration Keywords
  [Cc][Oo][Nn][Ss][Tt]               { return CONST; }
  [Ff][Nn]                           { return FN; }
  [Ll][Ee][Tt]                       { return LET; }
  [Ll][Aa][Mm][Bb][Dd][Aa]           { return LAMBDA; }
  [Oo][Pp]                           { return OP; }
  [Pp][Rr][Aa][Gg][Mm][Aa]           { return PRAGMA; }
  [Tt][Yy][Pp][Ee]                   { return TYPE_KW; }

  // Aggregation Keywords
  [Aa][Gg][Gg][Rr][Ee][Gg][Aa][Tt][Ee] { return AGGREGATE; }
  [Ss][Uu][Mm][Mm][Aa][Rr][Ii][Zz][Ee] { return SUMMARIZE; }

  // Switch Keywords
  [Dd][Ee][Ff][Aa][Uu][Ll][Tt]       { return DEFAULT; }

  // Type Keywords
  [Ee][Rr][Rr][Oo][Rr]               { return ERROR; }
  [Ee][Nn][Uu][Mm]                   { return ENUM; }
  [Mm][Aa][Pp]                       { return MAP_KW; }

  // Primitives Types
  "uint8"                            { return UINT8; }
  "uint16"                           { return UINT16; }
  "uint32"                           { return UINT32; }
  "uint64"                           { return UINT64; }
  "int8"                             { return INT8; }
  "int16"                            { return INT16; }
  "int32"                            { return INT32; }
  "int64"                            { return INT64; }
  "float16"                          { return FLOAT16; }
  "float32"                          { return FLOAT32; }
  "float64"                          { return FLOAT64; }
  "bool"                             { return BOOL; }
  "string"                           { return STRING_TYPE; }
  "duration"                         { return DURATION_TYPE; }
  "time"                             { return TIME_TYPE; }
  "bytes"                            { return BYTES_TYPE; }
  "ip"                               { return IP_TYPE; }
  "net"                              { return NET_TYPE; }

  // PostgreSQL Aliases (case insensitive)
  // Note: Compound types like "double precision" and "character varying" are not
  // supported as keywords because they conflict with valid identifiers.
  // Only single-word PostgreSQL type keywords are supported here.
  [Bb][Ii][Gg][Ii][Nn][Tt]           { return BIGINT; }
  [Bb][Oo][Oo][Ll][Ee][Aa][Nn]       { return BOOLEAN; }
  [Bb][Yy][Tt][Ee][Aa]               { return BYTEA; }
  [Cc][Hh][Aa][Rr]                   { return CHAR; }
  [Cc][Ii][Dd][Rr]                   { return CIDR; }
  [Ii][Nn][Tt][Ee][Gg][Ee][Rr]       { return INTEGER; }
  [Ii][Nn][Ee][Tt]                   { return INET; }
  [Rr][Ee][Aa][Ll]                   { return REAL; }
  [Ss][Mm][Aa][Ll][Ll][Ii][Nn][Tt]   { return SMALLINT; }
  [Tt][Ee][Xx][Tt]                   { return TEXT; }
  [Vv][Aa][Rr][Cc][Hh][Aa][Rr]       { return VARCHAR; }

  // Literals
  {NAN}                              { return NAN_LIT; }
  {INF}                              { return INF_LIT; }
  {TIMESTAMP}                        { return TIMESTAMP_LIT; }
  {DURATION}                         { return DURATION_LIT; }
  {IP6_NET}                          { return IP6_NET_LIT; }
  {IP4_NET}                          { return IP4_NET_LIT; }
  {IP6}                              { return IP6_LIT; }
  {IP4}                              { return IP4_LIT; }
  {HEX}                              { return HEX_LIT; }
  {FLOAT}                            { return FLOAT_LIT; }
  {INT}                              { return INT_LIT; }

  // Strings
  {FSTRING_DOUBLE}                   { return FSTRING; }
  {FSTRING_SINGLE}                   { return FSTRING; }
  {RAW_STRING_SINGLE}                { return RAW_STRING; }
  {RAW_STRING_DOUBLE}                { return RAW_STRING; }
  {DOUBLE_QUOTED_STRING}             { return DOUBLE_QUOTED_STRING; }
  {SINGLE_QUOTED_STRING}             { return SINGLE_QUOTED_STRING; }
  {BACKTICK_STRING}                  { return BACKTICK_STRING; }
  {REGEX}                            { return REGEX; }

  // Identifier (must come after keywords)
  {IDENTIFIER}                       { return IDENTIFIER; }
}

[^]                                  { return BAD_CHARACTER; }
