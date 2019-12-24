package ru.nsu.fit.snegireva.compiler.writer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.apache.commons.io.FileUtils;
import ru.nsu.fit.snegireva.compiler.parser.Type;
import ru.nsu.fit.snegireva.compiler.parser.Variable;

import java.io.File;
import java.io.IOException;

import static ru.nsu.fit.snegireva.compiler.parser.Type.INT;

public class ByteCodeWriter {
    private ClassWriter classWriter;
    private MethodVisitor methodVisitor;

    public ByteCodeWriter(){
        this.classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        this.methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
    }

    public MethodVisitor getMethodVisitor() {
        return methodVisitor;
    }

    public void write(String classFileName) throws IOException {
        classWriter.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "Romashka", null, "java/lang/Object", null);

        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();

        MethodVisitor mVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mVisitor.visitCode();
        mVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        mVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mVisitor.visitInsn(Opcodes.RETURN);
        mVisitor.visitMaxs(0, 0);
        mVisitor.visitEnd();

        FileUtils.writeByteArrayToFile(new File(classFileName), classWriter.toByteArray());
    }

    public Type writeNumber(Integer integer) {
        methodVisitor.visitIntInsn(Opcodes.SIPUSH, integer);
        return INT;
    }

    public Type compileVar(Variable variable) {
        switch (variable.getType()){
            case STRING:
                methodVisitor.visitVarInsn(Opcodes.ALOAD, variable.getIndex());
                break;
            case INT:
                methodVisitor.visitVarInsn(Opcodes.ILOAD, variable.getIndex());
                break;
        }
        return variable.getType();
    }

    public void writeAdd(Type type){
        if(type != INT){
            return;
        }
        methodVisitor.visitInsn(Opcodes.IADD);
    }

    public void writeSub(Type type){
        if(type != INT){
            return;
        }
        methodVisitor.visitInsn(Opcodes.ISUB);
    }

    public void writeDiv(Type type){
        if (type != INT){
            return;
        }
        methodVisitor.visitInsn(Opcodes.IDIV);
    }

    public void writeMul(Type type){
        if (type != INT){
            return;
        }
        methodVisitor.visitInsn(Opcodes.IMUL);
    }

}
