package agent;

import java.lang.instrument.Instrumentation;

/**
 * Java Agent which is loaded before Main class and creates a Transformer
 */
public class MemoryAllocationAgent {
    private static Instrumentation instr;

    /**
     * Stores the instrumentation instance and creates a transformer
     * This method is called from the VM before main
     * 
     * @param args arguments passed to the agent
     * @param inst instrumentation instance created by the agent
     */
    public static void premain(String args, Instrumentation inst) {
        instr = inst;
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
