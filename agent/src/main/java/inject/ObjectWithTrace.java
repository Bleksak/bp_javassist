package inject;

/**
 * Saves object and StackTraceElement, with its allocation position
 */
public class ObjectWithTrace {
    public final Object object;
    public final StackTraceElement stackTrace;

    public ObjectWithTrace(Object obj, StackTraceElement trace) {
        object = obj;
        stackTrace = trace;
    }
}
