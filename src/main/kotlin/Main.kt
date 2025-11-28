//fun main() {
//
//    println("Welcome sahhh, ito ang coding language para sa mga real ones")
//    println("Ilagay ang input mo sa ibaba G! (type 'exit' or 'tara' to bounce)")
//    println()
//
//    while (true) {
//        print("> ")
//        val input = readlnOrNull() ?: break
//        if (input.isEmpty()) continue
//
//        // manual quit keywords
//        if (input.lowercase() in listOf("exit", "quit", "tara", "bounce")) {
//            println("Tara na, tapos na tayo 😎")
//            break
//        }
//
//        val scanner = Scanner(input)
//        val tokens = scanner.scanTokens()
//
//        println("Tatagos ba tong mga tokens mo sahh:")
//        for (token in tokens) {
//            println(" ${token.type} | '${token.text}' | ${token.value ?: "null"} | line=${token.line}")
//        }
//
//        println()
//        val parser = Parser(tokens)
//        val expr = parser.parse() // whole expression
//        val AST = AstStringer().print(expr)
//        println("AST: $AST")
//
//        val value = Evaluator().evaluate(expr)
//        println(value)
//        println()
//    }
//
//    println("\nBounce nako par matsalove...")
//}


fun main(args: Array<String>) {
    // If a filename is provided, run that file instead of REPL
    if (args.isNotEmpty()) {
        val source = java.io.File(args[0]).readText()
        run(source)
        return
    }

    repl()
}

// Interactive REPL
fun repl() {
    println("Welcome sahhh, ito ang coding language para sa mga real ones")
    println("Ilagay ang input mo sa ibaba G! (type 'exit' or 'tara' to bounce)")
    println()

    val evaluator = Evaluator()

    while (true) {
        print("> ")
        val input = readlnOrNull() ?: break
        if (input.isBlank()) continue

        // manual quit keywords
        if (input.lowercase() in listOf("exit", "quit", "tara", "bounce")) {
            println("Tara na, tapos na tayo 😎")
            break
        }

        try {
            val scanner = Scanner(input)
            val tokens = scanner.scanTokens()

            // Old Parser still returns Expr (not List<Stmt>) in your file
            val parser = Parser(tokens)
            val expr = parser.parse()      // Expr

            // Wrap single expression into a statement, so it matches Evaluator.execute
            val stmt = Stmt.Expression(expr)
            evaluator.execute(listOf(stmt))        // Uses fun execute(stmt: Stmt)

        } catch (e: Exception) {
            println("[ERROR] ${e.message}")
        }
    }
}

// Runs a whole source string (e.g., from a file)
fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)

    // Parser currently: fun parse(): Expr
    val expr = parser.parse()
    val stmt = Stmt.Expression(expr)

    val evaluator = Evaluator()
    evaluator.execute(listOf(stmt))
}

