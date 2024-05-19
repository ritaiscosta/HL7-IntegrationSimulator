import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Configuration {
    private List<Simulation> simulations;

    @JsonCreator
    public Configuration(@JsonProperty("simulations") List<Simulation> simulations) {
        this.simulations = simulations;
    }

    public List<Simulation> getSimulations() {
        return simulations;
    }

    public void setSimulations(List<Simulation> simulations) {
        this.simulations = simulations;
    }
}
