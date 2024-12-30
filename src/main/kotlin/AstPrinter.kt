class AstPrinter : Expr.Visitor<String>, Stmt.Visitor<String> {
    private var indentationLevel = 0

    private operator fun String.times(rhs: Int): String =
        Array(rhs) { this }.joinToString(separator = "")

    private fun indent(s: String): String {
        return "  " * indentationLevel + s
    }

    fun printAst(statements: List<Stmt>): String {
        indentationLevel = 0
        return statements.joinToString("\n") {
            it.accept(this)
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        indentationLevel++
        val assignment = listOf(
            "assignment",
            indent(expr.name.lexeme),
            indent(expr.value.accept(this))
        )
        indentationLevel--
        return assignment.joinToString("\n")
    }

    override fun visitVariableExpr(expr: Expr.Variable): String =
        expr.name.lexeme

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        indentationLevel++
        val expression = listOf(
            expr.operator.lexeme,
            indent(expr.left.accept(this)),
            indent(expr.right.accept(this))
        )
        indentationLevel--
        return expression.joinToString("\n")
    }

    override fun visitCallExpr(expr: Expr.Call): String {
        indentationLevel++
        val call = listOf(
            expr.callee.accept(this),
            (expr.arguments.map { indent(it.accept(this)) }).joinToString("\n")
        )
        indentationLevel--
        return call.joinToString("\n")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        indentationLevel++
        val grouping = listOf(
            "(",
            indent(expr.expression.accept(this)),
            ")",
        )
        indentationLevel--
        return grouping.joinToString("\n")
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return expr.value.toString()
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        indentationLevel++
        val expression = listOf(
            expr.operator.lexeme,
            indent(expr.left.accept(this)),
            indent(expr.right.accept(this))
        )
        indentationLevel--
        return expression.joinToString("\n")
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        indentationLevel++
        val unary = listOf(
            expr.operator.lexeme,
            indent(expr.right.accept(this))
        )
        indentationLevel--
        return unary.joinToString("\n")
    }

    override fun visitTernaryExpr(expr: Expr.Ternary?): String {
        TODO("Not yet implemented")
    }

    override fun visitBlockStmt(stmt: Stmt.Block): String {
        indentationLevel++
        val block = listOf(
            "block",
            stmt.statements.map { indent(it.accept(this)) }.joinToString("\n")
        )
        indentationLevel--
        return block.joinToString("\n")
    }

    override fun visitBreakStmt(stmt: Stmt.Break): String {
        return "break"
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): String {
        return stmt.expression.accept(this)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): String {
        indentationLevel++
        val function = listOf(
            "function ${stmt.name.lexeme}",
            indent("params"),
            stmt.params.joinToString("\n") { indent(indent(it.lexeme)) },
            indent("body"),
            stmt.body.joinToString("\n") { indent(indent(it.accept(this))) },
        )
        indentationLevel--
        return function.joinToString("\n")
    }

    override fun visitIfStmt(stmt: Stmt.If): String {
        indentationLevel++
        val statement = listOf(
            "if",
            indent(stmt.condition.accept(this)),
            indent(stmt.thenBranch.accept(this)),
            indent(stmt.elseBranch?.let { it.accept(this) } ?: ""),
        )
        indentationLevel--
        return statement.joinToString("\n")
    }

    override fun visitPrintStmt(stmt: Stmt.Print): String {
        indentationLevel++
        val print = listOf(
            "print",
            indent(stmt.expression.accept(this)),
        )
        indentationLevel--
        return print.joinToString("\n")
    }

    override fun visitReturnStmt(stmt: Stmt.Return): String {
        indentationLevel++
        val ret = listOf(
            "return",
            indent(stmt.value.accept(this)),
        )
        indentationLevel--
        return ret.joinToString("\n")
    }

    override fun visitVarStmt(stmt: Stmt.Var): String {
        indentationLevel++
        val varStmt = listOf(
            "var",
            indent(stmt.name.lexeme),
            indent(stmt.initializer.accept(this)),
        )
        indentationLevel--
        return varStmt.joinToString("\n")
    }

    override fun visitWhileStmt(stmt: Stmt.While): String {
        indentationLevel++
        val whileStmt = listOf(
            "while",
            indent(stmt.condition.accept(this)),
            "body",
            stmt.Body.accept(this),
        )
        indentationLevel--
        return whileStmt.joinToString("\n")
    }

}