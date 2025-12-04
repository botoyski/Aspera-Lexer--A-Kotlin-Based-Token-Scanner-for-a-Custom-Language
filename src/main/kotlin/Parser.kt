class Parser(private val tokens: List<Token>) {

    private var current = 0

    fun parse(): Stmt.Program {
        val characters = mutableListOf<Stmt.Character>()
        while (!isAtEnd()) {
            characters += parseCharacter()
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

    // ===== fully random, class‑biased attributes =====
    private fun parseAttributes(clazz: String?): Map<String, Int> {
        // Require "Attributes:" in the script
        consume(TokenType.ATTRIBUTES_KW, "Expect 'Attributes:'.")
        // Ignore any STR=..., DEX=... etc. and just randomize with class bias
        return randomAttributes(clazz)
    }

    // Unused now, but kept in case you later want manual attributes again
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

    // ===== helpers =====

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

    // ===== class‑biased random attribute generator =====

    private fun randomAttributes(clazz: String?): Map<String, Int> {
        val keys = listOf("STR", "DEX", "INT", "WIS", "CHA", "END")
        val min = 1
        val max = 20
        val total = 80

        // weights per attribute depending on class
        val weights: Map<String, Int> = when (clazz?.lowercase()) {
            "ranger" -> mapOf(
                "STR" to 8,
                "DEX" to 20,
                "INT" to 8,
                "WIS" to 16,
                "CHA" to 8,
                "END" to 12
            )
            "sorcerer", "mage" -> mapOf(
                "STR" to 6,
                "DEX" to 8,
                "INT" to 20,
                "WIS" to 12,
                "CHA" to 16,
                "END" to 8
            )
            "warrior", "knight", "barbarian", "paladin" -> mapOf(
                "STR" to 20,
                "DEX" to 12,
                "INT" to 6,
                "WIS" to 8,
                "CHA" to 8,
                "END" to 16
            )
            else -> mapOf( // default: roughly even
                "STR" to 10,
                "DEX" to 10,
                "INT" to 10,
                "WIS" to 10,
                "CHA" to 10,
                "END" to 10
            )
        }

        while (true) {
            // start with all at min
            val values = IntArray(keys.size) { min }
            var remaining = total - keys.size * min

            // distribute remaining points randomly, using weights
            while (remaining > 0) {
                val idx = pickWeightedIndex(keys, weights, values, max)
                if (idx == -1) break
                values[idx]++
                remaining--
            }

            // safety check
            if (values.all { it in min..max } && values.sum() == total) {
                return keys.zip(values.asIterable()).toMap()
            }
        }
    }

    private fun pickWeightedIndex(
        keys: List<String>,
        weights: Map<String, Int>,
        values: IntArray,
        max: Int
    ): Int {
        // only attributes below max are candidates
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

    fun parseScriptProgram(): List<StmtLang> {
        val statements = mutableListOf<StmtLang>()
        while (!isAtEnd()) {
            statements += declaration()
        }
        return statements
    }

    private fun declaration(): StmtLang {
        return when {
            match(TokenType.FUN) -> function()
            match(TokenType.VAR) -> varDecl()
            else -> statement()
        }
    }

    // ===== statements =====

    private fun statement(): StmtLang {
        return when {
            match(TokenType.PRINT) -> printStmt()
            match(TokenType.IF) -> ifStmt()
            match(TokenType.WHILE) -> whileStmt()
            match(TokenType.FOR) -> forStmt()
            match(TokenType.LEFT_BRACE) -> StmtLang.Block(block())
            match(TokenType.RETURN) -> returnStmt()
            else -> exprStmt()
        }
    }

    private fun printStmt(): StmtLang {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return StmtLang.Print(value)
    }

    private fun ifStmt(): StmtLang {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val thenBranch = statement()

        var elseBranch: StmtLang? = null
        if (match(TokenType.ELIF)) {
            elseBranch = parseElifChain()
        } else if (match(TokenType.ELSE)) {
            elseBranch = statement()
        }

        return StmtLang.If(condition, thenBranch, elseBranch)
    }

    private fun parseElifChain(): StmtLang {
        // we are right after having matched ELIF
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'elif'.")
        val elifCond = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val elifThen = statement()

        var elifElse: StmtLang? = null
        if (match(TokenType.ELIF)) {
            // another elif: recurse
            elifElse = parseElifChain()
        } else if (match(TokenType.ELSE)) {
            // final else
            elifElse = statement()
        }

        return StmtLang.If(elifCond, elifThen, elifElse)
    }

    private fun whileStmt(): StmtLang {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return StmtLang.While(condition, body)
    }

    private fun forStmt(): StmtLang {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")

        // Initializer
        val initializer: StmtLang? =
            if (match(TokenType.SEMICOLON)) {
                null
            } else if (match(TokenType.VAR)) {
                varDecl()
            } else {
                exprStmt()
            }

        // Condition
        val condition: Expr? =
            if (!check(TokenType.SEMICOLON)) {
                expression()
            } else {
                null
            }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        // Increment
        val increment: Expr? =
            if (!check(TokenType.RIGHT_PAREN)) {
                expression()
            } else {
                null
            }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        // Put increment last if there is a body
        if (increment != null) {
            body = StmtLang.Block(
                listOf(
                    body,
                    StmtLang.Expression(increment)
                )
            )
        }

        // If no condition, use true
        val whileCond = condition ?: Expr.Literal(true)

        // If there is an initializer, wrap everything in a block
        val whileStmt = StmtLang.While(whileCond, body)
        return if (initializer != null) {
            StmtLang.Block(listOf(initializer, whileStmt))
        } else {
            whileStmt
        }
    }

    private fun returnStmt(): StmtLang {
        val keyword = previous()
        var value: Expr? = null
        if (!check(TokenType.SEMICOLON)) {
            value = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after return value.")
        return StmtLang.Return(keyword, value)
    }

    private fun block(): List<StmtLang> {
        val statements = mutableListOf<StmtLang>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements += declaration()
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun exprStmt(): StmtLang {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return StmtLang.Expression(expr)
    }

    private fun varDecl(): StmtLang {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return StmtLang.Var(name, initializer)
    }

    private fun function(): StmtLang.Function {
        val name = consume(TokenType.IDENTIFIER, "Expect function name.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.")
        val parameters = mutableListOf<Token>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                parameters += consume(TokenType.IDENTIFIER, "Expect parameter name.")
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        consume(TokenType.LEFT_BRACE, "Expect '{' before function body.")
        val body = block()
        return StmtLang.Function(name, parameters, body)
    }

    // ===== expressions =====

    private fun expression(): Expr = assignment()

    private fun assignment(): Expr {
        val expr = or()
        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expr.Variable) {
                return Expr.Assign(expr.name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun or(): Expr {
        var expr = and()
        while (match(TokenType.OR)) {
            val op = previous()
            val right = and()
            expr = Expr.Logical(expr, op, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()
        while (match(TokenType.AND)) {
            val op = previous()
            val right = equality()
            expr = Expr.Logical(expr, op, right)
        }
        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
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

    private fun term(): Expr {
        var expr = factor()
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val op = previous()
            val right = factor()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = unary()
        while (match(TokenType.SLASH, TokenType.STAR)) {
            val op = previous()
            val right = unary()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val op = previous()
            val right = unary()
            return Expr.Unary(op, right)
        }
        return call()
    }

    private fun call(): Expr {
        var expr = primary()
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else if (match(TokenType.LEFT_BRACKET)) {
            expr = finishIndex(expr)
            } else {
                break
            }
        }
        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments += expression()
            } while (match(TokenType.COMMA))
        }
        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
        return Expr.Call(callee, paren, arguments)
    }

    private fun finishIndex(target: Expr): Expr {
        // we are after '['
        val indexExpr = expression()
        val bracket = consume(TokenType.RIGHT_BRACKET, "Expect ']' after index.")
        return Expr.Index(target, indexExpr, bracket)
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().value)
        }

        if (match(TokenType.IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        error(peek(), "Expect expression.")
    }

    // ===== helper overload for match with varargs =====

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

}
