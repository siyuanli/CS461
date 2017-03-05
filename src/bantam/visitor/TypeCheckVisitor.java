package bantam.visitor;

import bantam.ast.*;
import bantam.util.ClassTreeNode;
import bantam.util.ErrorHandler;
import bantam.util.SymbolTable;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Phoebe Hughes on 3/4/2017.
 */
public class TypeCheckVisitor extends Visitor {


    private ClassTreeNode classTreeNode;
    private ErrorHandler errorHandler;
    private String currentMethodReturnType;
    private boolean inLoop;
    private Set<String> disallowedNames;


    public TypeCheckVisitor(ErrorHandler errorHandler, Set<String> disallowedNames) {
        this.disallowedNames = disallowedNames;
        this.errorHandler = errorHandler;
    }

    /**
     * @param type1 desired type
     * @param type2 actual type of expression
     * @return
     */
    private boolean compatibleType(String type1, String type2) {
        if (type1.equals(type2)) {
            return true;
        }
        else if (type1.equals("int") || type1.equals("boolean")){
            return false;
        }
        else if (type2.equals("null")){
            return true;
        }

        ClassTreeNode type2Node = this.classTreeNode.getClassMap().get(type2);
        if (type2Node != null){
            if (type2Node.getParent() == null) { //object
                return false;
            } else {
                return this.compatibleType(type1, type2Node.getParent().getName());
            }
        }
        else{ //undeclared type
            return false;
        }
    }

    private void registerErrorIfReservedName(String name, int lineNum) {
        if (disallowedNames.contains(name)) {
            this.registerError(lineNum,
                    "Reserved word," + name + ",cannot be used as a field or method name");
        }
    }

    private void registerErrorIfInvalidType(String type, int lineNum) {
        if (type.endsWith("[]")) {
            type = type.substring(0, type.length() - 2);
        }
        if (!this.classTreeNode.getClassMap().containsKey(type)
                && !type.equals("int") && !type.equals("boolean")) {
            this.registerError(lineNum, "Invalid Type");
        }
    }

    private void registerError(int lineNum, String error) {
        this.errorHandler.register(2, this.classTreeNode.getASTNode().getFilename(),
                lineNum, error);
    }

    public void checkTypes(ClassTreeNode classTreeNode) {
        this.classTreeNode = classTreeNode;
        Class_ classASTNode = this.classTreeNode.getASTNode();
        classASTNode.accept(this);
    }

    @Override
    public Object visit(Field field) {
        Expr init = field.getInit();
        init.accept(this);
        if (!compatibleType(field.getType(), init.getExprType())) {
            this.registerError(field.getLineNum(),
                    "Type of field incompatible with assignment.");
        }
        return null;
    }

    @Override
    public Object visit(Method method) {
        this.currentMethodReturnType = method.getReturnType();
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        varSymbolTable.enterScope();
        method.getFormalList().accept(this);
        StmtList stmtList = method.getStmtList();
        stmtList.accept(this);
        if (!method.getReturnType().equals("void")) {
            if (!(stmtList.get(stmtList.getSize() - 1) instanceof ReturnStmt)) {
                this.registerError(method.getLineNum(), "Missing return Statement");
            }
        }
        varSymbolTable.exitScope();
        return null;
    }

    @Override
    public Object visit(Formal formal) {
        this.classTreeNode.getVarSymbolTable().add(formal.getName(), formal.getType());
        return null;
    }

    //TODO : Figure out what we do with exprStmt, modify grammar or disallow certain exprs
    /*
    @Override
    public Object visit(ExprStmt exprStmt){
    }*/

