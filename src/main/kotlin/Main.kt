fun main() {
    val evaluator = Evaluator()

    // === Test 1: if (10 > 5) print 1 else print 0 ===
    run {
        val condition = Expr.Binary(
            Expr.Literal(10),
            Token(TokenType.GREATER, ">", null, 1),
            Expr.Literal(5)
        )
        val thenBranch = Stmt.Print(Expr.Literal(1))
        val elseBranch = Stmt.Print(Expr.Literal(0))
        val ifStmt = Stmt.If(condition, thenBranch, elseBranch)

        println("If test (expect 1):")
        evaluator.executeStmt(ifStmt)
        println()
    }

    // === Test 2: while (flag < 1) { print(0); flag = 1; } ===
    run {
        evaluator.setVar("flag", 0)   // make sure this method exists in Evaluator

        val flagToken = Token(TokenType.RACE_TYPE, "flag", null, 1)

        val condition = Expr.Binary(
            Expr.Variable(flagToken),
            Token(TokenType.LESS, "<", null, 1),
            Expr.Literal(1)
        )

        val printZero = Stmt.Print(Expr.Literal(0))

        val setFlagOne = Stmt.Expression(
            Expr.Assign(
                flagToken,
                Expr.Literal(1)
            )
        )

        val body = Stmt.Block(listOf(printZero, setFlagOne))
        val whileStmt = Stmt.While(condition, body)

        println("While test (expect a single 0):")
        evaluator.executeStmt(whileStmt)
        println()
    }

    println("Done.")
}
