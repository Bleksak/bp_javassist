package agent;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

public class MemoryAllocationAgent {
    private static Instrumentation instr;
    private static ClassFileTransformer transformer = new Transformer();

    public static void premain(String args, Instrumentation inst) {
        instr = inst;
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.getOrNull("inject.AllocationDetector");

        try {
            instr.redefineClasses(new ClassDefinition(inject.AllocationDetector.class, cc.toBytecode()));
        } catch (ClassNotFoundException | UnmodifiableClassException | IOException | CannotCompileException e) {
            e.printStackTrace();
        }

        instr.addTransformer(transformer);
        cc.detach();
    }

    public static long getObjectSize(Object obj) {
        return instr.getObjectSize(obj);
    }
}
