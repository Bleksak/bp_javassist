package inject;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AllocationCounter {
    /**
     * key = filename:line_number
     */
    private static HashMap<String, Long> lineCounter = new HashMap<>();

    /**
     * key = filename:classname:methodname
     */
    private static HashMap<String, Long> methodCounter = new HashMap<>();

    /**
     * key = filename:classname
     */
    private static HashMap<String, Long> classCounter = new HashMap<>();

    private final static Logger logger = LogManager.getRootLogger();

    /**
     * adds "size" to line, method and class counters
     * 
     * @param stackTrace the stack trace, where the allocation occured
     * @param size allocated size in bytes
     */
    public static void addCounts(StackTraceElement stackTrace, long size) {
        String lineCounterString = String.format("%s:%s", stackTrace.getFileName(), stackTrace.getLineNumber());
        String methodCounterString = String.format("%s:%s:%s", stackTrace.getFileName(), stackTrace.getClassName(), stackTrace.getMethodName());
        String classCounterString = String.format("%s:%s", stackTrace.getFileName(), stackTrace.getClassName());

        Long lineCount = lineCounter.getOrDefault(lineCounterString, 0l);
        lineCounter.put(lineCounterString, lineCount + size);

        Long methodCount = methodCounter.getOrDefault(methodCounterString, 0l);
        methodCounter.put(methodCounterString, methodCount + size);

        Long classCount = classCounter.getOrDefault(classCounterString, 0l);
        classCounter.put(classCounterString, classCount + size);
    }

    /**
     * logs all counts
     */
    public static void logInfo() {
        for(Map.Entry<String, Long> entry : lineCounter.entrySet()) {
            String line = entry.getKey();
            long allocated = entry.getValue();

            logger.info(line + " allocated " + allocated + " bytes");
        }

        for(Map.Entry<String, Long> entry : methodCounter.entrySet()) {
            String method = entry.getKey();
            long allocated = entry.getValue();

            logger.info(method + " allocated " + allocated + " bytes");
        }

        for(Map.Entry<String, Long> entry : classCounter.entrySet()) {
            String clazz = entry.getKey();
            long allocated = entry.getValue();

            logger.info(clazz + " allocated " + allocated + " bytes");
        }
    }
}
