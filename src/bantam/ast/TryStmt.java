package bantam.ast;

import bantam.visitor.Visitor;

/**
 * Created by Phoebe Hughes on 5/4/2017.
 */
public class TryStmt extends Stmt{

    protected StmtList stmtList;
    protected CatchList catchList;

    public TryStmt(int lineNum, StmtList stmtList, CatchList catchList) {
        super(lineNum);
        this.stmtList = stmtList;
        this.catchList = catchList;
    }

    public StmtList getStmtList(){
        return this.stmtList;
    }

    public CatchList getCatchList(){
        return this.catchList;
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
