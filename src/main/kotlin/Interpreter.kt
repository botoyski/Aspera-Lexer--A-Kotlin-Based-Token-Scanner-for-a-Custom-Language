class Interpreter(
    private val nativeBattleStep: (() -> Unit)? = null
) {
    private val globals = Environment()
    private var environment = globals

    init {
        // native stepBattle(): calls your Kotlin battle for one run
        globals.define("stepBattle", object : Callable {
            override fun arity() = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                nativeBattleStep?.invoke()
                return null
            }
        })

        // native len(s): string length
        globals.define("len", object : Callable {
            override fun arity() = 1
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val s = arguments[0] as? String
                    ?: error("len: argument must be a string")
                return s.length.toDouble() // your numbers are Double
            }
        })

        // native concat(a, b): concatenate strings
        globals.define("concat", object : Callable {
            override fun arity() = 2
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val a = arguments[0] as? String
                    ?: error("concat: first argument must be a string")
                val b = arguments[1] as? String
                    ?: error("concat: second argument must be a string")
                return a + b
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
            is StmtLang.For -> {
                // run initializer once, if any
                stmt.initializer?.let { execute(it) }
                // then loop while condition is true (or forever if null)
                while (stmt.condition?.let { isTruthy(evaluate(it)) } != false) {
                    execute(stmt.body)
                    stmt.increment?.let { evaluate(it) }
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

        is Expr.Index -> {
            val targetVal = evaluate(expr.target)
            val indexVal = evaluate(expr.index)

            val idx = when (indexVal) {
                is Int -> indexVal
                else -> error("Index must be a number.")
            }

            when (targetVal) {
                is String -> {
                    if (idx < 0 || idx >= targetVal.length) {
                        error("String index out of bounds.")
                    }
                    targetVal[idx].toString()  // return one-character string
                }
                else -> error("Indexing is only supported on strings (for now).")
            }
        }

        is Expr.AssignIndex -> {
            val targetVal = evaluate(expr.target)
            val indexVal = evaluate(expr.index)
            val newVal = evaluate(expr.value)

            val idx = when (indexVal) {
                is Int -> indexVal
                else -> error("Index must be a number.")
            }

            if (targetVal !is String) {
                error("Index assignment only supported on strings for now.")
            }
            if (newVal !is String || newVal.length != 1) {
                error("Index assignment value must be a single-character string.")
            }
            if (idx < 0 || idx >= targetVal.length) {
                error("String index out of bounds.")
            }

            val chars = targetVal.toCharArray()
            chars[idx] = newVal[0]
            val result = String(chars)

            if (expr.target is Expr.Variable) {
                environment.assign(expr.target.name, result)
            } else {
                error("Left side of index assignment must be a variable.")
            }

            result
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
