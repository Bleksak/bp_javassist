package agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
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
        String[] filter = new String[] {
                "sun.launcher.LauncherHelper",
                "java.lang.WeakPairMap$Pair$Weak",
                "java.lang.WeakPairMap$WeakRefPeer",
                "java.lang.WeakPairMap$Pair$Weak$1",
                "java.nio.charset.CharsetDecoder",
                "sun.nio.cs.SingleByte$Decoder",
                "sun.nio.cs.ArrayDecoder",
                "sun.nio.cs.MS1252$Holder",
                "java.util.jar.JarVerifier",
                "java.security.CodeSigner",
                "java.io.RandomAccessFile$1",
                "java.util.IdentityHashMap$IdentityHashMapIterator",
                "java.util.IdentityHashMap$KeyIterator",
                "java.lang.Shutdown",
                "java.lang.Shutdown$Lock",
                "inject.AllocationDetector",
        };

        for (String s : filter) {
            if (s.equals(className)) {
                return null;
            }
        }

        if(className.startsWith("java.") || className.startsWith("sun.") || className.startsWith("jdk.")) {
            return null;
        }

        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));

            CtMethod[] staticMethods = cc.getDeclaredMethods();

            for(CtMethod method : staticMethods) {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return classfileBuffer;
    }

    private void opcodeNew(CodeIterator iterator, ConstPool constPool, CodeAttribute codeAttribute) throws BadBytecode {
        // int index = iterator.u16bitAt(pos + 1);
        // String cname = constPool.getClassInfo(index);
        // System.out.println("new " + cname + "();");

        int classInfo = constPool.addClassInfo("inject.AllocationDetector");
        int registerMethodIndex = constPool.addMethodrefInfo(classInfo, "registerObject", "(Ljava/lang/Object;)V");
                
        // now we start searching for invokespecial (3 bytes)
        // because constructor can take many arguments
        // invokespecial should always be present, otherwise the object is unitialized

        int currentOpcode;
        boolean found = false;
        int pos;

        do {
            pos = iterator.next();
            currentOpcode = iterator.byteAt(pos);
            if(currentOpcode == Opcode.INVOKESPECIAL) {
                found = true;
            }
        } while(currentOpcode != Opcode.INVOKESPECIAL && iterator.hasNext());

        if(found) {
                    
            Gap dupOpcodePos = iterator.insertGapAt(pos + 3, 4, true);
            iterator.writeByte(Opcode.DUP, dupOpcodePos.position);
            iterator.writeByte(Opcode.INVOKESTATIC, dupOpcodePos.position + 1);
            iterator.write16bit(registerMethodIndex, dupOpcodePos.position + 2);
            codeAttribute.setMaxStack(codeAttribute.getMaxStack() + 1);
            codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);


        } else {
            System.out.println("cannot find");
        }

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

    private void findNewKeyword(CtMethod method) throws BadBytecode {

        MethodInfo methodInfo = method.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        CodeIterator iterator = codeAttribute.iterator();
        ConstPool constPool = method.getMethodInfo().getConstPool();

        while(iterator.hasNext()) {
            int pos = iterator.next();
            int op = iterator.byteAt(pos);

            if(op == Opcode.NEW) {
                opcodeNew(iterator, constPool, codeAttribute);
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
        // printer.print(method);
    }
}
