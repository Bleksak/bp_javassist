package inject;

import java.util.HashMap;
import java.util.List;

public class DuplicateFinder extends Thread {
    private HashMap<String, List<Object>> objects;
    private IDuplicateEquals eq;

    public int countDuplicates(Object obj) {

        String className = obj.getClass().getName();
        List<Object> arr = objects.get(className);

        int duplicates = 0;

        for(Object o : arr) {
            if(o == obj) continue; // it's this exact object, don't count it
            if(eq.eq(o, obj)) {
                duplicates++;
            }
        }

        return duplicates;
    }

    public DuplicateFinder(HashMap<String, List<Object>> objects, IDuplicateEquals eq) {
        this.objects = objects;
        this.eq = eq;
    }

    @Override
    public void run() {
        while(true) {
            try {
                System.out.println("Running thread");
                synchronized(objects) {
                    for(List<Object> list : objects.values()) {
                        for(Object o : list) {
                            int duplicates = countDuplicates(o);

                            if(duplicates > 0) {
                                System.out.println("Found " + duplicates + " duplicates of object: " + o.getClass().getName());
                            }
                        }
                    }
                }

                Thread.sleep(3000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
