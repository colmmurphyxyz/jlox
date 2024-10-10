class RpnConverter : Expr.Visitor<String> {

    fun convertToRpn(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String =
        "${expr.left.accept(this)} ${expr.right.accept(this)} ${expr.operator.lexeme}"

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return expr.expression.accept(this)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return "${expr.value}"
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return "${expr.right.accept(this)} ${expr.operator.lexeme}"
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): String {
        TODO("Not yet implemented")
    }

    override fun visitAssignExpr(expr: Expr.Assign?): String {
        TODO("Not yet implemented")
    }

    override fun visitVariableExpr(expr: Expr.Variable?): String {
        TODO("Not yet implemented")
    }

}