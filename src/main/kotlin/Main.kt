fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        val source = java.io.File(args[0]).readText()
        run(source)
        return
    }
    repl()
}

fun repl() {
    println("Welcome sahhh, ito ang coding language para sa mga real ones")
    println("Ilagay ang input mo sa ibaba G! (type 'exit' or 'tara' to bounce)")
    println()

    val evaluator = Evaluator()

    while (true) {
        print("> ")
        val input = readlnOrNull() ?: break
        if (input.isBlank()) continue

        if (input.lowercase() in listOf("exit", "quit", "tara", "bounce")) {
            println("Tara na, tapos na tayo")
            break
        }

        try {
            val scanner = Scanner(input)
            val tokens = scanner.scanTokens()
            val parser = Parser(tokens)
            val statements = parser.parse()
            evaluator.execute(statements)
        } catch (e: Exception) {
            println("[ERROR] ${e.message}")
        }
    }
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val statements = parser.parse()
    val evaluator = Evaluator()
    evaluator.execute(statements)
}
