package ru.nsu.fit.snegireva.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.parboiled.Node;
import ru.nsu.fit.snegireva.compiler.exception.CompilationException;
import ru.nsu.fit.snegireva.compiler.parser.Parser;
import ru.nsu.fit.snegireva.compiler.parser.Type;
import ru.nsu.fit.snegireva.compiler.parser.Variable;
import ru.nsu.fit.snegireva.compiler.writer.ByteCodeWriter;
import sun.tools.tree.Vset;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import static ru.nsu.fit.snegireva.compiler.parser.Type.INT;

@SuppressWarnings("Duplicates")
public class Compiler {

    private HashMap<String, Variable> variablesMap = new HashMap<>();
    private ByteCodeWriter writer;
    private Parser parser;
    private Label s_end;

    public void compile(String sourceFileName, String classFileName) throws IOException, CompilationException {
        writer = new ByteCodeWriter();
        parser = new Parser(sourceFileName);
        Node<Object> node = parser.parse();
        if (null == node || node.hasError()) {
            throw new CompilationException("Проблемы парсинга файла");
        }
        for (Node<Object> leaf : node.getChildren()) {
            compileStatement(leaf.getChildren().get(0));
        }
        writer.write(classFileName);
    }

    private void compileStatement(Node<Object> root) throws IOException, CompilationException {
        Node<Object> node = root.getChildren().get(1).getChildren().get(0);
        String nodeLabel = node.getLabel();
        if (nodeLabel.equalsIgnoreCase("If")){
            compileIf(node);
        } else if (nodeLabel.equalsIgnoreCase("While")){
            compileWhile(node);
        } else if (nodeLabel.equalsIgnoreCase("Print")){
            compilePrint(node);
        } else if (nodeLabel.equalsIgnoreCase("Assignment")){
            compileAssignment(node);
        }
    }

    private void compileIf(Node<Object> root) throws IOException, CompilationException {
        Type exprType = compileIfExpression(root.getChildren().get(1));
        if (exprType != Type.BOOL){
            throw new CompilationException("Ошибка при парсинге иф, expression вернул не bool");
        }
        Label labelFail = new Label();
        writer.getMethodVisitor().visitIntInsn(Opcodes.BIPUSH, 0);
        writer.getMethodVisitor().visitJumpInsn(Opcodes.IF_ICMPEQ, labelFail);
        //Romashka start
        if (root.getChildren().get(3).getChildren().get(0).getLabel().equals("Break")){
            //compileBreak(root.getChildren().get(3).getChildren().get(0));
            if (!root.getParent().getParent().getParent().getParent().getParent().getLabel().equals("While"))
                throw new CompilationException("break without while");
            writer.getMethodVisitor().visitJumpInsn(Opcodes.GOTO, s_end);
        } else {
            for (Node<Object> leaf : root.getChildren().get(3).getChildren().get(0).getChildren()) {
                compileStatement(leaf.getChildren().get(0));
            }
        }
        writer.getMethodVisitor().visitLabel(labelFail);
    }

    private void compileBreak(Node<Object> root){

    }

    private void compileWhile(Node<Object> root) throws IOException, CompilationException {
        Label start = new Label();
        s_end = new Label();
        Label condition = new Label();
        writer.getMethodVisitor().visitLabel(condition);
        Type exprType = compileIfExpression(root.getChildren().get(1));
        if (exprType != Type.BOOL){
            throw new CompilationException("Ошибка при парсинге иф, expression вернул не bool");
        }
        writer.getMethodVisitor().visitIntInsn(Opcodes.BIPUSH, 0);
        writer.getMethodVisitor().visitJumpInsn(Opcodes.IF_ICMPEQ, s_end);
        writer.getMethodVisitor().visitLabel(start);
        //Romashka start
        for (Node<Object> leaf : root.getChildren().get(3).getChildren()) {
            compileStatement(leaf.getChildren().get(0));
        }
        writer.getMethodVisitor().visitJumpInsn(Opcodes.GOTO, condition);
        writer.getMethodVisitor().visitLabel(s_end);
    }

    private void compilePrint(Node<Object> node) throws IOException, CompilationException {
        writer.getMethodVisitor().visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        String nodeLabel = node.getChildren().get(1).getChildren().get(0).getLabel();
        if (nodeLabel.equals("String")){
            String content = getNodeContent(node.getChildren().get(1).getChildren().get(0)).replaceAll("\"", "");
            writer.getMethodVisitor().visitLdcInsn(content);
            writer.getMethodVisitor().visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        } else if (nodeLabel.equals("Variable")){
            Variable variable = variablesMap.get(getNodeContent(node.getChildren().get(1).getChildren().get(0)));
            writer.compileVar(variable);
            if (variable.getType() == INT)
                writer.getMethodVisitor().visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            else
                writer.getMethodVisitor().visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        } else if (nodeLabel.equals("Expression")){
            Type someType = compileExpression(node.getChildren().get(1).getChildren().get(0));
            if (someType == INT) {
                writer.getMethodVisitor().visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            } else {
                throw new CompilationException("вычисление выражения со стрингом");
            }
        }
    }

    private Type compileExpression(Node<Object> node) throws IOException {
        Node<Object> child1 = node.getChildren().get(1);
        Type resType = compileTerm(node.getChildren().get(0));
        if (child1.getEndIndex() == child1.getStartIndex()){
            return resType;
        }
        for (Node<Object> tmp : child1.getChildren()){
            if (tmp == null) continue;
            forEachChildrenExpressionTask(tmp, resType);
        }
        return resType;
    }

