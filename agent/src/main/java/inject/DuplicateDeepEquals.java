package inject;

import java.lang.reflect.Field;

public class DuplicateDeepEquals implements IDuplicateEquals {
    private static final Class<?>[] primitiveList = new Class[] { char.class, byte.class, short.class, int.class, long.class, boolean.class, 
                                                Character.class, Byte.class, Short.class, Integer.class, Long.class, Boolean.class, String.class };

    @Override
    public boolean eq(Object a, Object b) {

        // first check if objects are null

        if(a == null && b == null) {
            return true;
        }

        if(a == null || b == null) {
            return false;
        }

        // we need to check if types of a and b are equal
        // if not, return false


        Class<?> aClass = a.getClass();
        Class<?> bClass = b.getClass();

        if(aClass != bClass) return false;

        // then we need to check, if a.equals(b) will return a definitive answer
        // this only happens with types that represent primitive types (Byte, Short, Integer, Long, Boolean, Character)
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

        Field[] fields = aClass.getDeclaredFields();

        for(Field field : fields) {
            field.setAccessible(true);

            try {
                Object aValue = field.get(a);
                Object bValue = field.get(b);

                // if equals is not true, we need to recursively call eq
                if(!eq(aValue, bValue)) {
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
