/* Code By:- Pulkit Kumar Dhir 
 * Took all the instruction & discussions into consideration while writing TypeCheckVisitor.java  
 * */

package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor 
{

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception 
	{
		TypeCheckException(String message) 
		{
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		TypeName tyn1 = (TypeName) binaryChain.getE0().visit(this, null);
		TypeName tyn2 = (TypeName) binaryChain.getE1().visit(this, null);
		Token binaryToken = binaryChain.getE1().firstToken;

		if(binaryChain.getArrow().kind.equals(ARROW))
		{
			if(tyn1.equals(URL) && tyn2.equals(IMAGE))
				binaryChain.typeval = tyn2;

			else if(tyn1.equals(FILE) && tyn2.equals(IMAGE))
				binaryChain.typeval = tyn2;

			else if(tyn1.equals(FRAME) && binaryChain.getE1() instanceof FrameOpChain){
				if(binaryToken.kind.equals(KW_XLOC) || binaryToken.kind.equals(KW_YLOC)){

					binaryChain.typeval = INTEGER;
				}

				else if(binaryToken.kind.equals(KW_SHOW) || binaryToken.kind.equals(KW_HIDE) || binaryToken.kind.equals(KW_MOVE)){
					binaryChain.typeval = FRAME;
				}

				else throw new TypeCheckException("Illegal Type");
			}

			else if(tyn1.equals(IMAGE) && binaryChain.getE1() instanceof ImageOpChain){
				if(binaryToken.kind.equals(OP_WIDTH) || binaryToken.kind.equals(OP_HEIGHT)){
					binaryChain.typeval = INTEGER;
				}

				else if(binaryToken.kind.equals(KW_SCALE)){
					binaryChain.typeval = IMAGE;
				}

				else throw new TypeCheckException("Illegal Type");
			}

			else if(tyn1.equals(IMAGE) && tyn2.equals(FRAME))
				binaryChain.typeval = tyn2;

			else if(tyn1.equals(IMAGE) && tyn2.equals(FILE))
				binaryChain.typeval = NONE;

			else if(tyn1.equals(IMAGE) && (binaryChain.getE1() instanceof IdentChain) && tyn2.equals(IMAGE))
				binaryChain.typeval = IMAGE;
			else if(tyn1.equals(INTEGER) && (binaryChain.getE1() instanceof IdentChain) && tyn2.equals(INTEGER))
				binaryChain.typeval = INTEGER;


			else if(tyn1.equals(IMAGE) && binaryChain.getE1() instanceof FilterOpChain){
				if(binaryToken.kind.equals(OP_GRAY) || binaryToken.kind.equals(OP_BLUR) || binaryToken.kind.equals(OP_CONVOLVE)){
					binaryChain.typeval = IMAGE;
				}

				else throw new TypeCheckException("Illegal Type");
			}
			else throw new TypeCheckException("Illegal Type");

		}

		else if(binaryChain.getArrow().kind.equals(BARARROW)){
			if(tyn1.equals(IMAGE) && binaryChain.getE1() instanceof FilterOpChain){

				if(binaryToken.kind.equals(OP_GRAY) || binaryToken.kind.equals(OP_BLUR) || binaryToken.kind.equals(OP_CONVOLVE))
					binaryChain.typeval = IMAGE;


				else throw new TypeCheckException("Illegal Type");
			}
			else throw new TypeCheckException("Illegal Type");

		}

		else throw new TypeCheckException("Illegal Type");


		return binaryChain.typeval;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		Expression exp1=binaryExpression.getE0();
		Expression exp2=binaryExpression.getE1();
		Token operator=binaryExpression.getOp();
		TypeName tyn1=(TypeName) exp1.visit(this, null);
		TypeName tyn2=(TypeName) exp2.visit(this, null);
		if(operator.kind.equals(PLUS) ||operator.kind.equals(MINUS))
		{
			if(tyn1.equals(INTEGER) && tyn2.equals(INTEGER))
				binaryExpression.typeval=INTEGER;
			else if(tyn1.equals(IMAGE) && tyn2.equals(IMAGE))
				binaryExpression.typeval=IMAGE;
			else throw new TypeCheckException("Illegal Type");

		}

		else if(operator.kind.equals(TIMES))
		{
			if(tyn1.equals(INTEGER) && tyn2.equals(INTEGER))
				binaryExpression.typeval=INTEGER;
			else if(tyn1.equals(INTEGER) && tyn2.equals(IMAGE))
				binaryExpression.typeval=IMAGE;
			else if(tyn1.equals(IMAGE) && tyn2.equals(INTEGER))
				binaryExpression.typeval=IMAGE;
			else throw new TypeCheckException("Illegal Type");


		}

		else if(operator.kind.equals(DIV))
		{
			if(tyn1.equals(INTEGER) && tyn2.equals(INTEGER))
				binaryExpression.typeval=INTEGER;
			else throw new TypeCheckException("Illegal Type");
		}


		else if(operator.kind.equals(LT) ||operator.kind.equals(GT) || operator.kind.equals(LE) ||operator.kind.equals(GE))
		{
			if(tyn1.equals(INTEGER) && tyn2.equals(INTEGER))
				binaryExpression.typeval=BOOLEAN;
			else if(tyn1.equals(BOOLEAN) && tyn2.equals(BOOLEAN))
				binaryExpression.typeval=BOOLEAN;
			else throw new TypeCheckException("Illegal Type");

		}

		else if(operator.kind.equals(EQUAL) ||operator.kind.equals(NOTEQUAL)){
			if(tyn1.equals(tyn2))
				binaryExpression.typeval=BOOLEAN;

			else throw new TypeCheckException("Illegal Type");
		}else if(operator.kind.equals(AND) ||operator.kind.equals(OR))
		{
			if(tyn1.equals(tyn2))
				binaryExpression.typeval=BOOLEAN;

			else throw new TypeCheckException("Illegal Type");
		}else if(operator.kind.equals(MOD))
		{
			if(tyn1.equals(INTEGER) && tyn2.equals(INTEGER))
				binaryExpression.typeval=tyn1;
			else if(tyn1.equals(IMAGE) && tyn2.equals(INTEGER))
				binaryExpression.typeval=tyn1;
			else throw new TypeCheckException("IIllegal Type");
		}
		else throw new TypeCheckException("Illegal Type");

		return binaryExpression.typeval;
	}
	
	@Override
	public Object visitBlock(Block block,Object arg) throws Exception 
	{
		symtab.enterScope();
		for(Dec dec : block.getDecs())
		{
			dec.visit(this,null);
		}
		for(Statement st: block.getStatements())
		{
			st.visit(this,null);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression,Object arg) throws Exception 
	{
		booleanLitExpression.typeval = BOOLEAN;
		return booleanLitExpression.typeval;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain,Object arg) throws Exception 
	{
		if(symtab.lookup(identChain.firstToken.getText())!= null)
		{
			identChain.typeval = symtab.lookup(identChain.firstToken.getText()).typeval;
		}
		return identChain.typeval;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain,Object arg) throws Exception 
	{
		filterOpChain.getArg().visit(this, null);
		if(filterOpChain.getArg().getExprList().size() == 0)
		{
			filterOpChain.typeval = IMAGE;}
		else 
		{
			throw new TypeCheckException("Illegal Type");}
		return filterOpChain.typeval;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression,Object arg) throws Exception 
	{
		if(symtab.lookup(identExpression.firstToken.getText())!= null)
		{
			identExpression.typeval = symtab.lookup(identExpression.firstToken.getText()).typeval;
			identExpression.dec = symtab.lookup(identExpression.firstToken.getText());
		}
		return identExpression.typeval;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression,Object arg) throws Exception 
	{
		intLitExpression.typeval = INTEGER;
		return intLitExpression.typeval;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement,Object arg) throws Exception 
	{
		if(!ifStatement.getE().visit(this,null).equals(BOOLEAN))
		{
			throw new TypeCheckException("Illegal Type");}
		ifStatement.getB().visit(this,null);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement,Object arg) throws Exception 
	{
		if(!whileStatement.getE().visit(this,null).equals(BOOLEAN))
		{
			throw new TypeCheckException("Illegal Type");}
		whileStatement.getB().visit(this,null);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement,Object arg) throws Exception 
	{
		if(!sleepStatement.getE().visit(this,null).equals(INTEGER))
		{
			throw new TypeCheckException("Illegal Type");}
		return null;
	}

	@Override
	public Object visitProgram(Program program,Object arg) throws Exception 
	{
		for(ParamDec paramdec : program.getParams())
		{
			paramdec.visit(this,null);
		}
		program.getB().visit(this,null);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration,Object arg) throws Exception 
	{
		declaration.typeval = Type.getTypeName(declaration.getFirstToken());
		if(!symtab.insert(declaration.getIdent().getText(), declaration))
		{
			throw new TypeCheckException("Illegal Type");}
		return declaration.typeval;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement,Object arg) throws Exception 
	{
		if(!assignStatement.getVar().visit(this, null).equals(assignStatement.getE().visit(this, null)))
		{
			throw new TypeCheckException("Illegal Type");}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX,Object arg) throws Exception 
	{
		Dec decident = symtab.lookup(identX.firstToken.getText());
		if(decident != null)
		{
			identX.dec = decident;}
		else
		{
			throw new TypeCheckException("Illegal Type");}
		return identX.dec.typeval;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec,Object arg) throws Exception 
	{
		paramDec.typeval = Type.getTypeName(paramDec.getFirstToken());
		if(!symtab.insert(paramDec.getIdent().getText(), paramDec))
		{
			throw new TypeCheckException("Illegal Type");}
		return paramDec.typeval;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression,Object arg) 
	{
		constantExpression.typeval = INTEGER;
		return constantExpression.typeval;	
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain,Object arg) throws Exception 
	{
		frameOpChain.getArg().visit(this, null);
		if(frameOpChain.getFirstToken().kind.equals(KW_HIDE) || frameOpChain.getFirstToken().kind.equals(KW_SHOW))
		{
			if(frameOpChain.getArg().getExprList().size() == 0)
			{
				frameOpChain.typeval = NONE;}
			else 
			{
				throw new TypeCheckException("Illegal Type");}
		}
		else if(frameOpChain.getFirstToken().kind.equals(KW_YLOC) || frameOpChain.getFirstToken().kind.equals(KW_XLOC))
		{
			if(frameOpChain.getArg().getExprList().size() == 0)
			{
				frameOpChain.typeval = INTEGER;}
			else 
			{
				throw new TypeCheckException("Illegal Type");}
		}
		else if(frameOpChain.getFirstToken().kind.equals(KW_MOVE))
		{
			if(frameOpChain.getArg().getExprList().size() == 2)
			{
				frameOpChain.typeval = NONE;}
			else 
			{
				throw new TypeCheckException("Illegal Type");}
		}
		else 
		{
			throw new TypeCheckException("Illegal Type");}
		return frameOpChain.typeval;
	}	

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain,Object arg) throws Exception 
	{
		imageOpChain.getArg().visit(this,null);		
		if(imageOpChain.getFirstToken().kind.equals(OP_HEIGHT)  || imageOpChain.getFirstToken().kind.equals(OP_WIDTH))
		{
			if(imageOpChain.getArg().getExprList().size() == 0)
			{
				imageOpChain.typeval = INTEGER;}
			else 
			{
				throw new TypeCheckException("Illegal Type");}
		}
		else if(imageOpChain.getFirstToken().kind.equals(KW_SCALE))
		{
			if(imageOpChain.getArg().getExprList().size() == 1)
			{
				imageOpChain.typeval = IMAGE;}
			else
			{
				throw new TypeCheckException("Illegal Type");}
		}
		else 
		{
			throw new TypeCheckException("Illegal Type");}
		return imageOpChain.typeval;
	}	

	@Override
	public Object visitTuple(Tuple tuple,Object arg) throws Exception 
	{
		for(Expression ex : tuple.getExprList())
		{
			if(!ex.visit(this,null).equals(INTEGER))
				throw new TypeCheckException("Illegal Type");
		}		
		return null;
	}
}
