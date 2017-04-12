package bantam.codegenmips;

import bantam.ast.*;
import bantam.util.ClassTreeNode;
import bantam.util.Location;
import bantam.util.SymbolTable;
import bantam.visitor.NumLocalVarsVisitor;
import bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by joseph on 4/5/17.
 */
public class ASTNodeCodeGenVisitor extends Visitor {

    private MipsSupport assemblySupport;
    private ClassTreeNode treeNode;
    private Map<String, Integer> numLocalVars;
    private int currentLocalVars;
    private String breakToLabel;
    private String returnLabel;
    private Map<String, String> stringConstantsMap;
    private List<String> classNamesList;

    public ASTNodeCodeGenVisitor(MipsSupport assemblySupport,
                                 Map<String, String> stringConstantsMap, List<String> classNamesList){
        this.stringConstantsMap = new HashMap<>();
        for(Map.Entry<String,String> entry : stringConstantsMap.entrySet()){
            stringConstantsMap.put(entry.getValue(),entry.getKey());
        }
        this.classNamesList = classNamesList;
        this.assemblySupport = assemblySupport;
    }

    public void genMips(ClassTreeNode treeNode){
        this.setTreeNode(treeNode);
        this.treeNode.getASTNode().accept(this);
    }

    public void setTreeNode(ClassTreeNode treeNode){
        this.treeNode = treeNode;
        NumLocalVarsVisitor numLocalVarsVisitor = new NumLocalVarsVisitor();
        this.numLocalVars = numLocalVarsVisitor.getNumsAllLocalVars(this.treeNode.getASTNode());
    }

    private void prolog(int numVariables){
        this.assemblySupport.genComment("Prolog:");
        this.assemblySupport.genComment("Pushing on $ra and $fp");
        this.push("$ra");
        this.push("$fp");
        this.assemblySupport.genComment("Making space for " + numVariables + " local vars");
        this.assemblySupport.genAdd("$fp", "$sp", -4*numVariables);
        this.assemblySupport.genMove("$sp", "$fp");
    }

