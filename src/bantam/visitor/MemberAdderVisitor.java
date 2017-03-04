package bantam.visitor;

import bantam.ast.*;
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

    private SymbolTable varSymbolTable;
    private SymbolTable methodSymbolTable;
    private ErrorHandler errorHandler;
    private String filename;
    private Set<String> disallowedNames;

    public MemberAdderVisitor(ErrorHandler errorHandler, Set<String> disallowedNames){
        this.errorHandler = errorHandler;
        this.disallowedNames = disallowedNames;
    }

    public void getSymbolTables(Class_ classNode, SymbolTable varSymbolTable,
                                SymbolTable methodSymbolTable){
        this.varSymbolTable = varSymbolTable;
        this.varSymbolTable.enterScope();

        this.methodSymbolTable = methodSymbolTable;
        this.methodSymbolTable.enterScope();
        this.filename = classNode.getFilename();
        classNode.accept(this);
    }
    
    @Override
    public Object visit(Field node) {
        String name = node.getName();
        int lineNum = node.getLineNum();
        if (this.varSymbolTable.peek(name) != null) {
            this.checkIfReservedName(name,lineNum);
            this.varSymbolTable.add(name, node.getType());
        }
        else{
            errorHandler.register(2, filename, lineNum,"Field already declared." );
        }
        return null;
    }
    
    @Override
    public Object visit(Method node) {
        String name = node.getName();
        int lineNum = node.getLineNum();
        if (this.methodSymbolTable.peek(name) != null) {
            checkIfReservedName(name,lineNum );
            List<String> paramTypes = (List) node.getFormalList().accept(this);
            Pair<String, List<String>> methodData = new Pair<>(node.getReturnType(), paramTypes);
            this.methodSymbolTable.add(name, methodData);

        }
        else{
            errorHandler.register(2, filename, lineNum,"Method already declared." );
        }
       return null;
    }

    private void checkIfReservedName(String name, int lineNum){
        if (disallowedNames.contains(name)){
            errorHandler.register(2, filename, lineNum,
                    "Reserved word, " + name
                            + ", cannot be used as a field or method name");
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
        return node.getType();
    }
}
