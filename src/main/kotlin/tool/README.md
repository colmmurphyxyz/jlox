The GenerateAst.hs script will output a Java class to represent Lox expressions

# Usage
The script can be compiled with GHC, or interpreted with `runhaskell`. It accepts two arguments,
the files to which the outputted Java code will be written. If this file does not exist already it will not be created.
The file's existing contents will be overwritten.

```shell
touch Expr.java
touch Stmt.java
runhaskell GenerateAst.hs Expr.java Stmt.java
```