    private void epilogue(int numVariables, int numParams){
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

    private void push(String register){
        this.assemblySupport.genAdd("$sp","$sp",-4);
        this.assemblySupport.genStoreWord(register,0,"$sp");
    }

    private void pop(String register){
        this.assemblySupport.genLoadWord(register,0,"$sp");
        this.assemblySupport.genAdd("$sp","$sp",4);
    }

    private void binaryProlog(BinaryExpr node){
        node.getLeftExpr().accept(this);
        this.push("$v0");
        node.getRightExpr().accept(this);
        this.pop("$v1");
    }

    public Object visit(Field field){
        return null;
    }

    public Object visit(Method method){
        this.returnLabel = this.assemblySupport.getLabel();
        String name = this.treeNode.getName() + "." + method.getName();
        this.assemblySupport.genLabel(name);
        int numVariables = this.numLocalVars.get(name);
        this.prolog(numVariables);
        this.currentLocalVars = 0;
        this.assemblySupport.genComment("Body: ");
        method.getStmtList().accept(this);
        this.epilogue(numVariables, method.getFormalList().getSize());
        return null;
    }

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return result of the visit
     */
    public Object visit(DeclStmt node) {
        this.treeNode.getVarSymbolTable().add(node.getName(), new Location("$fp",this.currentLocalVars*4 ));
        this.assemblySupport.genComment("Init of DeclStatement: ");
        node.getInit().accept(this);
        this.assemblySupport.genComment("Assigning value to: " + node.getName());
        this.assemblySupport.genStoreWord("$v0", this.currentLocalVars*4, "$fp");
        this.currentLocalVars++;
        return null;
    }


    /**
     * Visit an if statement node
     *
     * @param node the if statement node
     * @return result of the visit
     */
    public Object visit(IfStmt node) {
        this.assemblySupport.genComment("If statement: ");
        this.assemblySupport.genComment("Evaluating condition: ");
        node.getPredExpr().accept(this);
        String elseLabel = this.assemblySupport.getLabel();
        this.assemblySupport.genCondBeq("$v0", "$zero", elseLabel);
        this.assemblySupport.genComment("Then statement: ");
        node.getThenStmt().accept(this);
        String endLabel = this.assemblySupport.getLabel();
        this.assemblySupport.genUncondBr(endLabel);
        this.assemblySupport.genLabel(elseLabel);
        this.assemblySupport.genComment("Else statement: ");
        node.getElseStmt().accept(this);
        this.assemblySupport.genLabel(endLabel);
        return null;
    }

    /**
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return result of the visit
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
        node.getBodyStmt().accept(this);
        this.assemblySupport.genUncondBr(condition);
        this.assemblySupport.genLabel(this.breakToLabel);
        this.breakToLabel = oldBreakToLabel;
        return null;
    }

    /**
     * Visit a for statement node
     *
     * @param node the for statement node
     * @return result of the visit
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
        node.getBodyStmt().accept(this);
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
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return result of the visit
     */
    public Object visit(BreakStmt node) {
        this.assemblySupport.genUncondBr(this.breakToLabel);
        return null;
    }


    /**
     * Visit a return statement node
     *
     * @param node the return statement node
     * @return result of the visit
     */
    public Object visit(ReturnStmt node) {
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
     * Visit a dispatch expression node
     *
     * @param node the dispatch expression node
     * @return result of the visit
     */
    //TODO: HOW DOES SUPER WORK?!?!?!
    //TODO: THE REST OF DISPATCH EXPR
    public Object visit(DispatchExpr node) {
        this.assemblySupport.genComment("Dispatch expression: ");
        if(node.getRefExpr()!=null) {
            Expr refExpr = node.getRefExpr();
            if(refExpr instanceof VarExpr && ((VarExpr)refExpr).getName().equals("super")){
                SymbolTable methodSymbolTable = this.treeNode.getMethodSymbolTable();
                methodSymbolTable.lookup(node.getMethodName(),methodSymbolTable.getCurrScopeLevel()-1);
            }
            node.getRefExpr().accept(this);
            this.assemblySupport.genCondBeq("v0", "$zero", "");
        }
        else{
            this.assemblySupport.genMove("$v0","$a0");
        }
        node.getActualList().accept(this);
        return null;
    }

    /**
     * Visit a new expression node
     *
     * @param node the new expression node
     * @return result of the visit
     */
    public Object visit(NewExpr node) {
        this.push("$a0");
        this.assemblySupport.genLoadAddr("$a0",node.getType()+"_template");
        this.assemblySupport.genDirCall("Object.clone");
        this.assemblySupport.genDirCall(node.getType()+"_init");
        this.pop("$a0");
        return null;
    }

    /**
     * Visit a new array expression node
     *
     * @param node the new array expression node
     * @return result of the visit
     */
    public Object visit(NewArrayExpr node) {
        this.assemblySupport.genComment("NEW ARRAY EXPR");
        return null;
    }

    /**
     * Visit an instanceof expression node
     *
     * @param node the instanceof expression node
     * @return result of the visit
     */
    public Object visit(InstanceofExpr node) {
        node.getExpr().accept(this);
        if(node.getUpCheck()){
            this.assemblySupport.genComment("Upcheck instanceof:");
            this.assemblySupport.genLoadImm("$v0",1);
        }
        else{
            this.assemblySupport.genComment("Downcheck instanceof:");
            this.assemblySupport.genLoadWord("$t0",0,"$v0");
            int type = this.classNamesList.indexOf(node.getType());
            int numDescendants = this.treeNode.getClassMap().get(node.getType()).getNumDescendants();
            this.assemblySupport.genLoadImm("$t1",type);
            this.assemblySupport.genBinaryOp("sge","$v0","$t0","$t1");
            this.assemblySupport.genLoadImm("$t1",type+numDescendants);
            this.assemblySupport.genBinaryOp("sle","$v1","$t0","$t1");
            this.assemblySupport.genAnd("$v0","$v0","$v1");
        }
        return null;
    }

    /**
     * Visit a cast expression node
     *
     * @param node the cast expression node
     * @return result of the visit
     */
    public Object visit(CastExpr node) {
        node.getExpr().accept(this);
        if(!node.getUpCast()){
            this.assemblySupport.genComment("Downcast Expr:");
            this.assemblySupport.genLoadWord("$t0",0,"$v0");
            int type = this.classNamesList.indexOf(node.getType());
            int numDescendants = this.treeNode.getClassMap().get(node.getType()).getNumDescendants();
            this.assemblySupport.genLoadImm("$t1",type);
            this.assemblySupport.genCondBlt("$t0","$t1","_class_cast_error");
            this.assemblySupport.genLoadImm("$t1",type+numDescendants);
            this.assemblySupport.genCondBgt("$t0","$t1","_class_cast_error");
        }
        return null;
    }

    /**
     * Visit an assignment expression node
     *
     * @param node the assignment expression node
     * @return result of the visit
     */
    public Object visit(AssignExpr node) {
        Location loc;
        if(!"super".equals(node.getRefName())){//refname is absent or "this"
            loc = (Location) this.treeNode.getVarSymbolTable().lookup(node.getName());
        }
        else{//refname is super
            loc = (Location) this.treeNode.getVarSymbolTable().lookup(node.getName(),
                    this.treeNode.getVarSymbolTable().getCurrScopeLevel()-1);
        }
        this.assemblySupport.genComment("Assign Expr:");
        node.getExpr().accept(this);
        this.assemblySupport.genLoadWord("$v0",loc.getOffset(),loc.getBaseReg());
        return null;
    }

    /**
     * Visit an array assignment expression node
     *
     * @param node the array assignment expression node
     * @return result of the visit
     */
    public Object visit(ArrayAssignExpr node) {
        this.assemblySupport.genComment("ARRAY ASSIGN EXPR");
        return null;
    }

    /**
     * Visit a binary comparison equals expression node
     *
     * @param node the binary comparison equals expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompEqExpr node) {
        this.assemblySupport.genComment("Binary Equals Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("seq","$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary comparison not equals expression node
     *
     * @param node the binary comparison not equals expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompNeExpr node) {
        this.assemblySupport.genComment("Binary Not Equal Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("sne","$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary comparison less than expression node
     *
     * @param node the binary comparison less than expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompLtExpr node) {
        this.assemblySupport.genComment("Binary Less Than Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("slt","$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary comparison less than or equal to expression node
     *
     * @param node the binary comparison less than or equal to expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompLeqExpr node) {
        this.assemblySupport.genComment("Binary Less Than or Equal To Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("sle","$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary comparison greater than expression node
     *
     * @param node the binary comparison greater than expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompGtExpr node) {
        this.assemblySupport.genComment("Binary Greater Than Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genBinaryOp("sgt","$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary comparison greater than or equal to expression node
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return result of the visit
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
     * @return result of the visit
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
     * @return result of the visit
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
     * @return result of the visit
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
     * @return result of the visit
     */
    public Object visit(BinaryArithDivideExpr node) {
        this.assemblySupport.genComment("Binary Divide Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genCondBeq("$v1","$zero","_divide_zero_error");
        this.assemblySupport.genDiv("$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return result of the visit
     */
    public Object visit(BinaryArithModulusExpr node) {
        this.assemblySupport.genComment("Binary Modulus Expr:");
        this.binaryProlog(node);
        this.assemblySupport.genCondBeq("$v1","$zero","_divide_zero_error");
        this.assemblySupport.genMod("$v0","$v1","$v0");
        return null;
    }

    /**
     * Visit a binary logical AND expression node
     *
     * @param node the binary logical AND expression node
     * @return result of the visit
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
     * Visit a binary logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return result of the visit
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
     * Visit a unary negation expression node
     *
     * @param node the unary negation expression node
     * @return result of the visit
     */
    public Object visit(UnaryNegExpr node) {
        node.getExpr().accept(this);
        this.assemblySupport.genNeg("$v0","$v0");
        return null;
    }

    /**
     * Visit a unary NOT expression node
     *
     * @param node the unary NOT expression node
     * @return result of the visit
     */
    public Object visit(UnaryNotExpr node) {
        node.getExpr().accept(this);
        this.assemblySupport.genNot("$v0","$v0");
        return null;
    }

    /**
     * Visit a unary increment expression node
     *
     * @param node the unary increment expression node
     * @return result of the visit
     */
    public Object visit(UnaryIncrExpr node) {
        this.assemblySupport.genComment("Increment Expr: ");
        this.genIncrDecr(node, 1);
        return null;
    }

    /**
     * Visit a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return result of the visit
     */
    public Object visit(UnaryDecrExpr node) {
        this.assemblySupport.genComment("Decrement Expr: ");
        this.genIncrDecr(node, -1);
        return null;
    }

    private void genIncrDecr(UnaryExpr node, int num){
        node.getExpr().accept(this);
        this.assemblySupport.genLoadWord("$t0",0,"$v0");
        this.assemblySupport.genAdd("$t0","$t0",num);
        this.assemblySupport.genStoreWord("$t0",0,"$v1");
        this.assemblySupport.genMove("$v0","$t0");
    }

    /**
     * Visit a variable expression node
     *
     * @param node the variable expression node
     * @return result of the visit
     */
    public Object visit(VarExpr node) {

        String refname = null;
        if(node.getRef()!=null){
            refname = ((VarExpr) node.getRef()).getName();
        }
        Location loc;
        if(!"super".equals(refname)){//refname is absent or "this"
            loc = (Location) this.treeNode.getVarSymbolTable().lookup(node.getName());
        }
        else{//refname is super
            loc = (Location) this.treeNode.getVarSymbolTable().lookup(node.getName(),
                    this.treeNode.getVarSymbolTable().getCurrScopeLevel()-1);
        }
        this.assemblySupport.genComment("Moves the address of the variable into $v1");
        this.assemblySupport.genMove("$v1", loc.getBaseReg());
        this.assemblySupport.genAdd("$v1","$v1",loc.getOffset());
        this.assemblySupport.genComment("Loads the value of the variable into $v0");
        this.assemblySupport.genLoadWord("$v0",loc.getOffset(),"$v1");
        return null;
    }

    /**
     * Visit an array expression node
     *
     * @param node the array expression node
     * @return result of the visit
     */
    public Object visit(ArrayExpr node) {
        this.assemblySupport.genComment("ARRAY EXPR");
        return null;
    }

    /**
     * Visit an int constant expression node
     *
     * @param node the int constant expression node
     * @return result of the visit
     */
    public Object visit(ConstIntExpr node) {
        this.assemblySupport.genLoadImm("$v0",Integer.parseInt(node.getConstant()));
        return null;
    }

    /**
     * Visit a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return result of the visit
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
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return result of the visit
     */
    public Object visit(ConstStringExpr node) {
        this.assemblySupport.genLoadAddr("$v0",this.stringConstantsMap.get(node.getConstant()));
        return null;
    }
}
