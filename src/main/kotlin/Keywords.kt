// keyword table: reserved words mapped to their token types
val keywords = mapOf(
    "var" to TokenType.VAR,
    "if" to TokenType.IF,
    "else" to TokenType.ELSE,
    "true" to TokenType.TRUE,
    "false" to TokenType.FALSE,
    "nil" to TokenType.NIL,
    "for" to TokenType.FOR,
    "while" to TokenType.WHILE,
    "print" to TokenType.PRINT,
    "return" to TokenType.RETURN,
    "val" to TokenType.VAL,

    // new
    "then" to TokenType.THEN,     // you'll need to add THEN token
    "end" to TokenType.END,
    "while" to TokenType.WHILE_KW,
    "persists" to TokenType.PERSISTS,
    "and" to TokenType.AND,
    "or" to TokenType.OR,
    "name" to TokenType.NAME,

    // medieval
    "scroll" to TokenType.SCROLL,
    "seal_scroll" to TokenType.SEALSCROLL,
    "rune" to TokenType.RUNE_KW,
    "incantation" to TokenType.INCANTATION,
    "invoke" to TokenType.INVOKE,
    "whisper" to TokenType.WHISPER,
    "conjure" to TokenType.CONJURE,
    "release" to TokenType.RELEASE,
    "##" to TokenType.OMEN,
    "sigil" to TokenType.SIGIL,
    "end_sigil" to TokenType.ENDSIGIL,
    "with" to TokenType.WITH,
    "requires" to TokenType.REQUIRES,
    "enacts" to TokenType.ENACTS,
    "end_quest" to TokenType.ENDQUEST,

    // elemental literals as keywords OR as ELEMENT tokens recognized in scanner
    "flame" to TokenType.ELEMENT,
    "aqua" to TokenType.ELEMENT,
    "terra" to TokenType.ELEMENT,
    "aero" to TokenType.ELEMENT,
    "shadow" to TokenType.ELEMENT,
    "light" to TokenType.ELEMENT
)