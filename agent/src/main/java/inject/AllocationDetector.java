package inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import agent.MemoryAllocationAgent;

/**
 * This class holds the collection of all allocated objects.
 * It is responsible for adding objects to the collection.
 */
public class AllocationDetector {
    private final static Map<String, List<ObjectWithTrace>> objectMap = new HashMap<>();

    private final static IDuplicateEquals equals = new DuplicateDeepEquals();
    private final static DuplicateFinder finder = new DuplicateFinder(objectMap, equals);

    private final static Logger logger = LogManager.getRootLogger();

    /**
     * configures the logger
     */
    // public static void configure() {
    //     Configurator.initialize(null, "log4j2.xml");
    // }

    /**
     * runs the algorithm to find duplicate objects
     */
    public static void findDuplicates() {
        finder.findDuplicates();
    }

    private static void addObject(Object obj, StackTraceElement stackTrace) {
        String className = obj.getClass().getName();
        if(!objectMap.containsKey(className)) {
            objectMap.put(className, new ArrayList<>());
        }

        List<ObjectWithTrace> list = objectMap.get(className);
        list.add(new ObjectWithTrace(obj, stackTrace));
    }

    /**
     * adds a regular instance of a class to a collection of allocated objects
     * @param obj the newly allocated object
     */
    public static void registerObject(Object obj) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        String allocatedAt = trace.getFileName() + ":" + trace.getLineNumber();

        logger.trace("Object allocated at: " + allocatedAt);
        logger.trace("Object size: " + MemoryAllocationAgent.getObjectSize(obj));
        logger.trace("Object type: " + obj.getClass().getSimpleName());

        AllocationCounter.addCounts(trace, MemoryAllocationAgent.getObjectSize(obj));
        addObject(obj, trace);
    }

    /**
     * adds an array with elements of primitive data type to a collection of allocated objects
     * @param array
     */
    public static void registerPrimitiveArray(Object array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        long objectSize = MemoryAllocationAgent.getObjectSize(array);
        String allocatedAt = trace.getFileName() + ":" + trace.getLineNumber();

        logger.trace("Primitive array allocated at: " + allocatedAt);
        logger.trace("Array size: " + objectSize);
        logger.trace("Array type: " + array.getClass().getSimpleName());

        AllocationCounter.addCounts(trace, objectSize);
        addObject(array, trace);
    }

    /**
     * adds an array with elements of object data type to a collection of allocated objects
     * @param array
     */
    public static void registerObjectArray(Object[] array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        long objectSize = MemoryAllocationAgent.getObjectSize(array);
        String allocatedAt = trace.getFileName() + ":" + trace.getLineNumber();

        logger.trace("Object array allocated at: " + allocatedAt);
        logger.trace("Array size: " + objectSize);
        logger.trace("Array type: " + array.getClass().getSimpleName());

        AllocationCounter.addCounts(trace, objectSize);
        addObject(array, trace);
    }


    /**
     * adds an multi dimensional array to a collection of allocated objects
     * @param array
     */
    public static void registerMultiArray(Object[] array) {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
        long objectSize = MemoryAllocationAgent.getMultidimensionalArraySize(array);
        String allocatedAt = trace.getFileName() + ":" + trace.getLineNumber();

        logger.trace("Multi array allocated at: " + allocatedAt);
        logger.trace("Array size: " + objectSize);
        logger.trace("Array type: " + array.getClass().getSimpleName());

        AllocationCounter.addCounts(trace, objectSize);
        addObject(array, trace);
    }
}
