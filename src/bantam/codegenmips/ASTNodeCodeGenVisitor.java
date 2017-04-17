/*
 * File: ASTNodeCodeGenVisitor.java
 * CS461 Project 4B
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/17/17
 */

package bantam.codegenmips;

import bantam.ast.*;
import bantam.util.ClassTreeNode;
import bantam.util.Location;
import bantam.util.SymbolTable;
import bantam.visitor.NumLocalVarsVisitor;
import bantam.visitor.Visitor;
import java.util.List;
import java.util.Map;

/**
 * Generates code for nodes in the AST
 */
public class ASTNodeCodeGenVisitor extends Visitor {

    /**
     * Support object which writes the mips code to the output file.
     */
    private MipsSupport assemblySupport;
    /**
     * The ClassTreeNode that this object is currently generating code for.
     */
    private ClassTreeNode treeNode;
    /**
     * A map which maps method signatures to the number of local variables used within the
     * method.
     */
    private Map<String, Integer> numLocalVars;
    /**
     * The current number of local variables which have been assigned locations in the
     * current method.
     */
    private int currentLocalVars;
    /**
     * The current number of parameters which have been assigned locations in the current
     * method.
     */
    private int currentParam;
    /**
     * The total number of parameters that the current method has
     */
    private int totalParams;
    /**
     * The total number of local variables that the current method has.
     */
    private int totalLocalVars;

    /**
     * The current label that a break statement will break to.
     */
    private String breakToLabel;
    /**
     * The current label that a return statement will branch to.
     */
    private String returnLabel;
    /**
     * A map which takes string constants to their string constant id.
     */
    private Map<String, String> stringConstantsMap;
    /**
     * The list of all the names of the classes ordered by their type ids.
     */
    private List<String> classNamesList;
    /**
     * The scope level in which the fields reside.
     */
    private int fieldsScope;

    /**
     * Creates a new ASTNodeCodeGenVisitor with the given MipsSupport object, String
     * constants map and classNamesList
     * @param assemblySupport the Mips Support
     * @param stringConstantsMap the Map which takes String constants to their ids.
     * @param classNamesList The list of class names ordered by their type ids.
     */
    public ASTNodeCodeGenVisitor(MipsSupport assemblySupport,
                                 Map<String, String> stringConstantsMap,
                                 List<String> classNamesList){
        this.stringConstantsMap = stringConstantsMap;
        this.classNamesList = classNamesList;
        this.assemblySupport = assemblySupport;
    }

    /**
     * Generates the mips code for a specific class
     * @param treeNode The treeNode corresponding to the specific class.
     */
    public void genMips(ClassTreeNode treeNode){
        this.setTreeNode(treeNode);
        this.treeNode.getASTNode().accept(this);
    }

    /**
     * Sets the current ClassTreeNode that this object is generating code for.
     * @param treeNode the ClassTreeNode
     */
    public void setTreeNode(ClassTreeNode treeNode){
        this.treeNode = treeNode;
        this.fieldsScope = this.treeNode.getVarSymbolTable().getCurrScopeLevel() - 1;
        NumLocalVarsVisitor numLocalVarsVisitor = new NumLocalVarsVisitor();
        this.numLocalVars =
                numLocalVarsVisitor.getNumsAllLocalVars(this.treeNode.getASTNode());
    }

    /**
     * Generates the prologue for a method with the given number of local variables.
     * @param numVariables the number of local variables used in this method.
     */
    public void prolog(int numVariables){
        this.assemblySupport.genComment("Prolog:");
        this.assemblySupport.genComment("Pushing on $ra and $fp");
        this.push("$ra");
        this.push("$fp");
        this.assemblySupport
                .genComment("Making space for " + numVariables + " local vars");
        this.assemblySupport.genAdd("$fp", "$sp", -4*numVariables);
        this.assemblySupport.genMove("$sp", "$fp");
        this.returnLabel = this.assemblySupport.getLabel();
    }

