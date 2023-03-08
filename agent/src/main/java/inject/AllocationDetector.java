package inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.LogManager;

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

    private final static HashMap<String, List<Object>> objectMap = new HashMap<>();
    private final static DuplicateFinder finder = new DuplicateFinder(objectMap, equals);

    private final static Logger logger = LogManager.getRootLogger();

    public static void configure() {
        Configurator.initialize(null, "log4j2.xml");
    }

    public static void findDuplicates() {
        finder.findDuplicates();
    }

    public static void registerObject(Object obj) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];

        String allocatedAt = trace.getFileName() + ":" + trace.getLineNumber();

        logger.trace("Object allocated at: " + allocatedAt);
        logger.trace("Object size: " + MemoryAllocationAgent.getObjectSize(obj));
        logger.trace("Object type: " + obj.getClass().getSimpleName());

        AllocationCounter.addCounts(trace, MemoryAllocationAgent.getObjectSize(obj));

        String className = obj.getClass().getName();
        if(!objectMap.containsKey(className)) {
            objectMap.put(className, new ArrayList<>());
        }

        List<Object> list = objectMap.get(className);
        list.add(obj);
    }

    public static void registerPrimitiveArray(Object array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        long objectSize = MemoryAllocationAgent.getObjectSize(array);
        String allocatedAt = trace.getFileName() + ":" + trace.getLineNumber();

        logger.trace("Primitive array allocated at: " + allocatedAt);
        logger.trace("Array size: " + objectSize);
        logger.trace("Array type: " + array.getClass().getSimpleName());

        int index = primitiveArrayIndex.get(array.getClass().getName());

        @SuppressWarnings("unchecked")
        List<Object> bucket = (List<Object>) primitiveArrays.get(index);
        bucket.add(array);

        AllocationCounter.addCounts(trace, objectSize);
    }

    public static void registerObjectArray(Object[] array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        long objectSize = MemoryAllocationAgent.getObjectSize(array);
        String allocatedAt = trace.getFileName() + ":" + trace.getLineNumber();

        logger.trace("Object array allocated at: " + allocatedAt);
        logger.trace("Array size: " + objectSize);
        logger.trace("Array type: " + array.getClass().getSimpleName());

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
        String allocatedAt = trace.getFileName() + ":" + trace.getLineNumber();

        logger.trace("Multi array allocated at: " + allocatedAt);
        logger.trace("Array size: " + objectSize);
        logger.trace("Array type: " + array.getClass().getSimpleName());

        AllocationCounter.addCounts(trace, objectSize);
    }
}
