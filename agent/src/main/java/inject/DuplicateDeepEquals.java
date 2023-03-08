package inject;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;

public class DuplicateDeepEquals implements IDuplicateEquals {
    private static final Class<?>[] primitiveList = new Class[] { char.class, byte.class, short.class, int.class, long.class, boolean.class, 
                                                Character.class, Byte.class, Short.class, Integer.class, Long.class, Boolean.class, String.class };


    public static boolean comparePrimitiveArrays(Object array1, Object array2) {
        Class<?> componentType1 = array1.getClass().getComponentType();

        if (componentType1 == int.class) {
            return Arrays.equals((int[]) array1, (int[]) array2);
        } else if (componentType1 == byte.class) {
            return Arrays.equals((byte[]) array1, (byte[]) array2);
        } else if (componentType1 == short.class) {
            return Arrays.equals((short[]) array1, (short[]) array2);
        } else if (componentType1 == long.class) {
            return Arrays.equals((long[]) array1, (long[]) array2);
        } else if (componentType1 == float.class) {
            return Arrays.equals((float[]) array1, (float[]) array2);
        } else if (componentType1 == double.class) {
            return Arrays.equals((double[]) array1, (double[]) array2);
        } else if (componentType1 == boolean.class) {
            return Arrays.equals((boolean[]) array1, (boolean[]) array2);
        } else if (componentType1 == char.class) {
            return Arrays.equals((char[]) array1, (char[]) array2);
        }

        return false;
    }

    private boolean eqWithCycles(Object a, Object b, HashSet<Integer> visited) {
        // first check if objects are null

        if(a == b) {
            return true;
        }

        if(a == null || b == null) {
            return false;
        }

        int aCode = System.identityHashCode(a);
        int bCode = System.identityHashCode(b);

        if(visited.contains(aCode)) {
            return true;
        }

        if(visited.contains(aCode) || visited.contains(bCode)) {
            return false;
        }

        visited.add(aCode);
        visited.add(bCode);

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

        if(aClass.isArray()) {
            char objectIndicator = 'L';

            // if there's an L on 1st position of the class name, it means it's an object array
            if(aClass.getName().charAt(1) == objectIndicator) {
                Object[] aArray = (Object[]) a;
                Object[] bArray = (Object[]) b;

                if(aArray.length != bArray.length) {
                    return false;
                }

                for(int i = 0; i < aArray.length; ++i) {
                    if(!eq(aArray[i], bArray[i])) {
                        return false;
                    }
                }

                return true;
            } 

            return comparePrimitiveArrays(a, b);
        }

        Field[] fields = aClass.getDeclaredFields();

        for(Field field : fields) {
            field.setAccessible(true);

            try {
                Object aValue = field.get(a);
                Object bValue = field.get(b);

                // if equals is not true, we need to recursively call eq
                if(!eqWithCycles(aValue, bValue, visited)) {
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
        return eqWithCycles(a, b, new HashSet<>());
    }
}
