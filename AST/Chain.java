package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	public TypeName typeval = null;
	//public Dec Dec;
	
	public Chain(Token firstToken) {
		super(firstToken);
	}

	public TypeName getType(){
		return typeval;
	}

}