    /**
     * Generates the epilogue for a method with the given number of local variables and
     * parameters.
     * @param numVariables The number of local variables that the method used
     * @param numParams The number of parameters that the method has
     */
    public void epilogue(int numVariables, int numParams){
        this.assemblySupport.genComment("Epilogue:");
        this.assemblySupport.genLabel(this.returnLabel);
        this.assemblySupport.genComment("Popping off local vars");
        this.assemblySupport.genAdd("$sp", "$fp", 4*numVariables);
        this.assemblySupport.genComment("Pop saved $ra and $fp");
        this.pop("$fp");
        this.pop("$ra");
        this.assemblySupport.genComment("Pop parameters");
        this.assemblySupport.genAdd("$sp", "$sp", 4*numParams);
        this.assemblySupport.genRetn();
    }

    /**
     * Generates code to push the contents of the given register onto the stack.
     * @param register The register to be pushed
     */
    private void push(String register){
        this.assemblySupport.genAdd("$sp","$sp",-4);
        this.assemblySupport.genStoreWord(register,0,"$sp");
    }

    /**
     * Generates code to pop the top of the stack into the given register.
     * @param register The destination register
     */
    private void pop(String register){
        this.assemblySupport.genLoadWord(register,0,"$sp");
        this.assemblySupport.genAdd("$sp","$sp",4);
    }

    /**
     * Generates code for the beginning of a BinaryExpr
     * @param node the BinaryExpr node
     */
    private void binaryProlog(BinaryExpr node){
        node.getLeftExpr().accept(this);
        this.push("$v0");
        node.getRightExpr().accept(this);
        this.pop("$v1");
    }

    /**
     * Gets the Location object which represents where the given variable with the given
     * reference resides
     * @param name The name of the variable
     * @param refname the reference string of the variable ("this", "super", or null)
     * @return the Location object
     */
    private Location getLocation(String name, String refname) {
        Location loc;
        if(refname == null){//refname is absent or "this"
            loc = (Location) this.treeNode.getVarSymbolTable().lookup(name);
        }
        else if ("this".equals(refname)){
            loc = (Location) this.treeNode
                    .getVarSymbolTable().lookup(name, this.fieldsScope);
        }
        else{//refname is super
            loc = (Location) this.treeNode.getVarSymbolTable().lookup(name,
                    this.fieldsScope -1 );
        }
        return loc;
    }

    /**
     * Helper method which generates the code shared between unary increment and decrement
     * expressions
     * @param node the ASTNode
     * @param num the number to increment or decrement by
     */
    private void genIncrDecr(UnaryExpr node, int num){
        node.getExpr().accept(this);
        if (node.isPostfix()){
            this.assemblySupport.genComment("Saving original value for postfix.");
            this.assemblySupport.genMove("$t0", "$v0");
        }

        this.assemblySupport.genAdd("$v0","$v0",num);
        this.assemblySupport.genStoreWord("$v0",0,"$v1");

        if(node.isPostfix()) {
            this.assemblySupport.genComment("Returning original value for postfix.");
            this.assemblySupport.genMove("$v0", "$t0");
        }
    }

    /**
     * Does nothing (handled in the FieldAdderVisitor)
     * @param field the field
     * @return null
     */
    public Object visit(Field field){
        return null;
    }

    /**
     * Generates code for the given method
     * @param node the method node
     * @return null
     */
    public Object visit(Method node){
        this.treeNode.getVarSymbolTable().enterScope();
        String name = this.treeNode.getName() + "." + node.getName();
        this.assemblySupport.genLabel(name);
        this.totalLocalVars = this.numLocalVars.get(name);
        this.prolog(totalLocalVars);
        this.currentLocalVars = 0;
        this.currentParam = 0;
        this.totalParams = node.getFormalList().getSize();
        node.getFormalList().accept(this);
        this.assemblySupport.genComment("Body: ");
        node.getStmtList().accept(this);
        this.epilogue(totalLocalVars, node.getFormalList().getSize());
        this.treeNode.getVarSymbolTable().exitScope();
        return null;
    }


    /**
     * Generates code for the given formal parameter
     * @param node the ASTNode corresponding to the formal parameter
     * @return null
     */
    public Object visit(Formal node) {
        this.assemblySupport.genComment("Parameter: " + node.getName());
        this.treeNode.getVarSymbolTable().add(node.getName(), new Location("$fp", 8 +
                this.totalLocalVars*4 + this.totalParams*4 - this.currentParam*4 - 4));
        this.currentParam++;
        return null;
    }


