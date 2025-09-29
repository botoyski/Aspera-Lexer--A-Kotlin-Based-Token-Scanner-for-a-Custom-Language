enum class TokenType {
    //single character tokens
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    //one or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    //literals
    IDENTIFIER, STRING, NUMBER,

    //keywords
    VAR, IF, ELSE, TRUE, FALSE, NIL, FOR, WHILE, PRINT, RETURN,

    EOF
}

data class Token(
    val type: TokenType,
    val text: String,
    val value: Any?,
    val line: Int
)

class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    //map to store all keywords
    private val keywords = mapOf(
        "var" to TokenType.VAR,
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "true" to TokenType.TRUE,
        "false" to TokenType.FALSE,
        "nil" to TokenType.NIL,
        "for" to TokenType.FOR,
        "while" to TokenType.WHILE,
        "print" to TokenType.PRINT,
        "return" to TokenType.RETURN
    )

    fun scanTokens(): List<Token> {
        //keep scanning until we reach the end
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        //add eof token at the end
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
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

            //two character tokens
            '!' -> {
                if (match('=')) {
                    addToken(TokenType.BANG_EQUAL)
                } else {
                    addToken(TokenType.BANG)
                }
            }
            '=' -> {
                if (match('=')) {
                    addToken(TokenType.EQUAL_EQUAL)
                } else {
                    addToken(TokenType.EQUAL)
                }
            }
            '<' -> {
                if (match('=')) {
                    addToken(TokenType.LESS_EQUAL)
                } else {
                    addToken(TokenType.LESS)
                }
            }
            '>' -> {
                if (match('=')) {
                    addToken(TokenType.GREATER_EQUAL)
                } else {
                    addToken(TokenType.GREATER)
                }
            }

            //handle division and comments
            '/' -> {
                if (match('/')) {
                    //single line comment - ignore until end of line
                    while (peek() != '\n' && !isAtEnd()) {
                        advance()
                    }
                } else if (match('*')) {
                    //block comment - ignore until */
                    handleBlockComment()
                } else {
                    addToken(TokenType.SLASH)
                }
            }

            //ignore whitespace
            ' ' -> {}
            '\r' -> {}
            '\t' -> {}

            //new line
            '\n' -> line++

            //string literals
            '"' -> handleString()

            //everything else
            else -> {
                if (isDigit(c)) {
                    handleNumber()
                } else if (isAlpha(c)) {
                    handleIdentifier()
                } else {
                    println("Error on line $line: Unexpected character '$c'")
                }
            }
        }
    }

    private fun handleBlockComment() {
        //keep going until we find */
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance() //consume *
                advance() //consume /
                return
            }
            if (peek() == '\n') {
                line++
            }
            advance()
        }
        println("Error on line $line: Unterminated block comment")
    }

    private fun handleIdentifier() {
        //keep consuming while we see letters, digits, or underscore
        while (isAlphaNumeric(peek())) {
            advance()
        }

        val text = source.substring(start, current)

        //check if it's a keyword
        val type = keywords[text]
        if (type != null) {
            addToken(type)
        } else {
            addToken(TokenType.IDENTIFIER)
        }
    }

    private fun handleNumber() {
        //consume all digits
        while (isDigit(peek())) {
            advance()
        }

        //check for decimal point
        if (peek() == '.' && isDigit(peekNext())) {
            advance() //consume the .

            //consume digits after decimal
            while (isDigit(peek())) {
                advance()
            }
        }

        val text = source.substring(start, current)
        val numberValue = text.toDouble()
        addToken(TokenType.NUMBER, numberValue)
    }

    private fun handleString() {
        //keep consuming until we hit closing quote or end of file
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
            }
            advance()
        }

        if (isAtEnd()) {
            println("Error on line $line: Unterminated string")
            return
        }

        advance() //consume closing "

        //get string value (without quotes)
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    //helper functions
    private fun advance(): Char {
        val c = source[current]
        current++
        return c
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isAlpha(c: Char): Boolean {
        return (c in 'a'..'z') || (c in 'A'..'Z') || c == '_'
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }
}

fun main() {
    println("Welcome sahhh, ito ang coding language para sa mga real ones")
    println("Ilagay ang input mo sa ibaba G!(Ctrl+D or Ctrl+C para mag bounce)")
    println()

    while (true) {
        print("> ")
        val input = readln()

        if (input == null) {
            println("\nBounce nako par matsalove...")
            break
        }

        if (input.isEmpty()) {
            continue
        }

        //create scanner and get tokens
        val scanner = Scanner(input)
        val tokens = scanner.scanTokens()

        //print all tokens
        println("Tatagos ba tong mga tokens mo sahh:")
        for (token in tokens) {
            println("  ${token.type} | '${token.text}' | ${token.value ?: "null"}")
        }
        println()
    }
}
