import java.util.ArrayList;
import java.util.List;

public class Configurations {
    private List<Simulation> simulations;

    // Default constructor for Jackson deserialization
    public Configurations() {
        // Initialize empty list to avoid NullPointerException during deserialization
        this.simulations = new ArrayList<>();
    }

    public Configurations(List<Simulation> simulations) {
        this.simulations = simulations;
    }

    public List<Simulation> getSimulations() {
        return simulations;
    }

    public void setSimulations(List<Simulation> simulations) {
        this.simulations = simulations;
    }
}
