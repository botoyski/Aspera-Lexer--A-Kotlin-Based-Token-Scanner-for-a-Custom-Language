enum class TokenType {
    // punctuation
    COMMA,
    EQUAL,

    // literals
    NUMBER,

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

    EOF

}