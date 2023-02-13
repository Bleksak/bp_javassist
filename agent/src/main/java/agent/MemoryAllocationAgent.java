package agent;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

public class MemoryAllocationAgent {
    private static Instrumentation instr;
    private static ClassFileTransformer transformer = new Transformer();

    public static void premain(String args, Instrumentation inst) {
        instr = inst;
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.getOrNull("inject.AllocationDetector");
        CtClass cs = cp.getOrNull("java.lang.String");

        try {
            instr.redefineClasses(new ClassDefinition(inject.AllocationDetector.class, cc.toBytecode()));
            for(CtConstructor constructor : cs.getConstructors()) {
                // constructor.insertBeforeBody(
                    // "inject.AllocationDetector detector = inject.AllocationDetector.getInstance();" +
                    // "System.out.println(detector);"
                    // "inject.AllocationDetector.getInstance();"
                // );
            }
            instr.redefineClasses(new ClassDefinition(java.lang.String.class, cs.toBytecode()));
        } catch (ClassNotFoundException | UnmodifiableClassException | IOException | CannotCompileException e) {
            e.printStackTrace();
        }

        instr.addTransformer(transformer);
        cc.detach();
    }

    public static void agentmain(String args, Instrumentation inst) {
    }

    public static long getObjectSize(Object obj) {
        return instr.getObjectSize(obj);
    }
}
