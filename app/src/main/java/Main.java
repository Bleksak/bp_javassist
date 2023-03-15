import java.util.Optional;

class CyclicA {
    public CyclicB b;
}

class CyclicB {
    public CyclicA a;
}

class C {
    int[] a = new int[]{10, 20, 30};
    int k = 10;
    double f = 21.3f;
    C(int[] k) {
        a = k;
    }
}

class Complex {
    Complex next;
    int a;

    Complex(int i, Complex n) {
        a = i;
        next = n;
    }
}

class OptionalWillFail {
    Optional<Integer> test;
    OptionalWillFail() {
        test = Optional.empty();
    }

    OptionalWillFail(Integer a) {
        test = Optional.of(a);
    }
}

public class Main {

    public static void optionalTest() {
        OptionalWillFail owf1 = new OptionalWillFail(12);
        OptionalWillFail owf2 = new OptionalWillFail(13);
    }

    public static void cyclicTest() {
        CyclicA a = new CyclicA();
        CyclicB b = new CyclicB();
        a.b = b;
        b.a = a;

        CyclicA a2 = new CyclicA();
        CyclicB b2 = new CyclicB();
        a2.b = b2;
        b2.a = a2;
    }

    public static void simpleObjectTest() {
        C a = new C(new int[]{10, 25});
        C b = new C(new int[]{10, 20});
    }

    public static void deepObjectTest() {
        Complex c = new Complex(2, null);
        Complex cx = new Complex(1, c);
        Complex cx1 = new Complex(0, cx);
        Complex cx2 = new Complex(0, new Complex(1, new Complex(2, null)));
        Complex cx3 = new Complex(0, null);
    }

    public static void main(String... args) {
        int[] arr1 = new int[]{1,2,3,4,5};
        int[] arr2 = new int[]{1,2,3,4,5};
        int[][] k = new int[][] {
            {1, 3, 4},
            {2, 5, 8}
        };

        int[][][] r = new int[][][] {
            {
                {1, 3, 4},
                {2, 2, 1},
            },
            {
                {2, 5, 8},
                {1, 3, 1},
                {2, 3, 3},
            }
        };

        // simpleObjectTest();

        // for(int i = 0; i < 10; ++i) {
        //     deepObjectTest();
        // }

        // cyclicTest();
        // optionalTest();

        // for(int i = 0; i < 10; ++i) {
        // }
        // c.m();
        // String object = new String("string");
        // int[] primitiveArray = new int[30];
        // Object[] objectArray = new Object[20];

        // C[][][][] multiArray = new C[20][10][30][40];
        // C[]
        // C[] justArray = new C[20];
    }
}