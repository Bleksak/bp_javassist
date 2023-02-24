
class B {
    int a;
    B() {

    }

    void printA() {
        System.out.println(a);
    }
}

class C {
    int k;
    B b = new B();

    C() {
    }

    // void meme() {
    //     new String("adf");
    // }

    // void meme(int a) {
    //     new String("rere");
    // }
}

public class Main {

    public static void main(String... args) {
        for(int i = 0; i < 10; ++i) {
            C c = new C();
        }
        // c.meme();
        // c.meme(25);
        String object = new String("string");
        int[] primitiveArray = new int[30];
        Object[] objectArray = new Object[20];

        Object multiArray = new Object[20][10][30][40];
    }
}