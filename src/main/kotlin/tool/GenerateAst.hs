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

joinWith :: [a] -> [[a]] -> [a]
joinWith _ [] = []
joinWith _ [x] = x
joinWith sep (x:xs) = x ++ sep ++ joinWith sep xs

-- logic

generateAst :: [Class] -> String
generateAst classes =
    let
        header = "import java.util.List;\n" ++
            "abstract class Expr {\n"
    in
        header ++
        concatMap defineClass classes ++
        "}\n"

defineClass :: Class -> String
defineClass c =
    "   static class " ++ className c ++ " extends Expr {\n" ++
    generateConstructor c ++
    generateFields c ++
    "   }\n"

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

main :: IO ()
main = do
    args <- getArgs
    case args of
        [x] -> let outputFile = x in writeFile outputFile (generateAst classes)
        _ -> error "Usage: generateAst <output file>"
