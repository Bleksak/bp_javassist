package agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.jar.JarFile;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.bytecode.CodeIterator.Gap;

public class Transformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer)
            throws IllegalClassFormatException {
        className = className.replace("/", ".");

        String[] filter = new String[] {};

        for (String s : filter) {
            if (s.equals(className)) {
                return null;
            }
        }

        if(className.startsWith("java.") || className.startsWith("sun.") || className.startsWith("jdk.") || className.startsWith("inject.")) {
            return null;
        }

        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));

            CtMethod[] methods = cc.getDeclaredMethods();
            CtConstructor[] ctors = cc.getDeclaredConstructors();

            String jarFile = protectionDomain.getCodeSource().getLocation().toURI().getPath();
            JarFile jar = new JarFile(jarFile);
            String mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
            jar.close();

            for(CtMethod method : methods) {
                if(className.equals(mainClass) && method.getName().equals("main")) {
                    updateMain(method);
                }
                findNewKeyword(method);
            }

            for(CtConstructor method : ctors) {
                findNewKeyword(method);
            }

            byte[] code = cc.toBytecode();
            cc.detach();
            return code;
        } catch (IOException e) {
            e.printStackTrace();
        }
         catch (NoClassDefFoundError e) {
            e.printStackTrace();
        } catch (BadBytecode e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }

    private void opcodeNew(CodeIterator iterator, int pos, ConstPool constPool, CodeAttribute codeAttribute, Deque<String> stack) throws BadBytecode {
        // int index = iterator.u16bitAt(pos + 1);
        // String cname = constPool.getClassInfo(index);
        // System.out.println("new " + cname + "();");

        int classInfo = constPool.addClassInfo("inject.AllocationDetector");
        int registerMethodIndex = constPool.addMethodrefInfo(classInfo, "registerObject", "(Ljava/lang/Object;)V");

        int indexbyte1 = iterator.byteAt(pos + 1);
        int indexbyte2 = iterator.byteAt(pos + 2);

        int constPoolIndex = indexbyte1 << 8 | indexbyte2;

        String className = constPool.getClassInfo(constPoolIndex);

        System.out.println("Found NEW for: " + className);
        stack.push(className);
                
        // now we start searching for invokespecial (3 bytes)
        // because constructor can take many arguments
        // invokespecial should always be present, otherwise the object is unitialized
        // TODO: invokespecial is used to call <init>, private methods, and methods that are final, need to find a way to pair it correctly

        int currentOpcode;
        boolean found = false;
        // int pos;

        // do {
        //     pos = iterator.next();
        //     currentOpcode = iterator.byteAt(pos);
        //     if(currentOpcode == Opcode.INVOKESPECIAL) {
        //         found = true;
        //     }
        // } while(currentOpcode != Opcode.INVOKESPECIAL && iterator.hasNext());

        // if(found) {
        Gap dupOpcodePos = iterator.insertGapAt(pos + 3, 1, true);
        iterator.writeByte(Opcode.DUP, dupOpcodePos.position);
        //     iterator.writeByte(Opcode.INVOKESTATIC, dupOpcodePos.position + 1);
        //     iterator.write16bit(registerMethodIndex, dupOpcodePos.position + 2);
        codeAttribute.setMaxStack(codeAttribute.getMaxStack() + 1);
        codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);
        // } else {
        //     System.out.println("cannot find");
        // }

    }

    private void opcodeNewArray(CodeIterator iterator, int pos, ConstPool constPool, CodeAttribute codeAttribute) throws BadBytecode {
        int classInfo = constPool.addClassInfo("inject.AllocationDetector");
        int registerMethodIndex = constPool.addMethodrefInfo(classInfo, "registerPrimitiveArray", "(Ljava/lang/Object;)V");

        Gap dupOpcodePos = iterator.insertGapAt(pos + 2, 4, true);
        iterator.writeByte(Opcode.DUP, dupOpcodePos.position);
        iterator.writeByte(Opcode.INVOKESTATIC, dupOpcodePos.position + 1);
        iterator.write16bit(registerMethodIndex, dupOpcodePos.position + 2);
        codeAttribute.setMaxStack(codeAttribute.getMaxStack() + 1);
        codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);
    }

    private void opcodeANewArray(CodeIterator iterator, int pos, ConstPool constPool, CodeAttribute codeAttribute) throws BadBytecode {
        int classInfo = constPool.addClassInfo("inject.AllocationDetector");
        int registerMethodIndex = constPool.addMethodrefInfo(classInfo, "registerObjectArray", "([Ljava/lang/Object;)V");

        Gap dupOpcodePos = iterator.insertGapAt(pos + 3, 4, true);
        iterator.writeByte(Opcode.DUP, dupOpcodePos.position);
        iterator.writeByte(Opcode.INVOKESTATIC, dupOpcodePos.position + 1);
        iterator.write16bit(registerMethodIndex, dupOpcodePos.position + 2);
        codeAttribute.setMaxStack(codeAttribute.getMaxStack() + 1);
        codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);
    }

    private void opcodeMultiANewArray(CodeIterator iterator, int pos, ConstPool constPool, CodeAttribute codeAttribute) throws BadBytecode {
        int classInfo = constPool.addClassInfo("inject.AllocationDetector");
        int registerMethodIndex = constPool.addMethodrefInfo(classInfo, "registerMultiArray", "([Ljava/lang/Object;)V");

        // int dimensions = iterator.byteAt(pos + 3);
        // System.out.println(dimensions);

        Gap dupOpcodePos = iterator.insertGapAt(pos + 4, 4, true);
        iterator.writeByte(Opcode.DUP, dupOpcodePos.position);
        iterator.writeByte(Opcode.INVOKESTATIC, dupOpcodePos.position + 1);
        iterator.write16bit(registerMethodIndex, dupOpcodePos.position + 2);
        codeAttribute.setMaxStack(codeAttribute.getMaxStack() + 1);
        codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);
    }

    private void newInsertInvoke(CodeIterator iterator, int pos, ConstPool constPool, CodeAttribute codeAttribute, Deque<String> stack) throws BadBytecode {
        String top = stack.peek();
        int indexbyte1 = iterator.byteAt(pos + 1);
        int indexbyte2 = iterator.byteAt(pos + 2);

        int constPoolIndex = indexbyte1 << 8 | indexbyte2;
        String className = constPool.getMethodrefClassName(constPoolIndex);
        String methodName = constPool.getMethodrefName(constPoolIndex);

        if(!methodName.contains("<init>")) return;
        if(!className.equals(top)) return;
        stack.pop();

        int classInfo = constPool.addClassInfo("inject.AllocationDetector");
        int registerMethodIndex = constPool.addMethodrefInfo(classInfo, "registerObject", "(Ljava/lang/Object;)V");

        Gap dupOpcodePos = iterator.insertGapAt(pos + 3, 3, true);
        // iterator.writeByte(Opcode.DUP, dupOpcodePos.position);
        iterator.writeByte(Opcode.INVOKESTATIC, dupOpcodePos.position);
        iterator.write16bit(registerMethodIndex, dupOpcodePos.position + 1);
        // codeAttribute.setMaxStack(codeAttribute.getMaxStack() + 1);
        // codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);
    }

    private void updateMain(CtMethod main) throws CannotCompileException {
        ConstPool constPool = main.getMethodInfo().getConstPool();
        constPool.addClassInfo("inject.AllocationCounter");
        main.insertAfter("inject.AllocationCounter.printInfo();");
    }

    private void findNewKeyword(CtBehavior method) throws BadBytecode {

        MethodInfo methodInfo = method.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        CodeIterator iterator = codeAttribute.iterator();
        ConstPool constPool = method.getMethodInfo().getConstPool();

        Deque<String> stack = new ArrayDeque<>();

        while(iterator.hasNext()) {
            int pos = iterator.next();
            int op = iterator.byteAt(pos);

            if(op == Opcode.NEW) {
                opcodeNew(iterator, pos, constPool, codeAttribute, stack);
            }

            if(op == Opcode.INVOKESPECIAL) {
                if(!stack.isEmpty()) {
                    // pop from stack if match and call register
                    newInsertInvoke(iterator, pos, constPool, codeAttribute, stack);
                }
            }

            if(op == Opcode.NEWARRAY) {
                opcodeNewArray(iterator, pos, constPool, codeAttribute);
            }

            if(op == Opcode.ANEWARRAY) {
                opcodeANewArray(iterator, pos, constPool, codeAttribute);
            }

            if(op == Opcode.MULTIANEWARRAY) {
                opcodeMultiANewArray(iterator, pos, constPool, codeAttribute);
            }
        }

        // InstructionPrinter printer = new InstructionPrinter(System.out);
        // printer.print((CtMethod)method);
    }
}
