import org.antlr.v4.runtime.tree.TerminalNode;

public class MyVisitor extends SysYParserBaseVisitor<Void>{
    @Override
    public Void visitTerminal(TerminalNode node) {
        String n = (node.getParent()).toString();
        //name correct
        String name = node.getText();
        System.out.println(name);
        return null;
    }
}
