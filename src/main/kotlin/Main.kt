fun main() {
    println("Welcome sahhh, ito ang coding language para sa mga real ones")
    println("Ilagay ang input mo sa ibaba G! (type 'exit' or 'tara' to bounce)")
    println()

    while (true) {
        print("> ")
        val input = readlnOrNull() ?: break
        if (input.isEmpty()) continue

        // manual quit keywords
        if (input.lowercase() in listOf("exit", "quit", "tara", "bounce")) {
            println("Tara na, tapos na tayo 😎")
            break
        }

        val scanner = Scanner(input)
        val tokens = scanner.scanTokens()

        println("Tatagos ba tong mga tokens mo sahh:")
        for (token in tokens) {
            println("  ${token.type} | '${token.text}' | ${token.value ?: "null"} | line=${token.line}")
        }

        println()
        val parser = Parser(tokens)
        val expr = parser.parse()
        val pretty = AstPrinter().print(expr)
        println("AST: $pretty")
        println()
    }

    println("\nBounce nako par matsalove...")
}
