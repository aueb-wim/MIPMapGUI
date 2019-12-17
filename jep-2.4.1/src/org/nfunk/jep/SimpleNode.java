/*****************************************************************************
JEP 2.4.1, Extensions 1.1.1
April 30 2007
(c) Copyright 2007, Nathan Funk and Richard Morris
See LICENSE-*.txt for license information.
 *****************************************************************************/
/* Generated By:JJTree: Do not edit this line. SimpleNode.java */
package org.nfunk.jep;

import java.io.StringReader;

public class SimpleNode implements Node {

    protected Node parent;
    protected Node[] children;
    protected int id;
    protected Parser parser;

    public SimpleNode(int i) {
        id = i;
    }

    public SimpleNode(Parser p, int i) {
        this(i);
        parser = p;
    }

    @Override
    public Object clone() {
        try {
            SimpleNode clone = (SimpleNode) super.clone();
            if (this.children != null) {
                clone.children = new Node[this.children.length];
                for (int i = 0; i < this.children.length; i++) {
                    clone.children[i] = (Node) this.children[i].clone();
                    clone.children[i].jjtSetParent(clone);
                }
            }
            //clone.parser = new Parser(new StringReader(""));
            return clone;
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    public void accept(IExpressionVisitor visitor) {
        visitor.visitSimpleNode(this);
    }
    
    public void jjtOpen() {
    }

    public void jjtClose() {
    }

    public void jjtSetParent(Node n) {
        parent = n;
    }

    public Node jjtGetParent() {
        return parent;
    }

    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new Node[i + 1];
        } else if (i >= children.length) {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }

    public Node jjtGetChild(int i) {
        return children[i];
    }

    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data) throws ParseException {
        return visitor.visit(this, data);
    }

    /** Accept the visitor. **/
    public Object childrenAccept(ParserVisitor visitor, Object data) throws ParseException {
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                children[i].jjtAccept(visitor, data);
            }
        }
        return data;
    }

    /* You can override these two methods in subclasses of SimpleNode to
    customize the way the node appears when the tree is dumped.  If
    your output uses more than one line you should override
    toString(String), otherwise overriding toString() is probably all
    you need to do. */
    public String toString() {
        return ParserTreeConstants.jjtNodeName[id];
    }

    public String toLongString() {
        try {
            ParserDumpVisitor visitor = new ParserDumpVisitor();
            this.jjtAccept(visitor, null);
            return visitor.getResult();
        } catch (ParseException ex) {
            return null;
        }
    }

    public String toString(String prefix) {
        return prefix + toString();
    }

    /* Override this method if you want to customize how the node dumps
    out its children. */
    public void dump(String prefix) {
        System.out.println(toString(prefix));
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode) children[i];
                if (n != null) {
                    n.dump(prefix + " ");
                }
            }
        }
    }

    /**
     * Returns the id of the node (for simpler identification).
     */
    public int getId() {
        return id;
    }
}
