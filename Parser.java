
/*
 * Code By:- Pulkit Kumar Dhir
 * I have edited my comments handling logic in Scanner.java . 
 * Took all the discussions into consideration while writing Parser.java  
 * Edited my Parser.java According the return types for Assignment 3

 * */

package cop5556sp17;

import cop5556sp17.Scanner.*;
import cop5556sp17.AST.*;
import static cop5556sp17.Scanner.Kind.*;
import java.util.ArrayList;

public class Parser {

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	ASTNode parse() throws SyntaxException {
		Program p=program();
		matchEOF();
		return p;
	}

	Token relOp() throws SyntaxException 
	{
		if(t.kind==LT||t.kind==LE||t.kind==GT||t.kind==GE||t.kind==EQUAL||t.kind==NOTEQUAL)
		{	
			Token t1=null;
			t1=t;
			consume();
			return t1;
			
		}
		else
			throw new SyntaxException("Illegal Factor");
	}

	Token weakOp() throws SyntaxException 
	{
		if(t.kind==PLUS||t.kind==MINUS||t.kind==OR)
		{	
			Token t1=null;
			t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal Factor");
	}

	Token strongOp() throws SyntaxException 
	{
		if(t.kind==TIMES||t.kind==DIV||t.kind==AND||t.kind==MOD)
		{	
			Token t1=null;
			t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal Factor");
	}

	Token arrowOp() throws SyntaxException 
	{
		if(t.kind==ARROW||t.kind==BARARROW)
		{	
			Token t1=null;
			t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal Factor");
	}

	Token filterOp() throws SyntaxException 
	{
		if(t.kind==OP_BLUR||t.kind==OP_GRAY||t.kind==OP_CONVOLVE)
		{	
			Token t1=null;
			t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal Factor");
	}

	Token frameOp() throws SyntaxException 
	{
		if(t.kind==KW_SHOW||t.kind==KW_HIDE||t.kind==KW_MOVE||t.kind==KW_XLOC||t.kind==KW_YLOC)
		{	
			Token t1=null;
			t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal Factor");
	}

	Token imageOp() throws SyntaxException 
	{
		if(t.kind==OP_WIDTH||t.kind==OP_HEIGHT||t.kind==KW_SCALE)
		{	
			Token t1=null;
			t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal Factor");
	}

	Expression expression() throws SyntaxException 
	{
		
		Expression e1=null;
		Expression e2=null;
		Token t4=t;
		e1=term();
		
		while(t.kind==LT||t.kind==LE||t.kind==GT||t.kind==GE||t.kind==EQUAL||t.kind==NOTEQUAL)
		{
			Token t1=null;
			t1=t;
			consume();
			e2=term();
			e1=new BinaryExpression(t4,e1,t1,e2);
			
		}
		return e1;
	}

	Expression term() throws SyntaxException 
	{
		Expression e1=null;
		Expression e2=null;
		Token t4=t;
		e1=elem();
		while(t.kind==PLUS||t.kind==MINUS||t.kind==OR)
		{
			Token t1=null;
			t1=t;
			consume();
			e2=elem();
			e1 = new BinaryExpression(t4,e1,t1,e2);
		}
		return e1;
	}

	Expression elem() throws SyntaxException 
	{
		Expression e1=null;
		Expression e2=null;
		Token t4=t;
		e1=factor();
		while(t.kind==TIMES||t.kind==MOD||t.kind==DIV||t.kind==AND)
		{
		
			Token t1=null;
			t1=t;
			consume();
			e2=factor();
			e1 = new BinaryExpression(t4,e1,t1,e2);
		}
		return e1;
	}

	Expression factor() throws SyntaxException 
	{
		Expression e1=null;		
		Kind kind = t.kind;
		switch (kind) 
		{
		case IDENT: 
		{
			Token t1=null;
			t1=t;
			e1 = new IdentExpression(t1);
			consume();
			
		}
		break;
		case INT_LIT: 
		{
			Token t1=null;
			t1=t;
			e1 = new IntLitExpression(t1);
			consume();			
		}
		break;
		case KW_TRUE:
		{
			Token t1=null;
			t1=t;
			e1 = new BooleanLitExpression(t1);
			consume();
		}break;
		case KW_FALSE: 
		{
			Token t1=null;
			t1=t;
			
			e1 = new BooleanLitExpression(t1);
			consume();
		}
		break;
		case KW_SCREENWIDTH:
		{
			Token t1=null;
			t1=t;
			
			e1 = new ConstantExpression(t1);
			consume();
		}
		break;
		case KW_SCREENHEIGHT: 
		{
			Token t1=null;
			t1=t;
			
			e1 = new ConstantExpression(t1);
			consume();
		}
		break;
		case LPAREN: 
		{
			consume();
			e1=expression();
			match(RPAREN);
		}
		break;
		default:
			throw new SyntaxException("illegal factor");
		}
		return e1;
	}

	Block block() throws SyntaxException
	{
		Token t1=t;
		ArrayList<Dec> dl = new ArrayList<Dec> ();
		ArrayList<Statement> sl = new ArrayList<Statement> ();
		
		
		if(t.kind==LBRACE)
		{
			consume();
			while(t.kind!=RBRACE)
			{
				switch(t.kind)
				{
				case KW_BOOLEAN:
				case KW_IMAGE:
				case KW_FRAME:
				case KW_INTEGER:
				{
					Dec d=dec();
					dl.add(d);
				}break;
				case OP_SLEEP:
				{
					Statement st=statement();
					sl.add(st);
				}break;
				case KW_WHILE:
				{
					Statement st=statement();
					sl.add(st);
				}break;
				case KW_IF:
				{
					Statement st=statement();
					sl.add(st);
				}break;
				case IDENT:
				case OP_BLUR:
				case OP_CONVOLVE:
				case OP_GRAY:
				case KW_SHOW:
				case KW_HIDE:
				case KW_MOVE:
				case KW_XLOC:
				case KW_YLOC:
				case OP_WIDTH:
				case OP_HEIGHT:
				case KW_SCALE:
				{
					Statement st=statement();
					sl.add(st);
				}break;
				default:
					throw new SyntaxException("Illegal Factor");

				}
			}
			match(RBRACE);
			return new Block(t1,dl,sl);
		}


		else
			throw new SyntaxException("Illegal Factor");
	
	}


	Program program() throws SyntaxException
	{
		Token t1 = t;
		ArrayList<ParamDec> ll = new ArrayList<ParamDec> ();
		ParamDec pd1 = null;
		ParamDec pd2 = null;
		Block b1 =null;
		Block b2 =null;
		if(t.kind==IDENT)
		{
			consume();
			if(t.kind==KW_INTEGER||t.kind==KW_BOOLEAN||t.kind==KW_URL||t.kind==KW_FILE)
			{
				pd1= paramDec();
				ll.add(pd1);
				while(t.kind==COMMA)
				{
					consume();
					pd2=paramDec();
					ll.add(pd2);
				}
				b1=block();
				return new Program(t1,ll,b1);
			}
			else
				b2=block();
			return new Program(t1,ll,b2);
		}
		else
			throw new SyntaxException("Illegal Factor");
		 
	}

	ParamDec paramDec() throws SyntaxException 
	{
		ParamDec pd = null; 
		if(t.kind==KW_INTEGER||t.kind==KW_BOOLEAN||t.kind==KW_URL||t.kind==KW_FILE)
		{	
			Token t1 = t;
			consume();
			pd = new ParamDec(t1,t);
			match(IDENT);
		}
		else
			throw new SyntaxException("Illegal Factor");
		return pd ;
	}

	Dec dec() throws SyntaxException 
	{
		Dec d = null;
		if(t.kind==KW_INTEGER||t.kind==KW_BOOLEAN||t.kind==KW_IMAGE||t.kind==KW_FRAME)
		{	
			Token t1 = t;
			consume();
			d = new Dec(t1,t);			
			match(IDENT);
		}
		else
			throw new SyntaxException("Illegal Factor");
		return d ;
	}

	Statement assign() throws SyntaxException
	{
		Statement St = null;
		Token t1=t;
		Expression e1= null; 
		if(t.kind==IDENT)
		{
			IdentLValue val = new IdentLValue(t);
			ChainElem ch = chainElem();
			if(t.kind==ASSIGN)
			{
				consume();
				e1=expression();
				St = new AssignmentStatement(t1,val,e1);
			}
			else
				throw new SyntaxException("Illegal Factor");	
		}
	
		return St;
	}

	Statement statement() throws SyntaxException 
	{
		Statement St =null;
		Token t1=null;
		Expression e1= null;
		Block b1 =null;
		switch(t.kind)
		{
		case OP_SLEEP:
		{
			t1= t;
			
			consume();
			e1=expression();
			match(SEMI);
			St= new SleepStatement(t1,e1);
		}break;
		case KW_WHILE:
		{
			St =  whileStatement();
		}break;
		case KW_IF:
		{

			St = ifStatement();
		}break;
		case IDENT:
		{
			t1=t;
			IdentLValue val = new IdentLValue(t);
			ChainElem ch = chainElem();
			if(t.kind==ASSIGN)
			{
				consume();
				e1=expression();
				St = new AssignmentStatement(t1,val,e1);
				match(SEMI);
			}
			else if(t.kind==ARROW||t.kind==BARARROW)
			{
				Token t2= arrowOp();
				ChainElem ch1= chainElem();
				Chain ch4= new BinaryChain(t1,ch,t2,ch1);
				while(t.kind==ARROW||t.kind==BARARROW)
				{
					Token t3=t;
					consume();
					ChainElem ch2= chainElem();
					ch4= new BinaryChain(t1,ch4,t3,ch2);
				}
				match(SEMI);
				St =ch4;
			}
			else
				throw new SyntaxException(""+t.getLinePos());

		}break;
		case OP_CONVOLVE:
		case OP_GRAY:
		case OP_BLUR:
		{
			t1=t;
			consume();
			Tuple t2=arg();
			ChainElem ch=new FilterOpChain(t1,t2);
			Token t3=arrowOp();
			ChainElem ch1=chainElem();
			Chain ch3=new BinaryChain(t1,ch,t3,ch1);
			while(t.kind==ARROW||t.kind==BARARROW)
			{
				Token t4=t;
				consume();
				ChainElem ch4=chainElem();
				ch3=new BinaryChain(t1,ch3,t4,ch4);
			}
			match(SEMI);
			St=ch3;
		}break;
		case KW_HIDE:
		case KW_MOVE:
		case KW_SHOW:
		case KW_XLOC:
		case KW_YLOC:
		{
			t1=t;
			consume();
			Tuple t2=arg();
			ChainElem ch=new FrameOpChain(t1,t2);
			Token t3=arrowOp();
			ChainElem ch1=chainElem();
			Chain ch3=new BinaryChain(t1,ch,t3,ch1);
			while(t.kind==ARROW||t.kind==BARARROW)
			{
				Token t4=t;
				consume();
				ChainElem ch4=chainElem();
				ch3=new BinaryChain(t1,ch3,t4,ch4);
			}
			match(SEMI);
			St=ch3;
		}break;
		case OP_WIDTH:
		case OP_HEIGHT:
		case KW_SCALE:
		{
			t1=t;
			consume();
			Tuple t2=arg();
			ChainElem ch=new ImageOpChain(t1,t2);
			Token t3=arrowOp();
			ChainElem ch1=chainElem();
			Chain ch3=new BinaryChain(t1,ch,t3,ch1);
			while(t.kind==ARROW||t.kind==BARARROW)
			{
				Token t4=t;
				consume();
				ChainElem ch4=chainElem();
				ch3=new BinaryChain(t1,ch3,t4,ch4);
			}
			match(SEMI);
			St=ch3;
		}break;
		default:
			throw new SyntaxException("Illegal Factor");
		}
		return St;
	}


	WhileStatement whileStatement() throws SyntaxException {
		Token t1=t;
		match(KW_WHILE);
		match(LPAREN);
		Expression e1=expression();
		match(RPAREN);
		
		Block b1= block();
		return new WhileStatement(t1,e1,b1);
	}

	IfStatement ifStatement() throws SyntaxException {
		Token t1=t;
		match(KW_IF);
		match(LPAREN);
		Expression e1=expression();
		match(RPAREN);
		Block b1= block();
		return new IfStatement(t1,e1,b1);
	}

	Chain chain() throws SyntaxException 
	{
		Token t1=t;
		Chain ch1=chainElem();
		Token t2= arrowOp();
		ChainElem ch2=chainElem();
		Chain ch=new BinaryChain(t1,ch1,t2,ch2);
		while(t.kind==ARROW||t.kind==BARARROW)
		{
			Token t3=t;
			consume();
			ChainElem ch3=chainElem();
			ch=new BinaryChain(t1,ch,t3,ch3);
		}
		return ch;
	}

	ChainElem chainElem() throws SyntaxException 
	{
		ChainElem ch = null;
				
		if(t.kind==IDENT)
		{
			ch = new IdentChain(t);
			consume();			
		}
		else if(t.kind==OP_BLUR||t.kind==OP_GRAY||t.kind==OP_CONVOLVE)
		{
			Token t1= filterOp();
			Tuple tu= arg();
			ch = new FilterOpChain(t1,tu);
		}
		else if(t.kind==KW_SHOW||t.kind==KW_HIDE||t.kind==KW_MOVE||t.kind==KW_XLOC||t.kind==KW_YLOC)
		{
			Token t1= frameOp();
			Tuple tu=arg();
			ch = new FrameOpChain(t1,tu);
		}
		else if(t.kind==OP_WIDTH||t.kind==OP_HEIGHT||t.kind==KW_SCALE)
		{
			Token t1= imageOp();
			Tuple tu=arg();
			ch = new ImageOpChain(t1,tu);
		}
		else
			throw new SyntaxException("");
		return ch;
	}

	 Tuple arg() throws SyntaxException 
	{
		ArrayList<Expression> l = new ArrayList<Expression> ();
		Token t1=t;
		Expression e1=null;
		Expression e2=null;
		if(!t.kind.equals(LPAREN))
		{
			//consume();
		}
		else if(t.kind==LPAREN)
		{
			consume();
			e1 =expression();
			l.add(e1);
			while(t.kind==COMMA)
			{
				consume();
				e2 =expression();
				l.add(e2);
			}
			match(RPAREN);
			
		}
		else
			throw new SyntaxException("");
		return new Tuple(t1,l);
	}

	private Token matchEOF() throws SyntaxException 
	{
		if (t.kind==EOF) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	private Token match(Kind kind) throws SyntaxException 
	{
		if (t.kind==kind) 
		{
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	private Token match(Kind... kinds) throws SyntaxException 
	{
		// TODO. Optional but handy
		return null; //replace this statement
	}

	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}