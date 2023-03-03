package inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteConverter {

    private static int getPrimitiveSize(Class<?> clazz) {
        if (clazz == Boolean.class || clazz == Byte.class) {
            return 1;
        }
        if (clazz == Character.class || clazz == Short.class) {
            return 2;
        }
        if (clazz == Integer.class || clazz == Float.class) {
            return 4;
        }
        if (clazz == Long.class || clazz == Double.class) {
            return 8;
        }

        return -1;
    }

    private static byte[] objectToByteArray(Object obj) {
        Class<?> clazz = obj.getClass();
        int size = getPrimitiveSize(clazz);
        if(size != -1) {
            ByteBuffer buffer = ByteBuffer.allocate(getPrimitiveSize(clazz));
            if (obj instanceof Boolean) {
                buffer.put((byte) ((boolean) obj ? 1 : 0));
            } else if (obj instanceof Byte) {
                buffer.put((byte) obj);
            } else if (obj instanceof Character) {
                buffer.putChar((char) obj);
            } else if (obj instanceof Short) {
                buffer.putShort((short) obj);
            } else if (obj instanceof Integer) {
                buffer.putInt((int) obj);
            } else if (obj instanceof Long) {
                buffer.putLong((long) obj);
            } else if (obj instanceof Float) {
                buffer.putFloat((float) obj);
            } else if (obj instanceof Double) {
                buffer.putDouble((double) obj);
            }
            return buffer.array();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static byte[] getBytes(Object obj) {

        if(obj == null) {
            return new byte[Integer.BYTES];
        }

        if(obj instanceof Object[] arr) {
            
            List<Byte> bytes = new ArrayList<>();

            for(Object o : arr) {
                for(byte b : getBytes(o)) {
                    bytes.add(b);
                }
            }

            byte[] byteArray = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                byteArray[i] = bytes.get(i);
            }

            return byteArray;
        }

        if(obj instanceof Serializable && !(obj instanceof Object[])) {
            return objectToByteArray(obj);
        } else {
            // Get the class of the objects
            Class<?> clazz = obj.getClass();

            // Get the fields of the class
            Field[] fields = clazz.getDeclaredFields();
            List<Byte> bytes = new ArrayList<>();

            // Copy the field values into the byte arrays
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = null;
                try {
                    value = field.get(obj);
                } catch (IllegalAccessException e) {}

                // TODO: this won't work on circular data types
                if(value != null && (value.getClass().isPrimitive() || value instanceof Serializable)) {
                    for(byte b : objectToByteArray(value)) {
                        bytes.add(b);
                    }
                } else {
                    // TODO: this is used for deep scan
                    // if(value == null) {
                    //     for(byte b : ByteBuffer.allocate(Integer.BYTES).putInt(System.identityHashCode(value)).array()) {
                    //         bytes.add(b);
                    //     }
                    // } else {
                    //     for(byte b: getBytes(value)) {
                    //         bytes.add(b);
                    //     }
                    // }

                    for(byte b : ByteBuffer.allocate(Integer.BYTES).putInt(System.identityHashCode(value)).array()) {
                        bytes.add(b);
                    }
                }
            }

            byte[] byteArray = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                byteArray[i] = bytes.get(i);
            }

            return byteArray;
        }
    }

}
