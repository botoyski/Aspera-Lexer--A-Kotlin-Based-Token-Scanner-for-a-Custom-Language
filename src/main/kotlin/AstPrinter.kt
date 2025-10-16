// AstPrinter.kt — top-level [bracketed] groups with localized operator words
class AstPrinter {

    fun print(expr: Expr): String {
        // The very top just calls printInner — the brackets will be applied to top-level groups automatically
        return printInner(expr)
    }

    private fun printInner(expr: Expr): String = when (expr) {
        is Expr.Literal  -> literalToString(expr.value)
        is Expr.Grouping -> "(${printInner(expr.expression)})"
        is Expr.Unary    -> unaryToString(expr)
        is Expr.Binary   -> binaryToString(expr)
        is Expr.Chain    -> chainToString(expr)
    }

    // Convert binary expressions to readable form with parentheses for precedence
    private fun binaryToString(expr: Expr.Binary): String {
        val opPrec = getPrecedence(expr.operator)
        val leftStr = maybeParenthesize(expr.left, opPrec)
        val rightStr = maybeParenthesize(expr.right, opPrec)

        return when (expr.operator.text) {
            "+" -> "$leftStr plus $rightStr"
            "-" -> "$leftStr minus $rightStr"
            "*" -> "$leftStr times $rightStr"
            "/" -> "$leftStr dibaybay $rightStr"
            "==" -> "$leftStr equals $rightStr"
            "!=" -> "$leftStr not_equals $rightStr"
            ">"  -> "$leftStr greater_than $rightStr"
            ">=" -> "$leftStr greater_equal $rightStr"
            "<"  -> "$leftStr less_than $rightStr"
            "<=" -> "$leftStr less_equal $rightStr"
            else -> "($leftStr ${expr.operator.text} $rightStr)"
        }
    }

    private fun unaryToString(expr: Expr.Unary): String {
        val rightStr = maybeParenthesize(expr.right, Int.MAX_VALUE)
        return when (expr.operator.text) {
            "!" -> "not $rightStr"
            "-" -> "negative $rightStr"
            else -> "${expr.operator.text}$rightStr"
        }
    }

    // UNIQUE BEHAVIOR: chain printing with brackets around top-level chunks
    private fun chainToString(chain: Expr.Chain): String {
        val opPrec = getPrecedence(chain.operator)
        val opWord = when (chain.operator.text) {
            "+" -> "plus"
            "-" -> "minus"
            "*" -> "times"
            "/" -> "dibaybay"
            else -> chain.operator.text
        }

        // Build the first bracketed group: left + next operand
        val leftExpr = chain.elements[0]
        val nextExpr = chain.elements[1]
        val leftStr = maybeParenthesize(leftExpr, opPrec)
        val rightStr = maybeParenthesize(nextExpr, opPrec)
        var result = "[$leftStr $opWord $rightStr]"

        // Handle the rest of the chain
        for (i in 2 until chain.elements.size) {
            val next = chain.elements[i]
            val nextStr = maybeParenthesize(next, opPrec)
            result += " $opWord [$nextStr]"
        }

        return result
    }



    // Add parentheses only when inner precedence > parent precedence
    private fun maybeParenthesize(child: Expr, parentPrec: Int): String {
        return if (getExprPrecedence(child) > parentPrec) {
            "(${printInner(child)})"
        } else {
            printInner(child)
        }
    }

    private fun getExprPrecedence(expr: Expr): Int = when (expr) {
        is Expr.Binary -> getPrecedence(expr.operator)
        is Expr.Chain -> getPrecedence(expr.operator)
        else -> 0
    }

    private fun getPrecedence(op: Token): Int = when (op.type) {
        TokenType.STAR, TokenType.SLASH -> 3
        TokenType.PLUS, TokenType.MINUS -> 2
        TokenType.GREATER, TokenType.GREATER_EQUAL,
        TokenType.LESS, TokenType.LESS_EQUAL,
        TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL -> 1
        else -> 0
    }

    private fun literalToString(v: Any?): String =
        when (v) {
            null       -> "nil"
            is String  -> v
            is Double  -> if (v == Math.floor(v)) "${v.toInt()}.0" else v.toString()
            is Float   -> if (v == Math.floor(v.toDouble())) "${v.toInt()}.0" else v.toString()
            is Boolean -> v.toString()
            else       -> v.toString()
        }
}