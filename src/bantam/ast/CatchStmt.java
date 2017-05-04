package bantam.ast;

import bantam.visitor.Visitor;

/**
 * Created by Phoebe Hughes on 5/4/2017.
 */
public class CatchStmt extends Stmt {

    protected Formal formal;

    protected StmtList stmtList;

    public CatchStmt(int lineNum, Formal formal, StmtList stmtList) {
        super(lineNum);
        this.formal = formal;
        this.stmtList = stmtList;
    }


    public Formal getFormal() {
        return this.formal;
    }

    public StmtList getStmtList() {
        return this.stmtList;
    }

    /**
     * Visitor method
     *
     * @param v bantam.visitor object
     * @return result of visiting this node
     * @see bantam.visitor.Visitor
     */
    public Object accept(Visitor v) {
        return v.visit(this);
    }
}
