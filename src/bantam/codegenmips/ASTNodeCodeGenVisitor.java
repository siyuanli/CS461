package bantam.codegenmips;

import bantam.ast.*;
import bantam.util.ClassTreeNode;
import bantam.visitor.NumLocalVarsVisitor;
import bantam.visitor.Visitor;

import java.util.Map;

/**
 * Created by joseph on 4/5/17.
 */
public class ASTNodeCodeGenVisitor extends Visitor {

    private MipsSupport assemblySupport;
    private ClassTreeNode treeNode;
    private Map<String, Integer> numLocalVars;

    public ASTNodeCodeGenVisitor(MipsSupport assemblySupport, ClassTreeNode treeNode){
        this.assemblySupport = assemblySupport;
        this.treeNode = treeNode;
    }

    public void genMips(ClassTreeNode treeNode){
        this.treeNode = treeNode;
        NumLocalVarsVisitor numLocalVarsVisitor = new NumLocalVarsVisitor();
        this.numLocalVars = numLocalVarsVisitor.getNumsAllLocalVars(this.treeNode.getASTNode());
        this.treeNode.getASTNode().accept(this);
    }

    private void prolog(int numVariables){
        this.assemblySupport.genComment("Prolog:");
        this.assemblySupport.genComment("Pushing on $ra and $fp");
        this.assemblySupport.genAdd("$sp", "$sp", -4);
        this.assemblySupport.genStoreWord("$ra", 0, "$sp");
        this.assemblySupport.genAdd("$sp", "$sp", -4);
        this.assemblySupport.genStoreWord("$fp", 0, "$sp");
        this.assemblySupport.genComment("Making space for " + numVariables + " local vars");
        this.assemblySupport.genAdd("$fp", "$sp", -4*numVariables);
        this.assemblySupport.genMove("$sp", "$fp");
    }

    private void epilogue(int numVariables, int numParams){
        this.assemblySupport.genComment("Epilogue:");
        this.assemblySupport.genComment("Popping off local vars");
        this.assemblySupport.genAdd("$sp", "$fp", 4*numVariables);
        this.assemblySupport.genComment("Pop saved $ra and $fp");
        //TODO: figure out how to do $s registers
        this.assemblySupport.genLoadWord("$fp", 0, "$sp");
        this.assemblySupport.genAdd("$sp", "$sp", 4);
        this.assemblySupport.genLoadWord("$ra", 0, "$sp");
        this.assemblySupport.genAdd("$sp", "$sp", 4);
        this.assemblySupport.genComment("Pop parameters");
        this.assemblySupport.genAdd("$sp", "$sp", 4*numParams);
        this.assemblySupport.genRetn();
    }

    public Object visit(Field field){
        return null;
    }

    public Object visit(Method method){
        String name = this.treeNode.getName() + "." + method.getName();
        this.assemblySupport.genLabel(name);
        int numVariables = this.numLocalVars.get(name);
        this.prolog(numVariables);
        this.assemblySupport.genComment("Body: ");
        method.getStmtList().accept(this);
        this.epilogue(numVariables, method.getFormalList().getSize());
        return null;
    }

    //TODO: Implement methods from here on

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return result of the visit
     */
    public Object visit(DeclStmt node) {
        node.getInit().accept(this);
        return null;
    }


    /**
     * Visit an if statement node
     *
     * @param node the if statement node
     * @return result of the visit
     */
    public Object visit(IfStmt node) {
        node.getPredExpr().accept(this);
        node.getThenStmt().accept(this);
        if (node.getElseStmt() != null) {
            node.getElseStmt().accept(this);
        }
        return null;
    }

    /**
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return result of the visit
     */
    public Object visit(WhileStmt node) {
        node.getPredExpr().accept(this);
        node.getBodyStmt().accept(this);
        return null;
    }

    /**
     * Visit a for statement node
     *
     * @param node the for statement node
     * @return result of the visit
     */
    public Object visit(ForStmt node) {
        if (node.getInitExpr() != null) {
            node.getInitExpr().accept(this);
        }
        if (node.getPredExpr() != null) {
            node.getPredExpr().accept(this);
        }
        if (node.getUpdateExpr() != null) {
            node.getUpdateExpr().accept(this);
        }
        node.getBodyStmt().accept(this);
        return null;
    }

    /**
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return result of the visit
     */
    public Object visit(BreakStmt node) {
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
        return null;
    }


    /**
     * Visit a dispatch expression node
     *
     * @param node the dispatch expression node
     * @return result of the visit
     */
    public Object visit(DispatchExpr node) {
        if(node.getRefExpr() != null)
            node.getRefExpr().accept(this);
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
        return null;
    }

    /**
     * Visit an assignment expression node
     *
     * @param node the assignment expression node
     * @return result of the visit
     */
    public Object visit(AssignExpr node) {
        node.getExpr().accept(this);
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
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary comparison not equals expression node
     *
     * @param node the binary comparison not equals expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompNeExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary comparison less than expression node
     *
     * @param node the binary comparison less than expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompLtExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary comparison less than or equal to expression node
     *
     * @param node the binary comparison less than or equal to expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompLeqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary comparison greater than expression node
     *
     * @param node the binary comparison greater than expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompGtExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary comparison greater than or equal to expression node
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompGeqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return result of the visit
     */
    public Object visit(BinaryArithPlusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return result of the visit
     */
    public Object visit(BinaryArithMinusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return result of the visit
     */
    public Object visit(BinaryArithTimesExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return result of the visit
     */
    public Object visit(BinaryArithDivideExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return result of the visit
     */
    public Object visit(BinaryArithModulusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary logical AND expression node
     *
     * @param node the binary logical AND expression node
     * @return result of the visit
     */
    public Object visit(BinaryLogicAndExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        return null;
    }

    /**
     * Visit a binary logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return result of the visit
     */
    public Object visit(BinaryLogicOrExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
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
        return null;
    }

    /**
     * Visit a unary increment expression node
     *
     * @param node the unary increment expression node
     * @return result of the visit
     */
    public Object visit(UnaryIncrExpr node) {
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return result of the visit
     */
    public Object visit(UnaryDecrExpr node) {
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit a variable expression node
     *
     * @param node the variable expression node
     * @return result of the visit
     */
    public Object visit(VarExpr node) {
        if (node.getRef() != null) {
            node.getRef().accept(this);
        }
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
        return null;
    }

    /**
     * Visit a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return result of the visit
     */
    public Object visit(ConstBooleanExpr node) {
        return null;
    }

    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return result of the visit
     */
    public Object visit(ConstStringExpr node) {
        return null;
    }
}
