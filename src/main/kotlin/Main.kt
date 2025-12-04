fun main(args: Array<String>) {
    try {
        if (args.isNotEmpty()) {
            val source = java.io.File(args[0]).readText()
            run(source)
            return
        }

        println("Enter a character description, end with an empty line:")
        val builder = StringBuilder()
        while (true) {
            val line = readLine() ?: break
            if (line.isBlank()) break
            builder.appendLine(line)
        }

        run(builder.toString())
    } catch (e: RuntimeException) {
        println(e.message)
    }
}

fun run(source: String) {
    val normalized = source.replace("\r\n", "\n").replace("\r", "\n")

        val scanner = Scanner(normalized)
    val tokens = scanner.scanTokens()

    val parser = Parser(tokens)
    val scriptStmts = parser.parseScriptProgram()

    val interpreter = Interpreter()
    interpreter.interpret(scriptStmts)

}

