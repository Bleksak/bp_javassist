package agent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;

/**
 * Contains utility methods for using the Javassist library
 */
public class Utils {
    /**
     * Return an index to const pool
     * This method expects that <code>iterator.byteAt(pos)</code> is a valid instruction that accepts two indexbyte arguments
     * @param iterator CodeIterator instance of the instrumented method
     * @param pos position of the instrumented instruction
     * @return index to the const pool
     */
    public static int getConstPoolIndex(CodeIterator iterator, int pos) {
        int indexbyte1 = iterator.byteAt(pos + 1);
        int indexbyte2 = iterator.byteAt(pos + 2);

        return (indexbyte1 << 8) | indexbyte2;
    }

    /**
     * Get class name that the current instrumented instruction works with
     * This method expects that <code>iterator.byteAt(pos)</code> is a valid instruction that accepts two indexbyte arguments
     * @param iterator CodeIterator instance of the instrumented method
     * @param pos position of the instrumented instruction
     * @param constPool const pool of the instrumented class
     * @return class name that the current instrumented instruction works with
     */
    public static String getClassInfo(CodeIterator iterator, int pos, ConstPool constPool) {
        return constPool.getClassInfo(getConstPoolIndex(iterator, pos));
    }

    /**
     * Get class name that the current instrumented instruction works with
     * This method expects that <code>iterator.byteAt(pos)</code> is a valid instruction that accepts two indexbyte arguments
     * @param iterator CodeIterator instance of the instrumented method
     * @param pos position of the instrumented instruction
     * @param constPool const pool of the instrumented class
     * @return class name that the current instrumented instruction works with
     */
    public static String getClassName(CodeIterator iterator, int pos, ConstPool constPool) {
        return constPool.getMethodrefClassName(getConstPoolIndex(iterator, pos));
    }

    /**
     * Get method name that the current instrumented instruction works with
     * This method expects that <code>iterator.byteAt(pos)</code> is a valid instruction that accepts two indexbyte arguments
     * @param iterator CodeIterator instance of the instrumented method
     * @param pos position of the instrumented instruction
     * @param constPool const pool of the instrumented class
     * @return method name that the current instrumented instruction works with
     */
    public static String getMethodName(CodeIterator iterator, int pos, ConstPool constPool) {
        return constPool.getMethodrefName(getConstPoolIndex(iterator, pos));
    }

    /**
     * Get the class name that contains the main method
     * @param protectionDomain ProtectionDomain instance of the currently instrumented class
     * @return manifest "Main-Class" entry of the currently instrumented class
     * @throws IOException if JarFile cannot be instantiated
     * @throws URISyntaxException if JAR location is invalid
     */
    public static String getMainClass(ProtectionDomain protectionDomain) throws IOException, URISyntaxException {
        String jarFile = protectionDomain.getCodeSource().getLocation().toURI().getPath();

        try(JarFile jar = new JarFile(jarFile)) {
            return jar.getManifest().getMainAttributes().getValue("Main-Class");
        }
    }
}
