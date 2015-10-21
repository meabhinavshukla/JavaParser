package abhi;

import java.util.HashMap;

public class Parser 
{
	
	public static void main(String[] args)  
	{
		System.out.println("Enter program and terminate with 'end'!\n");
		Lexer.lex();
		new Program();
		Code.output();
	}
}

class Program 
{
	Stmts stmts;
	Decls decls;
	
	public Program()
	{
		 try 
		 {
			 if(Lexer.nextToken == Token.KEY_INT)
			 {
				 decls = new Decls();
			 }
			 stmts = new Stmts();
			 Code.gen("return");
		 } 
		 catch (ParserException e)
		 {
			e.printStackTrace();
		 }
		
	}
}

class Decls 
{
	Idlist idlist;
	
	public Decls() throws ParserException
	{
			Lexer.lex();
			idlist = new Idlist();
	}
}

class Idlist 
{
	static int ptr = 0;
	Idlist idlist ;
	public Idlist()
	{
		while(Lexer.nextToken!=Token.ID && Lexer.nextToken != Token.SEMICOLON)
		{
			Lexer.lex();
		}
		
		if(Lexer.nextToken==Token.ID)
		{
			Code.variables.put(Lexer.ident,ptr++);
			Lexer.lex();
			if(Lexer.nextToken==Token.COMMA)
				idlist = new Idlist();
			if(Lexer.nextToken==Token.SEMICOLON)
				Lexer.lex();
		}
	} 
}

class Stmt 
{
	
	Assign assign;
	Cmpd cmpd;
	Cond cond;
	Loop loop;
	
	public Stmt() throws ParserException
	{

		switch(Lexer.nextToken)
		{
			case Token.ID:
				assign = new Assign();
				break;
			case Token.KEY_IF:
				cond = new Cond();
				break;
			case Token.KEY_FOR:
				loop = new Loop();
				break;
			case Token.LEFT_BRACE:
				cmpd = new Cmpd();
				break;
			
			default :
				throw new ParserException(Lexer.nextChar);
		}
		
	}
} 

class Cmpd 
{
	Stmts stmts;
	public Cmpd() throws ParserException{
		Lexer.lex();
		stmts = new Stmts();
	}
}

class Stmts 
{
	Stmt stmt;
	Stmts stmts;
	public Stmts() throws ParserException
	{
		stmt = new Stmt();
		if(Lexer.nextToken == Token.KEY_IF || Lexer.nextToken == Token.KEY_FOR ||Lexer.nextToken == Token.ID )
		{
			stmts = new Stmts();
		}
		
	}
	 
}

class Assign 
{
	Expr expr;
	
	public Assign() throws ParserException
	{
		int idnum;
		if(Code.variables.containsKey(Lexer.ident))
		{
			idnum= Code.variables.get(Lexer.ident);
		}
		else throw new ParserException(Lexer.nextChar);
		
		Lexer.lex();
		
		if(Lexer.nextToken == Token.ASSIGN_OP)
		{
			Lexer.lex();
		}
		else throw new ParserException(Lexer.nextChar);
		
		expr= new Expr();
		
		if(Lexer.nextToken == Token.SEMICOLON)
		{
			Lexer.lex();
		}
		else if (Lexer.nextToken == Token.RIGHT_PAREN)
		{
			Lexer.lex();
		}
		else throw new ParserException(Lexer.nextChar);
		 
		
		if(!Loop.inc)
		{
			if(idnum<3)
			{
				Code.gen("istore_"+(idnum+1));
			}
			else
			{
				Code.gen("istore_"+(idnum+1),2);
			}
		}
		else
		{
			Loop.storeValue[Loop.incpt] = "istore_"+(idnum+1);
			Loop.incpt++;
			Loop.inc = false;
		}
	}
}
	
	
class Loop 
{
	
	static String[] storeValue = new String[8];
	static int incpt =0;
	Assign assign,assign1;
	Rexpr rexpr ;
	int loopExprstart;
	Stmt stmt,stmt1,stmt2,stmt3,stmt4;
	static boolean inc = false;
	int  loopStart[] = new int[1];
	int loopEnd;
	static boolean toPrint=false;
	
