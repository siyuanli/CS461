/*
 * File: TypeCheckVisitor.java
 * CS461 Project 3
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 3/11/17
 */

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
 * Visitor class for type checking each field initialization expression and each method,
 * annotating expressions as type checking.
 */
public class TypeCheckVisitor extends Visitor {
    /**
     * The class tree node represents a class in the class hierarchy tree.
     */
    private ClassTreeNode classTreeNode;
    /**
     * The error handler that performs error checking.
     */
    private ErrorHandler errorHandler;
    /**
     * The return type of the method that is currently checked.
     */
    private String currentMethodReturnType;
    /**
     * The set of the reserved key words that cannot be a field name or a method name.
     */
    private Set<String> disallowedNames;
    /**
     * The flag indicating if the current statement or expr is in a loop.
     */
    private boolean inLoop;
    /**
     * The number indicating the scope containing all of the fields.
     */
    private int fieldScope;
    /**
     * The number indicating the scope level containing the methods.
     */
    private int methodScope;

    /**
     * Create a type check visitor that checks types for each field, expression, and method.
     * @param errorHandler the error handler that performs error checking
     * @param disallowedNames the set of reserved key words that cannot be names
     */
    public TypeCheckVisitor(ErrorHandler errorHandler, Set<String> disallowedNames) {
        this.disallowedNames = disallowedNames;
        this.errorHandler = errorHandler;
    }

    /**
     * Check the types for each field, expr, and method are compatible in a class.
     * Otherwise, register a semantic error.
     * @param classTreeNode a class in the class hierarchy tree
     */
    public void checkTypes(ClassTreeNode classTreeNode) {
        this.classTreeNode = classTreeNode;
        Class_ classASTNode = this.classTreeNode.getASTNode();
        this.fieldScope = this.classTreeNode.getVarSymbolTable().getCurrScopeLevel() -1;
        this.methodScope = this.classTreeNode.getMethodSymbolTable().getCurrScopeLevel() -1;
        classASTNode.accept(this);
    }