    /**
     * Generates code for a declaration statement
     *
     * @param node the declaration statement node
     * @return null
     */
    public Object visit(DeclStmt node) {
        this.treeNode.getVarSymbolTable()
                .add(node.getName(), new Location("$fp",this.currentLocalVars*4));
        this.assemblySupport.genComment("Init of DeclStatement: ");
        node.getInit().accept(this);
        this.assemblySupport.genComment("Assigning value to: " + node.getName());
        this.assemblySupport.genStoreWord("$v0", this.currentLocalVars*4, "$fp");
        this.currentLocalVars++;
        return null;
    }

    /**
     * Generates code for the given if-statement
     *
     * @param node the if statement node
     * @return null
     */
    public Object visit(IfStmt node) {
        this.assemblySupport.genComment("If statement: ");
        this.assemblySupport.genComment("Evaluating condition: ");
        node.getPredExpr().accept(this);
        String elseLabel = this.assemblySupport.getLabel();
        this.assemblySupport.genCondBeq("$v0", "$zero", elseLabel);
        this.assemblySupport.genComment("Then statement: ");
        this.treeNode.getVarSymbolTable().enterScope();
        node.getThenStmt().accept(this);
        this.treeNode.getVarSymbolTable().exitScope();
        if (node.getElseStmt() != null) {
            String endLabel = this.assemblySupport.getLabel();
            this.assemblySupport.genUncondBr(endLabel);
            this.assemblySupport.genLabel(elseLabel);
            this.assemblySupport.genComment("Else statement: ");
            this.treeNode.getVarSymbolTable().enterScope();
            node.getElseStmt().accept(this);
            this.treeNode.getVarSymbolTable().exitScope();
            this.assemblySupport.genLabel(endLabel);
        }
        else{
            this.assemblySupport.genLabel(elseLabel);
        }
        return null;
    }

    /**
     * Generates code for the given while statement
     *
     * @param node the while statement node
     * @return null
     */
    public Object visit(WhileStmt node) {
        this.assemblySupport.genComment("While statement: ");
        String oldBreakToLabel = this.breakToLabel;
        this.breakToLabel = this.assemblySupport.getLabel();
        this.assemblySupport.genComment("Evaluating condition: ");
        String condition = this.assemblySupport.getLabel();
        this.assemblySupport.genLabel(condition);
        node.getPredExpr().accept(this);
        this.assemblySupport.genCondBeq("$v0", "$zero", this.breakToLabel);
        this.assemblySupport.genComment("Body of while: ");
        this.treeNode.getVarSymbolTable().enterScope();
        node.getBodyStmt().accept(this);
        this.treeNode.getVarSymbolTable().exitScope();
        this.assemblySupport.genUncondBr(condition);
        this.assemblySupport.genLabel(this.breakToLabel);
        this.breakToLabel = oldBreakToLabel;
        return null;
    }

    /**
     * Generates code for the given for statement
     *
     * @param node the for statement node
     * @return null
     */
    public Object visit(ForStmt node) {
        this.assemblySupport.genComment("For loop: ");
        String oldBreakToLabel = this.breakToLabel;
        this.breakToLabel = this.assemblySupport.getLabel();
        if (node.getInitExpr() != null) {
            this.assemblySupport.genComment("Init Expr: ");
            node.getInitExpr().accept(this);
        }

        String conditionLabel = this.assemblySupport.getLabel();
        this.assemblySupport.genLabel(conditionLabel);
        if (node.getPredExpr() != null) {
            this.assemblySupport.genComment("Predicate: ");
            node.getPredExpr().accept(this);
            this.assemblySupport.genCondBeq("$v0", "$zero", this.breakToLabel);
        }

        this.assemblySupport.genComment("Body of for loop: ");
        this.treeNode.getVarSymbolTable().enterScope();
        node.getBodyStmt().accept(this);
        this.treeNode.getVarSymbolTable().exitScope();

        if (node.getUpdateExpr() != null) {
            this.assemblySupport.genComment("UpdateExpr: ");
            node.getUpdateExpr().accept(this);
        }

        this.assemblySupport.genUncondBr(conditionLabel);
        this.assemblySupport.genLabel(breakToLabel);
        this.breakToLabel = oldBreakToLabel;
        return null;
    }

