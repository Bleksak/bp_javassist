package inject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class DuplicateFinder {
    public static int countDuplicates(Object obj, HashMap<String, List<Object>> objects) {

        Class<?> clazz = obj.getClass();

        String className = clazz.getName();
        List<Object> arr = objects.get(className);

        int duplicates = 0;

        for(int i = 0; i < arr.size(); ++i) {
            boolean dup = true;

            if(arr.get(i) == obj) continue; // it's this exact object, don't count it as a duplicate

            for(Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);

                try {
                    Object val1 = f.get(obj);
                    Object val2 = f.get(arr.get(i));

                    if(val1 != val2) {
                        dup = false;
                        break;
                    }
                } catch (IllegalAccessException e) {
                    System.out.println(e.getMessage());
                }
            }

            if(dup) {
                duplicates++;
            }
        }

        return duplicates;
    }
}
