class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): Stmt.Program {
        val character = parseCharacter()
        return Stmt.Program(character)
    }

    private fun parseCharacter(): Stmt.Character {
        consume(TokenType.CHARACTER_KW, "Expect 'Character:' at start.")

        val race = parseRace()
        val clazz = parseOptionalClass()
        val background = parseOptionalBackground()
        val attributes = parseAttributes()
        val skills = parseSkills()
        val (weapon, armor, accessory) = parseEquipment()
        val alignment = parseAlignment()
        val magic = parseMagic()

        return Stmt.Character(
            race = race,
            clazz = clazz,
            background = background,
            attributes = attributes,
            skills = skills,
            weapon = weapon,
            armor = armor,
            accessory = accessory,
            alignment = alignment,
            magicAffinity = magic
        )
    }

    private fun parseRace(): String {
        consume(TokenType.RACE_KW, "Expect 'Race:'.")
        val raceToken = consume(TokenType.RACE_TYPE, "Expect race type.")
        return raceToken.text
    }

    private fun parseOptionalClass(): String? {
        if (match(TokenType.CLASS_KW)) {
            val classToken = consume(TokenType.CLASS_TYPE, "Expect class type.")
            return classToken.text
        }
        return null
    }

    private fun parseOptionalBackground(): String? {
        if (match(TokenType.BACKGROUND_KW)) {
            val bgToken = consume(TokenType.BACKGROUND_TYPE, "Expect background type.")
            return bgToken.text
        }
        return null
    }

    private fun parseAttributes(): Map<String, Int> {
        consume(TokenType.ATTRIBUTES_KW, "Expect 'Attributes:'.")
        val result = mutableMapOf<String, Int>()
        parseAttribute(result)
        while (match(TokenType.COMMA)) {
            parseAttribute(result)
        }
        return result
    }

    private fun parseAttribute(map: MutableMap<String, Int>) {
        val attrToken = when {
            match(TokenType.STR_KW) -> previous()
            match(TokenType.DEX_KW) -> previous()
            match(TokenType.INT_KW) -> previous()
            match(TokenType.WIS_KW) -> previous()
            match(TokenType.CHA_KW) -> previous()
            match(TokenType.END_KW) -> previous()
            else -> error(peek(), "Expect attribute name like STR, DEX, etc.")
        }
        consume(TokenType.EQUAL, "Expect '=' after attribute name.")
        val valueToken = consume(TokenType.NUMBER, "Expect number for attribute value.")
        val key = attrToken.text
        val value = valueToken.value as Int
        map[key] = value
    }

    private fun parseSkills(): List<String> {
        consume(TokenType.SKILLS_KW, "Expect 'Skills:'.")
        val skills = mutableListOf<String>()
        val first = consume(TokenType.SKILL, "Expect skill.")
        skills.add(first.text)
        while (match(TokenType.COMMA)) {
            val s = consume(TokenType.SKILL, "Expect skill.")
            skills.add(s.text)
        }
        return skills
    }

    private fun parseEquipment(): Triple<String, String, String> {
        consume(TokenType.EQUIPMENT_KW, "Expect 'Equipment:'.")
        // Weapon=WeaponValue
        consume(TokenType.WEAPON_LABEL, "Expect 'Weapon'.")
        consume(TokenType.EQUAL, "Expect '=' after Weapon.")
        val weaponTok = consume(TokenType.WEAPON_VALUE, "Expect weapon value.")
        // Armor=ArmorValue
        consume(TokenType.ARMOR_LABEL, "Expect 'Armor'.")
        consume(TokenType.EQUAL, "Expect '=' after Armor.")
        val armorTok = consume(TokenType.ARMOR_VALUE, "Expect armor value.")
        // Accessory=AccessoryValue
        consume(TokenType.ACCESSORY_LABEL, "Expect 'Accessory'.")
        consume(TokenType.EQUAL, "Expect '=' after Accessory.")
        val accessoryTok = consume(TokenType.ACCESSORY_VALUE, "Expect accessory value.")
        return Triple(weaponTok.text, armorTok.text, accessoryTok.text)
    }

    private fun parseAlignment(): String {
        consume(TokenType.ALIGNMENT_KW, "Expect 'Alignment:'.")
        val align = consume(TokenType.ALIGNMENT_TYPE, "Expect alignment value.")
        return align.text
    }

    private fun parseMagic(): String {
        consume(TokenType.MAGIC_AFFINITY_KW, "Expect 'Magic Affinity:'.")
        val magic = consume(TokenType.MAGIC_TYPE, "Expect magic affinity value.")
        return magic.text
    }

// helpers

    private fun match(type: TokenType): Boolean {
        if (check(type)) {
            advance()
            return true
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        error(peek(), message)
    }

    private fun check(type: TokenType): Boolean =
        !isAtEnd() && peek().type == type

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String): Nothing {
        throw RuntimeException("[line ${token.line}] Error at '${token.text}': $message")
    }


}