    /**
     * Generates code for the given for break statement
     *
     * @param node the break statement node
     * @return null
     */
    public Object visit(BreakStmt node) {
        this.assemblySupport.genUncondBr(this.breakToLabel);
        return null;
    }

    /**
     * Generates code for the given Block Statement
     * @param node the block statement node
     * @return null
     */
    public Object visit(BlockStmt node) {
        this.treeNode.getVarSymbolTable().enterScope();
        node.getStmtList().accept(this);
        this.treeNode.getVarSymbolTable().exitScope();
        return null;
    }

    /**
     * Generates code for the given return statement
     *
     * @param node the return statement node
     * @return null
     */
    public Object visit(ReturnStmt node) {
        this.assemblySupport.genComment("Return Statement: ");
        if (node.getExpr() != null) {
            node.getExpr().accept(this);
        }
        else{
            this.assemblySupport.genLoadImm("$v0",0);
        }
        this.assemblySupport.genUncondBr(this.returnLabel);
        return null;
    }


    /**
     * Generates code for the given list of expressions
     * @param node the expression list node
     * @return null
     */
    public Object visit(ExprList node) {
        for (ASTNode e: node) {
            ((Expr) e).accept(this);
            this.push("$v0");
        }
        return null;
    }

    /**
     * Generates code for the given dispatch expression
     *
     * @param node the dispatch expression node
     * @return result of the visit
     */
    public Object visit(DispatchExpr node) {
        this.push("$a0");
        int methodIndex; // the index of the method within the dispatch table
        SymbolTable methodSymbolTable = this.treeNode.getMethodSymbolTable();
        String refName = null;
        if(node.getRefExpr() instanceof VarExpr){
            refName = ((VarExpr)node.getRefExpr()).getName();
        }
        if(node.getRefExpr() == null){//If the method has no reference, it is "this"
            refName = "this";
        }
        if("super".equals(refName)||"this".equals(refName)) {
            this.assemblySupport.genComment(
                    "Dispatch expression: "+refName+"."+node.getMethodName());
            if (refName.equals("super")){
                String parentName = this.treeNode.getParent().getName();
                methodIndex = (int)methodSymbolTable.lookup(node.getMethodName(),
                        methodSymbolTable.getCurrScopeLevel()-1);
                this.assemblySupport.genLoadAddr("$v0", parentName+"_dispatch_table");
            }
            else{
                methodIndex = (int)methodSymbolTable.lookup(node.getMethodName());
                this.assemblySupport.genLoadWord("$v0", 8, "$a0"); //dispatch table
            }
        }
        else{//If the reference is a different expression
            this.assemblySupport.genComment("Dispatch expression:"+
                    node.getRefExpr().getExprType()+"."+node.getMethodName());
            node.getRefExpr().accept(this);
            this.assemblySupport.genComment("The object reference is in $v0:");
            this.assemblySupport.genCondBeq("$v0", "$zero", "_null_pointer_error");
            //load the object on which the method is being called into a0
            this.assemblySupport.genMove("$a0", "$v0");
            methodIndex = (int)this.treeNode.getClassMap()
                    .get(node.getRefExpr().getExprType())
                    .getMethodSymbolTable()
                    .lookup(node.getMethodName());
            this.assemblySupport.genComment("Moving the dispatch table to $v0");
            this.assemblySupport.genLoadWord("$v0", 8, "$a0"); //dispatch table
        }
        this.assemblySupport.genComment("Loading "
                + node.getMethodName() +" into Register $t0: ");
        //load method into t0
        this.assemblySupport.genLoadWord("$t0", 4*methodIndex, "$v0");
        //put "this" back into a0 to calculate the arguments of the method
        this.push("$a0");
        this.assemblySupport.genLoadWord("$a0", 4, "$sp");
        //put the method reference onto the stack to calculate the arguments
        this.push("$t0");
        this.assemblySupport.genComment(
                "Evaluating Parameters for: " + node.getMethodName());
        node.getActualList().accept(this);
        this.assemblySupport.genComment("Calling Method: " + node.getMethodName());
        //now that the arguments have been calculated, return a0 and t0
        this.assemblySupport.genLoadWord("$t0", node.getActualList().getSize()*4,"$sp");
        this.assemblySupport
                .genLoadWord("$a0", node.getActualList().getSize()*4 + 4,"$sp");
        this.assemblySupport.genInDirCall("$t0");
        this.assemblySupport.genAdd("$sp", "$sp", 8);
        //return "this" to a0
        this.pop("$a0");
        this.assemblySupport.genComment("End of Dispatch Expr: " + node.getMethodName());
        return null;
    }