    /**
     * Check if type2 conforms to type1.
     * @param type1 the proposed subtype
     * @param type2 the proposed supertype
     * @return if type2 conforms to type1
     */
    private boolean compatibleType(String type1, String type2) {
        if (type1.equals(type2)) {
            return true;
        }
        else if (type1.equals("int") || type1.equals("boolean")){
            return false;
        }
        else if ("null".equals(type2)){
            return true;
        }

        if (type1.endsWith("[]") && type2.endsWith("[]")){
            type1 = type1.substring(0, type1.length()-2);
            type2 = type2.substring(0, type2.length()-2);
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

    /**
     * Register an error if the given string is a reserved word.
     * @param name the string
     * @param lineNum the line number of the error
     */
    private void registerErrorIfReservedName(String name, int lineNum) {
        if (disallowedNames.contains(name)) {
            this.registerError(lineNum,
                    "Reserved word," + name + ",cannot be used as a field or method name");
        }
    }

    /**
     * Register an error if the given type invalid.
     * @param type the type that is currently checked
     * @param lineNum the line number of the error
     */
    private void registerErrorIfInvalidType(String type, int lineNum) {
        if (type.endsWith("[]")) {
            type = type.substring(0, type.length() - 2);
        }
        if (!this.classTreeNode.getClassMap().containsKey(type)
                && !type.equals("int") && !type.equals("boolean")) {
            this.registerError(lineNum, "Invalid Type");
        }
    }

    /**
     * Register a semantic error with given line number and error message.
     * @param lineNum the line number of the error
     * @param error the error message
     */
    private void registerError(int lineNum, String error) {
        this.errorHandler.register(2, this.classTreeNode.getASTNode().getFilename(),
                lineNum, error);
    }

    /**
     * Check for compatible types in an assignment.
     * @param refName the reference name of the variable
     * @param name the name of the variable
     * @param exprType the type of the value
     * @param lineNum the line number of the assignment
     * @param isArrayElementAssign if assigning to an array element
     */
    private void checkAssignment(String refName, String name, String exprType,
                                 int lineNum, boolean isArrayElementAssign) {
        String variableType = this.findVariableType(refName, name, lineNum);
        //checking if types are compatible
        if (variableType == null){
            this.registerError(lineNum, "Cannot find variable.");
        }
        else{
            if (isArrayElementAssign){
                variableType = variableType.substring(0,variableType.length()-2);
            }
            if(!this.compatibleType(variableType, exprType)){
                this.registerError(lineNum, "Incompatible variable type assignment.");
            }
        }
    }

    /**
     * Return the type of the given variable and also check the reference.
     * Register an error for illegal referencing.
     * @param refName the reference name of the variable
     * @param name the name of the variable
     * @param lineNum the line number of the error
     * @return the type of the variable
     */
    private String findVariableType(String refName, String name, int lineNum) {
        Object type = null;
        //finding the type of the variable
        if (refName == null){
            type = this.classTreeNode.getVarSymbolTable().lookup(name);
        }
        else{ //refName != null
            if (refName.equals("this")){
                type = this.classTreeNode.getVarSymbolTable().lookup(name,this.fieldScope);
            }
            else if (refName.equals("super")){
                type = this.classTreeNode.getVarSymbolTable().lookup(name,this.fieldScope-1);
            }
            else{
                this.registerError(lineNum,
                        "Can only use 'this' or 'super' when referencing.");
            }
        }
        return (String)type;
    }

    /**
     * Check the types of operands in a binary expression and
     * register an error for incompatible types.
     * @param binaryExpr the given binary expression
     * @param desiredType the desired type of the operands
     * @param exprType the actual type of the binary expression
     */
    private void binaryExprTypeChecker(BinaryExpr binaryExpr, String desiredType,
                                       String exprType){
        binaryExpr.getLeftExpr().accept(this);
        binaryExpr.getRightExpr().accept(this);
        if(!binaryExpr.getLeftExpr().getExprType().equals(desiredType) ||
                !binaryExpr.getRightExpr().getExprType().equals(desiredType)){
            this.registerError(binaryExpr.getLineNum(),
                    "Both operands must be " + desiredType);
        }
        binaryExpr.setExprType(exprType);
    }

    /**
     * Check the types of operands in a binary comparison expression and
     * register an error for incompatible types.
     * @param binaryCompExpr the given binary comparison expression
     */
    private void binaryCompEqualityChecker(BinaryCompExpr binaryCompExpr){
        Expr left = binaryCompExpr.getLeftExpr();
        Expr right = binaryCompExpr.getRightExpr();
        left.accept(this);
        right.accept(this);
        if (!this.compatibleType(left.getExprType(), right.getExprType()) &&
                !this.compatibleType(right.getExprType(), left.getExprType())){
            this.registerError(binaryCompExpr.getLineNum(),
                    "Both expressions in comparison must be compatible types.");
        }
        binaryCompExpr.setExprType("boolean");
    }

    /**
     * Check the type of negation or not unary expression and
     * register an error for incompatible types.
     * @param unaryExpr the given unary expression
     * @param desiredType the desired type of the unary expression
     * @param error the error message
     */
    private void negNotChecker(UnaryExpr unaryExpr, String desiredType, String error){
        unaryExpr.getExpr().accept(this);
        if(!desiredType.equals(unaryExpr.getExpr().getExprType())){
            this.registerError(unaryExpr.getLineNum(), error);
        }
        unaryExpr.setExprType(desiredType);
    }

    /**
     * Check the type of increment or decrement unary expression and
     * register an error if the incremented or decremented is an expression is not a
     * variable of type int.
     * @param unaryExpr the given unary expression
     */
    private void incrDecrChecker(UnaryExpr unaryExpr){
        Expr expr = unaryExpr.getExpr();
        expr.accept(this);
        String type = null;
        if (expr instanceof  VarExpr){
            VarExpr varExpr = (VarExpr)expr;
            String refName = null;
            if(varExpr.getRef()!=null){
                refName = ((VarExpr)varExpr.getRef()).getName();
            }
            type = this.findVariableType(refName, varExpr.getName(), varExpr.getLineNum());
        }
        else if (expr instanceof ArrayExpr){
            ArrayExpr arrayExpr = (ArrayExpr)expr;
            String refName = null;
            if(arrayExpr.getRef()!=null){
                refName = ((VarExpr)arrayExpr.getRef()).getName();
            }
            type=this.findVariableType(refName, arrayExpr.getName(), arrayExpr.getLineNum());
            if(type!=null && type.endsWith("[]")){
                type = type.substring(0,type.length()-2);
            }
        }
        else{
            this.registerError(unaryExpr.getLineNum(),
                    "Incremented or decremented expressions must be variables.");
        }

        if (type == null){
            this.registerError(unaryExpr.getLineNum(),
                    "Incremented or decremented variable not defined");
        }
        else if (!type.equals("int")){
            this.registerError(unaryExpr.getLineNum(),
                    "Incremented or decremented variable must be an int.");
        }

        unaryExpr.setExprType("int");
    }

    /**
     * Check the type of the field with assignment and
     * register an error for incompatible types.
     * @param field the given field
     * @return null
     */
    @Override
    public Object visit(Field field) {
        Expr init = field.getInit();
        if(init!=null) {
            init.accept(this);
            if (!compatibleType(field.getType(), init.getExprType())) {
                this.registerError(field.getLineNum(),
                        "Type of field incompatible with assignment.");
            }
        }
        return null;
    }

    /**
     * Check the type of the method and register an error for missing a return statement.
     * @param method the given method
     * @return null
     */
    @Override
    public Object visit(Method method) {
        this.currentMethodReturnType = method.getReturnType();
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        varSymbolTable.enterScope();
        method.getFormalList().accept(this);
        StmtList stmtList = method.getStmtList();
        stmtList.accept(this);
        if (!method.getReturnType().equals("void")) {
            if (stmtList.getSize()>0 &&
                    !(stmtList.get(stmtList.getSize() - 1) instanceof ReturnStmt)) {
                this.registerError(method.getLineNum(), "Missing return Statement");
            }
        }
        varSymbolTable.exitScope();
        return null;
    }

    /**
     * Check the type of a formal and register an error for a duplicated parameter name.
     * @param formal the given formal
     * @return null
     */
    @Override
    public Object visit(Formal formal) {
        int lineNum = formal.getLineNum();
        this.registerErrorIfReservedName(formal.getName(), lineNum);
        this.registerErrorIfInvalidType(formal.getType(), lineNum);
        if (this.classTreeNode.getVarSymbolTable().peek(formal.getName())!= null){
            this.registerError(lineNum, "Parameter already exists with same name.");
        }
        this.classTreeNode.getVarSymbolTable().add(formal.getName(), formal.getType());
        return null;
    }

    /**
     * Check the type of a declaration statement and register an error for
     * duplicated variables or incompatible types.
     * @param stmt the given declaration statement
     * @return null
     */
    @Override
    public Object visit(DeclStmt stmt) {
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        int lineNum = stmt.getLineNum();
        String type = stmt.getType();
        registerErrorIfInvalidType(type, lineNum);
        registerErrorIfReservedName(stmt.getName(), lineNum);
        stmt.getInit().accept(this);
        for (int i = varSymbolTable.getCurrScopeLevel() - 1; i > this.fieldScope; i--) {
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

    /**
     * Check the type of a if statement and register an error for incompatible types.
     * @param ifStmt the given if statement
     * @return null
     */
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

    /**
     * Check the type of a while statement and register an error for incompatible types.
     * @param whileStmt the given while statement
     * @return null
     */
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

    /**
     * Check the type of a for statement and register an error for incompatible types.
     * @param forStmt the given for statement
     * @return null
     */
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

    /**
     * Check the type of a break statement and register an error when not in a loop.
     * @param breakStmt the given break statement
     * @return null
     */
    @Override
    public Object visit(BreakStmt breakStmt){
        if (!this.inLoop){
            this.registerError(breakStmt.getLineNum(),
                    "Break statements must be in a loop.");
        }
        return null;
    }

    /**
     * Check the type of a return statement and register an error for incompatible types.
     * @param returnStmt the given return statement
     * @return null
     */
    @Override
    public Object visit(ReturnStmt returnStmt){
        if (returnStmt.getExpr() != null) {
            returnStmt.getExpr().accept(this);
            String returnType = returnStmt.getExpr().getExprType();
            if(returnType.equals("null") && (currentMethodReturnType.equals("boolean")
                    || currentMethodReturnType.equals("int"))){
                this.registerError(returnStmt.getLineNum(),
                        "Cannot return null from int or boolean method");

            }
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

    /**
     * Checks the types of all the statements in the given block statement and makes the
     * statements be in their own scope
     * @param blockStmt The block statement to be checked
     * @return null
     */
    @Override
    public Object visit(BlockStmt blockStmt){
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        varSymbolTable.enterScope();
        blockStmt.getStmtList().accept(this);
        varSymbolTable.exitScope();
        return null;
    }

    /**
     * Checks to see that the variable assignment occurs on a valid variable and that the
     * types of the assignment is valid.
     * @param assignExpr The assignment expression
     * @return null
     */
    @Override
    public Object visit(AssignExpr assignExpr){
        assignExpr.getExpr().accept(this);

        String refName = assignExpr.getRefName();
        if (assignExpr.getName().equals("length") && refName!=null &&
                !refName.equals("this") && !refName.equals("super")){
            this.registerError(assignExpr.getLineNum(),
                    "Cannot assign the length field of an array. ");
        }

        this.checkAssignment(assignExpr.getRefName(), assignExpr.getName(),
                assignExpr.getExpr().getExprType(), assignExpr.getLineNum(), false);
        assignExpr.setExprType(assignExpr.getExpr().getExprType());
        return null;
    }

    /**
     * Checks to see if the given ArrayAssignExpr is indexed by an integer, and that the
     * types of the assignment are compatible
     * @param arrayAssignExpr
     * @return
     */
    @Override
    public Object visit(ArrayAssignExpr arrayAssignExpr){
        arrayAssignExpr.getExpr().accept(this);
        arrayAssignExpr.getIndex().accept(this);

        if (!arrayAssignExpr.getIndex().getExprType().equals("int")){
            this.registerError(arrayAssignExpr.getLineNum(),
                    "Index of array must be an integer.");
        }

        this.checkAssignment(arrayAssignExpr.getRefName(), arrayAssignExpr.getName(),
                arrayAssignExpr.getExpr().getExprType(), arrayAssignExpr.getLineNum(), true);
        arrayAssignExpr.setExprType(arrayAssignExpr.getExpr().getExprType());
        return null;
    }

    /**
     *
     * @param dispatchExpr
     * @return
     */
    @Override
    public Object visit(DispatchExpr dispatchExpr){
        Expr refExpr = dispatchExpr.getRefExpr();

        SymbolTable methodTable = this.classTreeNode.getMethodSymbolTable();

        Pair<String, List<String>> methodPair = null;
        if (refExpr != null){
            if(refExpr instanceof VarExpr && ((VarExpr)refExpr).getName().equals("this")){
                methodPair = ((Pair<String, List<String>>)methodTable.lookup(dispatchExpr.getMethodName(),this.methodScope));
            }
            else if(refExpr instanceof VarExpr && ((VarExpr)refExpr).getName().equals("super")){
                methodPair = ((Pair<String, List<String>>) this.classTreeNode
                            .getMethodSymbolTable().lookup(dispatchExpr.getMethodName(), this.methodScope -1));

            }
            else {
                refExpr.accept(this);
                String typeReference = refExpr.getExprType();
                ClassTreeNode refNode = this.classTreeNode.getClassMap().get(typeReference);
                if (refNode == null) {
                    this.registerError(dispatchExpr.getLineNum(),
                            "Reference does not contain given method.");
                }
                else {
                    methodPair = ((Pair<String, List<String>>) refNode.getMethodSymbolTable()
                            .lookup(dispatchExpr.getMethodName()));
                }
            }
        }
        else{
            methodPair = ((Pair<String, List<String>>)methodTable.lookup(dispatchExpr.getMethodName()));
        }


        List<String> params = null;
        String exprType = null;
        if (methodPair != null) {
            params = methodPair.getValue();
            exprType = methodPair.getKey();
        }
        else {
            this.registerError(dispatchExpr.getLineNum(),"Unknown method call.");
        }

        List<String> actualParams = (List<String>)dispatchExpr.getActualList().accept(this);

        if (params != null) {
            if (actualParams.size() != params.size()) {
                this.registerError(dispatchExpr.getLineNum(), "Wrong number of parameters");
            }
            for (int i = 0; i < params.size(); i++) {
                if (i >= actualParams.size()) {
                    break;
                }
                else if (!this.compatibleType(params.get(i), actualParams.get(i))) {
                    this.registerError(dispatchExpr.getLineNum(),
                            "Value passed in has incompatible type with parameter.");
                }
            }
        }
        dispatchExpr.setExprType(exprType);
        return null;
    }

    /**
     *
     * @param exprList
     * @return
     */
    @Override
    public Object visit(ExprList exprList){
        List<String> paramTypes = new ArrayList<>();
        for(ASTNode node: exprList){
            node.accept(this);
            paramTypes.add(((Expr)node).getExprType());
        }
        return paramTypes;
    }

    /**
     *
     * @param newExpr
     * @return
     */
    @Override
    public Object visit(NewExpr newExpr){
        if (!this.classTreeNode.getClassMap().containsKey(newExpr.getType())){
            this.registerError(newExpr.getLineNum(), "Object type undefined.");
        }
        newExpr.setExprType(newExpr.getType());
        return null;
    }

    /**
     *
     * @param newArrayExpr
     * @return
     */
    @Override
    public Object visit(NewArrayExpr newArrayExpr){
        this.registerErrorIfInvalidType(newArrayExpr.getType(),newArrayExpr.getLineNum());
        newArrayExpr.getSize().accept(this);
        if (!newArrayExpr.getSize().getExprType().equals("int")){
            registerError(newArrayExpr.getLineNum(), "Array size is not int.");
        }
        newArrayExpr.setExprType(newArrayExpr.getType());
        return null;
    }

    /**
     *
     * @param instanceofExpr
     * @return
     */
    @Override
    public Object visit(InstanceofExpr instanceofExpr){
        String type = instanceofExpr.getType();
        boolean typeIsArray = false;

        if (type.endsWith("[]")){
            typeIsArray = true;
            type = type.substring(0, type.length()-2);
        }

        if (instanceofExpr.getType().equals("int") || instanceofExpr.getType().equals("boolean")){
            this.registerError(instanceofExpr.getLineNum(), "Cannot check if expr is primitive type.");
        }
        else if (!this.classTreeNode.getClassMap().containsKey(type)){
            this.registerError(instanceofExpr.getLineNum(), "Unknown instanceof type.");
        }
        instanceofExpr.getExpr().accept(this);

        String exprType = instanceofExpr.getExpr().getExprType();

        if (exprType != null){
            if (typeIsArray && exprType.endsWith("[]")){
                exprType = exprType.substring(0, exprType.length()-2);
            }
            else if (typeIsArray){
                type += "[]";
            }

            if ("int".equals(exprType) || "boolean".equals(exprType)) {
                this.registerError(instanceofExpr.getLineNum(),
                        "Cannot check instance of primitives.");
            } else if (this.compatibleType(type, exprType)) {
                instanceofExpr.setUpCheck(true);
            } else if (this.compatibleType(exprType, type)) {
                instanceofExpr.setUpCheck(false);
            } else {
                this.registerError(instanceofExpr.getLineNum(),
                        "Incompatible types in instanceof.");

            }
        }
        instanceofExpr.setExprType("boolean");
        return null;
    }

    /**
     *
     * @param castExpr
     * @return
     */
    @Override
    public Object visit(CastExpr castExpr){
        String type = castExpr.getType();
        boolean typeIsArray = false;

        if (type.endsWith("[]")){
            typeIsArray = true;
            type = type.substring(0, type.length()-2);
        }

        if (castExpr.getType().equals("int") || castExpr.getType().equals("boolean")){
            this.registerError(castExpr.getLineNum(), "Cannot cast to a primitive type.");
        }
        else if (!this.classTreeNode.getClassMap().containsKey(type)){
            this.registerError(castExpr.getLineNum(), "Unknown cast type.");
        }
        castExpr.getExpr().accept(this);

        String exprType = castExpr.getExpr().getExprType();

        if (exprType != null){
            if (typeIsArray && exprType.endsWith("[]")){
                exprType = exprType.substring(0, exprType.length()-2);
            }
            else if (typeIsArray){
                type += "[]";
            }

            if ("int".equals(exprType) || "boolean".equals(exprType)) {
                this.registerError(castExpr.getLineNum(),
                        "Cannot cast a primitives.");
            } else if (this.compatibleType(type, exprType)) {
                castExpr.setUpCast(true);
            } else if (this.compatibleType(exprType, type)) {
                castExpr.setUpCast(false);
            } else {
                this.registerError(castExpr.getLineNum(),
                        "Incompatible types in cast.");

            }
        }
        castExpr.setExprType(castExpr.getType());

        return null;
    }

    /**
     *
     * @param constIntExpr
     * @return
     */
    @Override
    public Object visit(ConstIntExpr constIntExpr){
        constIntExpr.setExprType("int");
        return null;
    }

    /**
     *
     * @param constBooleanExpr
     * @return
     */
    @Override
    public Object visit(ConstBooleanExpr constBooleanExpr){
        constBooleanExpr.setExprType("boolean");
        return null;
    }

    /**
     *
     * @param constStringExpr
     * @return
     */
    @Override
    public Object visit(ConstStringExpr constStringExpr){
        constStringExpr.setExprType("String");
        return null;
    }

    /**
     *
     * @param binaryArithPlusExprExpr
     * @return
     */
    @Override
    public Object visit(BinaryArithPlusExpr binaryArithPlusExprExpr){
        this.binaryExprTypeChecker(binaryArithPlusExprExpr, "int", "int");
        return null;
    }

    /**
     *
     * @param binaryArithMinusExprExpr
     * @return
     */
    @Override
    public Object visit(BinaryArithMinusExpr binaryArithMinusExprExpr){
        this.binaryExprTypeChecker(binaryArithMinusExprExpr, "int", "int");
        return null;
    }

    /**
     *
     * @param binaryArithTimesExpr
     * @return
     */
    @Override
    public Object visit(BinaryArithTimesExpr binaryArithTimesExpr){
        this.binaryExprTypeChecker(binaryArithTimesExpr, "int", "int");
        return null;
    }

    /**
     *
     * @param binaryArithDivideExpr
     * @return
     */
    @Override
    public Object visit(BinaryArithDivideExpr binaryArithDivideExpr){
        this.binaryExprTypeChecker(binaryArithDivideExpr, "int", "int");
        return null;
    }

    /**
     *
     * @param binaryArithModulusExpr
     * @return
     */
    @Override
    public Object visit(BinaryArithModulusExpr binaryArithModulusExpr){
        this.binaryExprTypeChecker(binaryArithModulusExpr, "int", "int");
        return null;
    }

    /**
     *
     * @param binaryCompEqExpr
     * @return
     */
    @Override
    public Object visit(BinaryCompEqExpr binaryCompEqExpr) {
        this.binaryCompEqualityChecker(binaryCompEqExpr);
        return null;
    }

    /**
     *
     * @param binaryCompNeExpr
     * @return
     */
    @Override
    public Object visit(BinaryCompNeExpr binaryCompNeExpr) {
        this.binaryCompEqualityChecker(binaryCompNeExpr);
        return null;
    }

    /**
     *
     * @param binaryCompLtExpr
     * @return
     */
    @Override
    public Object visit(BinaryCompLtExpr binaryCompLtExpr) {
        this.binaryExprTypeChecker(binaryCompLtExpr, "int", "boolean");
        return null;
    }

    /**
     *
     * @param binaryCompLeqExpr
     * @return
     */
    @Override
    public Object visit(BinaryCompLeqExpr binaryCompLeqExpr) {
        this.binaryExprTypeChecker(binaryCompLeqExpr, "int", "boolean");
        return null;
    }

    /**
     *
     * @param binaryCompGtExpr
     * @return
     */
    @Override
    public Object visit(BinaryCompGtExpr binaryCompGtExpr) {
        this.binaryExprTypeChecker(binaryCompGtExpr, "int", "boolean");
        return null;
    }

    /**
     *
     * @param binaryCompGeqExpr
     * @return
     */
    @Override
    public Object visit(BinaryCompGeqExpr binaryCompGeqExpr) {
        this.binaryExprTypeChecker(binaryCompGeqExpr, "int", "boolean");
        return null;
    }

    /**
     *
     * @param binaryLogicAndExpr
     * @return
     */
    @Override
    public Object visit(BinaryLogicAndExpr binaryLogicAndExpr) {
        this.binaryExprTypeChecker(binaryLogicAndExpr, "boolean", "boolean");
        return null;
    }

    /**
     *
     * @param binaryLogicOrExpr
     * @return
     */
    @Override
    public Object visit(BinaryLogicOrExpr binaryLogicOrExpr) {
        this.binaryExprTypeChecker(binaryLogicOrExpr, "boolean", "boolean");
        return null;
    }

    /**
     *
     * @param unaryNegExpr
     * @return
     */
    @Override
    public Object visit(UnaryNegExpr unaryNegExpr){
        this.negNotChecker(unaryNegExpr, "int",
                "Type of arithmetically negated expression must be int.");
        return null;
    }

    /**
     *
     * @param unaryNotExpr
     * @return
     */
    @Override
    public Object visit(UnaryNotExpr unaryNotExpr){
        this.negNotChecker(unaryNotExpr, "boolean",
                "Type of logically negated expression must be boolean.");
        return null;
    }

    /**
     *
     * @param unaryIncrExpr
     * @return
     */
    @Override
    public Object visit(UnaryIncrExpr unaryIncrExpr){
        this.incrDecrChecker(unaryIncrExpr);
        return null;
    }

    /**
     *
     * @param unaryDecrExpr
     * @return
     */
    @Override
    public Object visit(UnaryDecrExpr unaryDecrExpr){
        this.incrDecrChecker(unaryDecrExpr);
        return null;
    }

    /**
     *
     * @param varExpr
     * @return
     */
    @Override
    public Object visit(VarExpr varExpr){
        String type = null;
        String refName=null;

        if(varExpr.getName().equals("null")){
            if(varExpr.getRef()!=null){
                this.registerError(varExpr.getLineNum(),
                        "Cannot reference reserved keyword null");
            }
            else{
                varExpr.setExprType("null");
                return null;
            }
        }

        if(varExpr.getRef()!=null){
            refName = ((VarExpr) varExpr.getRef()).getName();
        }

        if (varExpr.getName().equals("length") && !"this".equals(refName) &&
                !"super".equals(refName) && refName!=null) {
            type = this.findVariableType(null, refName, varExpr.getLineNum());
            if (!type.endsWith("[]")) {
                this.registerError(varExpr.getLineNum(),
                        "Only array variables have length attribute.");
            } else {
                type = "int";
            }
        }
        else {
            this.registerErrorIfReservedName(varExpr.getName(),varExpr.getLineNum());
            type=this.findVariableType(refName,varExpr.getName(),varExpr.getLineNum());
        }

        if(type==null){
            this.registerError(varExpr.getLineNum(),"Undeclared variable access.");
        }
        varExpr.setExprType(type);

        return null;
    }

    /**
     *
     * @param arrayExpr
     * @return
     */
    @Override
    public Object visit(ArrayExpr arrayExpr){
        String ref = null;
        if (arrayExpr.getRef() != null){
            ref = ((VarExpr)arrayExpr.getRef()).getName();
        }

        String type = this.findVariableType(ref, arrayExpr.getName(),
                arrayExpr.getLineNum());

        if (type == null){
            this.registerError(arrayExpr.getLineNum(), "Undeclared variable access.");
        }
        else if (!type.endsWith("[]")){
            this.registerError(arrayExpr.getLineNum(),
                    "Indexed variable must be an array type.");
        }
        else{
            type = type.substring(0, type.length()-2);
        }

        if (arrayExpr.getIndex() != null) {
            arrayExpr.getIndex().accept(this);
            if (!arrayExpr.getIndex().getExprType().equals("int")) {
                this.registerError(arrayExpr.getLineNum(), "Array index must be an int.");
            }
        }

        arrayExpr.setExprType(type);

        return null;
    }

}
