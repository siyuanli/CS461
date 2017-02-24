package bantam.visitor;


import bantam.ast.*;


public class MainMainVisitor extends Visitor {

    public boolean hasMain(ASTNode root){
        return (boolean)root.accept(this);
    }

    private boolean listNodeHasMain(ListNode nodes) {
        for (ASTNode node: nodes){
            boolean hasMain = (boolean)node.accept(this);
            if (hasMain){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object visit(Program node) {
        return node.getClassList().accept(this);
    }

    @Override
    public Object visit(ClassList nodes){
        return this.listNodeHasMain(nodes);
    }

    @Override
    public Object visit(Class_ node) {
        boolean nameMain = node.getName().toLowerCase().equals("main");
        if (nameMain){
            return node.getMemberList().accept(this);
        }
        return false;
    }

    @Override
    public Object visit(MemberList nodes){
       return this.listNodeHasMain(nodes);
    }

    @Override
    public Object visit(Field node) {
        return false;
    }

    @Override
    public Object visit(Method node) {
        boolean nameMain = node.getName().toLowerCase().equals("main");
        boolean typeVoid = node.getReturnType().toLowerCase().equals("void");
        if (nameMain && typeVoid){
            return true;
        }
        return false;
    }
}