    /**
     * Generates code for an object instantiation
     *
     * @param node the new expression node
     * @return null
     */
    public Object visit(NewExpr node) {
        this.assemblySupport.genComment("NewExpr:");
        this.push("$a0");
        this.assemblySupport.genLoadAddr("$a0",node.getType()+"_template");
        this.assemblySupport.genDirCall("Object.clone");
        this.assemblySupport.genMove("$a0","$v0");
        this.push("$v0");
        this.assemblySupport.genDirCall(node.getType()+"_init");
        this.pop("$v0");
        this.pop("$a0");
        return null;
    }

    /**
     * Unimplemented method
     *
     * @param node the new array expression node
     * @return null
     */
    public Object visit(NewArrayExpr node) {
        this.assemblySupport.genComment("NEW ARRAY EXPR");
        return null;
    }

    /**
     * Generate code for an instanceof expression
     *
     * @param node the instanceof expression node
     * @return null
     */
    public Object visit(InstanceofExpr node) {
        node.getExpr().accept(this);
        if(node.getUpCheck()){//if an upcheck the value of the expression is true
            this.assemblySupport.genComment("Upcheck instanceof:");
            this.assemblySupport.genLoadImm("$v0",1);
        }
        else{
            this.assemblySupport.genComment("Downcheck instanceof:");
            String nullLabel = this.assemblySupport.getLabel();
            //If the object reference is null, branch to nullLabel
            this.assemblySupport.genCondBeq("$v0", "$zero", nullLabel);
            this.assemblySupport.genLoadWord("$t0",0,"$v0");
            int type = this.classNamesList.indexOf(node.getType());
            int numDescendants =
                    this.treeNode.getClassMap().get(node.getType()).getNumDescendants();
            this.assemblySupport.genLoadImm("$t1",type);
            this.assemblySupport.genComment(
                    "If instanceOf left operand is descendant of right operand");
            this.assemblySupport.genBinaryOp("sge","$v0","$t0","$t1");
            this.assemblySupport.genLoadImm("$t1",type+numDescendants);
            this.assemblySupport.genBinaryOp("sle","$v1","$t0","$t1");
            this.assemblySupport.genAnd("$v0","$v0","$v1");
            String endLabel = this.assemblySupport.getLabel();
            this.assemblySupport.genUncondBr(endLabel);
            this.assemblySupport.genComment("If instanceOf left operand is null: ");
            //null is an instanceof every object
            this.assemblySupport.genLabel(nullLabel);
            this.assemblySupport.genLoadImm("$v0", 1);
            this.assemblySupport.genLabel(endLabel);
        }
        return null;
    }

    /**
     * Generates code for a cast expression
     *
     * @param node the cast expression node
     * @return null
     */
    public Object visit(CastExpr node) {
        node.getExpr().accept(this);
        if(!node.getUpCast()){
            this.assemblySupport.genComment("Downcast Expr:");
            String endLabel = this.assemblySupport.getLabel();
            //If the object is null, skip the error checking
            this.assemblySupport.genCondBeq("$v0", "$zero", endLabel);
            this.assemblySupport.genLoadWord("$t0",0,"$v0");
            int type = this.classNamesList.indexOf(node.getType());
            int numDescendants =
                    this.treeNode.getClassMap().get(node.getType()).getNumDescendants();
            this.assemblySupport.genLoadImm("$t1",type);
            this.assemblySupport.genCondBlt("$t0","$t1","_class_cast_error");
            this.assemblySupport.genLoadImm("$t1",type+numDescendants);
            this.assemblySupport.genCondBgt("$t0","$t1","_class_cast_error");
            this.assemblySupport.genLabel(endLabel);
        }
        return null;
    }

    /**
     * Generates code for an assignment expression
     *
     * @param node the assignment expression node
     * @return null
     */
    public Object visit(AssignExpr node) {
        Location loc = this.getLocation(node.getName(), node.getRefName());
        this.assemblySupport.genComment("Assign Expr:");
        node.getExpr().accept(this);
        this.assemblySupport.genStoreWord("$v0", loc.getOffset(), loc.getBaseReg());
        return null;
    }

