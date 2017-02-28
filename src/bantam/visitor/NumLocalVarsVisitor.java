package bantam.visitor;

import bantam.ast.*;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Phoebe Hughes on 2/27/2017.
 */
public class NumLocalVarsVisitor extends Visitor {

    private Map<String, Integer> localVars;

    public Map<String, Integer> getNumLocalVars(Program ast){
        this.localVars = new HashMap<>();
        ast.accept(this);
        return localVars;
    }

    public Object visit(Class_ node) {
        List<Pair<String, Integer>> varPairList =
                (List<Pair<String, Integer>>) node.getMemberList().accept(this);
        for (Pair<String, Integer> varPair : varPairList){
            String name = node.getName() + "." + varPair.getKey();
            this.localVars.put(name, varPair.getValue());
        }
        return null;
    }

    public Object visit(MemberList nodes) {
        List<Pair<String, Integer>> varPairList = new ArrayList<>();
        for (ASTNode node : nodes) {
            Pair<String, Integer> numVarPair = (Pair<String, Integer>)node.accept(this);
            if (numVarPair != null){
                varPairList.add(numVarPair);
            }
        }
        return varPairList;
    }

    public Object visit(Field node){
        return null;
    }

    public Object visit(Method node){
        int numVars = node.getFormalList().getSize();
        numVars += (int)node.getStmtList().accept(this);
        return new Pair<>(node.getName(), numVars);
    }

    public Object visit(StmtList nodes){
        int numVars = 0;
        for(ASTNode node: nodes){
            numVars += (int)node.accept(this);
        }
        return numVars;
    }

    public Object visit(ExprStmt node){
        return 0;
    }

    public Object visit(DeclStmt node) {
        return 1;
    }

    public Object visit(IfStmt node){
        Stmt elseStmt = node.getElseStmt();
        int numVar = (int)node.getThenStmt().accept(this);
        if (elseStmt != null){
            numVar += (int)elseStmt.accept(this);
        }
        return numVar;
    }

    public Object visit(WhileStmt node){
        return node.getBodyStmt().accept(this);
    }

    public Object visit(ForStmt node){
        return node.getBodyStmt().accept(this);
    }

    public Object visit(BreakStmt node){
        return 0;
    }

    public Object visit(ReturnStmt node){
        return 0;
    }

    public Object visit(BlockStmt node){
        return node.getStmtList().accept(this);
    }


}
