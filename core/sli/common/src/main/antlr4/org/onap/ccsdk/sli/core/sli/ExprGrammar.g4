
grammar ExprGrammar;

options {
	language = Java;
}


COMPAREOP : '==' | '!=' | '>' | '<' | '>=' | '<=';

RELOP : 'and' | 'or';

ADDOP : '+' | '-';

MULTOP : '/' | '*';

NUMBER : ('0'..'9')+;

STRING : '\'' ~[']* '\'';

IDENTIFIER : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-')*;

// CONTEXT_VAR : '$' IDENTIFIER;

WS: [ \n\t\r]+ -> skip;

constant : NUMBER | STRING ;

variableLead : ('$')? variableTerm ;

variableTerm : IDENTIFIER ('[' expr ']')? ;

variable : variableLead ('.' variableTerm)* ('.')?;

// variable : CONTEXT_VAR  ( '[' expr ']' )? ('.' IDENTIFIER )? ;

atom : constant | variable;


expr : atom
     | parenExpr
     | multExpr
     | addExpr
     | compareExpr
     | relExpr
     | funcExpr;

parenExpr : '(' expr ')';

term : atom | parenExpr | funcExpr;

multExpr : term (MULTOP term)*;

addExpr : multExpr (ADDOP multExpr)*;

compareExpr : addExpr COMPAREOP addExpr;

relExpr : compareExpr (RELOP expr)*;

funcExpr : IDENTIFIER '(' expr (',' expr)* ')';