    /**
     * Unimplemented method
     *
     * @param node the array assignment expression node
     * @return null
     */
    public Object visit(ArrayAssignExpr node) {
        this.assemblySupport.genComment("ARRAY ASSIGN EXPR");
        return null;
    }

    /**
     * Generates code for a binary comparison equals
     *
     * @param node the binary comparison equals expression node
     * @return null
     */
    public Object visit(BinaryCompEqExpr node) {
        this.assemblySupport.genComment("Binary Equals Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("seq","$v0","$v1","$v0");
        return null;
    }

    /**
     * Generates code for a binary comparison not equals
     *
     * @param node the binary comparison not equals expression node
     * @return null
     */
    public Object visit(BinaryCompNeExpr node) {
        this.assemblySupport.genComment("Binary Not Equal Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("sne","$v0","$v1","$v0");
        return null;
    }

    /**
     * Generates code for a binary comparison less than
     *
     * @param node the binary comparison less than expression node
     * @return null
     */
    public Object visit(BinaryCompLtExpr node) {
        this.assemblySupport.genComment("Binary Less Than Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("slt","$v0","$v1","$v0");
        return null;
    }

    /**
     * Generates code for a binary comparison less than or equal to
     *
     * @param node the binary comparison less than or equal to expression node
     * @return null
     */
    public Object visit(BinaryCompLeqExpr node) {
        this.assemblySupport.genComment("Binary Less Than or Equal To Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("sle","$v0","$v1","$v0");
        return null;
    }

    /**
     * Generates code for a binary comparison greater than
     *
     * @param node the binary comparison greater than expression node
     * @return null
     */
    public Object visit(BinaryCompGtExpr node) {
        this.assemblySupport.genComment("Binary Greater Than Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("sgt","$v0","$v1","$v0");
        return null;
    }

    /**
     * Generates code for a binary comparison greater than or equal to
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return null
     */
    public Object visit(BinaryCompGeqExpr node) {
        this.assemblySupport.genComment("Binary Greater Than or Equal To Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("sge","$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return null
     */
    public Object visit(BinaryArithPlusExpr node) {
        this.assemblySupport.genComment("Binary Plus Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genAdd("$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return null
     */
    public Object visit(BinaryArithMinusExpr node) {
        this.assemblySupport.genComment("Binary Minus Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genSub("$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return null
     */
    public Object visit(BinaryArithTimesExpr node) {
        this.assemblySupport.genComment("Binary Times Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genMul("$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return null
     */
    public Object visit(BinaryArithDivideExpr node) {
        this.assemblySupport.genComment("Binary Divide Expr:");
        this.binaryProlog(node);
        //branch if division by zero
        this.assemblySupport.genCondBeq("$v0","$zero","_divide_zero_error");
        this.assemblySupport.genDiv("$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return null
     */
    public Object visit(BinaryArithModulusExpr node) {
        this.assemblySupport.genComment("Binary Modulus Expr:");
        this.binaryProlog(node);
        //branch if division by zero
        this.assemblySupport.genCondBeq("$v0","$zero","_divide_zero_error");
        this.assemblySupport.genMod("$v0","$v1","$v0");
        return null;
    }

    /**
     * Generates code for a binary logical AND expression node
     *
     * @param node the binary logical AND expression node
     * @return null
     */
    public Object visit(BinaryLogicAndExpr node) {
        this.assemblySupport.genComment("Logical And:");
        node.getLeftExpr().accept(this);
        String label = this.assemblySupport.getLabel();
        this.assemblySupport.genCondBeq("$v0","$zero",label);
        node.getRightExpr().accept(this);
        this.assemblySupport.genLabel(label);
        return null;
    }


    /**
     * Generates code for a binary logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return null
     */
    public Object visit(BinaryLogicOrExpr node) {
        this.assemblySupport.genComment("Logical Or:");
        node.getLeftExpr().accept(this);
        String label = this.assemblySupport.getLabel();
        this.assemblySupport.genCondBne("$v0","$zero",label);
        node.getRightExpr().accept(this);
        this.assemblySupport.genLabel(label);
        return null;
    }

    /**
     * Generates code for a unary negation expression node
     *
     * @param node the unary negation expression node
     * @return null
     */
    public Object visit(UnaryNegExpr node) {
        node.getExpr().accept(this);
        this.assemblySupport.genNeg("$v0","$v0");
        return null;
    }

    /**
     * Generates code for  a unary NOT expression node
     *
     * @param node the unary NOT expression node
     * @return null
     */
    public Object visit(UnaryNotExpr node) {
        this.assemblySupport.genComment("Unary Not Expr: ");
        node.getExpr().accept(this);
        String trueLabel = this.assemblySupport.getLabel();
        String endLabel = this.assemblySupport.getLabel();
        this.assemblySupport.genCondBeq("$v0", "$zero", trueLabel);
        this.assemblySupport.genLoadImm("$v0", 0);
        this.assemblySupport.genUncondBr(endLabel);
        this.assemblySupport.genLabel(trueLabel);
        this.assemblySupport.genComment("Node was true");
        this.assemblySupport.genLoadImm("$v0", 1);
        this.assemblySupport.genLabel(endLabel);
        return null;
    }

    /**
     * Generates code for a unary increment expression node
     *
     * @param node the unary increment expression node
     * @return null
     */
    public Object visit(UnaryIncrExpr node) {
        this.assemblySupport.genComment("Increment Expr: ");
        this.genIncrDecr(node, 1);
        return null;
    }

    /**
     * Generates code for a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return result of the visit
     */
    public Object visit(UnaryDecrExpr node) {
        this.assemblySupport.genComment("Decrement Expr: ");
        this.genIncrDecr(node, -1);
        return null;
    }

    /**
     * Generates code for a variable expression node, the address of the variable is
     * stored in $v1
     *
     * @param node the variable expression node
     * @return null
     */
    public Object visit(VarExpr node) {
        this.assemblySupport.genComment("VarExpr: " + node.getName());
        String refname = null;
        if(node.getRef()!=null){
            refname = ((VarExpr) node.getRef()).getName();
        }
        Location loc = this.getLocation(node.getName(), refname);
        if (loc != null) {//If the variable represents a field or local variable
            this.assemblySupport.genComment(
                    "Moves the address of " + node.getName() + " into $v1");
            this.assemblySupport.genMove("$v1", loc.getBaseReg());
            this.assemblySupport.genAdd("$v1", "$v1", loc.getOffset());
            this.assemblySupport.genComment(
                    "Loads the value of " + node.getName() + " into $v0");
            this.assemblySupport.genLoadWord("$v0", 0, "$v1");
        }
        else{
            if (node.getName().equals("this")){
                this.assemblySupport.genMove("$v0", "$a0");
                this.assemblySupport.genMove("$v1", "$a0");
            }
            else if (node.getName().equals("null")) {
                this.assemblySupport.genComment("Referenced variable was null.");
                this.assemblySupport.genMove("$v0", "$zero");
                this.assemblySupport.genMove("$v1", "$zero");
            }
            else {//Should never be reached (indicates an error in our Semantic Analyzer
                System.out.println("Variable undefined?");
            }
        }
        return null;
    }

    /**
     * Unimplemented method
     *
     * @param node the array expression node
     * @return null
     */
    public Object visit(ArrayExpr node) {
        this.assemblySupport.genComment("ARRAY EXPR");
        return null;
    }

    /**
     * Generates code for an int constant expression node
     *
     * @param node the int constant expression node
     * @return null
     */
    public Object visit(ConstIntExpr node) {
        this.assemblySupport.genLoadImm("$v0",Integer.parseInt(node.getConstant()));
        return null;
    }

    /**
     * Generates code for a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return null
     */
    public Object visit(ConstBooleanExpr node) {
        if(node.getConstant().equals("true")){
            this.assemblySupport.genLoadImm("$v0",1);
        }
        else{
            this.assemblySupport.genLoadImm("$v0",0);
        }
        return null;
    }

    /**
     * Generates code for a string constant expression node
     *
     * @param node the string constant expression node
     * @return null
     */
    public Object visit(ConstStringExpr node) {
        this.assemblySupport.genLoadAddr(
                "$v0",this.stringConstantsMap.get(node.getConstant()));
        return null;
    }
}