    @Override
    public Object visit(DeclStmt stmt) {
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        int lineNum = stmt.getLineNum();
        String type = stmt.getType();
        registerErrorIfInvalidType(type, lineNum);
        registerErrorIfReservedName(stmt.getName(), lineNum);
        stmt.getInit().accept(this);
        for (int i = varSymbolTable.getCurrScopeLevel(); i > 0; i--) {
            if (varSymbolTable.peek(stmt.getName(), i) != null) {
                this.registerError(lineNum, "Variable already declared");
            }
        }
        varSymbolTable.add(stmt.getName(), type);
        if (!compatibleType(type, stmt.getInit().getExprType())) {
            this.registerError(lineNum, "Type of variable incompatible with assignment.");
        }
        return null;
    }

    @Override
    public Object visit(IfStmt ifStmt) {
        ifStmt.getPredExpr().accept(this);

        if (!ifStmt.getPredExpr().getExprType().equals("boolean")) {
            this.registerError(ifStmt.getLineNum(),
                    "If statement conditional must be a boolean.");
        }

        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();

        varSymbolTable.enterScope();
        ifStmt.getThenStmt().accept(this);
        varSymbolTable.exitScope();

        if (ifStmt.getElseStmt() != null) {
            varSymbolTable.enterScope();
            ifStmt.getElseStmt().accept(this);
            varSymbolTable.exitScope();
        }

        return null;
    }

    @Override
    public Object visit(WhileStmt whileStmt) {
        whileStmt.getPredExpr().accept(this);

        if (!whileStmt.getPredExpr().getExprType().equals("boolean")) {
            this.registerError(whileStmt.getLineNum(),
                    "While statement conditional must be a boolean.");
        }

        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();

        varSymbolTable.enterScope();
        boolean priorInLoop = this.inLoop;
        this.inLoop = true;
        whileStmt.getBodyStmt().accept(this);
        this.inLoop = priorInLoop;
        varSymbolTable.exitScope();

        return null;
    }

    @Override
    public Object visit(ForStmt forStmt) {
        if (forStmt.getInitExpr() != null) {
            forStmt.getInitExpr().accept(this);
        }

        if (forStmt.getPredExpr() != null) {
            forStmt.getPredExpr().accept(this);
            if (!forStmt.getPredExpr().getExprType().equals("boolean")) {
                this.registerError(forStmt.getLineNum(),
                        "For statement conditional must be a boolean.");
            }
        }

        if (forStmt.getUpdateExpr() != null) {
            forStmt.getUpdateExpr().accept(this);
        }

        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();

        varSymbolTable.enterScope();
        boolean priorInLoop = this.inLoop;
        this.inLoop = true;
        forStmt.getBodyStmt().accept(this);
        this.inLoop = priorInLoop;
        varSymbolTable.exitScope();

        return null;
    }

    @Override
    public Object visit(BreakStmt breakStmt){
        if (this.inLoop != true){
            this.registerError(breakStmt.getLineNum(),
                    "Break statements must be in loops.");
        }
        return null;
    }


    @Override
    public Object visit(ReturnStmt returnStmt){
        if (returnStmt.getExpr() != null) {
            returnStmt.getExpr().accept(this);
            String returnType = returnStmt.getExpr().getExprType();
            if (!this.compatibleType(this.currentMethodReturnType, returnType)) {
                this.registerError(returnStmt.getLineNum(),
                        "Return statement type does not match method return type.");
            }
        }
        else{
            if (!this.currentMethodReturnType.equals("void")){
                this.registerError(returnStmt.getLineNum(),
                        "Must return value in non void method.");
            }
        }

        return null;
    }

    @Override
    public Object visit(BlockStmt blockStmt){
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        varSymbolTable.enterScope();
        blockStmt.getStmtList().accept(this);
        varSymbolTable.exitScope();
        return null;
    }

    @Override
    public Object visit(AssignExpr assignExpr){
        assignExpr.accept(this);
        checkAssignment(assignExpr.getRefName(), assignExpr.getName(),
                assignExpr.getExprType(), assignExpr.getLineNum(), false);

        return null;
    }

