fun main() {
    println("Welcome sahhh, ito ang coding language para sa mga real ones")
    println("Ilagay ang input mo sa ibaba G! (Ctrl+D or Ctrl+C para mag bounce)")
    println()

    while (true) {
        print("> ")
        val input = readlnOrNull() ?: break

        if (input.isEmpty()) continue

        val scanner = Scanner(input)
        val tokens = scanner.scanTokens()

        println("Tatagos ba tong mga tokens mo sahh:")
        for (token in tokens) {
            println("  ${token.type} | '${token.text}' | ${token.value ?: "null"} | line=${token.line}")
        }
        println()
    }

    println("\nBounce nako par matsalove...")
}
