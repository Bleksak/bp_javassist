package agent;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.bytecode.CodeIterator.Gap;

/**
 * MemoryAllocationDetectionTransformer transforms the bytecode of all NEW, NEWARRAY, ANEWARRAY and MULTIANEWARRAY instructions
 * and calls an object registration method after allocating and initializing the object.
 */
public class MemoryAllocationDetectionTransformer implements ClassFileTransformer {

    private static final List<String> prefixFilter = List.of("java.", "sun.", "jdk.", "inject.", "org.apache.logging.log4j");
    private static final String configurationFile = "config.txt";

    static {
        try (BufferedReader br = new BufferedReader(new FileReader(configurationFile))) {
            String line;

            while ((line = br.readLine()) != null) {
                prefixFilter.add(line);
            }
        } catch (IOException e) {
            // if the file does not exist, don't do anything
        }
    }

    private boolean filter(String className) {
        for (String prefix : prefixFilter) {
            if (className.startsWith(prefix)) {
                return false;
            }
        }

        return true;
    }

    private String getMainClass(ProtectionDomain protectionDomain) throws IOException, URISyntaxException {
        String jarFile = protectionDomain.getCodeSource().getLocation().toURI().getPath();
        JarFile jar = new JarFile(jarFile);
        String mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
        jar.close();
        return mainClass;
    }

    /**
     * Transforms the bytes of the loaded class
     * This method is called by the Java Agent
     */
    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer)
            throws IllegalClassFormatException {
        className = className.replace("/", ".");

        if(!filter(className)) {
            return null;
        }

        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            CtMethod[] methods = cc.getDeclaredMethods();
            CtConstructor[] constructors = cc.getDeclaredConstructors();

            String mainClass = getMainClass(protectionDomain);

            for(CtMethod method : methods) {
                if(className.equals(mainClass) && method.getName().equals("main")) {
                    updateMain(method);
                }

                findNewKeyword(method);
            }

            for(CtConstructor method : constructors) {
                findNewKeyword(method);
            }

            byte[] code = cc.toBytecode();
            cc.detach();
            return code;
        }

        catch (IOException e) {}
        catch (NoClassDefFoundError e) {}
        catch (BadBytecode e) {}
        catch (CannotCompileException e) {} 
        catch (URISyntaxException e) {}

        return classfileBuffer;
    }

    private void opcodeNew(CodeIterator iterator, int pos, ConstPool constPool, CodeAttribute codeAttribute, Deque<String> stack) throws BadBytecode {
        // find class name of the allocated object
        int indexbyte1 = iterator.byteAt(pos + 1);
        int indexbyte2 = iterator.byteAt(pos + 2);

        int constPoolIndex = indexbyte1 << 8 | indexbyte2;
        String className = constPool.getClassInfo(constPoolIndex);

        stack.push(className);
        // invokespecial should always be present, otherwise the object is unitialized and unusable

        // create space for the dup opcode
        Gap dupOpcodePos = iterator.insertGapAt(pos + 3, 1, true);
        iterator.writeByte(Opcode.DUP, dupOpcodePos.position);
        // write the dup opcode
        // the new bytecode looks like: "new", "dup", ....
        codeAttribute.setMaxStack(codeAttribute.getMaxStack() + 1);
        codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);
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
        iterator.writeByte(Opcode.INVOKESTATIC, dupOpcodePos.position);
        iterator.write16bit(registerMethodIndex, dupOpcodePos.position + 1);
    }

    private void updateMain(CtMethod main) throws CannotCompileException {
        ConstPool constPool = main.getMethodInfo().getConstPool();
        constPool.addClassInfo("inject.AllocationDetector");
        constPool.addClassInfo("inject.AllocationCounter");

        // main.insertBefore("inject.AllocationDetector.configure();");
        main.insertAfter("inject.AllocationCounter.logInfo(); inject.AllocationDetector.findDuplicates();");
    }

    private void findNewKeyword(CtBehavior method) throws BadBytecode {

        MethodInfo methodInfo = method.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        CodeIterator iterator = codeAttribute.iterator();
        ConstPool constPool = method.getMethodInfo().getConstPool();

        Deque<String> newKeywordStack = new ArrayDeque<>();

        while(iterator.hasNext()) {
            int pos = iterator.next();
            int op = iterator.byteAt(pos);

            if(op == Opcode.NEW) {
                opcodeNew(iterator, pos, constPool, codeAttribute, newKeywordStack);
            }

            if(op == Opcode.INVOKESPECIAL) {
                if(!newKeywordStack.isEmpty()) {
                    // pop from stack if match and call register
                    newInsertInvoke(iterator, pos, constPool, codeAttribute, newKeywordStack);
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
    }
}
