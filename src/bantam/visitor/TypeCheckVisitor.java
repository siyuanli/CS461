package bantam.visitor;

import bantam.ast.*;
import bantam.util.ClassTreeNode;
import bantam.util.ErrorHandler;
import bantam.util.SymbolTable;
import com.sun.deploy.ref.AppModel;

import java.util.List;
import java.util.Set;

/**
 * Created by Phoebe Hughes on 3/4/2017.
 */
public class TypeCheckVisitor extends Visitor{


    private ClassTreeNode classTreeNode;
    private ErrorHandler errorHandler;
    private String filename;
    private String currentMethodReturnType;
    private Set<String> disallowedNames;



    public TypeCheckVisitor(ErrorHandler errorHandler, Set<String> disallowedNames){
        this.disallowedNames = disallowedNames;
        this.errorHandler = errorHandler;
    }

    /**
     *
     * @param type1 the hypothetical parent
     * @param type2 the hypothetical child
     * @return
     */
    private boolean isAncestorOf(String type1, String type2){
        if(type1.equals(type2)){
            return true;
        }
        ClassTreeNode type2Parent=this.classTreeNode.getClassMap().get(type2).getParent();
        if(type2Parent==null){
            return false;
        }
        else{
            return this.isAncestorOf(type1,type2Parent.getName());
        }
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

    public void checkTypes(ClassTreeNode classTreeNode){
        this.classTreeNode = classTreeNode;
        Class_ classASTNode = this.classTreeNode.getASTNode();
        classASTNode.accept(this);
        this.filename = classASTNode.getFilename();
    }

    @Override
    public Object visit(Field field){
        Expr init = field.getInit();
        init.accept(this);
        if(!isAncestorOf(field.getType(),init.getExprType())){
            this.errorHandler.register(2,this.filename,field.getLineNum(),
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
        method.getStmtList().accept(this);
        varSymbolTable.exitScope();
        return null;
    }

    @Override
    public Object visit(Formal formal){
        this.classTreeNode.getVarSymbolTable().add(formal.getName(),formal.getType());
        return null;
    }

    @Override
    public Object visit(DeclStmt stmt){
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        registerErrorIfInvalidType(stmt.getType(),stmt.getLineNum());
        registerErrorIfReservedName(stmt.getName(),stmt.getLineNum());
        stmt.getInit().accept(this);
        for(int i = varSymbolTable.getCurrScopeLevel();i>0;i--){
            if(varSymbolTable.peek(stmt.getName(),i)!=null){
                this.errorHandler.register(2,this.filename,stmt.getLineNum(),
                        "Variable already declared");
            }
        }
        varSymbolTable.add(stmt.getName(),stmt.getType());
        if(!isAncestorOf(stmt.getType(),stmt.getInit().getExprType())){
            this.errorHandler.register(2, this.filename,stmt.getLineNum(),
                    "Type of variable incompatible with assignment.");
        }
        return null;
    }


}
