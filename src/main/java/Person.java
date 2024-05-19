public class Person {
    private static int idCounter = 1; // Static counter to generate unique IDs
    private int id;
    private String name;

    public Person(String name) {
        this.id = idCounter++;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static int getIdCounter() {
        return idCounter;
    }

    @Override
    public String toString() {
        return name + " (ID: " + id + ")";
    }
}
