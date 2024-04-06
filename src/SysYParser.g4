parser grammar SysYParser;

options {
    tokenVocab = SysYLexer;
}

compUnit
	: ( decl | funcDef )+ EOF
	;

decl
   	: constDecl
   	| varDecl
   	;

constDecl
   	: CONST bType constDef (COMMA constDef)* SEMICOLON
   	;

bType
   	: INT
   	;

constDef
   	: IDENT (L_BRACKT constExp R_BRACKT)* ASSIGN constInitVal
   	;

constInitVal
   	: constExp
   	| L_BRACE (constInitVal (COMMA constInitVal)*)? R_BRACE
   	;

varDecl
   	: bType varDef (COMMA varDef)* SEMICOLON
   	;

varDef
   	: IDENT (L_BRACKT constExp R_BRACKT)* (ASSIGN initVal)?
   	;

initVal
   	: exp
   	| L_BRACE (initVal (COMMA initVal)*)? R_BRACE
   	;

funcDef
   	: funcType funcName L_PAREN funcFParams? R_PAREN block		#DefFunc
   	;

funcType
   	: VOID
   	| INT
   	;

funcFParams
   	: funcFParam (COMMA funcFParam)*
   	;

funcFParam
   	: bType IDENT (L_BRACKT R_BRACKT (L_BRACKT exp R_BRACKT)*)?
   	;

block
   	: L_BRACE blockItem* R_BRACE
   	;

blockItem
   	: decl
   	| statement
   	;

statement
   	: lVal ASSIGN exp SEMICOLON									# StatementIVal
   	| exp? SEMICOLON											# StatementExp
   	| block														# StatementBlock
   	| IF L_PAREN cond R_PAREN statement (statementElse)?		# StatementIf
   	| WHILE L_PAREN cond R_PAREN statement						# StatementWhile
   	| BREAK SEMICOLON											# StatementBreak
   	| CONTINUE SEMICOLON										# StatementContinue
   	| RETURN exp SEMICOLON										# StatementReturnWithExp
   	| RETURN SEMICOLON											# StatementReturnWithoutExp
   	;

statementElse
	: ELSE statement
	;

exp
   	: L_PAREN exp R_PAREN										# ExpressionExp
   	| lVal														# ExpressionLVal
   	| number													# ExpressionNumber
   	| funcName L_PAREN funcRParams? R_PAREN						# ExpressionFunc
   	| unaryOp exp												# ExpressionUnaryOp
   	| exp (MUL | DIV | MOD) exp									# ExpressionMulDivMod
   	| exp (PLUS | MINUS) exp									# ExpressionPlusMinus
   	;

cond
   	: exp
   	| cond (LT | GT | LE | GE) cond
   	| cond (EQ | NEQ) cond
   	| cond AND cond
   	| cond OR cond
   	;

lVal
   	: IDENT (L_BRACKT exp R_BRACKT)*
   	;

number
   	: INTEGER_CONST
   	;

unaryOp
   	: PLUS
   	| MINUS
   	| NOT
   	;

funcRParams
   	: param (COMMA param)*
   	;

param
   	: exp
  	;

constExp
   	: exp
   	;

funcName
	: IDENT
	;