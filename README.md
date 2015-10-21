# JavaParser
Write an object-oriented top-down parser in Java that translates every TinyPL program into an equivalent sequence of byte-codes for a Java Virtual Machine

Consider the following grammar for a simple programming language, TinyPL:
program -> decls stmts end
decls -> int idlist ';'
idlist -> id [',' idlist ]
stmts -> stmt [ stmts ]
stmt -> assign ';'| cmpd | cond | loop
assign -> id '=' expr
cmpd -> '{' stmts '}'
cond -> if '(' rexp ')' stmt [ else stmt ]
loop -> for '(' [assign] ';' [rexp] ';' [assign] ')' stmt

rexp -> expr ('<' | '>' | '==' | '!= ') expr
expr -> term [ ('+' | '-') expr ]
term -> factor [ ('*' | '/') term ]
factor -> int_lit | id | '(' expr ')'
