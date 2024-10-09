public abstract class Stmt {
   interface Visitor<R> {
      R visitExpressionStmt(Expression stmt);

      R visitPrintStmt(Print stmt);
   }

   abstract <R> R accept(Visitor<R> visitor);

   public static class Expression extends Stmt {
      Expression(Expr expr) {
         this.expr = expr;
      }

      @Override
      <R> R accept(Visitor<R> visitor) {
         return visitor.visitExpressionStmt(this);
      }

      final Expr expr;
   }

   public static class Print extends Stmt {
      Print(Expr expression) {
         this.expression = expression;
      }

      @Override
      <R> R accept(Visitor<R> visitor) {
         return visitor.visitPrintStmt(this);
      }

      final Expr expression;
   }
}