    private void checkAssignment(String refName, String name, String exprType, int lineNum, boolean isArrayElementAssign) {
        String variableType = (String)this.findMember(this.classTreeNode.getVarSymbolTable(),
                this.classTreeNode.getParent().getVarSymbolTable(),
                refName, name, lineNum);

        //checking if types are compatible
        if (variableType == null){
            this.registerError(lineNum, "Cannot find variable.");
        }
        else{
            if (isArrayElementAssign){
                variableType = variableType.substring(0,variableType.length()-2);
            }
            if(!this.compatibleType(variableType, exprType)){
                this.registerError(lineNum, "Incompatible variableType assignment.");
            }
        }
    }

    private Object findMember(SymbolTable symbolTable, SymbolTable parentSymbolTable,
                              String refName, String name, int lineNum) {
        Object value = null;
        //finding the value of the variable
        if (refName == null){
            value = symbolTable.lookup(name);
        }
        else{ //refName != null
            if (refName.equals("this")){
                value = symbolTable.peek(name,0);
            }
            else if (refName.equals("super")){
                value = parentSymbolTable.lookup(name);
            }
            else{
                this.registerError(lineNum,
                        "Can only use 'this' or 'super' when referencing.");
            }
        }
        return value;
    }

    @Override
    public Object visit(ArrayAssignExpr arrayAssignExpr){
        arrayAssignExpr.getExpr().accept(this);
        arrayAssignExpr.getIndex().accept(this);

        if (!arrayAssignExpr.getIndex().getExprType().equals("int")){
            this.registerError(arrayAssignExpr.getLineNum(),
                    "Index of array must be an integer.");
        }

        checkAssignment(arrayAssignExpr.getRefName(), arrayAssignExpr.getName(),
                arrayAssignExpr.getExprType(), arrayAssignExpr.getLineNum(), true);
        return null;
    }

    @Override
    public Object visit(DispatchExpr dispatchExpr){
        Expr refExpr = dispatchExpr.getRefExpr();
        List<String> params = null;
        if (refExpr != null){
            if(refExpr instanceof VarExpr && ((VarExpr)refExpr).getName().equals("this")){
                params = ((Pair<String, List<String>>)this.classTreeNode
                        .getMethodSymbolTable().peek(dispatchExpr.getMethodName(),0))
                        .getValue();
            }
            if(refExpr instanceof VarExpr && ((VarExpr)refExpr).getName().equals("super")){
                params = ((Pair<String, List<String>>)this.classTreeNode.getParent()
                        .getMethodSymbolTable().peek(dispatchExpr.getMethodName(),0))
                        .getValue();
            }
            refExpr.accept(this);
            String typeReference = refExpr.getExprType();
            ClassTreeNode refNode = this.classTreeNode.getClassMap().get(typeReference);
            if (refNode == null){
                this.registerError(dispatchExpr.getLineNum(),
                        "Reference does not contain given method.");
            }
            else {
                params = ((Pair<String, List<String>>)this.classTreeNode
                        .getMethodSymbolTable().lookup(dispatchExpr.getMethodName()))
                        .getValue();
            }
        }
        else{
            params = ((Pair<String, List<String>>)this.classTreeNode
                    .getMethodSymbolTable().lookup(dispatchExpr.getMethodName()))
                    .getValue();
        }

        List<String> actualParams = (List<String>)dispatchExpr.getActualList().accept(this);

        if (params != null) {
            if (actualParams.size() != params.size()) {
                this.registerError(dispatchExpr.getLineNum(), "Wrong number of parameters");
            }
            for (int i = 0; i < params.size(); i++) {
                if (i >= actualParams.size()) {
                    break;
                } else {
                    if (!this.compatibleType(params.get(i), actualParams.get(i))) {
                        this.registerError(dispatchExpr.getLineNum(),
                                "Value passed in has incompatible type with parameter.");
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Object visit(ExprList exprList){
        List<String> paramTypes = new ArrayList<>();
        for(ASTNode node: exprList){
            node.accept(this);
            paramTypes.add(((Expr)node).getExprType());
        }
        return paramTypes;
    }
}