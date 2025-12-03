class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): Stmt.Program {
        val characters = mutableListOf<Stmt.Character>()
        while (!isAtEnd()) {
            characters += parseCharacter()   // or characters.add(parseCharacter())
        }
        return Stmt.Program(characters)
    }


    private fun parseCharacter(): Stmt.Character {
        consume(TokenType.CHARACTER_KW, "Expect 'Character:' at start.")
        val race = parseRace()
        val clazz = parseOptionalClass()
        val background = parseOptionalBackground()
        val attributes = parseAttributes(clazz)
        val skills = parseSkills()
        val (weapon, armor, accessory) = parseEquipment()
        val alignment = parseAlignment()
        val magic = parseMagic()
        return Stmt.Character(race, clazz, background, attributes, skills, weapon, armor, accessory, alignment, magic)
    }

    private fun parseStatement(): Stmt {
        return when {
            match(TokenType.IF_KW) -> parseIfStatement()
            match(TokenType.WHILE_KW) -> parseWhileStatement()
            match(TokenType.LEFT_BRACE) -> parseBlock()
            else -> parseExpressionStatement()
        }
    }

    private fun parseIfStatement(): Stmt.If {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = parseStatement()
        val elseBranch = if (match(TokenType.ELSE_KW)) parseStatement() else null
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun parseWhileStatement(): Stmt.While {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = parseStatement()
        return Stmt.While(condition, body)
    }

    private fun parseBlock(): Stmt.Block {
        val statements = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(parseStatement())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return Stmt.Block(statements)
    }

    private fun parseExpressionStatement(): Stmt.Expression {
        val expr = expression()
        return Stmt.Expression(expr)
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

    private fun parseAttributes(clazz: String?): Map<String, Int> {
        consume(TokenType.ATTRIBUTES_KW, "Expect 'Attributes:'.")
        return randomAttributes(clazz)
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
        consume(TokenType.WEAPON_LABEL, "Expect 'Weapon'.")
        consume(TokenType.EQUAL, "Expect '=' after Weapon.")
        val weaponTok = consume(TokenType.WEAPON_VALUE, "Expect weapon value.")
        consume(TokenType.ARMOR_LABEL, "Expect 'Armor'.")
        consume(TokenType.EQUAL, "Expect '=' after Armor.")
        val armorTok = consume(TokenType.ARMOR_VALUE, "Expect armor value.")
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

    // ===== EXPRESSION PARSING =====
    private fun expression(): Expr = equality()

    private fun equality(): Expr {
        var expr = comparison()
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            val op = previous()
            val right = comparison()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    private fun comparison(): Expr {
        var expr = term()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val op = previous()
            val right = term()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    private fun term(): Expr = factor()

    private fun factor(): Expr = primary()

    private fun primary(): Expr {
        if (match(TokenType.NUMBER)) {
            return Expr.Literal(previous().value)
        }
        if (match(TokenType.STR_KW, TokenType.DEX_KW, TokenType.INT_KW, TokenType.WIS_KW, TokenType.CHA_KW, TokenType.END_KW)) {
            return Expr.Variable(previous())
        }
        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }
        throw error(peek(), "Expect expression.")
    }

    // ===== HELPERS =====
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean = !isAtEnd() && peek().type == type

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

    // ===== RANDOM ATTRIBUTES =====
    private fun randomAttributes(clazz: String?): Map<String, Int> {
        val keys = listOf("STR", "DEX", "INT", "WIS", "CHA", "END")
        val min = 1
        val max = 20
        val total = 80

        val weights: Map<String, Int> = when (clazz?.lowercase()) {
            "ranger" -> mapOf("STR" to 8, "DEX" to 20, "INT" to 8, "WIS" to 16, "CHA" to 8, "END" to 12)
            "sorcerer", "mage" -> mapOf("STR" to 6, "DEX" to 8, "INT" to 20, "WIS" to 12, "CHA" to 16, "END" to 8)
            "warrior", "knight", "barbarian", "paladin" -> mapOf("STR" to 20, "DEX" to 12, "INT" to 6, "WIS" to 8, "CHA" to 8, "END" to 16)
            else -> mapOf("STR" to 10, "DEX" to 10, "INT" to 10, "WIS" to 10, "CHA" to 10, "END" to 10)
        }

        while (true) {
            val values = IntArray(keys.size) { min }
            var remaining = total - keys.size * min

            while (remaining > 0) {
                val idx = pickWeightedIndex(keys, weights, values, max)
                if (idx == -1) break
                values[idx]++
                remaining--
            }

            if (values.all { it in min..max } && values.sum() == total) {
                return keys.zip(values.asIterable()).toMap()
            }
        }
    }

    private fun pickWeightedIndex(keys: List<String>, weights: Map<String, Int>, values: IntArray, max: Int): Int {
        val candidates = keys.indices.filter { values[it] < max }
        if (candidates.isEmpty()) return -1

        val weightedList = mutableListOf<Int>()
        for (i in candidates) {
            val key = keys[i]
            val w = weights[key] ?: 1
            repeat(w) { weightedList.add(i) }
        }
        return weightedList.random()
    }

    // ===== STATEMENT PARSING =====
    private fun statement(): Stmt {
        return when {
            match(TokenType.FOR_KW) -> forStatement()
            match(TokenType.IF_KW) -> ifStatement()
            match(TokenType.WHILE_KW) -> whileStatement()
            match(TokenType.LEFT_BRACE) -> Stmt.Block(block())
            else -> expressionStatement()
        }
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        val elseBranch = if (match(TokenType.ELSE_KW)) statement() else null
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return Stmt.While(condition, body)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        return Stmt.Expression(expr)
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")

        // initializer
        val initializer: Stmt? = when {
            match(TokenType.SEMICOLON) -> null      // empty init: for (; ...
            else -> expressionStatement()           // we only have exprStmt for now
        }

        // condition
        val condition =
            if (!check(TokenType.SEMICOLON)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        // increment
        val increment =
            if (!check(TokenType.RIGHT_PAREN)) expression() else null
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        // desugar: body = { body; increment; }
        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        val condExpr = condition ?: Expr.Literal(true)
        val whileStmt = Stmt.While(condExpr, body)

        // if we have initializer, wrap it in a block: { initializer; while (...) { ... } }
        return if (initializer != null) {
            Stmt.Block(listOf(initializer, whileStmt))
        } else {
            whileStmt
        }
    }


}
