/*
 * File: TypeCheckVisitor.java
 * CS461 Project 4A
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/2/17
 */

package bantam.visitor;

import bantam.ast.*;
import bantam.util.ClassTreeNode;
import bantam.util.ErrorHandler;
import bantam.util.ErrorHandlerUtilities;
import bantam.util.SymbolTable;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Visitor class for type checking each field initialization expression and each method,
 * annotating expressions as type checking.
 *
 * @author Joseph Maionek
 * @author Siyuan Li
 * @author Phoebe Hughes
 */
public class TypeCheckVisitor extends Visitor {
    /**
     * The class tree node represents a class in the class hierarchy tree.
     */
    private ClassTreeNode classTreeNode;

    /**
     * The return type of the method that is currently checked.
     */
    private String currentMethodReturnType;

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
     * The utilities that help register errors
     */
    private ErrorHandlerUtilities errorUtil;

    /**
     * Create a type check visitor that checks types
     * for each field, expression, and method.
     * @param errorHandler the error handler that performs error checking
     * @param disallowedNames the set of reserved key words that cannot be names
     */
    public TypeCheckVisitor(ErrorHandler errorHandler, Set<String> disallowedNames) {
        this.errorUtil = new ErrorHandlerUtilities(errorHandler, disallowedNames, null,
                null);
    }

    /**
     * Check the types for each field, expr, and method are compatible in a class.
     * Otherwise, register a semantic error.
     * @param classTreeNode a class in the class hierarchy tree
     */
    public void checkTypes(ClassTreeNode classTreeNode) {
        this.classTreeNode = classTreeNode;
        this.errorUtil.setFilename(this.classTreeNode.getASTNode().getFilename());
        this.errorUtil.setClassMap(this.classTreeNode.getClassMap());
        Class_ classASTNode = this.classTreeNode.getASTNode();
        this.fieldScope = this.classTreeNode.getVarSymbolTable()
                .getCurrScopeLevel() -1;
        this.methodScope = this.classTreeNode.getMethodSymbolTable()
                .getCurrScopeLevel() -1;
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


        if(type2.endsWith("[]")) {
            if (type1.endsWith("[]")) {
                type1 = type1.substring(0, type1.length() - 2);
                type2 = type2.substring(0, type2.length() - 2);
            } else if (type1.equals("Object")) {
                type2 = type2.substring(0, type2.length() - 2);
                if(type2.equals("int")||type2.equals("boolean")){
                    return true;
                }
            } else {
                return false;
            }
        }

        ClassTreeNode type2Node = this.classTreeNode.getClassMap().get(type2);
        if (type2Node != null){
            //At this point, we know type2 is a class and so if type1 is Object, it's valid
            if(type1.equals("Object")){
                return true;
            }
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
            this.errorUtil.registerError(lineNum, "Cannot find variable.");
        }
        else{
            if (isArrayElementAssign){
                variableType = variableType.substring(0,variableType.length()-2);
            }
            if(!this.compatibleType(variableType, exprType)){
                this.errorUtil.registerError(lineNum,
                        "Incompatible variable type assignment. Cannot assign expression"+
                                " of type "+exprType+" to variable " + name + " of type "
                                + variableType);
            }
        }
    }

