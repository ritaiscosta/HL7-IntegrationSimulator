public class Person {
    private static int idCounter = 0;
    private String id;

    public Person(String id) {
        this.id = id;
    }

    public static synchronized int getIdCounter() {
        return idCounter++;
    }

    @Override
    public String toString() {
        return id;
    }
}
