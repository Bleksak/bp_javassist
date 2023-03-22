package agent;

import java.lang.instrument.Instrumentation;

import org.apache.logging.log4j.core.config.Configurator;

/**
 * Java Agent which is loaded before Main class and creates a Transformer
 */
public class MemoryAllocationAgent {
    private static Instrumentation instr;

    /**
     * Stores the instrumentation instance and creates a transformer
     * Initializes the log4j logging library
     * This method is called from the JVM before main
     * 
     * @param args arguments passed to the agent
     * @param inst instrumentation instance created by the agent
     */
    public static void premain(String args, Instrumentation inst) {
        instr = inst;
        Configurator.initialize(null, "log4j2.xml");
        instr.addTransformer(new MemoryAllocationDetectionTransformer());
    }

    /**
     * Returns the size of the object
     * 
     * @param obj
     * @return size of the object
     */
    public static long getObjectSize(Object obj) {
        return instr.getObjectSize(obj);
    }
}
