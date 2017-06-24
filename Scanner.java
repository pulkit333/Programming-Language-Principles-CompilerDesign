
/*
 * Code By:- Pulkit Kumar Dhir
 * 1. I am not throwing exception in case a comment is not closed , running it to EOF.
 * 2. I adding new line only when '\n' is encountered not when '\r' is encountered .
 * 3. I am using a HashMap to check keywords .
 * 4. Skip white space is only taking care of spaces rest are taken care in start switch case .
 * 5. Created relevant states and iterating through them using switch case .
 * 6. Using FindLineNumber function , linenum array list and binary search to find the position in that line.(linepos for any token).
 * 7. Throwing exceptions when required with relevant message included  
 * */

package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cop5556sp17.Scanner.Kind;

public class Scanner {
	/**
	 * Kind enum
	 */

	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		public String getText() {
			return text;
		}
	}

	public static enum State {
		Start, Int_literal, After_not, After_or, After_or_minus, After_equal, 
		After_less, After_minus, After_greater, After_divison, 
		In_ident,Start_comment,End_comment
	}

	Map<String, Kind> hash = new HashMap<String, Kind>();

	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
		public IllegalNumberException(String message){
			super(message);
		}
	}

	static class LinePos {
		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}

	public class Token {
		public final Kind kind;
		public final int pos;  
		public final int length;  

		public String getText() {
			if(kind == Kind.EOF)
				return Kind.EOF.getText();
			return chars.substring(pos, pos + length);
		}

		LinePos getLinePos(){
			return new LinePos(FindLineNumber(pos), pos - linenum.get(FindLineNumber(pos)));
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		public int intVal() throws NumberFormatException{
			return Integer.parseInt(chars.substring(pos, pos + length));
		}
		
		public boolean isKind(Kind kind) {
			// TODO Auto-generated method stub
			return this.kind.equals(kind);
		}
		
		public Kind getKind() {
			return kind;
		}
		  @Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }

	}


	private int skipWhiteSpace(int pos, int len){
		int pos2 = pos;

		if(pos2 < len){
			while(Character.isSpaceChar(chars.charAt(pos2))){
				pos2 ++;
				if(pos2 == len)
					break;
			}
		}

		return pos2;
	}

	private int FindLineNumber(int pos){
		int i = pos;
		int temp = -1;
		int start = 0;
		int end = linenum.size() - 1;
		while(i >= 0){
			temp = bianrySearch(start, end, linenum, i);
			if(temp == -1){
				i--;
				continue;
			}

			break;
		}
		return temp;
	}
	private int bianrySearch(int start, int end, List<Integer> al, int i){

		while(start <= end){
			int mid = (start + end)/2;
			int midNum = al.get(mid);
			if(i == midNum){
				return al.indexOf(i);
			}else if(i < midNum){
				end = mid - 1;
				continue;
			}else{
				start = mid + 1;
			}
		}

		return -1;
	}



	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		hash.put("boolean", Kind.KW_BOOLEAN);
		hash.put("false",Kind.KW_FALSE);
		hash.put("true",Kind.KW_TRUE);
		hash.put("frame",Kind.KW_FRAME);
		hash.put("file",Kind.KW_FILE);
		hash.put("screenheight",Kind.KW_SCREENHEIGHT);
		hash.put("screenwidth",Kind.KW_SCREENWIDTH);
		hash.put("url",Kind.KW_URL);
		hash.put("if",Kind.KW_IF);
		hash.put("while",Kind.KW_WHILE);
		hash.put("hide",Kind.KW_HIDE);
		hash.put("integer",Kind.KW_INTEGER);
		hash.put("move",Kind.KW_MOVE);
		hash.put("image",Kind.KW_IMAGE);
		hash.put("scale",Kind.KW_SCALE);
		hash.put("show",Kind.KW_SHOW);
		hash.put("xloc",Kind.KW_XLOC);
		hash.put("yloc",Kind.KW_YLOC);
		hash.put("blur",Kind.OP_BLUR);
		hash.put("convolve",Kind.OP_CONVOLVE);
		hash.put("gray",Kind.OP_GRAY);
		hash.put("height",Kind.OP_HEIGHT);
		hash.put("sleep",Kind.OP_SLEEP);
		hash.put("width",Kind.OP_WIDTH);
	}

	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		int len = chars.length();
		int ch;
		int startPos = 0;
		linenum.add(0);
		State state = State.Start;

		while(pos <= len){
			ch = pos < len ? chars.charAt(pos) : -1; 

			switch (state) {

			case Start:{
				pos = skipWhiteSpace(pos, len);
				startPos = pos;
				ch = pos < len ? chars.charAt(pos) : -1; 
				switch (ch) {

				case -1:{tokens.add(new Token(Kind.EOF, startPos, 0));pos++;}
				break;	
				case ';':{tokens.add(new Token(Kind.SEMI, startPos, 1));pos++;}
				break;
				case ',':{tokens.add(new Token(Kind.COMMA, startPos, 1));pos++;}
				break;	
				case '(':{tokens.add(new Token(Kind.LPAREN, startPos, 1));pos++;}
				break;
				case ')':{tokens.add(new Token(Kind.RPAREN, startPos, 1));pos++;}
				break;
				case '{':{tokens.add(new Token(Kind.LBRACE, startPos, 1));pos++;}
				break;					
				case '}':{tokens.add(new Token(Kind.RBRACE, startPos, 1));pos++;}
				break;
				case '+':{tokens.add(new Token(Kind.PLUS, startPos, 1));pos++;}
				break;				
				case '%':{tokens.add(new Token(Kind.MOD, startPos, 1));pos++;}
				break;
				case '&':{tokens.add(new Token(Kind.AND, startPos, 1));pos++;}
				break;
				case '*':{tokens.add(new Token(Kind.TIMES, startPos, 1));pos++;}
				break;
				case '!':{state = State.After_not;pos++;}
				break;
				case '=':{state = State.After_equal;pos++;}
				break;	
				case '<':{state = State.After_less;pos++;}
				break;
				case '>':{state = State.After_greater;pos++;}
				break;	
				case '-':{state = State.After_minus;pos++;}
				break;
				case '|':{state = State.After_or;pos++;}
				break;
				case '\n':{
					line++;
					pos++;
					linenum.add(pos);
				}break;
				case '/':{state = State.After_divison;pos++;}
				break;


				case '\t':{
					pos++;
				}break;

				case '\r':{
					pos++;
				}break;

				case '\b':{
					pos++;
				}break;

				default:{
					if(Character.isDigit(ch))
					{
						if(ch == '0'){
							tokens.add(new Token(Kind.INT_LIT, startPos, 1));
							pos++;
							state = State.Start;
						}
						else
						{
							state = State.Int_literal;
							pos++;
						}
					}
					else if(Character.isJavaIdentifierStart(ch))
					{
						state = State.In_ident;
						pos++;
					}
					else
					{
						throw new IllegalCharException("At line: " + FindLineNumber(pos) + 
								" at pos: " + (pos - linenum.get(FindLineNumber(pos))) + 
								" the character '" + Character.toString((char)ch) + 
								"' is not defined in the system.");
					}
				}
				break;
				}
			}
			break;

			case Int_literal:{
				if(Character.isDigit(ch)){
					pos++;
				}else{
					try{
						Integer.parseInt(chars.substring(startPos, pos));
						tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
						state = State.Start;
					}catch (NumberFormatException e){
						throw new IllegalNumberException("At line: " + 
								FindLineNumber(pos) + " at pos: " + 
								(pos - linenum.get(FindLineNumber(pos))) + 
								" the imput number: " + chars.substring(startPos, pos) + " exceed the limit of Integer range");
					}

				}
			}
			break;

			case In_ident:{
				if(Character.isJavaIdentifierPart(ch))
				{
					pos++;
				}
				else if(hash.containsKey(chars.substring(startPos, pos).toString()))
				{
					tokens.add(new Token(hash.get(chars.substring(startPos, pos)), startPos, pos - startPos));
					state = State.Start;
				}
				else
				{
					tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
					state = State.Start;
				}



			}
			break;

			case After_or:{
				if(ch == '-'){
					state = State.After_or_minus;
					pos++;
				}else{
					tokens.add(new Token(Kind.OR, startPos, 1));
					state = State.Start;
				}
			}
			break;

			case After_equal:{
				if(ch == '='){
					state = State.Start;
					tokens.add(new Token(Kind.EQUAL, startPos, 2));
					pos++;
				}else{
					throw new IllegalCharException("At line: " + FindLineNumber(pos) + " at pos: " + 
							(pos - linenum.get(FindLineNumber(pos))) + 
							" expected '=' but got '" + Character.toString((char)ch) + "'");
				}
			}
			break;

			case After_less:{
				if(ch == '='){
					tokens.add(new Token(Kind.LE, startPos, 2));
					pos++;
				}else if(ch == '-'){
					tokens.add(new Token(Kind.ASSIGN, startPos, 2));
					pos++;
				}else{
					tokens.add(new Token(Kind.LT, startPos, 1));
				}

				state = State.Start;
			}
			break;

			case After_greater:{
				if(ch == '='){
					tokens.add(new Token(Kind.GE, startPos, 2));
					pos++;
				}else{
					tokens.add(new Token(Kind.GT, startPos, 1));
				}

				state = State.Start;
			}
			break;

			case After_minus:{
				if(ch == '>'){
					tokens.add(new Token(Kind.ARROW, startPos, 2));
					pos++;
				}else{
					tokens.add(new Token(Kind.MINUS, startPos, 1));
				}

				state = State.Start;
			}
			break;

			case After_not:{
				if(ch == '='){
					tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));
					pos++;
				}else{
					tokens.add(new Token(Kind.NOT, startPos, 1));
				}

				state = State.Start;
			}
			break;

			case After_divison:{
				if(ch == '*'){
					state = State.Start_comment;
					pos++;
				}else{
					tokens.add(new Token(Kind.DIV, startPos, 1));
					state = State.Start;
				}
			}
			break;
			
			case Start_comment:{
				if(ch == '*'){
					state = State.End_comment;
					pos++;
				}else if(ch == -1){
					state = State.Start;
				}else if(ch == '\n' ){
					line++;
					pos++;
					linenum.add(pos);
				}
					
					else if( ch == '\r'){
						pos++;
				}else{
					pos++;
				}
			}
				break;
				
			case End_comment:{
				if(ch == '/'){
					state = State.Start;
					pos++;					
				}else{
					state = State.Start_comment;
				}	
			}
				break;

			case After_or_minus:{
				if(ch == '>'){
					tokens.add(new Token(Kind.BARARROW, startPos, 3));
					state = State.Start;
					pos++;
				}else{
					tokens.add(new Token(Kind.OR, startPos, 1));
					tokens.add(new Token(Kind.MINUS, startPos + 1, 1));
					state = State.Start;
				}
			}
			break;

			default:
				assert false : "Unknow state: " + state;
			}
		}

		return this;  
	}


	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum = 0;

	int line = 0;
	protected List<Integer> linenum = new ArrayList<Integer>();


	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	public LinePos getLinePos(Token t) {
		return t.getLinePos();
	}
}
/*
 * @ Pulkit Kumar Dhir
 */