    /**
     * Checks to see if the given type is a valid array type and if so returns the type
     * of the array. Registers an error if the type is null or not an array type
     * @param lineNum the lineNum of the expression
     * @param type the type of the variable
     * @return the variable type with the brackets removed  if it was an array type,
     * otherwise, return the original string.
     */
    private String checkValidArrayType(int lineNum, String type) {
        //checks that type is valid and it exists
        if (type == null){
            this.errorUtil.registerError(lineNum, "Undeclared variable access.");
        }
        else if (!type.endsWith("[]")){
            this.errorUtil.registerError(lineNum,
                    "Indexed variable must be an array type.");
        }
        else{
            type = type.substring(0, type.length()-2);
        }
        return type;
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
                type = this.classTreeNode.getVarSymbolTable()
                        .lookup(name,this.fieldScope);
            }
            else if (refName.equals("super")){
                type = this.classTreeNode.getVarSymbolTable()
                        .lookup(name,this.fieldScope-1);
            }
            else{
                this.errorUtil.registerError(lineNum,
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
            this.errorUtil.registerError(binaryExpr.getLineNum(),
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
            this.errorUtil.registerError(binaryCompExpr.getLineNum(),
                    "Both expressions in comparison must be compatible types.");
        }
        binaryCompExpr.setExprType("boolean");
    }

    /**
     * Gets the return type and paramets of a method when there is a reference.
     * @param dispatchExpr The dispatch expression
     * @return a pair consisting of the return type and a list of the parameter types
     */
    private Pair<String, List<String>> getStringListPair(DispatchExpr dispatchExpr) {
        Pair<String, List<String>> methodPair=null;
        SymbolTable methodTable = this.classTreeNode.getMethodSymbolTable();
        Expr refExpr= dispatchExpr.getRefExpr();
        if(refExpr instanceof VarExpr && ((VarExpr)refExpr).getName().equals("this")){
            methodPair = ((Pair<String, List<String>>)methodTable
                    .lookup(dispatchExpr.getMethodName(),this.methodScope));
        }
        else if(refExpr instanceof VarExpr
                && ((VarExpr)refExpr).getName().equals("super")){
            methodPair = ((Pair<String, List<String>>) this.classTreeNode
                    .getMethodSymbolTable()
                    .lookup(dispatchExpr.getMethodName(), this.methodScope -1));

        }
        else { //any scope
            refExpr.accept(this);
            String typeReference = refExpr.getExprType();

            if (typeReference.endsWith("[]")){
                typeReference = "Object";
            }

            ClassTreeNode refNode = this.classTreeNode.getClassMap().get(typeReference);

            if (refNode == null) { //reference does not exist
                this.errorUtil.registerError(dispatchExpr.getLineNum(),
                        "Reference does not contain given method.");
            }
            else {
                methodPair = ((Pair<String, List<String>>)
                        refNode.getMethodSymbolTable()
                                .lookup(dispatchExpr.getMethodName()));
            }
        }
        return methodPair;
    }

    /**
     * Check if the given actual paramaters conform to parameter types given.
     * @param params the list of formal parameters
     * @param actualParams the list of actual parameters
     * @param lineNum the line number of the expression
     */
    private void checkMatchingParamLists(List<String> params, List<String>
            actualParams, int lineNum) {
        if (params != null) {

            //must have correct number of params
            if (actualParams.size() != params.size()) {
                this.errorUtil.registerError(lineNum,
                        "Wrong number of parameters");
            }
            for (int i = 0; i < params.size(); i++) {
                if (i >= actualParams.size()) {
                    break;
                }
                //must have correct types for params
                else if (!this.compatibleType(params.get(i), actualParams.get(i))) {
                    this.errorUtil.registerError(lineNum,
                            "Value passed in has incompatible type with parameter.");
                }
            }
        }
    }

    /**
     * Gets the type of the VarExpr and registers an error if you try to get the length
     * attribute of a non-array variable
     * @param varExpr the VarExpr
     * @param refName the Reference name
     * @return the type of the variable
     */
    private String getVarExprType(VarExpr varExpr, String refName) {

        if (varExpr.getName().equals("length") && !"this".equals(refName) &&
                !"super".equals(refName) && refName!=null) {
            String type = this.findVariableType(null, refName, varExpr.getLineNum());
            if (type == null){
                this.errorUtil.registerError(varExpr.getLineNum(),"Length attribute accessed on unknown variable "+refName);
            }
            else if (!type.endsWith("[]")) {
                this.errorUtil.registerError(varExpr.getLineNum(),
                        "Only array variables have length attribute.");
            }
            else {
                type = "int";
            }
            return type;
        }
        else if(varExpr.getName().equals("this")){
            return this.classTreeNode.getName();
        }
        else {
            this.errorUtil.registerErrorIfReservedName(
                    varExpr.getName(),varExpr.getLineNum());
            return this.findVariableType(refName,varExpr.getName(),varExpr.getLineNum());
        }

    }

    /**
     * Determines if the given castExpr is an upcast
     * @param castExpr the CastExpr
     * @param type the type to cast to
     * @param exprType the type of the expression being cast
     */
    private void determineUpCast(CastExpr castExpr, String type, String exprType) {
        //check if casting to primitive
        if ("int".equals(exprType) || "boolean".equals(exprType)) {
            this.errorUtil.registerError(castExpr.getLineNum(),
                    "Cannot cast primitives.");
            //sets if up cast
        } else if (this.compatibleType(type, exprType)) {
            castExpr.setUpCast(true);
        } else if (this.compatibleType(exprType, type)) {
            castExpr.setUpCast(false);
        } else {
            this.errorUtil.registerError(castExpr.getLineNum(),
                    "Incompatible types in cast.");

        }
    }

    /**
     * Determines if the given InstanceofExpr is an upcast
     * @param instanceofExpr the InstanceofExpr
     * @param type the type to be checked
     * @param exprType the type of the expression being checked
     */
    private void determineUpCheck(InstanceofExpr instanceofExpr, String type, String
            exprType) {
        //disallows checking if expr is a primitive
        if ("int".equals(exprType) || "boolean".equals(exprType)) {
            this.errorUtil.registerError(instanceofExpr.getLineNum(),
                    "Cannot check instance of primitives.");

            //checks if upcasting/downcasting
        } else if (this.compatibleType(type, exprType)) {
            instanceofExpr.setUpCheck(true);
        } else if (this.compatibleType(exprType, type)) {
            instanceofExpr.setUpCheck(false);
        } else { // disallows anything but up/downcasting
            this.errorUtil.registerError(instanceofExpr.getLineNum(),
                    "Incompatible types in instanceof.");
        }
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
            this.errorUtil.registerError(unaryExpr.getLineNum(), error);
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
        String type = getIncrDecrType(unaryExpr);

        if (type == null){
            this.errorUtil.registerError(unaryExpr.getLineNum(),
                    "Incremented or decremented variable not defined");
        }
        else if (!type.equals("int")){
            this.errorUtil.registerError(unaryExpr.getLineNum(),
                    "Incremented or decremented variable must be an int.");
        }

        unaryExpr.setExprType("int");
    }

    /**
     * Gets the type of the given UnaryIncr or UnaryDecr expressions and registers an
     * error if the expression that they are operating on is not a variable.
     * @param unaryExpr The Incr or Decr expression
     * @return the type of the incremented or decremented expression
     */
    private String getIncrDecrType(UnaryExpr unaryExpr) {
        String type = null;
        Expr expr = unaryExpr.getExpr();
        if (expr instanceof VarExpr){
            VarExpr varExpr = (VarExpr)expr;
            String refName = null;
            if(varExpr.getRef()!=null){
                refName = ((VarExpr)varExpr.getRef()).getName();
            }
            type = this.findVariableType(refName, varExpr.getName(),
                    varExpr.getLineNum());
        }
        else if (expr instanceof ArrayExpr){
            ArrayExpr arrayExpr = (ArrayExpr)expr;
            String refName = null;
            if(arrayExpr.getRef()!=null){
                refName = ((VarExpr)arrayExpr.getRef()).getName();
            }
            type=this.findVariableType(refName, arrayExpr.getName(),
                    arrayExpr.getLineNum());
            if(type!=null && type.endsWith("[]")){
                type = type.substring(0,type.length()-2);
            }
        }
        //We modified the grammar to prohibit this from happening, but just in case...
        else{
            this.errorUtil.registerError(unaryExpr.getLineNum(),
                    "Incremented or decremented expressions must be variables.");
        }
        return type;
    }

    /**
     * Registers an error if the variable, name, already exists as a local variable
     * @param name the name of the variable
     * @param lineNum the line number it occurs on
     */
    private void registerErrorIfLocalVarAlreadyDeclared(String name, int lineNum) {
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        for (int i = varSymbolTable.getCurrScopeLevel() - 1; i > this.fieldScope; i--) {
            if (varSymbolTable.peek(name, i) != null) {
                this.errorUtil.registerError(lineNum, "Variable already declared");
            }
        }
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
                this.errorUtil.registerError(field.getLineNum(),
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
            if (stmtList.getSize()==0 ||
                    !(stmtList.get(stmtList.getSize() - 1) instanceof ReturnStmt)) {
                this.errorUtil.registerError(method.getLineNum(),
                        "Missing return Statement");
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
        this.errorUtil.registerErrorIfReservedName(formal.getName(), lineNum);
        this.errorUtil.registerErrorIfInvalidType(formal.getType(), lineNum);
        if (this.classTreeNode.getVarSymbolTable().peek(formal.getName())!= null){
            this.errorUtil.registerError(lineNum, "Parameter already exists with same name.");
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
        int lineNum = stmt.getLineNum();
        String type = stmt.getType();
        this.errorUtil.registerErrorIfInvalidType(type, lineNum);
        this.errorUtil.registerErrorIfReservedName(stmt.getName(), lineNum);
        stmt.getInit().accept(this);
        this.registerErrorIfLocalVarAlreadyDeclared(stmt.getName(), lineNum);
        this.classTreeNode.getVarSymbolTable().add(stmt.getName(), type);
        if (!compatibleType(type, stmt.getInit().getExprType())) {
            this.errorUtil.registerError(lineNum,
                    "Type of variable incompatible with assignment.");
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
            this.errorUtil.registerError(ifStmt.getLineNum(),
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
            this.errorUtil.registerError(whileStmt.getLineNum(),
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
                this.errorUtil.registerError(forStmt.getLineNum(),
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
            this.errorUtil.registerError(breakStmt.getLineNum(),
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
                this.errorUtil.registerError(returnStmt.getLineNum(),
                        "Cannot return null from int or boolean method");

            }
            if (!this.compatibleType(this.currentMethodReturnType, returnType)) {
                this.errorUtil.registerError(returnStmt.getLineNum(),
                        "Return statement type does not match method return type.");
            }
        }
        else{
            if (!this.currentMethodReturnType.equals("void")){
                this.errorUtil.registerError(returnStmt.getLineNum(),
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
            this.errorUtil.registerError(assignExpr.getLineNum(),
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
            this.errorUtil.registerError(arrayAssignExpr.getLineNum(),
                    "Index of array must be an integer.");
        }

        this.checkAssignment(arrayAssignExpr.getRefName(), arrayAssignExpr.getName(),
                arrayAssignExpr.getExpr().getExprType(),
                arrayAssignExpr.getLineNum(), true);
        arrayAssignExpr.setExprType(arrayAssignExpr.getExpr().getExprType());
        return null;
    }

    /**
     * Visits dispatchExpr
     *
     * Checks that:
     * the method exists in the correct scope, defined by the reference
     * the parameters match the number and types of the method
     *
     *
     * @param dispatchExpr the dispatchExpr node
     * @return null
     */
    @Override
    public Object visit(DispatchExpr dispatchExpr){
        Expr refExpr = dispatchExpr.getRefExpr();

        SymbolTable methodTable = this.classTreeNode.getMethodSymbolTable();

        Pair<String, List<String>> methodPair = null;
        if (refExpr != null){

            //gets the method from the correct scope
            methodPair = getStringListPair(dispatchExpr);
        }
        else{
            methodPair = ((Pair<String, List<String>>)methodTable
                                    .lookup(dispatchExpr.getMethodName()));
        }

        //checks that method exists
        List<String> params = null;
        String exprType = null;
        if (methodPair != null) {
            params = methodPair.getValue();
            exprType = methodPair.getKey();
        }
        else {
            this.errorUtil.registerError(dispatchExpr.getLineNum(),
                    "Unknown method call.");
        }

        List<String> actualParams =
                (List<String>)dispatchExpr.getActualList().accept(this);

        //checks that the params are correct for method
        checkMatchingParamLists(params, actualParams, dispatchExpr.getLineNum());
        dispatchExpr.setExprType(exprType);
        return null;
    }



    /**
     * Visits an exprList, getting all of the parameter types from each
     * @param exprList the exprList node
     * @return the params
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
     * Visits the newExpr
     *
     * Checks to makes sure:
     * it is a valid type
     *
     * @param newExpr the newExpr node
     * @return null
     */
    @Override
    public Object visit(NewExpr newExpr){
        if (!this.classTreeNode.getClassMap().containsKey(newExpr.getType())){
            this.errorUtil.registerError(newExpr.getLineNum(), "Object type undefined.");
        }
        newExpr.setExprType(newExpr.getType());
        return null;
    }

    /**
     * Visits a newArrayExpr
     *
     * Checks to make sure:
     * its index is an integer
     * it is a valid object
     *
     * @param newArrayExpr the newArrayExpr node
     * @return null
     */
    @Override
    public Object visit(NewArrayExpr newArrayExpr){
        this.errorUtil.registerErrorIfInvalidType(newArrayExpr.getType(),
                newArrayExpr.getLineNum());

        Expr size = newArrayExpr.getSize();
        size.accept(this);
        if(size.getExprType() != null) {
            if (!size.getExprType().equals("int")) {
                this.errorUtil.registerError(newArrayExpr.getLineNum(),
                        "Array size is not int.");
            }
        }
        newArrayExpr.setExprType(newArrayExpr.getType());
        return null;
    }

    /**
     * Visits a instanceofExpr
     *
     * Makes sure that:
     * only checking to see if it is a super/subtype
     * cannot check primatives/ if it is a primitive
     *
     * @param instanceofExpr the instanceofExpr
     * @return null
     */
    @Override
    public Object visit(InstanceofExpr instanceofExpr){
        String type = instanceofExpr.getType();
        boolean typeIsArray = false;

        //edits type of array so that it can be found in the class map
        if (type.endsWith("[]")){
            typeIsArray = true;
            type = type.substring(0, type.length()-2);
        }

        //disallows checking if expr is a primitive
        if (instanceofExpr.getType().equals("int") ||
                instanceofExpr.getType().equals("boolean")){
            this.errorUtil.registerError(instanceofExpr.getLineNum(),
                    "Cannot check if expr is primitive type.");
        } //makes sure that the type is valid
        else if (!this.classTreeNode.getClassMap().containsKey(type)){
            this.errorUtil.registerError(instanceofExpr.getLineNum(),
                    "Unknown instanceof type.");
        }
        instanceofExpr.getExpr().accept(this);

        String exprType = instanceofExpr.getExpr().getExprType();

        if (exprType != null){
            //deals with checking types of arrays
            if (typeIsArray && exprType.endsWith("[]")){
                exprType = exprType.substring(0, exprType.length()-2);
            }
            else if (typeIsArray){
                type += "[]";
            }
            determineUpCheck(instanceofExpr, type, exprType);
        }
        instanceofExpr.setExprType("boolean");
        return null;
    }



    /**
     * Visits castExpr
     *
     * Makes sure that:
     * Casting to a valid type
     * Cannot cast to a primitive
     * Cannot cast a primitive
     * Can only up cast/downcast
     *
     * @param castExpr the castExpr node
     * @return null
     */
    @Override
    public Object visit(CastExpr castExpr){


        String type = castExpr.getType();
        boolean typeIsArray = false;

        //edits type if casting to array
        if (type.endsWith("[]")){
            typeIsArray = true;
            type = type.substring(0, type.length()-2);
        }

        //checks if casting to primitive
        if (castExpr.getType().equals("int") || castExpr.getType().equals("boolean")){
            this.errorUtil.registerError(castExpr.getLineNum(),
                    "Cannot cast to a primitive type.");
        }
        //checks if casting to a valid type
        else if (!this.classTreeNode.getClassMap().containsKey(type)
                && !type.equals("int") && !type.equals("boolean")){
            this.errorUtil.registerError(castExpr.getLineNum(), "Unknown cast type.");
        }
        castExpr.getExpr().accept(this);

        String exprType = castExpr.getExpr().getExprType();

        if(castExpr.getType().equals(exprType)){
            castExpr.setExprType(castExpr.getType());
            castExpr.setUpCast(true);
            return null;
        }

        if (exprType != null){
            //deals with casting arrays
            if (typeIsArray && exprType.endsWith("[]")){
                exprType = exprType.substring(0, exprType.length()-2);
            }
            else if (typeIsArray){
                type += "[]";
            }
            determineUpCast(castExpr, type, exprType);
        }
        castExpr.setExprType(castExpr.getType());

        return null;
    }



    /**
     * Visits a constIntExpr
     * @param constIntExpr the constIntExpr
     * @return null
     */
    @Override
    public Object visit(ConstIntExpr constIntExpr){
        constIntExpr.setExprType("int");
        return null;
    }

    /**
     * Visits a constBooleanExpr
     * @param constBooleanExpr the constBooleanExpr
     * @return null
     */
    @Override
    public Object visit(ConstBooleanExpr constBooleanExpr){
        constBooleanExpr.setExprType("boolean");
        return null;
    }

    /**
     * Visits a constStringExpr
     * @param constStringExpr the constStringExpr
     * @return null
     */
    @Override
    public Object visit(ConstStringExpr constStringExpr){
        constStringExpr.setExprType("String");
        return null;
    }

    /**
     * Visits a binaryArithPlusExpr, checking if it has valid types
     * @param binaryArithPlusExpr the binary arith expr
     * @return null
     */
    @Override
    public Object visit(BinaryArithPlusExpr binaryArithPlusExpr){
        this.binaryExprTypeChecker(binaryArithPlusExpr, "int", "int");
        return null;
    }

    /**
     * Visits a binaryArithMinusExpr, checking if it has valid types
     * @param binaryArithMinusExpr the binary arith expr
     * @return null
     */
    @Override
    public Object visit(BinaryArithMinusExpr binaryArithMinusExpr){
        this.binaryExprTypeChecker(binaryArithMinusExpr, "int", "int");
        return null;
    }

    /**
     * Visits a binaryArithTimesExpr, checking if it has valid types
     * @param binaryArithTimesExpr the binary arith expr
     * @return null
     */
    @Override
    public Object visit(BinaryArithTimesExpr binaryArithTimesExpr){
        this.binaryExprTypeChecker(binaryArithTimesExpr, "int", "int");
        return null;
    }

    /**
     * Visits a binaryArithDivideExpr, checking if it has valid types
     * @param binaryArithDivideExpr the binary arith expr
     * @return null
     */
    @Override
    public Object visit(BinaryArithDivideExpr binaryArithDivideExpr){
        this.binaryExprTypeChecker(binaryArithDivideExpr, "int", "int");
        return null;
    }

    /**
     * Visits a binaryArithModulusExpr, checking if it has valid types
     * @param binaryArithModulusExpr the binary arith expr
     * @return null
     */
    @Override
    public Object visit(BinaryArithModulusExpr binaryArithModulusExpr){
        this.binaryExprTypeChecker(binaryArithModulusExpr, "int", "int");
        return null;
    }

    /**
     * Visits a binaryCompEqExpr, checking if it has valid types
     * @param binaryCompEqExpr the binary comp expr
     * @return null
     */
    @Override
    public Object visit(BinaryCompEqExpr binaryCompEqExpr) {
        this.binaryCompEqualityChecker(binaryCompEqExpr);
        return null;
    }

    /**
     * Visits a binaryCompNeExpr, checking if it has valid types
     * @param binaryCompNeExpr the binary comp expr
     * @return null
     */
    @Override
    public Object visit(BinaryCompNeExpr binaryCompNeExpr) {
        this.binaryCompEqualityChecker(binaryCompNeExpr);
        return null;
    }

    /**
     * Visits a binaryCompLtExpr, checking if it has valid types
     * @param binaryCompLtExpr the binary comp expr
     * @return null
     */
    @Override
    public Object visit(BinaryCompLtExpr binaryCompLtExpr) {
        this.binaryExprTypeChecker(binaryCompLtExpr, "int", "boolean");
        return null;
    }

    /**
     * Visits a binaryCompLeqExpr, checking if it has valid types
     * @param binaryCompLeqExpr the binary comp expr
     * @return null
     */
    @Override
    public Object visit(BinaryCompLeqExpr binaryCompLeqExpr) {
        this.binaryExprTypeChecker(binaryCompLeqExpr, "int", "boolean");
        return null;
    }

    /**
     * Visits a binaryCompGtExpr, checking if it has valid types
     * @param binaryCompGtExpr the binary comp expr
     * @return null
     */
    @Override
    public Object visit(BinaryCompGtExpr binaryCompGtExpr) {
        this.binaryExprTypeChecker(binaryCompGtExpr, "int", "boolean");
        return null;
    }

    /**
     * Visits a binaryCompGeqExpr, checking if it has valid types
     * @param binaryCompGeqExpr the binary comp expr
     * @return null
     */
    @Override
    public Object visit(BinaryCompGeqExpr binaryCompGeqExpr) {
        this.binaryExprTypeChecker(binaryCompGeqExpr, "int", "boolean");
        return null;
    }

    /**
     * Visits a binaryLogicAndExpr, checking if it has valid types
     * @param binaryLogicAndExpr the binary logic expr
     * @return null
     */
    @Override
    public Object visit(BinaryLogicAndExpr binaryLogicAndExpr) {
        this.binaryExprTypeChecker(binaryLogicAndExpr, "boolean", "boolean");
        return null;
    }

    /**
     * Visits a binaryLogicOrExpr, checking if it has valid types
     * @param binaryLogicOrExpr the binary logic expr
     * @return null
     */
    @Override
    public Object visit(BinaryLogicOrExpr binaryLogicOrExpr) {
        this.binaryExprTypeChecker(binaryLogicOrExpr, "boolean", "boolean");
        return null;
    }

    /**
     * Visits a unary neg expr, checking if it has valid types
     * @param unaryNegExpr the unary expr node
     * @return null
     */
    @Override
    public Object visit(UnaryNegExpr unaryNegExpr){
        this.negNotChecker(unaryNegExpr, "int",
                "Type of arithmetically negated expression must be int.");
        return null;
    }

    /**
     * Visits a unary not expr, checking if it has valid types
     * @param unaryNotExpr the unary expr node
     * @return null
     */
    @Override
    public Object visit(UnaryNotExpr unaryNotExpr){
        this.negNotChecker(unaryNotExpr, "boolean",
                "Type of logically negated expression must be boolean.");
        return null;
    }

    /**
     * Visits a unary incr expr, checking if it has valid types
     * @param unaryIncrExpr the unary expr node
     * @return null
     */
    @Override
    public Object visit(UnaryIncrExpr unaryIncrExpr){
        this.incrDecrChecker(unaryIncrExpr);
        return null;
    }

    /**
     * Visits a unary decr expr, checking if it has valid types
     * @param unaryDecrExpr the unary decr expr node
     * @return null
     */
    @Override
    public Object visit(UnaryDecrExpr unaryDecrExpr){
        this.incrDecrChecker(unaryDecrExpr);
        return null;
    }

    /**
     * Visits a var expr
     *
     * Makes sure that:
     * the var expr exists and has a valid type
     *
     *
     * @param varExpr the var expr node
     * @return null
     */
    @Override
    public Object visit(VarExpr varExpr){
        String refName = null;

        if(varExpr.getName().equals("null")){
            if(varExpr.getRef()!=null){
                this.errorUtil.registerError(varExpr.getLineNum(),
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

        String type = getVarExprType(varExpr, refName);
        if(type==null){
            this.errorUtil.registerError(varExpr.getLineNum(),
                    "Undeclared variable access.");
        }
        varExpr.setExprType(type);

        return null;
    }



    /**
     * Visits an array expr
     *
     * Makes sure that :
     * has a valid name and type
     * the index of the arrary is an integer
     *
     *
     * @param arrayExpr the array expr node
     * @return null
     */
    @Override
    public Object visit(ArrayExpr arrayExpr){
        String ref = null;
        if (arrayExpr.getRef() != null){
            ref = ((VarExpr)arrayExpr.getRef()).getName();
        }

        String type = this.findVariableType(ref, arrayExpr.getName(),
                arrayExpr.getLineNum());
        type = checkValidArrayType(arrayExpr.getLineNum(), type);

        // index of an array is an integer
        if (arrayExpr.getIndex() != null) {
            arrayExpr.getIndex().accept(this);
            if (!arrayExpr.getIndex().getExprType().equals("int")) {
                this.errorUtil.registerError(arrayExpr.getLineNum(),
                        "Array index must be an int.");
            }
        }

        arrayExpr.setExprType(type);

        return null;
    }

    /**
     * Visits a try stmt, creating a new scope for the body of the stmt
     * @param node the try stmt
     * @return null
     */
    public Object visit(TryStmt node) {
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        varSymbolTable.enterScope();
        node.getStmtList().accept(this);
        varSymbolTable.exitScope();
        node.getCatchList().accept(this);
        return null;
    }

    /**
     * Visits a throw stmt, checking that it throws an Exception
     * @param node the throw stmt
     * @return null
     */
    @Override
    public Object visit(ThrowStmt node) {
        node.getExpr().accept(this);

        if (!this.compatibleType("Exception", node.getExpr().getExprType())){
            this.errorUtil.registerError(node.getLineNum(),
                    "Object thrown must be an Exception.");
        }
        return null;
    }

    /**
     * Visits a Catch stmt, checking that it catches an Exception and
     * creating a new scope for the body
     * @param node
     * @return
     */
    @Override
    public Object visit(CatchStmt node) {
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        varSymbolTable.enterScope();
        Formal formal = node.getFormal();
        this.registerErrorIfLocalVarAlreadyDeclared(formal.getName(), node.getLineNum());
        formal.accept(this);

        if (!this.compatibleType( "Exception", formal.getType())){
            this.errorUtil.registerError(node.getLineNum(),
                    "Object thrown must be an Exception.");
        }


        node.getStmtList().accept(this);
        varSymbolTable.exitScope();
        return null;
    }
}
