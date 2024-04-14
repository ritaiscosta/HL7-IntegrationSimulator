import java.util.Random;

public class Person {

    public static String generatePatientInternalID(){
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
    public static String generateRandomFirstName() {

        String[] firstNames = {"Maria", "João", "Ana", "José", "Rita", "Miguel", "Sofia", "Pedro", "Carla", "Rui"};
        Random random = new Random();
        return firstNames[random.nextInt(firstNames.length)];
    }

    public static String generateRandomLastName() {

        String[] lastNames = {"Silva", "Santos", "Pereira", "Fernandes", "Costa", "Oliveira", "Martins", "Rodrigues", "Nunes", "Gomes"};
        Random random = new Random();
        return lastNames[random.nextInt(lastNames.length)];
    }

    public static String generateRandomDOB() {
        // Generate random date of birth between 1950 and 2000
        Random random = new Random();
        int year = random.nextInt(51) + 1950; // Random year between 1950 and 2000
        int month = random.nextInt(12) + 1; // Random month between 1 and 12
        int day = random.nextInt(28) + 1; // Random day between 1 and 28 (to keep it simple)
        return String.format("%04d%02d%02d", year, month, day);
    }

    public static String generateRandomSex() {
        // Generate random sex (M/F)
        Random random = new Random();
        return random.nextBoolean() ? "M" : "F";
    }

    public static String generateRandomStreet() {

        String[] streetNames = {"Rua da luz", "Avenida da liberdade", "Travessa da alegria", "Largo da perdição", "Praça da répulica", "Alameda dos condes", "Estrada nacional 4", "Praceta da feira"};

        Random random = new Random();
        int streetIndex = random.nextInt(streetNames.length);

        int streetNumber = random.nextInt(999) + 1; // Random number between 1 and 999
        String streetAddress = streetNames[streetIndex];
        return streetAddress;

    }
    public static String generateRandomCity() {

        String[] cities = {"Lisboa", "Porto", "Vila Nova de Gaia", "Amadora", "Braga", "Funchal", "Coimbra", "Setúbal", "Queluz", "Almada"};

        Random random = new Random();
        int cityIndex = random.nextInt(cities.length);

        String city = cities[cityIndex];
        return city;
    }

    public static int generateRandomStreetNumber() {
        Random random = new Random();
        int streetNumber = random.nextInt(999) + 1; // Random number between 1 and 999
        return streetNumber;
    }

    public static String generateRandomPhoneNumber() {

        Random random = new Random();
        String[] prefixes = {"91", "93", "96"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        String number = String.format("%07d", random.nextInt(10000000)); // 7 digit random number
        return prefix + number;
    }

    public static String generateRandomCountry() {

        String[] countries = {"Portugal"};
        Random random = new Random();
        return countries[random.nextInt(countries.length)];
    }

}
