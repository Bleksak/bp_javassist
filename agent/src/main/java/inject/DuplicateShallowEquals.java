package inject;

import java.lang.reflect.Field;
import java.util.Set;

public class DuplicateShallowEquals implements IDuplicateEquals {

    private static final Class<?>[] primitiveList = new Class[] { Character.class, Byte.class, Short.class, Integer.class, Long.class, Boolean.class, String.class };

    public boolean eqCyclic(Object a, Object b, Set<Object> visited) {


        // first we need to check if types of a and b are equal
        // if not, return false

        Class<?> aClass = a.getClass();
        Class<?> bClass = b.getClass();

        if(aClass != bClass) {
            return false;
        }

        // then we need to check, if a.equals(b) will return a definitive answer
        // this only happens with object types that represent primitive types (Byte, Short, Integer, Long, Boolean, Character)
        // + String type

        for(Class<?> cls : primitiveList) {
            if(aClass == cls) {
                return a.equals(b);
            }
        }

        // if a.equals(b) then objects are equal
        // however, if !a.equals(b), they are not "unequal", because objects don't have to override equals method

        if(a.equals(b)) {
            return true;
        }

        Field[] fields = a.getClass().getDeclaredFields();

        for(Field field : fields) {

            // we loop through all fields using reflection and check if they are equal
            // if one of the fields is not equal, the objects are not equal

            field.setAccessible(true);

            try {
                Object aValue = field.get(a);
                Object bValue = field.get(b);

                if(!aValue.equals(bValue)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                System.out.println("Cannot access field");
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean eq(Object a, Object b) {

        // first we need to check if types of a and b are equal
        // if not, return false

        Class<?> aClass = a.getClass();
        Class<?> bClass = b.getClass();

        if(aClass != bClass) {
            return false;
        }

        // then we need to check, if a.equals(b) will return a definitive answer
        // this only happens with object types that represent primitive types (Byte, Short, Integer, Long, Boolean, Character)
        // + String type

        for(Class<?> cls : primitiveList) {
            if(aClass == cls) {
                return a.equals(b);
            }
        }

        // if a.equals(b) then objects are equal
        // however, if !a.equals(b), they are not "unequal", because objects don't have to override equals method

        if(a.equals(b)) {
            return true;
        }

        Field[] fields = a.getClass().getDeclaredFields();

        for(Field field : fields) {

            // we loop through all fields using reflection and check if they are equal
            // if one of the fields is not equal, the objects are not equal

            field.setAccessible(true);

            try {
                Object aValue = field.get(a);
                Object bValue = field.get(b);

                if(!aValue.equals(bValue)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                System.out.println("Cannot access field");
                return false;
            }
        }

        return true;
    }
}
