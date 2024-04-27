import java.util.ArrayList;
import java.util.List;

public class States {
    private String name;
    private int maxCapacity;
    private List<Person> person;

    // Default constructor for Jackson deserialization
    public States() {
        this.name = "";
        this.maxCapacity = 0;
        this.person = new ArrayList<>();
    }

    public States(String name, int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.person = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Person> getPerson() {
        return person;
    }

    public void setPerson(List<Person> person) {
        this.person = person;
    }
}
