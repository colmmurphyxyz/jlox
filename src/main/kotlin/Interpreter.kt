import TokenType.*

class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Unit> {

    private val environment = Environment()

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? =
        expr.value

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            BANG -> !isTruthy(right)
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }

            else -> null
        }
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    private fun checkNumberOperand(operator: Token, operand: Any) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any, right: Any) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any =
        evaluate(expr.expression)

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) - (right as Double)
            }

            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                if (right as Double == 0.0) {
                    throw ArithmeticError(expr.operator, "Cannot divide by zero.")
                }
                return (left as Double) / (right)
            }

            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) * (right as Double)
            }

            PLUS -> {
                return if (left is Double && right is Double) {
                    left + right
                } else if (left is String || right is String) {
                    stringify(left) + stringify(right)
                } else {
                    throw RuntimeError(
                        expr.operator,
                        "Incompatible operands ${left::class.simpleName} and ${right::class.simpleName} for operator +"
                    )
                }
            }

            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double > right as Double
            }

            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double >= right as Double
            }

            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) < (right as Double)
            }

            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double <= right as Double
            }

            BANG_EQUAL -> return !isEqual(left, right)
            EQUAL_EQUAL -> return isEqual(left, right)
            else -> return null
        }
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): Any? {
        return if (isTruthy(evaluate(expr.condition))) {
            expr.left.accept(this)
        } else {
            expr.right.accept(this)
        }
    }

    private fun isEqual(lhs: Any?, rhs: Any?): Boolean {
        if (lhs == null && rhs == null) return true
        if (lhs == null) return false

        return lhs == rhs
    }

    private fun stringify(value: Any?): String {
        if (value == null) return "nil"

        if (value is Double) {
            val text = value.toString().let {
                if (it.endsWith(".0")) {
                    it.substring(0..< it.length - 2)
                } else {
                    it
                }
            }
            return text
        }

        return value.toString()
    }

    private fun isTruthy(value: Any?): Boolean {
        if (value == null) return false
        if (value is Boolean) return value
        return true
    }

    private fun evaluate(expr: Expr): Any =
        expr.accept(this)

    private fun execute(stmt: Stmt) =
        stmt.accept(this)

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }
}