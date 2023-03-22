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
import java.util.Deque;
import java.util.List;

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

    private static final List<String> prefixFilter = List.of("java.", "sun.", "jdk.", "inject.", "org.apache.logging.log4j", "agent.");
    private static final String configurationFile = "config.txt";

    private static final String[] arrayRegisterMethods = {"registerPrimitiveArray", "registerObjectArray", "registerMultiArray"};
    private static final String[] arrayRegisterMethodSignatures = {"(Ljava/lang/Object;)V", "([Ljava/lang/Object;)V", "([Ljava/lang/Object;)V"};
    private static final int[] arrayAllocationBytecode = {Opcode.NEWARRAY, Opcode.ANEWARRAY, Opcode.MULTIANEWARRAY};
    private static final int[] bytecodeEffectiveSize = {2, 3, 4};

    static {
        try (BufferedReader br = new BufferedReader(new FileReader(configurationFile))) {
            String line;

            while ((line = br.readLine()) != null) {
                if(!line.isEmpty()) {
                    prefixFilter.add(line);
                }
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

            String mainClass = Utils.getMainClass(protectionDomain);
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
        String className = Utils.getClassInfo(iterator, pos, constPool);
        stack.push(className);

        // invokespecial should always be present, otherwise the object is unitialized and unusable
        // if invokespecial is not present, this method will cause undefined behavior

        // create space for the dup opcode
        Gap dupOpcodePos = iterator.insertGapAt(pos + 3, 1, true);
        // write the dup opcode
        iterator.writeByte(Opcode.DUP, dupOpcodePos.position);
        codeAttribute.setMaxStack(codeAttribute.getMaxStack() + 1);
        codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);
    }

    private void newInsertInvoke(CodeIterator iterator, int pos, ConstPool constPool, CodeAttribute codeAttribute, Deque<String> stack) throws BadBytecode {
        String top = stack.peek();

        String className = Utils.getClassName(iterator, pos, constPool);
        String methodName = Utils.getMethodName(iterator, pos, constPool);

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

    private void opcodeArrayAllocation(CodeIterator iterator, int pos, ConstPool constPool, CodeAttribute codeAttribute, int typeIndex) throws BadBytecode {
        int classInfo = constPool.addClassInfo("inject.AllocationDetector");

        String registerMethodName = arrayRegisterMethods[typeIndex];
        String registerMethodSignature = arrayRegisterMethodSignatures[typeIndex];
        
        int registerMethodConstPoolIndex = constPool.addMethodrefInfo(classInfo, registerMethodName, registerMethodSignature);
        int effectiveSize = bytecodeEffectiveSize[typeIndex];

        Gap dupOpcodePos = iterator.insertGapAt(pos + effectiveSize, 4, true);
        iterator.writeByte(Opcode.DUP, dupOpcodePos.position);
        iterator.writeByte(Opcode.INVOKESTATIC, dupOpcodePos.position + 1);
        iterator.write16bit(registerMethodConstPoolIndex, dupOpcodePos.position + 2);
        codeAttribute.setMaxStack(codeAttribute.getMaxStack() + 1);
        codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);
    }

    private void findNewKeyword(CtBehavior method) throws BadBytecode {
        MethodInfo methodInfo = method.getMethodInfo();
        ConstPool constPool = methodInfo.getConstPool();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        CodeIterator iterator = codeAttribute.iterator();

        Deque<String> newKeywordStack = new ArrayDeque<>();

        while(iterator.hasNext()) {
            int pos = iterator.next();
            int op = iterator.byteAt(pos);

            for(int i = 0; i < arrayAllocationBytecode.length; ++i) {
                if(arrayAllocationBytecode[i] == op) {
                    opcodeArrayAllocation(iterator, pos, constPool, codeAttribute, i);
                    break;
                }
            }

            if(op == Opcode.NEW) {
                opcodeNew(iterator, pos, constPool, codeAttribute, newKeywordStack);
            }

            if(op == Opcode.INVOKESPECIAL) {
                if(!newKeywordStack.isEmpty()) {
                    // pop from stack if match and call register
                    newInsertInvoke(iterator, pos, constPool, codeAttribute, newKeywordStack);
                }
            }
        }
    }
}
