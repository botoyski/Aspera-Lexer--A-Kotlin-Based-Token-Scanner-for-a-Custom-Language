class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            skipWhitespace()
            start = current
            if (isAtEnd()) break
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean = current >= source.length

    private fun skipWhitespace() {
        while (!isAtEnd()) {
            val c = peek()
            when (c) {
                ' ', '\t', '\r' -> advance()
                '\n' -> {
                    line++
                    advance()
                }
                else -> return
            }
        }
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '[' -> addToken(TokenType.LEFT_BRACKET)
            ']' -> addToken(TokenType.RIGHT_BRACKET)

            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)

            '/' -> {
                if (match('/')) {
                    while (!isAtEnd() && peek() != '\n') advance()
                } else {
                    addToken(TokenType.SLASH)
                }
            }

            ' ', '\r', '\t' -> {}
            '\n' -> { line++ }

            '"' -> string()

            else -> {
                if (c.isDigit()) number()
                else word()
            }
        }
    }


    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            // unterminated string; for lab you could throw an error
            return
        }

        // closing "
        advance()

        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun identifier() {
        while (peek().isLetterOrDigit() || peek() == '_') advance()

        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun peek(): Char =
        if (isAtEnd()) '\u0000' else source[current]

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun number() {
        while (!isAtEnd() && peek().isDigit()) advance()
        val text = source.substring(start, current)
        val value = text.toDouble()
        tokens.add(Token(TokenType.NUMBER, text, value, line))
    }

    private fun word() {
        // read a word (up to whitespace, comma, or '=')
        while (!isAtEnd()
            && !peek().isWhitespace()
            && peek() != ','
            && peek() != '='
            && peek() != ';'
            && peek() != '('
            && peek() != ')'
            && peek() != '{'
            && peek() != '}'
            && peek() != '['
            && peek() != ']') {
            advance()
        }
        val raw = source.substring(start, current)
        val text = raw.trim()

        // Special case: merge "Magic" + "Affinity:" into "Magic Affinity:"
        if (text == "Magic") {
            // look ahead on same line
            skipSpacesSameLine()
            val nextStart = current
            while (!isAtEnd() && !peek().isWhitespace() && peek() != ',' && peek() != '=') {
                advance()
            }
            val nextRaw = source.substring(nextStart, current)
            val nextText = nextRaw.trim()
            if (nextText == "Affinity:") {
                val combined = "Magic Affinity:"
                tokens.add(Token(TokenType.MAGIC_AFFINITY_KW, combined, null, line))
                return
            } else {
                // just treat "Magic" as identifier and rewind to nextText start
                current = nextStart
                tokens.add(Token(TokenType.IDENTIFIER, text, null, line))
                return
            }
        }

        // Special case: if this word is "Alignment:", next non-empty sequence on this line
        // should be the full alignment string (can contain a space).
        if (text == "Alignment:") {
            tokens.add(Token(TokenType.ALIGNMENT_KW, text, null, line))
            skipSpacesSameLine()
            val alignStart = current
            while (!isAtEnd() && peek() != '\n' && peek() != '\r') {
                advance()
            }
            val alignText = source.substring(alignStart, current).trim()
            if (alignText.isNotEmpty()) {
                tokens.add(Token(TokenType.ALIGNMENT_TYPE, alignText, null, line))
            }
            return
        }

        val type = when (text) {
            "Character:" -> TokenType.CHARACTER_KW
            "Race:" -> TokenType.RACE_KW
            "Class:" -> TokenType.CLASS_KW
            "Background:" -> TokenType.BACKGROUND_KW
            "Attributes:" -> TokenType.ATTRIBUTES_KW
            "Skills:" -> TokenType.SKILLS_KW
            "Equipment:" -> TokenType.EQUIPMENT_KW
            "Magic" -> null
            "Affinity:" -> null  // do NOT emit a separate token
            "Magic Affinity:" -> TokenType.MAGIC_AFFINITY_KW

            "STR" -> TokenType.STR_KW
            "DEX" -> TokenType.DEX_KW
            "INT" -> TokenType.INT_KW
            "WIS" -> TokenType.WIS_KW
            "CHA" -> TokenType.CHA_KW
            "END" -> TokenType.END_KW

            "Weapon" -> TokenType.WEAPON_LABEL
            "Armor" -> TokenType.ARMOR_LABEL
            "Accessory" -> TokenType.ACCESSORY_LABEL

            "Orc", "Human", "Elf", "Fairy", "Spirit", "Demihuman", "Angel", "Demon", "Dwarf" ->
                TokenType.RACE_TYPE

            "Warrior", "Knight", "Mage", "Thief", "Ranger", "Paladin", "Barbarian",
            "Monk", "Druid", "Sorcerer", "Warlock" ->
                TokenType.CLASS_TYPE

            "Noble", "Commoner", "Outcast", "Mercenary",
            "Acolyte", "Hermit", "Scholar", "Hunter", "Nomad" ->
                TokenType.BACKGROUND_TYPE

            "Tracking", "Alchemy", "Blacksmithing", "Stealth", "Healing",
            "TwoHanded", "Archery", "ElementalMagic", "Cooking" ->
                TokenType.SKILL

            "Sword", "Axe", "Dagger", "Bow", "Spear", "Staff" ->
                TokenType.WEAPON_VALUE

            "Leather", "Plate", "Robe", "Chainmail" ->
                TokenType.ARMOR_VALUE

            "Ring", "Amulet", "Charm", "Rune" ->
                TokenType.ACCESSORY_VALUE

            "Fire", "Water", "Earth", "Air", "Light", "Dark",
            "Nature", "Arcane", "None" ->
                TokenType.MAGIC_TYPE

            // scripting keywords:
            "var" -> TokenType.VAR
            "val" -> TokenType.VAL
            "if" -> TokenType.IF
            "else" -> TokenType.ELSE
            "while" -> TokenType.WHILE
            "for" -> TokenType.FOR
            "fun" -> TokenType.FUN
            "return" -> TokenType.RETURN
            "print" -> TokenType.PRINT
            "true" -> TokenType.TRUE
            "false" -> TokenType.FALSE
            "nil" -> TokenType.NIL
            "and" -> TokenType.AND
            "or" -> TokenType.OR
            "elif" -> TokenType.ELIF

            else -> null
        }

        if (type != null) {
            tokens.add(Token(type, text, null, line))
        } else {
            // variable and function names
            addToken(TokenType.IDENTIFIER)
        }
    }

    private fun skipSpacesSameLine() {
        while (!isAtEnd() && (peek() == ' ' || peek() == '\t')) {
            advance()
        }
    }

    companion object {
        private val keywords = mapOf(
            "var" to TokenType.VAR,
            "print" to TokenType.PRINT,
            "true" to TokenType.TRUE,
            "false" to TokenType.FALSE,
            "nil" to TokenType.NIL
            // add more keywords if your language has them
        )
    }

}
