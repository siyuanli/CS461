package bantam.visitor;

import bantam.ast.ArrayExpr;
import bantam.ast.VarExpr;
import bantam.util.ClassTreeNode;
import bantam.util.ErrorHandler;

import java.util.Set;

/**
 * Created by Phoebe Hughes on 3/7/2017.
 */
public class RegisterForwardReferenceVisitor extends Visitor {

    private ClassTreeNode classTreeNode;
    private ErrorHandler errorHandler;
    private String varName;

    public RegisterForwardReferenceVisitor(ClassTreeNode classTreeNode,
                                           ErrorHandler errorHandler, String varName){
        this.classTreeNode = classTreeNode;
        this.errorHandler = errorHandler;
        this.varName = varName;
    }

    private void registerError(int lineNum, String error) {
        this.errorHandler.register(2, this.classTreeNode.getASTNode().getFilename(),
                lineNum, error);
    }

    private void checkForwardReference(String exprName, int lineNum) {
        if (exprName == null){
            this.registerError(lineNum, "Illegal forward reference.");
        }
        else if(exprName.equals(this.varName) && this.classTreeNode.getVarSymbolTable().peek(exprName) != null){
            this.registerError(lineNum, "Cannot reference itself.");
        }
    }

    @Override
    public Object visit(VarExpr varExpr){
        String exprName;
        if (varExpr.getName().equals("this") || varExpr.getName().equals("super")){
            return null;
        }
        else if (varExpr.getName().equals("length")){
            exprName = (String)this.classTreeNode.getVarSymbolTable().lookup(((VarExpr)varExpr.getRef()).getName());
        }
        else{
            exprName = (String)this.classTreeNode.getVarSymbolTable().lookup(varExpr.getName());
        }

        checkForwardReference(exprName, varExpr.getLineNum());

        return null;
    }


    @Override
    public Object visit(ArrayExpr arrayExpr){
        String exprName = (String)this.classTreeNode.getVarSymbolTable().lookup(arrayExpr.getName());
        checkForwardReference(exprName, arrayExpr.getLineNum());
        return null;
    }

}