	public Loop() throws ParserException
	{
		if(Lexer.nextToken == Token.KEY_FOR)
		{
			Lexer.lex();
		}
		if(Lexer.nextToken == Token.LEFT_PAREN)
		{
			Lexer.lex();
		}
		else throw new ParserException(Lexer.nextChar);
		
		loopStart[0] = Code.codeptr;
		
		if(Lexer.nextToken != Token.SEMICOLON && Lexer.nextToken == Token.ID)
		{
			assign = new Assign();
		}
		else if(Lexer.nextToken == Token.SEMICOLON)
		{
			Lexer.lex();
		}
		
		loopExprstart = Code.codeptr;
		
		if(Lexer.nextToken != Token.SEMICOLON && Lexer.nextToken == Token.ID)
		{
			rexpr = new Rexpr(loopStart);
			Lexer.lex();
		}
		
		if(Lexer.nextToken != Token.SEMICOLON && Lexer.nextToken == Token.ID)
		{
			inc = true;
			toPrint = true;
			assign1 = new Assign();
		}
		else if(Lexer.nextToken == Token.RIGHT_PAREN){
			Lexer.lex();
		}
		else if(Lexer.nextToken == Token.LEFT_BRACE)
		{
			Lexer.lex();
		}
		else throw new ParserException(Lexer.nextChar);
		
		if(Lexer.nextToken == Token.LEFT_BRACE)
		{
			stmt3 = new Stmt();
			
		}
		//else
		//	toPrint=false;

		
		if(Lexer.nextToken== Token.ID)
		{
			stmt1 = new Stmt();
		}
		if(Lexer.nextToken== Token.KEY_IF)
		{
			stmt4 = new Stmt();
		}
		if(Lexer.nextToken== Token.KEY_FOR)
		{
			toPrint=false;
			stmt2 = new Stmt();
			if(Lexer.nextToken == Token.KEY_END && Loop.storeValue[0]!=null && Loop.storeValue[4]!=null )
			{
				
				
				if(!Loop.storeValue[4].equals("0"))
				{
					String[] brk = Loop.storeValue[4].split("_");
					int id = Integer.parseInt(brk[1]);
					if(id<=3)
					{
						Code.gen("iload_"+(id));
					}
					else{
						Code.gen("iload_"+(id),2);
					}
					brk = Loop.storeValue[5].split("_");
					id = Integer.parseInt(brk[1]);
					if(id>=0 && id <=5){
							Code.gen("iconst_" + id);
						}
						else if(id<=127 && id>=-128){
							Code.gen("bipush " + id,2);
							
						}
						else if(id<=32767 && id>=-32768){
							Code.gen("sipush "+ id,3);
							
						}
					Code.gen(Loop.storeValue[6]);
					brk = Loop.storeValue[7].split("_");
					id = Integer.parseInt(brk[1]);
					if(id<=3){
						Code.gen("istore_"+(id));
						}
					else{
						Code.gen("istore_"+(id),2);
						}
					Loop.storeValue[4]="0";
				}
			}
		}

		
		if(Lexer.nextToken == Token.KEY_END && toPrint)
		{
			String[] brk = Loop.storeValue[0].split("_");
			int id = Integer.parseInt(brk[1]);
			if(id<=3)
			{
				Code.gen("iload_"+(id));
			}
			else{
				Code.gen("iload_"+(id),2);
			}
			brk = Loop.storeValue[1].split("_");
			id = Integer.parseInt(brk[1]);
			if(id>=0 && id <=5){
					Code.gen("iconst_" + id);
				}
				else if(id<=127 && id>=-128){
					Code.gen("bipush " + id,2);
					
				}
				else if(id<=32767 && id>=-32768){
					Code.gen("sipush "+ id,3);
					
				}
			Code.gen(Loop.storeValue[2]);
			brk = Loop.storeValue[3].split("_");
			id = Integer.parseInt(brk[1]);
			if(id<=3){
				Code.gen("istore_"+(id));
				}
			else{
				Code.gen("istore_"+(id),2);
				}
			
		
		}
		
		Code.gen("goto "+loopExprstart,3);
		loopEnd = Code.codeptr;
		Code.code[loopStart[0]] =Code.code[loopStart[0]] + " " + loopEnd; 
		toPrint=false;
		
	}
}

class Factor {  
	Expr expr;
	int i;

	public Factor() throws ParserException {
		switch (Lexer.nextToken) 
		{
		
			case Token.INT_LIT: 
			i = Lexer.intValue;
			Lexer.lex();
			if(!Loop.inc)
			{
				if(i>=0 && i <=5)
				{
					Code.gen("iconst_" + i);
				}
				else if(i<=127 && i>=-128)
				{
					Code.gen("bipush " + i,2);
				}
				else if(i<=32767 && i>=-32768)
				{
					Code.gen("sipush "+ i,3);
				}
			}
			else
			{
				if(i>=0 && i <=5)
				{
					Loop.storeValue[Loop.incpt] = "iconst_" + i;
					Loop.incpt++;
				}
				else if(i<=127 && i>=-128)
				{
					Loop.storeValue[Loop.incpt] = "bipush " + i;
					Loop.incpt++;
				}
				else if(i<=32767 && i>=-32768)
				{
					Loop.storeValue[Loop.incpt] = "sipush "+ i;
					Loop.incpt++;
				}
			}
			break;
					
		case Token.ID:
			int idnum;
			if(Code.variables.containsKey(Lexer.ident))
			{
				idnum = Code.variables.get(Lexer.ident);
			}
			else throw new ParserException(Lexer.nextChar);
			
			if(!Loop.inc)
			{	
				if(idnum<3)
				{
					Code.gen("iload_"+(idnum+1));
				}
				else
				{
					Code.gen("iload_"+(idnum+1),2);
				}
			}
			else
			{
				Loop.storeValue[Loop.incpt] = "iload_"+(idnum+1);
				Loop.incpt++;
			}
			Lexer.lex();
			break;
			
		case Token.LEFT_PAREN: 
			Lexer.lex();
			expr = new Expr();
			Lexer.lex(); 
			break;
			
		default:
			throw new ParserException(Lexer.nextChar);
		}
	}
}

