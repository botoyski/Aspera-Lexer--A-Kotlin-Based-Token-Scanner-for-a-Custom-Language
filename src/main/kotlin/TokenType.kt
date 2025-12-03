enum class TokenType {
    // section headers
    CHARACTER_KW,       // "Character:"
    RACE_KW,            // "Race:"
    CLASS_KW,           // "Class:"
    BACKGROUND_KW,      // "Background:"
    ATTRIBUTES_KW,      // "Attributes:"
    SKILLS_KW,          // "Skills:"
    EQUIPMENT_KW,       // "Equipment:"
    ALIGNMENT_KW,       // "Alignment:"
    MAGIC_AFFINITY_KW,  // "Magic Affinity:"

    // attribute keywords
    STR_KW,
    DEX_KW,
    INT_KW,
    WIS_KW,
    CHA_KW,
    END_KW,

    // equipment labels
    WEAPON_LABEL,
    ARMOR_LABEL,
    ACCESSORY_LABEL,

    // categories for values
    RACE_TYPE,
    CLASS_TYPE,
    BACKGROUND_TYPE,
    SKILL,
    WEAPON_VALUE,
    ARMOR_VALUE,
    ACCESSORY_VALUE,
    ALIGNMENT_TYPE,
    MAGIC_TYPE,

    // single-char tokens used by scripting
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // two-char operators
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // literals
    IDENTIFIER, STRING, NUMBER,

    // keywords
    AND, OR,
    IF, ELSE,
    TRUE, FALSE, NIL,
    VAR, VAL,
    WHILE, FOR,
    FUN,
    RETURN,
    PRINT,

    EOF
}