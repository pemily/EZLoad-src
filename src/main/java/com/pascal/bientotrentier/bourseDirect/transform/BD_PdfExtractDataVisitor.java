package com.pascal.bientotrentier.bourseDirect.transform;

import com.pascal.bientotrentier.parsers.bourseDirect.*;

public class BD_PdfExtractDataVisitor implements BourseDirectParserVisitor {
    @Override
    public Object visit(SimpleNode node, Object data) {
        throw new RuntimeException("Visit SimpleNode Impossible");
    }

    @Override
    public Object visit(BD_start node, Object data) {
        return next(node, data);
    }

    @Override
    public Object visit(BD_account node, Object data) {
        model(data).setAccountNumber(values(node)[0]);
        model(data).setAccountType(values(node)[1]);
        return next(node, data);
    }

    @Override
    public Object visit(BD_tab_header node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(BD_tab_body node, Object data) {
        return next(node, data);
    }

    private BourseDirectVisitorModel model(Object data){
        return (BourseDirectVisitorModel) data;
    }

    private String[] values(SimpleNode node){
        return (String[]) node.jjtGetValue();
    }

    private Object next(SimpleNode node, Object data){
        return node.childrenAccept(this, data);
    }
}
