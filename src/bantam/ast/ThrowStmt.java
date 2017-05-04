package bantam.ast;

import bantam.visitor.Visitor;

/**
 * Created by Phoebe Hughes on 5/4/2017.
 */
public class ThrowStmt extends Stmt{
    /**
     * An expression to be thrown
     */
    protected Expr expr;

    /**
     * ThrowStmt constructor
     *
     * @param lineNum source line number corresponding to this AST node
     * @param expr    expression to be returned (null for no return expression)
     */
    public ThrowStmt(int lineNum, Expr expr) {
        super(lineNum);
        this.expr = expr;
    }

    /**
     * Get the return expression
     *
     * @return expression
     */
    public Expr getExpr() {
        return expr;
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
