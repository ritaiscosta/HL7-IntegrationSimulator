import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Configuration {
    private List<Simulation> simulations;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonCreator
    public Configuration(@JsonProperty("simulations") List<Simulation> simulations) {
        this.simulations = simulations;
    }

    public List<Simulation> getSimulations() {
        return simulations;
    }

    public static String generateSimulationCode() {
        Date currentDate = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        return dateFormat.format(currentDate);
    }

    public static void writeToJson(Configuration configuration, String fileName) throws IOException {
        File folder = new File("Existing Configurations");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File[] files = folder.listFiles((dir, name) -> name.equals(fileName));
        if (files != null && files.length > 0) {
            throw new IOException("A configuration file with the same name already exists.");
        }

        File configFile = new File(folder, fileName);
        objectMapper.writeValue(configFile, configuration);
    }


    public static Configuration readFromJson(String fileName) throws IOException {
        Configuration configuration = objectMapper.readValue(new File("Existing Configurations", fileName), Configuration.class);

        return configuration;
    }


    public static List<String> getConfigurationFiles() {
        File folder = new File("Existing Configurations");
        File[] files = folder.listFiles((dir, name) -> name.startsWith("simulation_") && name.endsWith(".json"));
        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                fileNames.add(file.getName());
            }
        }
        return fileNames;
    }

    public static void updateJsonFile(Configuration configuration, String fileName) throws IOException {
        File configFile = new File("Existing Configurations", fileName);
        objectMapper.writeValue(configFile, configuration);
    }
}
