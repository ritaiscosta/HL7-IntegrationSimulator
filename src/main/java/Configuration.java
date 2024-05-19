import java.util.List;

public class Configuration {
    private List<Simulation> simulations;

    public Configuration() {
    }

    public Configuration(List<Simulation> simulations) {
        this.simulations = simulations;
    }

    public List<Simulation> getSimulations() {
        return simulations;
    }

    public void setSimulations(List<Simulation> simulations) {
        this.simulations = simulations;
    }
}
