package bantam.ast;

import bantam.visitor.Visitor;

/**
 * Created by Phoebe Hughes on 5/4/2017.
 */
public class CatchList extends ListNode{


    /**
     * Catch list constructor
     *
     * @param lineNum source line number corresponding to this AST node
     */
    public CatchList(int lineNum) {
        super(lineNum);
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
