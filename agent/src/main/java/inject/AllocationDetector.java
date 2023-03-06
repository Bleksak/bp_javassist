package inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agent.MemoryAllocationAgent;

public class AllocationDetector {
    private final static IDuplicateEquals equals = new DuplicateDeepEquals();
    private final static ArrayList<Object> primitiveArrays = new ArrayList<>();
    private final static HashMap<String, Integer> primitiveArrayIndex = new HashMap<>();

    static {
        primitiveArrays.add(new ArrayList<boolean[]>());
        primitiveArrays.add(new ArrayList<byte[]>());
        primitiveArrays.add(new ArrayList<char[]>());
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

    private final static List<String> strings = new ArrayList<>();
    private final static HashMap<String, List<Object>> objectMap = new HashMap<>();

    private final static DuplicateFinder finder = new DuplicateFinder(objectMap, equals);

    public static void registerObject(Object obj) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        System.out.println("Object allocated at: " + trace.getFileName() + ":" + trace.getLineNumber());
        System.out.println("Object size: " + MemoryAllocationAgent.getObjectSize(obj));
        System.out.println("Object type: " + obj.getClass().getSimpleName());

        AllocationCounter.addCounts(trace, MemoryAllocationAgent.getObjectSize(obj));

        if(obj instanceof String str) {
            strings.add(str);
        } else {
            String className = obj.getClass().getName();
            if(!objectMap.containsKey(className)) {
                objectMap.put(className, new ArrayList<>());
            }

            synchronized(objectMap) {
                List<Object> list = objectMap.get(className);
                list.add(obj);
            }

            if(!finder.isAlive()) {
                finder.start();
            }
        }
    }

    public static void registerPrimitiveArray(Object array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        long objectSize = MemoryAllocationAgent.getObjectSize(array);

        System.out.println("Primitive array allocated at: " + trace.getFileName() + ":" + trace.getLineNumber());
        System.out.println("Array size: " + objectSize);
        System.out.println("Array type: " + array.getClass().getSimpleName());

        int index = primitiveArrayIndex.get(array.getClass().getName());

        @SuppressWarnings("unchecked")
        List<Object> bucket = (List<Object>) primitiveArrays.get(index);
        bucket.add(array);

        AllocationCounter.addCounts(trace, objectSize);
    }

    public static void registerObjectArray(Object[] array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        long objectSize = MemoryAllocationAgent.getObjectSize(array);

        System.out.println("Object array allocated at: " + trace.getFileName() + ":" + trace.getLineNumber());
        System.out.println("Array size: " + objectSize);
        System.out.println("Array type: " + array.getClass().getSimpleName());

        AllocationCounter.addCounts(trace, objectSize);
    }

    private static long getMultidimensionalArraySize(Object array) {
        long objectSize = MemoryAllocationAgent.getObjectSize(array);
        int length = java.lang.reflect.Array.getLength(array);

        for (int i = 0; i < length; i++) {
            Object element = java.lang.reflect.Array.get(array, i);
            if (element != null && element.getClass().isArray()) {
                objectSize += getMultidimensionalArraySize(element);
            }
        }

        return objectSize;
    }

    public static void registerMultiArray(Object[] array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        long objectSize = getMultidimensionalArraySize(array);

        System.out.println("Multi array allocated at: " + trace.getFileName() + ":" + trace.getLineNumber());
        System.out.println("Array size: " + objectSize);
        System.out.println("Array type: " + array.getClass().getSimpleName());

        AllocationCounter.addCounts(trace, objectSize);
    }
}
