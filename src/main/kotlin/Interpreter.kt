import TokenType.*

class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Unit> {

    companion object {
        class BreakStatementException: RuntimeException()
    }

    val globals = Environment()
    private var environment = globals
    private val locals: MutableMap<Expr, Int> = mutableMapOf()

    init {
        globals.define("clock", object : LoxCallable {
            override val arity: Int = 0
            override fun call(interpreter: Interpreter, arguments: List<Any>) =
                (System.currentTimeMillis() / 1000.0).toDouble()

            override fun toString(): String =
                "<native fn clock>"
        })

        globals.define("sleep", object : LoxCallable {
            override val arity: Int = 1
            override fun call(interpreter: Interpreter, arguments: List<Any>) {
                val time = (arguments.first() as Double).toLong()
                Thread.sleep(time)
            }

            override fun toString(): String =
                "<native fn sleep>"
        })
    }

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

    override fun visitLogicalExpr(expr: Expr.Logical): Any {
        val left = evaluate(expr.left)

        if (expr.operator.type == OR) {
            // no need to evaluate RHS if LHS is truthy
            if (isTruthy(left)) return left
        } else {
            // if LHS of and operator is falsey, we don't need to evaluate the RHS
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set): Any {
        val obj = evaluate(expr.`object`)

        if (obj !is LoxInstance) {
            throw RuntimeError(expr.name, "Only instances have fields.")
        }

        val value = evaluate(expr.value)
        obj.set(expr.name, value)
        return value
    }

    override fun visitSuperExpr(expr: Expr.Super): Any {
        val distance = locals[expr]!!
        val superclass = environment.getAt(distance, "super") as LoxClass
        val obj = environment.getAt(distance - 1, "this") as LoxInstance
        val method = superclass.findMethod(expr.method.lexeme)
            ?: throw RuntimeError(expr.method, "Undefined property '${expr.method.lexeme}'.")
        return method.bind(obj)
    }

    override fun visitThisExpr(expr: Expr.This): Any?
        = lookupVariable(expr.keyword, expr)

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
        return lookupVariable(expr.name, expr)
    }

    private fun lookupVariable(name: Token, expr: Expr): Any? {
        val distance = locals[expr]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
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

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)
        val arguments = expr.arguments.map(::evaluate)

        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes")
        }
        if (arguments.size != callee.arity) {
            throw RuntimeError(expr.paren, "Expected ${callee.arity} arguments but got ${arguments.size}.")
        }

        return callee.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val obj = evaluate(expr.`object`)
        if (obj is LoxInstance) {
            return (obj as LoxInstance).get(expr.name)
        }

        throw RuntimeError(expr.name , "Only instances have properties")
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
        if (value == null || value is Nil) return "nil"

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
        if (value == null || value is Nil) return false
        if (value is Boolean) return value
        return true
    }

    private fun evaluate(expr: Expr): Any =
        expr.accept(this)

    private fun execute(stmt: Stmt) =
        stmt.accept(this)

    fun resolve(expr: Expr, depth: Int) {
        locals.put(expr, depth)
    }

    fun executeBlock(statements: List<Stmt>, blockEnv: Environment) {
        val previousEnv = environment
        try {
            environment = blockEnv
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            environment = previousEnv
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        var superclass: Any? = null
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass)
            if (superclass !is LoxClass) {
                throw RuntimeError(stmt.superclass.name, "Superclass must be a class.")
            }
        }

        environment.define(stmt.name.lexeme, null)

        stmt.superclass?.let {
            environment = Environment(environment)
            environment.define("super", superclass)
        }

        val methods = HashMap<String, LoxFunction>()
        for (method in stmt.methods) {
            val function = LoxFunction(method, environment, method.name.lexeme == "init")
            methods[method.name.lexeme] = function
        }

        val klass = LoxClass(stmt.name.lexeme, superclass as? LoxClass, methods)
        if (superclass != null) {
            environment = environment.enclosing!!
        }
        environment.assign(stmt.name, klass)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        var value: Any? = null
        if (stmt.value != null) {
            value = evaluate(stmt.value)
        }

        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        var value: Any = Nil
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitBreakStmt(stmt: Stmt.Break) {
        throw BreakStatementException()
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.Body)
            } catch (e: BreakStatementException) {
                print("break")
                break
            }
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any {
        val value = evaluate(expr.value)
        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }
        return value
    }
}