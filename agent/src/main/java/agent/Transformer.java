package agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.Modifier;

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
                return classfileBuffer;
            }
        }

        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));

            if (!cc.isInterface() && !Modifier.isAbstract(cc.getModifiers())) {
                CtConstructor[] constructors = cc.getConstructors();
                for (CtConstructor constructor : constructors) {
                    constructor.insertBeforeBody(
                            "try {"
                                    + "inject.AllocationDetector.getInstance().register(this);"
                                    + "} catch (java.lang.NoClassDefFoundError e) {"
                                    + "}");
                }

                byte[] code = cc.toBytecode();
                cc.detach();
                return code;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }
}