class Cond {
	
	Rexpr rexpr;
	int condEnd;
	Stmt smt1,smt2,smt3,smt4;
	int elseEnd;
	int condExprstart;
	Assign assign;
	int  condStart[] = new int[1];
	
	public Cond() throws ParserException{
		if(Lexer.nextToken == Token.KEY_IF)
		{
			Lexer.lex();
		}
		if(Lexer.nextToken == Token.LEFT_PAREN)
		{
			Lexer.lex();
		}
		else throw new ParserException(Lexer.nextChar);
		
		rexpr = new Rexpr(condStart);
		if(Lexer.nextToken == Token.RIGHT_PAREN)
		{
			Lexer.lex();
		}
		else throw new ParserException(Lexer.nextChar);
		
		if(Lexer.nextToken==Token.LEFT_BRACE)
		{
			smt1 = new Stmt();
		}
		else if(Lexer.nextToken==Token.KEY_IF)
		{
			smt3 = new Stmt();
		}
		else if(Lexer.nextToken==Token.ID)
		{
			smt4 = new Stmt();
		}
		else throw new ParserException(Lexer.nextChar);
		
		if(Lexer.nextToken == Token.RIGHT_BRACE)
		{
			Lexer.lex();
		}
		
		condEnd = Code.codeptr;
		
		if(Lexer.nextToken == Token.KEY_ELSE)
		{
			int elseGotoCodePtr = Code.codeptr;
			Code.gen("goto",3);
			condEnd = Code.codeptr;
			Lexer.lex();
			if(Lexer.nextToken==Token.LEFT_BRACE || Lexer.nextToken==Token.ID || Lexer.nextToken == Token.KEY_IF)
			{
				smt2 = new Stmt();
			}
			
			if(Lexer.nextToken==Token.RIGHT_BRACE)
			{
				Lexer.lex();
			}
			
			elseEnd = Code.codeptr;
			Code.code[elseGotoCodePtr] = Code.code[elseGotoCodePtr] + " "+elseEnd;
		}
			Code.code[condStart[0]] = Code.code[condStart[0]]+" "+ condEnd;
	}
}


class Rexpr {
	
	String Op;
	Expr expr1;
	Expr expr2; 

	
	public Rexpr(int[] loopStart) throws ParserException
	{
		expr1 = new Expr();
		switch (Lexer.nextToken) 
		{
			case Token.EQ_OP:
				Op = "if_icmpne";
				Lexer.lex();
				break;
			case Token.NOT_EQ:
				Op = "if_icmpeq";
				Lexer.lex();
				break;
			case Token.LESSER_OP:
				Op = "if_icmpge";
				Lexer.lex();
				break;
			case Token.GREATER_OP:
				Op = "if_icmple";
				Lexer.lex();
				break;
			default:
				throw new ParserException(Lexer.nextChar);
		}
		
		expr2 = new Expr();
		loopStart[0] = Code.codeptr;
		Code.gen(Op,3);
	}
}

class Expr 
{  
	Term term;
	Expr expr;
	char Op;

	public Expr() throws ParserException 
	{
		term = new Term();
		if (Lexer.nextToken == Token.ADD_OP || Lexer.nextToken == Token.SUB_OP) 
		{
			Op = Lexer.nextChar;
			Lexer.lex();
			expr = new Expr();
			
			if(!Loop.inc)
				Code.gen(Code.opcode(Op));
			else
			{
				Loop.storeValue[Loop.incpt] = Code.opcode(Op);
				Loop.incpt++;
			}
		}
	}
}

class Term {  
	Factor factor;
	Term term;
	char Op;

	public Term() throws ParserException 
	{
		factor = new Factor();
		if (Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP) 
		{
			Op = Lexer.nextChar;
			Lexer.lex();
			term = new Term();
			if(!Loop.inc)
				Code.gen(Code.opcode(Op));
			else
			{
				Loop.storeValue[Loop.incpt] = Code.opcode(Op);
				Loop.incpt++;
			}
		}
	}
}



class Code {
	
	static String[] code = new String[100];
	static int codeptr = 0;
	static HashMap<Character,Integer> variables = new HashMap<Character, Integer>();
		
	public static void gen(String str, int Len)
	{
		code[codeptr] = str;
		codeptr=codeptr+Len;
	}
	
	public static void gen(String str) 
	{
		code[codeptr] = str;
		codeptr++;
	}
	

	public static String opcode(char op) 
	{
		switch(op) 
		{
			case '+' : return "iadd";
			case '-':  return "isub";
			case '*':  return "imul";
			case '/':  return "idiv";
			default: return "";
		}
	}
	
	public static void output() 
	{
		for (int i=0; i<codeptr; i++)
		{
			if(code[i]!=null)
				System.out.println(i + ": " + code[i]);
		}
	}
	 
}

class ParserException extends Exception 
{
	private static final long serialVersionUID = 1L;
	public ParserException(char ch) 
	{ 
		super("Error at character : '" + ch+"'"); 
	}
}
