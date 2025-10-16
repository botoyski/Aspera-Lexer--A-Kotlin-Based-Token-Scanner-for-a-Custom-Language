class Scanner(private val source: String) { // input program text to scan
    private val tokens = mutableListOf<Token>() // list of all recognized tokens
    private var start = 0 // index where the current token starts
    private var current = 0 // index of the current character being scanned
    private var line = 1   // keeps track of the current line number (for errors)

    // Entry point: scans the whole source into a list of tokens
    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current // mark beginning of next token
            scanToken()     // scan one token
        }
        // add end-of-file token at the end
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    // Recognizes a single token based on the current character
    private fun scanToken() {
        val c = advance() // consume current character

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

            // Handle operators that may be one- or two-character tokens
            '!' -> if (match('=')) addToken(TokenType.BANG_EQUAL) else addToken(TokenType.BANG)
            '=' -> if (match('=')) addToken(TokenType.EQUAL_EQUAL) else addToken(TokenType.EQUAL)
            '<' -> if (match('=')) addToken(TokenType.LESS_EQUAL) else addToken(TokenType.LESS)
            '>' -> if (match('=')) addToken(TokenType.GREATER_EQUAL) else addToken(TokenType.GREATER)

            // Slash could be division or start of a comment
            '/' -> when {
                match('/') -> while (peek() != '\n' && !isAtEnd()) advance() // single-line comment
                match('*') -> handleBlockComment() // multi-line comment /* ... */
                else -> addToken(TokenType.SLASH)  // just division
            }

            // Ignore whitespace
            ' ', '\r', '\t' -> {}
            '\n' -> line++ // new line

            // String literal
            '"' -> handleString()

            // Numbers, identifiers, or errors
            else -> when {
                isDigit(c) -> handleNumber()
                isAlpha(c) -> handleIdentifier()
                else -> println("Error on line $line: Unexpected character '$c'")
            }
        }
    }

    // Handles block comments /* ... */
    private fun handleBlockComment() {
        while (!isAtEnd()) {
            // stop when we find closing */
            if (peek() == '*' && peekNext() == '/') {
                advance(); advance() // consume both characters
                return
            }
            // count newlines inside block comments
            if (peek() == '\n') line++
            advance()
        }
        // if we get here, no closing */ was found
        println("Error on line $line: Unterminated block comment")
    }

    // Handles identifiers and keywords
    private fun handleIdentifier() {
        // keep consuming letters, digits, underscores
        while (isAlphaNumeric(peek())) advance()

        val text = source.substring(start, current) // extract identifier
        val type = keywords[text] // check if it's a reserved keyword
        if (type != null) addToken(type) else addToken(TokenType.IDENTIFIER)
    }

    // Handles number literals (integers or decimals)
    private fun handleNumber() {
        while (isDigit(peek())) advance()

        // check for decimal part
        if (peek() == '.' && isDigit(peekNext())) {
            advance() // consume '.'
            while (isDigit(peek())) advance()
        }

        val text = source.substring(start, current)
        val numberValue = text.toDouble() // store as Double
        addToken(TokenType.NUMBER, numberValue)
    }

    // Handles string literals
    private fun handleString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++ // support multiline strings
            advance()
        }
        if (isAtEnd()) {
            println("Error on line $line: Unterminated string")
            return
        }
        advance() // closing quote
        val value = source.substring(start + 1, current - 1) // strip quotes
        addToken(TokenType.STRING, value)
    }

    // ------------------------
    // Helper functions
    // ------------------------

    // consume current character and return it
    private fun advance(): Char = source[current++]

    // add token with optional literal value
    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    // conditionally consume character if it matches expected
    private fun match(expected: Char): Boolean {
        if (isAtEnd() || source[current] != expected) return false
        current++
        return true
    }

    // look at current character without consuming it
    private fun peek(): Char = if (isAtEnd()) '\u0000' else source[current]

    // look one character ahead
    private fun peekNext(): Char =
        if (current + 1 >= source.length) '\u0000' else source[current + 1]

    // check if at end of input
    private fun isAtEnd(): Boolean = current >= source.length

    // check if character is a digit
    private fun isDigit(c: Char): Boolean = c in '0'..'9'

    // check if character is a letter or underscore
    private fun isAlpha(c: Char): Boolean = c in 'a'..'z' || c in 'A'..'Z' || c == '_'

    // check if character is letter, digit, or underscore
    private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)
}
