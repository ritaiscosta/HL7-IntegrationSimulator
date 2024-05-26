public class Person {
    private static int idCounter = 0;
    private final String id;
    private final String firstName;
    private final String lastName;

    public Person(String firstName, String lastName, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }

    public static synchronized int getIdCounter() {
        return idCounter++;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + id + ")";
    }
}
