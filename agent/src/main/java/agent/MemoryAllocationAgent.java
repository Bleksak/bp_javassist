package agent;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

public class MemoryAllocationAgent {
    private static Instrumentation instr;

    public static void premain(String args, Instrumentation inst) {
        instr = inst;
        ClassPool cp = ClassPool.getDefault();

        HashMap<String, Class<?>> injectedClasses = new HashMap<>();
        injectedClasses.put("inject.AllocationDetector", inject.AllocationDetector.class);
        injectedClasses.put("inject.AllocationCounter", inject.AllocationCounter.class);
        injectedClasses.put("inject.ByteConverter", inject.ByteConverter.class);

        try {
            for(Map.Entry<String, Class<?>> clazz : injectedClasses.entrySet()) {
                CtClass cc = cp.getOrNull(clazz.getKey());
                instr.redefineClasses(new ClassDefinition(clazz.getValue(), cc.toBytecode()));
                cc.detach();
            }
            
        } catch (ClassNotFoundException | UnmodifiableClassException | IOException | CannotCompileException e) {
            e.printStackTrace();
        }

        instr.addTransformer(new Transformer());
    }

    public static long getObjectSize(Object obj) {
        return instr.getObjectSize(obj);
    }
}
