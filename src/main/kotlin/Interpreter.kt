class Interpreter(
    private val nativeBattleStep: (() -> Unit)? = null
) {
    private val globals = Environment()
    private var environment = globals

    init {
        // native print (if you want)
        globals.define("clock", object : Callable {
            override fun arity() = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? =
                System.currentTimeMillis().toDouble() / 1000.0
        })

        // native stepBattle(): calls your Kotlin battle for one run
        globals.define("stepBattle", object : Callable {
            override fun arity() = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                nativeBattleStep?.invoke()
                return null
            }
        })
    }

    fun interpret(statements: List<StmtLang>) {
        for (s in statements) execute(s)
    }

    private fun execute(stmt: StmtLang) {
        when (stmt) {
            is StmtLang.Expression -> evaluate(stmt.expr)
            is StmtLang.Print -> println(stringify(evaluate(stmt.expr)))
            is StmtLang.Var -> {
                val value = stmt.initializer?.let { evaluate(it) }
                environment.define(stmt.name.text, value)
            }
            is StmtLang.Block -> executeBlock(stmt.statements, Environment(environment))
            is StmtLang.If -> {
                if (isTruthy(evaluate(stmt.condition))) execute(stmt.thenBranch)
                else stmt.elseBranch?.let { execute(it) }
            }
            is StmtLang.While -> {
                while (isTruthy(evaluate(stmt.condition))) {
                    execute(stmt.body)
                }
            }
            is StmtLang.Function -> {
                val function = UserFunction(stmt, environment)
                environment.define(stmt.name.text, function)
            }
            is StmtLang.Return -> throw ReturnException(stmt.value?.let { evaluate(it) })
        }
    }

    fun executeBlock(statements: List<StmtLang>, env: Environment) {
        val previous = environment
        try {
            environment = env
            for (s in statements) execute(s)
        } finally {
            environment = previous
        }
    }

    private fun evaluate(expr: Expr): Any? = when (expr) {
        is Expr.Literal -> expr.value
        is Expr.Grouping -> evaluate(expr.expression)
        is Expr.Variable -> environment.get(expr.name)
        is Expr.Assign -> {
            val value = evaluate(expr.value)
            environment.assign(expr.name, value)
            value
        }
        is Expr.Unary -> {
            val right = evaluate(expr.right)
            when (expr.operator.type) {
                TokenType.MINUS -> -(right as Double)
                TokenType.BANG -> !isTruthy(right)
                else -> error("Unknown unary")
            }
        }
        is Expr.Binary -> {
            val left = evaluate(expr.left) as Double
            val right = evaluate(expr.right) as Double
            when (expr.operator.type) {
                TokenType.PLUS -> left + right
                TokenType.MINUS -> left - right
                TokenType.STAR -> left * right
                TokenType.SLASH -> left / right
                TokenType.GREATER -> left > right
                TokenType.GREATER_EQUAL -> left >= right
                TokenType.LESS -> left < right
                TokenType.LESS_EQUAL -> left <= right
                TokenType.EQUAL_EQUAL -> left == right
                TokenType.BANG_EQUAL -> left != right
                else -> error("Unknown binary")
            }
        }
        is Expr.Logical -> {
            val left = evaluate(expr.left)
            when (expr.operator.type) {
                TokenType.OR ->
                    if (isTruthy(left)) left else evaluate(expr.right)
                else -> // AND
                    if (!isTruthy(left)) left else evaluate(expr.right)
            }
        }
        is Expr.Call -> {
            val callee = evaluate(expr.callee)
            val args = expr.arguments.map { evaluate(it) }
            val function = callee as? Callable ?: error("Can only call functions.")
            if (args.size != function.arity()) error("Expected ${function.arity()} args.")
            function.call(this, args)
        }
    }

    private fun isTruthy(v: Any?): Boolean = when (v) {
        null -> false
        is Boolean -> v
        else -> true
    }

    private fun stringify(v: Any?): String = v?.toString() ?: "nil"
}

class UserFunction(
    private val declaration: StmtLang.Function,
    private val closure: Environment
) : Callable {
    override fun arity() = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val env = Environment(closure)
        for ((i, param) in declaration.params.withIndex()) {
            env.define(param.text, arguments[i])
        }
        try {
            interpreter.executeBlock(declaration.body, env)
        } catch (r: ReturnException) {
            return r.value
        }
        return null
    }
}

class ReturnException(val value: Any?) : RuntimeException(null, null, false, false)
