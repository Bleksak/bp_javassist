class CyclicA {
    public CyclicB b;
}

class CyclicB {
    public CyclicA a;
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

    public static void main(String... args) {
        cyclicTest();
    }
}