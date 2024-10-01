module Main where

import Prelude
import System.Environment (getArgs)

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
classes :: [Class]
classes = [
    Class "Binary" [Field "Expr" "left", Field "Token" "operator", Field "Expr" "right"],
    Class "Grouping" [Field "Expr" "expression"],
    Class "Literal" [Field "Object" "value"],
    Class "Unary" [Field "Token" "operator", Field "Expr" "Right"]
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

-- logic

generateAst :: [Class] -> String
generateAst classes =
    let
        header = "import java.util.List;\n" ++
            "abstract class Expr {\n" ++
            generateVisitorInterface classes ++
            indent 1 ++ "abstract <R> R accept(Visitor<R> visitor);\n"
    in
        header ++
        concatMap defineClass classes ++
        "}\n"

defineClass :: Class -> String
defineClass c =
    indent 1 ++ "static class " ++ className c ++ " extends Expr {\n" ++
    generateConstructor c ++
    generateVisitorImpl c ++
    generateFields c ++
    indent 1 ++ "}\n"

generateConstructor :: Class -> String
generateConstructor c =
    "       " ++
    className c ++
    "(" ++
    joinWith ", " (map fieldDeclaration (fields c)) ++
    -- concatMap fieldDeclaration (fields c) ++
    ") {\n" ++
    concatMap (\f -> "          this." ++ fieldName f ++ " = " ++ fieldName f ++ ";\n") (fields c) ++
    "       }\n"

generateFields :: Class -> String
generateFields c =
    concatMap (\f -> "      final " ++ fieldDeclaration f ++ ";\n") (fields c)

fieldDeclaration :: Field -> String
fieldDeclaration f = fieldType f ++ " " ++ fieldName f

generateVisitorInterface :: [Class] -> String
generateVisitorInterface classes =
    "interface Visitor<R> {\n" ++
    concatMap (\c -> "R visit" ++ className c ++ "Expr" ++ "(" ++ className c ++ " expr);\n") classes ++
    "}\n"

generateVisitorImpl :: Class -> String
generateVisitorImpl c =
    "@Override\n" ++
    "<R> R accept(Visitor<R> visitor) {\n" ++
    "return visitor.visit" ++
    className c ++ "Expr" ++ "(this);\n"
    ++ "}\n"

main :: IO ()
main = do
    args <- getArgs
    case args of
        [x] -> let outputFile = x in writeFile outputFile (generateAst classes)
        _ -> error "Usage: generateAst <output file>"
