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
 * Created by Phoebe Hughes on 3/3/2017.
 */
public class MemberAdderVisitor extends Visitor {

    private ClassTreeNode classTreeNode;
    private ErrorHandler errorHandler;
    private Set<String> disallowedNames;

    public MemberAdderVisitor(ErrorHandler errorHandler, Set<String> disallowedNames){
        this.errorHandler = errorHandler;
        this.disallowedNames = disallowedNames;
    }

    public void getSymbolTables(ClassTreeNode classTreeNode){
        this.classTreeNode = classTreeNode;
        this.classTreeNode.getVarSymbolTable().enterScope();
        this.classTreeNode.getMethodSymbolTable().enterScope();
        this.classTreeNode.getASTNode().accept(this);
    }
    
    @Override
    public Object visit(Field node) {
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        String name = node.getName();
        int lineNum = node.getLineNum();
        this.registerErrorIfInvalidType(node.getType(),node.getLineNum());
        if (varSymbolTable.peek(name) == null) {
            this.registerErrorIfReservedName(name,lineNum);
            varSymbolTable.add(name, node.getType());
        }
        else{
            this.errorHandler.register(2, this.classTreeNode.getASTNode().getFilename(),
                    lineNum,"Field already declared." );
        }
        return null;
    }
    
    @Override
    public Object visit(Method node) {
        SymbolTable methodSymbolTable = this.classTreeNode.getMethodSymbolTable();
        String name = node.getName();
        int lineNum = node.getLineNum();
        this.registerErrorIfInvalidType(node.getReturnType(),node.getLineNum());
        if (methodSymbolTable.peek(name) == null) {
            registerErrorIfReservedName(name,lineNum );
            List<String> paramTypes = (List) node.getFormalList().accept(this);
            Pair<String, List<String>> methodData = new Pair<>(node.getReturnType(), paramTypes);
            methodSymbolTable.add(name, methodData);
        }
        else{
            this.errorHandler.register(2, this.classTreeNode.getASTNode().getFilename(),
                    lineNum,"Method already declared." );
        }
       return null;
    }

    private void registerErrorIfReservedName(String name, int lineNum){
        if (disallowedNames.contains(name)){
            this.errorHandler.register(
                    2, this.classTreeNode.getASTNode().getFilename(), lineNum,
                    "Reserved word, "+name+", cannot be used as a field or method name");
        }
    }

    private void registerErrorIfInvalidType(String type, int lineNum){
        if(type.endsWith("[]")){
            type = type.substring(0,type.length()-2);
        }
        if(!this.classTreeNode.getClassMap().containsKey(type)
                && !type.equals("int") && !type.equals("boolean")){
            this.errorHandler.register(2,this.classTreeNode.getASTNode().getFilename(),
                    lineNum, "Invalid Type");
        }
    }

    @Override
    public Object visit(FormalList node) {
        List<String> paramTypes = new ArrayList<>();
        for (ASTNode element : node){
            paramTypes.add((String)element.accept(this));
        }
        return paramTypes;
    }

    public Object visit(Formal node){
        this.registerErrorIfInvalidType(node.getType(),node.getLineNum());
        return node.getType();
    }
}
