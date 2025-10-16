// AstStringer.kt — prints AST in prefix (Lisp-like) form, no flattening
class AstStringer {

    fun print(expr: Expr): String = innerString(expr)

    private fun innerString(expr: Expr): String = when (expr) {
        is Expr.Literal  -> literalToString(expr.value)
        is Expr.Grouping -> "(group ${innerString(expr.expression)})"
        is Expr.Unary    -> "(${expr.operator.text} ${innerString(expr.right)})"
        is Expr.Binary   -> "(${expr.operator.text} ${innerString(expr.left)} ${innerString(expr.right)})"
    }

    private fun literalToString(v: Any?): String =
        when (v) {
            null       -> "nil"
            is String  -> "\"$v\""
            is Double  -> if (v == Math.floor(v)) "${v.toInt()}.0" else v.toString()
            is Float   -> if (v == Math.floor(v.toDouble())) "${v.toInt()}.0" else v.toString()
            is Boolean -> v.toString()
            else       -> v.toString()
        }
}
