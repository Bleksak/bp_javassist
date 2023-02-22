package inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AllocationDetector {
    private final static ArrayList<Object> primitiveArrays = new ArrayList<>();
    private final static HashMap<String, Integer> primitiveArrayIndex = new HashMap<>();

    static {
        primitiveArrays.add(new ArrayList<boolean[]>());
        primitiveArrays.add(new ArrayList<byte[]>());
        primitiveArrays.add(new ArrayList<short[]>());
        primitiveArrays.add(new ArrayList<int[]>());
        primitiveArrays.add(new ArrayList<long[]>());
        primitiveArrays.add(new ArrayList<float[]>());
        primitiveArrays.add(new ArrayList<double[]>());
        
        primitiveArrayIndex.put("[Z", 0);
        primitiveArrayIndex.put("[B", 1);
        primitiveArrayIndex.put("[C", 2);
        primitiveArrayIndex.put("[S", 3);
        primitiveArrayIndex.put("[I", 4);
        primitiveArrayIndex.put("[J", 5);
        primitiveArrayIndex.put("[F", 6);
        primitiveArrayIndex.put("[D", 7);
    };

    // private final List<WeakReference<Object>> objects = new ArrayList<>();
    private final static List<String> strings = new ArrayList<>();

    public static byte[] objectToByteArray(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static void registerObject(Object obj) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        System.out.println("Object allocated at: " + trace.getFileName() + ":" + trace.getLineNumber());
        System.out.println("Object size: " + objectToByteArray(obj).length);
        System.out.println("Object type: " + obj.getClass().getSimpleName());

        if(obj instanceof String str) {
            strings.add(str);
        }
    }

    public static void registerPrimitiveArray(Object array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        System.out.println("Primitive array allocated at: " + trace.getFileName() + ":" + trace.getLineNumber());
        System.out.println("Array size: " + objectToByteArray(array).length);
        System.out.println("Array type: " + array.getClass().getSimpleName());
        int index = primitiveArrayIndex.get(array.getClass().getName());

        @SuppressWarnings("unchecked")
        List<Object> bucket = (List<Object>) primitiveArrays.get(index);
        bucket.add(array);
    }

    public static void registerObjectArray(Object[] array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        System.out.println("Object array allocated at: " + trace.getFileName() + ":" + trace.getLineNumber());
        System.out.println("Array size: " + objectToByteArray(array).length);
        System.out.println("Array type: " + array.getClass().getSimpleName());
    }

    public static void registerMultiArray(Object[] array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        System.out.println("Multi array allocated at: " + trace.getFileName() + ":" + trace.getLineNumber());
        System.out.println("Array size: " + objectToByteArray(array).length);
        System.out.println("Array type: " + array.getClass().getSimpleName());
    }
}
