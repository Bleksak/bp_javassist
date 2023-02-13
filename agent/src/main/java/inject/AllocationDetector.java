package inject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AllocationDetector {

    private static AllocationDetector instance = new AllocationDetector();

    private final List<WeakReference<Object>> objects = new ArrayList<>();
    private final List<WeakReference<String>> strings = new ArrayList<>();
    public final HashMap<Object, Integer> duplicateMap = new HashMap<>();

    public int counter = 0;

    public static AllocationDetector getInstance() {
        return instance;
    }

    public void register(Object obj) {
        System.out.println("counter: " + counter++);
        System.out.println(obj.getClass().getSimpleName());
        // duplicateMap.clear();

        // if(obj instanceof String str) {
        //     System.out.println("Registering string: " + obj.getClass().getSimpleName());
        //     strings.add(new WeakReference<String>(str));
        //     Iterator<WeakReference<String>> stringIterator = strings.iterator();
        //     while(stringIterator.hasNext()) {
        //         WeakReference<String> ref = stringIterator.next();
        //         String s = ref.get();
        //         if(s == null) {
        //             stringIterator.remove();
        //         } else {
        //             if(!duplicateMap.containsKey(s)) {
        //                 duplicateMap.put(s, 1);
        //             } else {
        //                 duplicateMap.put(s, duplicateMap.get(s) + 1);
        //             }
        //         }
        //     }

        //     Map<Object, Integer> duplicates = new HashMap<>();
        //     for (Map.Entry<Object, Integer> entry : duplicateMap.entrySet()) {
        //         if (entry.getValue() > 1) {
        //             duplicates.put(entry.getKey(), entry.getValue());
        //         }
        //     }
        //     // System.out.println(duplicates);
        // } else {
        //     objects.add(new WeakReference<Object>(obj));
        // }

    }
}
