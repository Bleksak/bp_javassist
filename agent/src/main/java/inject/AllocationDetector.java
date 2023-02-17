package inject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AllocationDetector {

    private static AllocationDetector instance = new AllocationDetector();

    private final List<WeakReference<Object>> objects = new ArrayList<>();
    private final List<WeakReference<String>> strings = new ArrayList<>();
    public final HashMap<Object, Integer> duplicateMap = new HashMap<>();

    public static int counter = 0;

    public static AllocationDetector getInstance() {
        return instance;
    }

    public static void registerObject(Object obj) {
        System.out.println("counter: " + counter++);
        System.out.println(obj.getClass().getSimpleName());

        if(obj instanceof String str) {
            System.out.println("Registering string: " + obj.toString());
        }
    }

    public static void registerPrimitiveArray(Object array) {
        System.out.println("PRIMITIVE ARRAY");
    }

    public static void registerObjectArray(Object[] array) {

    }

    public static void registerMultiArray(Object[] mutliArray) {

    }
}
