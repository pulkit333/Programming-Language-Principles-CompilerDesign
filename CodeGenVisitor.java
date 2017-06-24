package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.*;
import cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	* @param DEVEL
	* used as parameter to genPrint and genPrintTOS
	* @param GRADE
	* used as parameter to genPrint and genPrintTOS
	* @param sourceFileName
	* name of source file, may be null.
	*/
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
	super();
	this.DEVEL = DEVEL;
	this.GRADE = GRADE;
	this.sourceFileName = sourceFileName;
	// symbolTable = new SymbolTable();
	slotNum = 1;
	slotStack = new Stack<Integer>();
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	// SymbolTable symbolTable;
	int slotNum;
	Stack<Integer> slotStack;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
	cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
	className = program.getName();
	classDesc = "L" + className + ";";
	String sourceFileName = (String) arg;
	cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
	new String[] { "java/lang/Runnable" });
	cw.visitSource(sourceFileName, null);

	mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null, null);
	mv.visitCode();
	// Create label at start of code
	Label constructorStart = new Label();
	mv.visitLabel(constructorStart);
	// this is for convenience during development--you can see that the code
	// is doing something.
	CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
	// generate code to call superclass constructor
	mv.visitVarInsn(ALOAD, 0);

	mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
	// visit parameter decs to add each as field to the class
	// pass in mv so decs can add their initialization code to the
	// constructor.
	// ArrayList<ParamDec> params = program.getParams();
	int i = 0;
	for (ParamDec dec : program.getParams()) {
	dec.setSlot(i++);
	cw.visitField(0, dec.getIdent().getText(), dec.getValue().getJVMTypeDesc(), null, null);
	dec.visit(this, mv);
	}
	mv.visitInsn(RETURN);
	// create label at end of code
	Label constructorEnd = new Label();
	mv.visitLabel(constructorEnd);
	// finish up by visiting local vars of constructor
	// the fourth and fifth arguments are the region of code where the local
	// variable is defined as represented by the labels we inserted.
	mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
	mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
	// indicates the max stack size for the method.
	// because we used the COMPUTE_FRAMES parameter in the classwriter
	// constructor, asm
	// will do this for us. The parameters to visitMaxs don't matter, but
	// the method must
	// be called.
	mv.visitMaxs(1, 1);
	// finish up code generation for this method.
	mv.visitEnd();
	// end of constructor

	// create main method which does the following
	// 1. instantiate an instance of the class being generated, passing the
	// String[] with command line arguments
	// 2. invoke the run method.
	mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
	mv.visitCode();
	Label mainStart = new Label();
	mv.visitLabel(mainStart);
	// this is for convenience during development--you can see that the code
	// is doing something.
	CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
	mv.visitTypeInsn(NEW, className);
	mv.visitInsn(DUP);
	mv.visitVarInsn(ALOAD, 0);
	mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
	mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
	mv.visitInsn(RETURN);
	Label mainEnd = new Label();
	mv.visitLabel(mainEnd);
	mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
	mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
	mv.visitMaxs(0, 0);
	mv.visitEnd();

	// create run method
	mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
	mv.visitCode();
	Label startRun = new Label();
	mv.visitLabel(startRun);
	CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
	program.getB().visit(this, null);
	mv.visitInsn(RETURN);
	Label endRun = new Label();
	mv.visitLabel(endRun);
	mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
	// TODO visit the local variables
	mv.visitMaxs(1, 1);
	mv.visitEnd(); // end of run method

	cw.visitEnd();// end of class

	// generate classfile and return it
	return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
	assignStatement.getE().visit(this, arg);
	CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
	CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
	assignStatement.getVar().visit(this, arg);
	return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
	binaryChain.getE0().visit(this, 0);
	if (binaryChain.getArrow().equals(Kind.BARARROW)) 
	{
	mv.visitInsn(DUP);
	} 
	else 
	{
	if (binaryChain.getE0().getType() == URL) 
	{
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL",
	PLPRuntimeImageIO.readFromURLSig, false);
	} 
	else if (binaryChain.getE0().getType() == FILE) 
	{
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile",
	PLPRuntimeImageIO.readFromFileDesc, false);
	}
	}

	if (binaryChain.getArrow().equals(Kind.BARARROW)) 
	{
	binaryChain.getE1().visit(this, 3);
	} else 
	{
	binaryChain.getE1().visit(this, 1);
	}
	if (binaryChain.getE1() instanceof IdentChain) 
	{
	IdentChain identChain = (IdentChain) binaryChain.getE1();
	if ((identChain.getDec() instanceof ParamDec)) 
	{
	if (identChain.getDec().getValue() == TypeName.INTEGER) 
	{
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
	identChain.getDec().getValue().getJVMTypeDesc());
	}
	
	
	}
	else 
	{
	if (identChain.getDec().getValue() == TypeName.INTEGER) 
	{
	mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
	}
	else 
	{
	mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
	}
	}
	}

	return null;
	}

	/*@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
	// TODO Implement this
	TypeName t0 = binaryExpression.getE0().getType();
	TypeName t1 = binaryExpression.getE1().getType();
	Token op = binaryExpression.getOp();
	switch (op.kind) {
	case PLUS:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	if (t0 == TypeName.INTEGER) 
	mv.visitInsn(IADD);
	
	else 
	{
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
	}
	break;

	case MINUS:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	if (t0 == TypeName.INTEGER) 
	mv.visitInsn(ISUB);	
	else {
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
	}
	break;

	case TIMES:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	if ((t0 == TypeName.INTEGER) && (t1 == TypeName.INTEGER)) 
	mv.visitInsn(IMUL);
	

	 else if ((t0 == TypeName.INTEGER) && (t1 == TypeName.IMAGE)) {
	mv.visitInsn(SWAP);
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
	} 
	else 
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
	
	break;

	case DIV:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	if ((t0 == TypeName.INTEGER) && (t1 == TypeName.INTEGER))
	mv.visitInsn(IDIV);
	else 
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
	break;

	case MOD:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	if ((t0 == TypeName.INTEGER) && (t1 == TypeName.INTEGER)) {
	mv.visitInsn(IREM);
	
	} else {
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
	
	System.out.println("INVOKESTATIC " + PLPRuntimeImageOps.JVMName + " mod " + PLPRuntimeImageOps.modSig);

	}
	break;

	case OR:
	binaryExpression.getE0().visit(this, arg);
	Label or_l1 = new Label();
	mv.visitJumpInsn(IFNE, or_l1);
	binaryExpression.getE1().visit(this, arg);
	mv.visitJumpInsn(IFNE, or_l1);
	mv.visitInsn(ICONST_0);
	Label or_l2 = new Label();
	mv.visitJumpInsn(GOTO, or_l2);
	mv.visitLabel(or_l1);
	mv.visitInsn(ICONST_1);
	mv.visitLabel(or_l2);
	break;

	case AND:
	binaryExpression.getE0().visit(this, arg);
	Label and_l1 = new Label();
	mv.visitJumpInsn(IFEQ, and_l1);
	binaryExpression.getE1().visit(this, arg);
	mv.visitJumpInsn(IFEQ, and_l1);
	mv.visitInsn(ICONST_1);
	Label and_l2 = new Label();
	mv.visitJumpInsn(GOTO, and_l2);
	mv.visitLabel(and_l1);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(and_l2);
	break;

	case LT:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	Label lt_l1 = new Label();
	mv.visitJumpInsn(IF_ICMPGE, lt_l1);
	mv.visitInsn(ICONST_1);
	Label lt_l2 = new Label();
	mv.visitJumpInsn(GOTO, lt_l2);
	mv.visitLabel(lt_l1);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(lt_l2);
	break;

	case LE:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	Label le_l1 = new Label();
	mv.visitJumpInsn(IF_ICMPGT, le_l1);
	mv.visitInsn(ICONST_1);
	Label le_l2 = new Label();
	mv.visitJumpInsn(GOTO, le_l2);
	mv.visitLabel(le_l1);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(le_l2);
	break;

	case GT:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	Label gt_l1 = new Label();
	mv.visitJumpInsn(IF_ICMPLE, gt_l1);
	mv.visitInsn(ICONST_1);
	Label gt_l2 = new Label();
	mv.visitJumpInsn(GOTO, gt_l2);
	mv.visitLabel(gt_l1);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(gt_l2);
	break;

	case GE:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	Label ge_l1 = new Label();
	mv.visitJumpInsn(IF_ICMPLT, ge_l1);
	mv.visitInsn(ICONST_1);
	Label ge_l2 = new Label();
	mv.visitJumpInsn(GOTO, ge_l2);
	mv.visitLabel(ge_l1);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(ge_l2);
	break;

	case EQUAL:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	if (t0 == TypeName.INTEGER || t0 == BOOLEAN) {
	Label equal_l1 = new Label();
	mv.visitJumpInsn(IF_ICMPNE, equal_l1);
	mv.visitInsn(ICONST_1);
	Label equal_l2 = new Label();
	mv.visitJumpInsn(GOTO, equal_l2);
	mv.visitLabel(equal_l1);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(equal_l2);
	} else {
	Label equal_l1 = new Label();
	mv.visitJumpInsn(IF_ACMPNE, equal_l1);
	mv.visitInsn(ICONST_1);
	Label equal_l2 = new Label();
	mv.visitJumpInsn(GOTO, equal_l2);
	mv.visitLabel(equal_l1);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(equal_l2);
	}

	break;

	case NOTEQUAL:
	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	if (t0 == TypeName.INTEGER || t0 == BOOLEAN) {
	Label notequal_l1 = new Label();
	mv.visitJumpInsn(IF_ICMPEQ, notequal_l1);
	mv.visitInsn(ICONST_1);
	Label notequal_l2 = new Label();
	mv.visitJumpInsn(GOTO, notequal_l2);
	mv.visitLabel(notequal_l1);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(notequal_l2);
	} else {
	Label notequal_l1 = new Label();
	mv.visitJumpInsn(IF_ACMPEQ, notequal_l1);
	mv.visitInsn(ICONST_1);
	Label notequal_l2 = new Label();
	mv.visitJumpInsn(GOTO, notequal_l2);
	mv.visitLabel(notequal_l1);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(notequal_l2);
	}
	break;

	default:
	break;
	}
	return null;
	}*/
	
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {

	TypeName tn0 = binaryExpression.getE0().getType();
	TypeName tn1 = binaryExpression.getE1().getType();

	Token t = binaryExpression.getOp();

	binaryExpression.getE0().visit(this, arg);
	binaryExpression.getE1().visit(this, arg);
	if(t.kind == Kind.PLUS){
	if (tn0 == TypeName.INTEGER) {
	mv.visitInsn(IADD);
	} else {
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
	"add", PLPRuntimeImageOps.addSig, false);
	}
	}
	else if(t.kind == Kind.MINUS){
	if (tn0 == TypeName.INTEGER) {
	mv.visitInsn(ISUB);
	} else {
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
	"sub", PLPRuntimeImageOps.subSig, false);
	}
	}
	else if(t.kind == Kind.TIMES){
	if ((tn0 == TypeName.INTEGER) && (tn1 == TypeName.INTEGER)) {
	mv.visitInsn(IMUL);
	} else if ((tn0 == TypeName.INTEGER) && (tn1 == TypeName.IMAGE)) {
	mv.visitInsn(SWAP);
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
	"mul", PLPRuntimeImageOps.mulSig, false);
	} else {
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
	"mul", PLPRuntimeImageOps.mulSig, false);
	}
	}
	else if(t.kind == Kind.DIV){
	if ((tn0 == TypeName.INTEGER) && (tn1 == TypeName.INTEGER)) {
	mv.visitInsn(IDIV);
	} else {
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
	"div", PLPRuntimeImageOps.divSig, false);
	}
	}
	else if(t.kind == Kind.MOD){
	if ((tn0 == TypeName.INTEGER) && (tn1 == TypeName.INTEGER)) {
	mv.visitInsn(IREM);
	} else {
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
	"mod", PLPRuntimeImageOps.modSig, false);
	}
	}
	else if(t.kind == Kind.OR){
	Label startOr = new Label();
	Label endOr = new Label();
	mv.visitJumpInsn(IFNE, startOr);
	mv.visitInsn(ICONST_0);
	mv.visitJumpInsn(GOTO, endOr);
	mv.visitLabel(startOr);
	mv.visitInsn(ICONST_1);
	mv.visitLabel(endOr);
	}

	else if(t.kind == Kind.AND){
	Label startAnd = new Label();
	Label endAnd = new Label();
	mv.visitJumpInsn(IFEQ, startAnd);
	mv.visitInsn(ICONST_0);
	mv.visitJumpInsn(GOTO, endAnd);
	mv.visitLabel(startAnd);
	mv.visitInsn(ICONST_1);
	mv.visitLabel(endAnd);
	}
	else if(t.kind == Kind.LT){
	Label startLT = new Label();
	Label endLT = new Label();
	mv.visitJumpInsn(IF_ICMPGE, startLT);
	mv.visitInsn(ICONST_1);
	mv.visitJumpInsn(GOTO, endLT);
	mv.visitLabel(startLT);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(endLT);
	}
	else if(t.kind == Kind.LE){
	Label startLE = new Label();
	Label endLE = new Label();
	mv.visitJumpInsn(IF_ICMPGT, startLE);
	mv.visitInsn(ICONST_1);
	mv.visitJumpInsn(GOTO, endLE);
	mv.visitLabel(startLE);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(endLE);
	}
	else if(t.kind == Kind.GT){
	Label startGT = new Label();
	Label endGT = new Label();
	mv.visitJumpInsn(IF_ICMPLE, startGT);
	mv.visitInsn(ICONST_1);
	mv.visitJumpInsn(GOTO, endGT);
	mv.visitLabel(startGT);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(endGT);
	}

	else if(t.kind == Kind.GE){
	Label startGE = new Label();
	Label endGE = new Label();
	mv.visitJumpInsn(IF_ICMPLT, startGE);
	mv.visitInsn(ICONST_1);
	mv.visitJumpInsn(GOTO, endGE);
	mv.visitLabel(startGE);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(endGE);
	}
	else if(t.kind == Kind.EQUAL){
	if(tn0 == TypeName.INTEGER || tn0 == TypeName.BOOLEAN){
	Label startEq = new Label();
	Label endEq = new Label();
	mv.visitJumpInsn(IF_ICMPNE, startEq);
	mv.visitInsn(ICONST_1);
	mv.visitJumpInsn(GOTO, endEq);
	mv.visitLabel(startEq);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(endEq);
	}
	else{
	Label startEq = new Label();
	Label endEq = new Label();
	mv.visitJumpInsn(IF_ACMPNE, startEq);
	mv.visitInsn(ICONST_1);
	mv.visitJumpInsn(GOTO, endEq);
	mv.visitLabel(startEq);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(endEq);
	}
	}
	else if(t.kind == Kind.NOTEQUAL){
	if(tn0 == TypeName.INTEGER || tn0 == TypeName.BOOLEAN){
	Label startNot = new Label();
	Label endNot = new Label();
	mv.visitJumpInsn(IF_ICMPEQ, startNot);
	mv.visitInsn(ICONST_1);
	mv.visitJumpInsn(GOTO, endNot);
	mv.visitLabel(startNot);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(endNot);
	}
	else{
	Label startNot = new Label();
	Label endNot = new Label();
	mv.visitJumpInsn(IF_ACMPEQ, startNot);
	mv.visitInsn(ICONST_1);
	mv.visitJumpInsn(GOTO, endNot);
	mv.visitLabel(startNot);
	mv.visitInsn(ICONST_0);
	mv.visitLabel(endNot);
	}
	}

	return null;

	}


	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
	// TODO Implement this
	// symbolTable.enterScope();
	Label blockstart = new Label();
	mv.visitLineNumber(block.getFirstToken().getLinePos().line, blockstart);
	mv.visitLabel(blockstart);
	for (Dec dec : block.getDecs()) {
	dec.visit(this, mv);
	}
	for (Statement statement : block.getStatements()) {
	statement.visit(this, mv);
	if (statement instanceof BinaryChain) {
	mv.visitInsn(POP);
	// Debug
	}
	}
	// symbolTable.leaveScope();
	Label blockend = new Label();
	mv.visitLineNumber(0, blockend);
	mv.visitLabel(blockend);
	for (Dec dec : block.getDecs()) {
	mv.visitLocalVariable(dec.getIdent().getText(), dec.getValue().getJVMTypeDesc(), null, blockstart,
	blockend, dec.getSlot());
	slotNum--;
	slotStack.pop();
	}
	
	return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
	// TODO Implement this
	if (booleanLitExpression.getValue()) {
	mv.visitInsn(ICONST_1);
	} else {
	mv.visitInsn(ICONST_0);
	}
	return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
	if (constantExpression.getFirstToken().equals(Kind.KW_SCREENHEIGHT)) {
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",
	PLPRuntimeFrame.getScreenHeightSig, false);
	} else if (constantExpression.getFirstToken().equals(Kind.KW_SCREENWIDTH)) {
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",
	PLPRuntimeFrame.getScreenWidthSig, false);
	}
	return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
	slotStack.push(slotNum++);
	declaration.setSlot(slotStack.peek());
	if(declaration.getValue().equals(IMAGE) || declaration.getValue().equals(FRAME)){
	mv.visitInsn(ACONST_NULL);
	mv.visitVarInsn(ASTORE, declaration.getSlot());
	}
	return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
	Kind k = filterOpChain.getFirstToken().kind;
	switch (k) {
	case OP_BLUR:
	mv.visitInsn(ACONST_NULL);
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
	break;

	case OP_GRAY:
	if ((int) arg != 3) {
	mv.visitInsn(ACONST_NULL);
	}
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
	break;

	case OP_CONVOLVE:
	mv.visitInsn(ACONST_NULL);
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig,
	false);
	break;

	default:
	break;
	}
	return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
	frameOpChain.getArg().visit(this, arg);
	Kind k = frameOpChain.getFirstToken().kind;
	switch (k) {
	case KW_SHOW:
	mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc,false);
	break;

	case KW_HIDE:
	mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc,false);
	break;

	case KW_MOVE:
	mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc,false);
	break;

	case KW_XLOC:
	mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc,false);
	break;

	case KW_YLOC:
	mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc,
	false);
	
	break;

	default:
	break;
	}
	return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
	// define true if right, false is left.
	Boolean side = (int) arg == 1;
	if (side) {
	// right side operation
	if (identChain.getDec() instanceof ParamDec) {
	// file, url, integer, boolean
	switch (identChain.getDec().getValue()) {
	case INTEGER:
	mv.visitVarInsn(ALOAD, 0);
	mv.visitInsn(SWAP);
	mv.visitFieldInsn(PUTFIELD, className, identChain.getDec().getIdent().getText(),
	identChain.getDec().getValue().getJVMTypeDesc());
	identChain.getDec().setInitialized(true);

	

	break;

	case FILE:
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
	identChain.getDec().getValue().getJVMTypeDesc());
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
	PLPRuntimeImageIO.writeImageDesc, false);
	identChain.getDec().setInitialized(true);
	
	break;

	default:
	break;
	}
	} else {
	switch (identChain.getDec().getValue()) {
	case INTEGER:
	mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
	identChain.getDec().setInitialized(true);
	

	break;

	case IMAGE:
	mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
	identChain.getDec().setInitialized(true);
	
	

	break;

	case FILE:
	mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
	PLPRuntimeImageIO.writeImageDesc, false);
	identChain.getDec().setInitialized(true);
	
	
	break;

	case FRAME:
	if (identChain.getDec().getInitialized()) {
	mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
	PLPRuntimeFrame.createOrSetFrameSig, false);
	mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());

	
	} else {
	mv.visitInsn(ACONST_NULL);
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
	PLPRuntimeFrame.createOrSetFrameSig, false);
	mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
	identChain.getDec().setInitialized(true);

	
	}

	

	break;

	default:
	break;
	}
	}
	} else {
	// left side operation
	if (identChain.getDec() instanceof ParamDec) {
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
	identChain.getDec().getValue().getJVMTypeDesc());

	

	
	} else {
	if (identChain.getDec().getValue() == FRAME) {
	if (identChain.getDec().getInitialized()) {

	mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
	

	} else {
	mv.visitInsn(ACONST_NULL);
	
	}

	} else {
	mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
	

	}

	}

	}
	return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
	// TODO Implement this
	if (identExpression.getDec() instanceof ParamDec) {
	mv.visitVarInsn(ALOAD, 0);
	
	mv.visitFieldInsn(GETFIELD, className, identExpression.getDec().getIdent().getText(),
	identExpression.getDec().getValue().getJVMTypeDesc());
	} 
	else 
	{
	if (identExpression.getType() == TypeName.INTEGER|| identExpression.getType() == TypeName.BOOLEAN) 
	{
	mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
	} 
	else 
	{
	mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
	}
	}

	return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
	// TODO Implement this
	if (identX.getDec() instanceof ParamDec) 
	{
	mv.visitVarInsn(ALOAD, 0);
	mv.visitInsn(SWAP);
	mv.visitFieldInsn(PUTFIELD, className, identX.getDec().getIdent().getText(),
	identX.getDec().getValue().getJVMTypeDesc());
	} 
	
	else 
	{
	if (identX.getDec().getValue() == IMAGE) 
	{
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
	PLPRuntimeImageOps.copyImageSig, false);
	mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
	identX.getDec().setInitialized(true);
	} 
	else if (identX.getDec().getValue() == TypeName.INTEGER|| identX.getDec().getValue() == TypeName.BOOLEAN) 
	{
	mv.visitVarInsn(ISTORE, identX.getDec().getSlot());
	identX.getDec().setInitialized(true);
	} else 
	{
	mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
	identX.getDec().setInitialized(true);
	}
	}
	return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
	// TODO Implement this

	ifStatement.getE().visit(this, arg);
	Label l_false = new Label();
	Label l_true = new Label();
	mv.visitJumpInsn(IFEQ, l_false);
	mv.visitLabel(l_true);
	ifStatement.getB().visit(this, arg);
	mv.visitLabel(l_false);
	return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
	imageOpChain.getArg().visit(this, arg);
	switch (imageOpChain.getFirstToken().kind) {
	case OP_WIDTH:
	mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);

	

	break;

	case OP_HEIGHT:
	mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);

	break;

	case KW_SCALE:
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);

	

	break;

	default:
	break;
	}
	return null;
	}
	
	/*@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {

	imageOpChain.getArg().visit(this, arg);

	Kind k = imageOpChain.getFirstToken().kind;

	if(k.equals(OP_HEIGHT)){

	mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage",

	"getHeight", "()I", false);

	}

	else if(k.equals(KW_SCALE)){

	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,

	"scale", PLPRuntimeImageOps.scaleSig, false);

	}

	else if(k.equals(OP_WIDTH)){

	mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage",

	"getWidth", "()I", false);

	}

	else{

	throw new Exception("Invalid ImageOP");

	}

	return null;



	}*/
	


	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
	// TODO Implement this
	mv.visitLdcInsn(intLitExpression.value);
	return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
	// TODO Implement this
	// For assignment 5, only needs to handle integers and booleans
	MethodVisitor mv = (MethodVisitor) arg;

	TypeName typeName = paramDec.getValue();

	switch (typeName) {
	case INTEGER:
	mv.visitVarInsn(ALOAD, 0);
	mv.visitVarInsn(ALOAD, 1);
	indexLoader(paramDec.getSlot(), mv);
	mv.visitInsn(AALOAD);
	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
	mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
	break;

	case BOOLEAN:
	mv.visitVarInsn(ALOAD, 0);
	mv.visitVarInsn(ALOAD, 1);
	indexLoader(paramDec.getSlot(), mv);
	mv.visitInsn(AALOAD);
	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
	mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
	break;

	case FILE:
	mv.visitVarInsn(ALOAD, 0);
	mv.visitTypeInsn(NEW, "java/io/File");
	mv.visitInsn(DUP);
	mv.visitVarInsn(ALOAD, 1);
	indexLoader(paramDec.getSlot(), mv);
	mv.visitInsn(AALOAD);
	mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
	mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
	break;

	case FRAME:
	// illegal
	break;

	case IMAGE:
	// illegal
	break;

	case URL:

	mv.visitVarInsn(ALOAD, 0);
	mv.visitVarInsn(ALOAD, 1);
	indexLoader(paramDec.getSlot(), mv);
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
	mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");

	break;

	case NONE:

	break;

	default:
	break;
	}
	return null;

	}
	
	/*@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
	// TODO Implement this
	 //For assignment 5, only needs to handle integers and booleans
	MethodVisitor mv = (MethodVisitor) arg;

	TypeName typeName = paramDec.getValue();
	if(typeName.equals(INTEGER)){
	mv.visitVarInsn(ALOAD, 0);
	mv.visitVarInsn(ALOAD, 1);
	indexLoader(paramDec.getSlot(), mv);
	mv.visitInsn(AALOAD);
	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
	mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
	}

	else if(typeName.equals(BOOLEAN)){
	mv.visitVarInsn(ALOAD, 0);
	mv.visitVarInsn(ALOAD, 1);
	indexLoader(paramDec.getSlot(), mv);
	mv.visitInsn(AALOAD);
	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
	mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");}


	else if(typeName.equals(FILE)){
	mv.visitVarInsn(ALOAD, 0);
	mv.visitTypeInsn(NEW, "java/io/File");
	mv.visitInsn(DUP);
	mv.visitVarInsn(ALOAD, 1);
	indexLoader(paramDec.getSlot(), mv);
	mv.visitInsn(AALOAD);
	mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
	mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");}
	


	
	else if(typeName.equals(FRAME) || typeName.equals(IMAGE) ||typeName.equals(NONE)){
	}

	else if(typeName.equals(URL)){

	mv.visitVarInsn(ALOAD, 0);
	mv.visitVarInsn(ALOAD, 1);
	indexLoader(paramDec.getSlot(), mv);
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
	mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
	}
	
	else
	{
	
	}
	return null;

	}*/

	public void indexLoader(int index, MethodVisitor mv) {
	// mv.visitVarInsn(ALOAD, 1);
	switch (index) {
	case 0:
	mv.visitInsn(ICONST_0);
	break;

	case 1:
	mv.visitInsn(ICONST_1);
	break;

	case 2:
	mv.visitInsn(ICONST_2);
	break;

	case 3:
	mv.visitInsn(ICONST_3);
	break;

	case 4:
	mv.visitInsn(ICONST_4);
	break;

	case 5:
	mv.visitInsn(ICONST_5);
	break;

	default:
	mv.visitLdcInsn(index);
	break;
	}
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
	sleepStatement.getE().visit(this, arg);
	
	mv.visitInsn(I2L);
	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
	
	return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
	for (Expression expression : tuple.getExprList()) {
	expression.visit(this, arg);
	}
	return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
	// TODO Implement this
	Label while_guard = new Label();
	mv.visitJumpInsn(GOTO, while_guard);
	Label while_body = new Label();
	mv.visitLabel(while_body);
	whileStatement.getB().visit(this, arg);
	mv.visitLabel(while_guard);
	whileStatement.getE().visit(this, arg);
	mv.visitJumpInsn(IFNE, while_body);
	return null;
	}

}