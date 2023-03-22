package inject;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DuplicateFinder finds all duplicated objects on the heap and provides information about them
 */
public class DuplicateFinder {
    private final Logger logger = LogManager.getRootLogger();

    private final Map<String, List<ObjectWithTrace>> objects;
    private final IDuplicateEquals eq;
    
    /**
     * @param objects Collection of all objects allocated on the heap
     * @param eq Equals implementation (shallow or deep)
     */
    public DuplicateFinder(Map<String, List<ObjectWithTrace>> objects, IDuplicateEquals eq) {
        this.objects = objects;
        this.eq = eq;
    }

    /**
     * Counts the duplicate count for the given object
     * @param obj
     * @return duplicate count for the given object
     */
    private int countDuplicates(Object obj) {

        String className = obj.getClass().getName();
        List<ObjectWithTrace> arr = objects.get(className);
        // List<Object> copy = List.copyOf(arr);

        int duplicates = 0;

        for(int i = 0; i < arr.size(); ++i) {
            ObjectWithTrace objectWithTrace = arr.get(i);

            if(objectWithTrace == null) continue;

            Object o = objectWithTrace.object;
            if(o == null) continue;
            if(o == obj) continue; // it's this exact object, don't count it
            
            if(eq.eq(o, obj)) {
                arr.set(i, null);
                duplicates++;
            }
        }

        return duplicates;
    }

    /**
     * Finds all object duplicates allocated on the heap and logs the information (number of duplicates and where the allocation occured)
     */
    public void findDuplicates() {
        for(List<ObjectWithTrace> list : objects.values()) {
            for(ObjectWithTrace o : list) {
                if(o == null) continue;
                if(o.object == null) continue;
                int duplicates = countDuplicates(o.object);

                if(duplicates > 0) {
                    String stackTraceString = String.format("%s:%s", o.stackTrace.getFileName(), o.stackTrace.getLineNumber());
                    logger.info("Found " + duplicates + " duplicates of object: " + o.object.getClass().getName() + " first allocated at: " + stackTraceString);
                }
            }
        }
    }
}
