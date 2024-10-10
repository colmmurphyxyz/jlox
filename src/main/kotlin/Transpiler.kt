import TokenType.*

class Transpiler : Expr.Visitor<String> {
    fun transpile(expr: Expr): String =
        expr.accept(this)

    override fun visitAssignExpr(expr: Expr.Assign?): String {
        TODO("Not yet implemented")
    }

    override fun visitVariableExpr(expr: Expr.Variable?): String {
        TODO("Not yet implemented")
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String =
        "${expr.left.accept(this)} ${expr.operator.lexeme} ${expr.right.accept(this)}"

    override fun visitGroupingExpr(expr: Expr.Grouping): String =
        "(" + expr.expression.accept(this) + ")"

    override fun visitLiteralExpr(expr: Expr.Literal): String =
        expr.value.toString()

    override fun visitUnaryExpr(expr: Expr.Unary): String =
        when (expr.operator.type) {
            MINUS -> "-${expr.right.accept(this)}"
            BANG -> "not ${expr.right.accept(this)}"
            else -> ""
        }

    override fun visitTernaryExpr(expr: Expr.Ternary): String =
        "${expr.left.accept(this)} if ${expr.condition.accept(this)} else ${expr.right.accept(this)}"

}