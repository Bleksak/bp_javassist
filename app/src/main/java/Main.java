class CyclicA {
    public CyclicB b;
}

class CyclicB {
    public CyclicA a;
}

class C {
    int k = 10;
    double f = 21.3f;
}

class Complex {
    Complex next;
    int a;

    Complex(int i, Complex n) {
        a = i;
        next = n;
    }
}

public class Main {

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

    // public static void simpleObjectTest() {
    //     C a = new C();
    //     C b = new C();
    // }

    public static void deepObjectTest() {
        // Complex c = new Complex(2, null);
        // Complex cx = new Complex(1, c);
        // Complex cx1 = new Complex(0, cx);
        Complex cx2 = new Complex(0, new Complex(1, new Complex(2, null)));
        // Complex cx3 = new Complex(0, null);
    }

    public static void main(String... args) {
        // for(int i = 0; i < 10; ++i) {
        //     deepObjectTest();
        // }

        cyclicTest();

        // for(int i = 0; i < 10; ++i) {
        // }
        // c.m();
        // String object = new String("string");
        // int[] primitiveArray = new int[30];
        // Object[] objectArray = new Object[20];

        // C[][][][] multiArray = new C[20][10][30][40];
        // C[]
        // C[] justArray = new C[20];

        while(true) {}
    }
}