module Main where

import Prelude
import System.Environment (getArgs)
import Data.Char(toLower)

-- Data Types
data Class = Class {
    className :: String,
    fields :: [Field]
} deriving (Show)

data Field = Field {
    fieldType :: String,
    fieldName :: String
} deriving (Show)

-- definitions
-- edit this value to change the Java code outputted by this script
exprClasses :: [Class]
exprClasses = [
    Class "Assign" [Field "Token" "name", Field "Expr" "value"],
    Class "Variable" [Field "Token" "name"],
    Class "Binary" [Field "Expr" "left", Field "Token" "operator", Field "Expr" "right"],
    Class "Grouping" [Field "Expr" "expression"],
    Class "Literal" [Field "Object" "value"],
    Class "Logical" [Field "Expr" "left", Field "Token" "operator", Field "Expr" "right"],
    Class "Unary" [Field "Token" "operator", Field "Expr" "right"],
    Class "Ternary" [Field "Expr" "condition", Field "Expr" "left", Field "Expr" "right"]
    ]

stmtClasses :: [Class]
stmtClasses = [
    Class "Block" [Field "List<Stmt>" "statements"],
    Class "Expression" [Field "Expr" "expression"],
    Class "If" [Field "Expr" "condition", Field "Stmt" " thenBranch", Field "Stmt" "elseBranch"],
    Class "Print" [Field "Expr" "expression"],
    Class "Var" [Field "Token" "name", Field "Expr" "initializer"]
    ]

-- Util. functions

indent :: Int -> String
indent n
    | n == 0 = ""
    | otherwise = "    " ++ indent (n - 1)

joinWith :: [a] -> [[a]] -> [a]
joinWith _ [] = []
joinWith _ [x] = x
joinWith sep (x:xs) = x ++ sep ++ joinWith sep xs

lowercase :: String -> String
lowercase = map toLower

-- logic

generateAst :: String -> [Class] -> String
generateAst baseName classes =
    let
        header = "import java.util.List;\n" ++
            "public abstract class " ++ baseName ++ " {\n" ++
            generateVisitorInterface baseName classes ++
            indent 1 ++ "abstract <R> R accept(Visitor<R> visitor);\n"
    in
        header ++
        concatMap (defineClass baseName) classes ++
        "}\n"

defineClass :: String -> Class -> String
defineClass baseName c =
    indent 1 ++ "public static class " ++ className c ++ " extends " ++ baseName ++ " {\n" ++
    generateConstructor c ++
    generateVisitorImpl baseName c ++
    generateFields c ++
    indent 1 ++ "}\n"

generateConstructor :: Class -> String
generateConstructor c =
    "       " ++
    className c ++
    "(" ++
    joinWith ", " (map fieldDeclaration (fields c)) ++
    ") {\n" ++
    concatMap (\f -> "          this." ++ fieldName f ++ " = " ++ fieldName f ++ ";\n") (fields c) ++
    "       }\n"

generateFields :: Class -> String
generateFields c =
    concatMap (\f -> "      final " ++ fieldDeclaration f ++ ";\n") (fields c)

fieldDeclaration :: Field -> String
fieldDeclaration f = fieldType f ++ " " ++ fieldName f

generateVisitorInterface :: String -> [Class] -> String
generateVisitorInterface baseName classes =
    indent 1 ++ "interface Visitor<R> {\n" ++
    concatMap (\c -> indent 2 ++ "R visit" ++ className c ++ baseName ++ "(" ++ className c ++ " " ++ lowercase baseName ++ ");\n") classes ++
    indent 1 ++ "}\n"

generateVisitorImpl :: String -> Class -> String
generateVisitorImpl baseName c =
    "@Override\n" ++
    "<R> R accept(Visitor<R> visitor) {\n" ++
    "return visitor.visit" ++
    className c ++ baseName ++ "(this);\n"
    ++ "}\n"

main :: IO ()
main = do
    args <- getArgs
    case args of
        [exprOutput, stmtOutput] -> do
            writeFile exprOutput (generateAst "Expr" exprClasses)
            writeFile stmtOutput (generateAst "Stmt" stmtClasses)
        _ -> error "Usage: generateAst <Expr file> <Stmt file>"