    private Type compileIfExpression(Node<Object> node) throws IOException, CompilationException {
        Node<Object> leftExpr = node.getChildren().get(1);
        if (leftExpr.getEndIndex() == leftExpr.getStartIndex()){
            return compileExpression(node.getChildren().get(0));
        }

        Node<Object> rightExpr = leftExpr.getChildren().get(0);
        String operation = getNodeContent(rightExpr.getChildren().get(0));       //TODO может как-то по-другому

        Node<Object> leftOperand = node.getChildren().get(0);
        Node<Object> rightOperand = rightExpr.getChildren().get(1);

        Type leftType = compileExpression(leftOperand);
        Type rightType = compileExpression(rightOperand);

        if (leftType != rightType){
            throw new CompilationException("if expression has different types");
        }

        if (operation.equals(" < ")){
            compileComparableOperands(Opcodes.IF_ICMPLT);
        } else if(operation.equals(" > ")){
            compileComparableOperands(Opcodes.IF_ICMPGT);
        } else {
            throw new CompilationException("только < >");
        }

        return Type.BOOL;
    }

    private void compileComparableOperands(int opcode){
        Label trueLabel = new Label();
        Label falseLabel = new Label();
        Label finalLabel = new Label();
        writer.getMethodVisitor().visitJumpInsn(opcode, trueLabel);
        writer.getMethodVisitor().visitJumpInsn(Opcodes.GOTO, falseLabel);
        writer.getMethodVisitor().visitLabel(trueLabel);
        writer.getMethodVisitor().visitIntInsn(Opcodes.BIPUSH, 1);
        writer.getMethodVisitor().visitJumpInsn(Opcodes.GOTO, finalLabel);
        writer.getMethodVisitor().visitLabel(falseLabel);
        writer.getMethodVisitor().visitIntInsn(Opcodes.BIPUSH, 0);
        writer.getMethodVisitor().visitLabel(finalLabel);
    }

    private void forEachChildrenTermTask(Node<Object> currentChildren, Type resType) throws IOException {
        Type currentType = compileFactor(currentChildren.getChildren().get(1));
        if (resType != currentType){
            return;
        }
        Node<Object> tmp = currentChildren.getChildren().get(0);
        String action = getNodeContent(tmp);
        if (action.equals(" / ")){
            writer.writeDiv(currentType);
        } else if (action.equals(" * ")){
            writer.writeMul(currentType);
        }
    }

    private void forEachChildrenExpressionTask(Node<Object> currentChildren, Type resType) throws IOException {
        Type currentType = compileTerm(currentChildren.getChildren().get(1));

        if (resType != currentType){
            return;
        }
        Node<Object> tmp = currentChildren.getChildren().get(0);
        String action = getNodeContent(tmp);
        if (action.equals(" + ")){
            writer.writeAdd(currentType);
        } else if (action.equals(" - ")){
            writer.writeSub(currentType);
        }
    }

    private Type compileFactor(Node<Object> node) throws IOException {
        Node<Object> child = node.getChildren().get(0);
        Type resType = compileAtom(node.getChildren().get(1));
        if (!isThere(child)){
            return resType;
        }
        writer.getMethodVisitor().visitInsn(Opcodes.INEG);
        return resType;
    }

    private Type compileAtom(Node<Object> node) throws IOException {
        Node<Object> child = node.getChildren().get(0);
        if (child == null){
            return Type.BOOL;
        }
        if (child.getLabel().equals("Sequence")){
            return compileExpression(child.getChildren().get(1));
        } else if (child.getLabel().equals("Variable")){
            return writer.compileVar(variablesMap.get(getNodeContent(child)));
        } else if (child.getLabel().equals("Number")){
            return writer.writeNumber(Integer.valueOf(getNodeContent(child)));
        }
        return null;
    }

    private void compileAssignment(Node<Object> node) throws IOException, CompilationException {
        Node<Object> varKeyword = node.getChildren().get(0);
        Node<Object> variable = node.getChildren().get(1);
        Node<Object> value = node.getChildren().get(3);
        Type exprType;

        if (value.getChildren().get(0).getLabel().equals("Expression"))
            exprType = compileExpression(value.getChildren().get(0));
        else if (value.getChildren().get(0).getLabel().equals("String")){            //TODO compil string

            exprType = Type.STRING;
        } else
            throw new CompilationException("compileAssignment failed");

        String variableName = getNodeContent(variable);

        Variable varToAssign = isThere(varKeyword) ? addVariable(exprType, variableName) : variablesMap.get(variableName);

        if ((varToAssign != null ? varToAssign.getType() : null) != exprType){
            System.out.println("Sorry, variable has incorrect type");
            return;
        }

        switch (exprType){
            case INT:
                writer.getMethodVisitor().visitVarInsn(Opcodes.ISTORE, varToAssign.getIndex());
                break;
            case STRING:
                writer.getMethodVisitor().visitVarInsn(Opcodes.ASTORE, varToAssign.getIndex());
                break;
        }
    }

    private Type compileTerm(Node<Object> node) throws IOException {
        Node<Object> child = node.getChildren().get(1);
        Type resType = compileFactor(node.getChildren().get(0));
        if (!isThere(child)){
            return resType;
        }
        for (Node<Object> tmp : child.getChildren()){
            forEachChildrenTermTask(tmp, resType);
        }
        return resType;
    }

    private Variable addVariable(Type type, String name){
        int allVariablesCount = variablesMap.size();
        Collection<String> existVariables = variablesMap.keySet();
        if (!existVariables.contains(name)){
            Variable tmp = new Variable(type, name, allVariablesCount + 1);
            variablesMap.put(name, tmp);
            return tmp;

        }
        return null;
    }

    private String getNodeContent(Node<Object> node) {
        return parser.getFullProgramCode().substring(node.getStartIndex(), node.getEndIndex());
    }

    private boolean isThere(Node<Object> node){
        return node.getEndIndex() != node.getStartIndex();
    }


}
