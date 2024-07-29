import java.util.Random;

public class Person {
    private String firstName;
    private String lastName;
    private String id;
    private String DOB;
    private String street;
    private String city;
    private String phoneNumber;
    private String country;
    private String sex;

    public Person(String firstName, String lastName, String id, String DOB, String street, String city, String phoneNumber, String country, String sex) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.DOB = DOB;
        this.street = street;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.sex = sex;
    }

    // Getters and setters

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDOB() {
        return DOB;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (ID:" + id + ")";
    }

    public static String generatePatientInternalID(){
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    public static String generateRandomFirstName(String sex) {
        String[] femaleNames = {"Maria", "Beatriz", "Bárbara", "Ana", "Rita", "Patrícia",  "Sofia",  "Carla",  "Sandra"};
        String[] maleNames = { "João", "José","Guilherme", "Miguel", "Pedro", "Rui",};
        Random random = new Random();
        if (sex.equals("M")) {
            return maleNames[random.nextInt(maleNames.length)];
        } else {
            return femaleNames[random.nextInt(femaleNames.length)];
        }
    }

    public static String generateRandomLastName() {
        String[] lastNames = {"Silva", "Morais", "Santos", "Teixeira", "Pereira", "Fernandes", "Costa", "Oliveira", "Maia", "Martins", "Rodrigues", "Nunes", "Gomes"};
        Random random = new Random();
        return lastNames[random.nextInt(lastNames.length)];
    }

    public static String generateRandomDOB() {
        // Generate random date of birth between 1950 and 2000
        Random random = new Random();
        int year = random.nextInt(51) + 1950;
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        return String.format("%04d%02d%02d", year, month, day);
    }

    public static String generateRandomSex() {
        String[] genders = {"M", "F"};
        Random random = new Random();
        int genderIndex = random.nextInt(genders.length);
        return genders[genderIndex];
    }

    public static String generateRandomStreet() {
        String[] streetNames = {"Rua da luz", "Avenida da liberdade", "Travessa da alegria", "Largo da perdição", "Praça da répulica", "Alameda dos condes", "Estrada nacional 4", "Praceta da feira"};
        Random random = new Random();
        int streetIndex = random.nextInt(streetNames.length);
        return streetNames[streetIndex];
    }

    public static String generateRandomCity() {
        String[] cities = {"Lisboa", "Porto", "Vila Nova de Gaia", "Amadora", "Braga", "Funchal", "Coimbra", "Setúbal", "Queluz", "Almada"};
        Random random = new Random();
        return cities[random.nextInt(cities.length)];
    }


    public static String generateRandomPhoneNumber() {
        Random random = new Random();
        String[] prefixes = {"91", "93", "96"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        String number = String.format("%07d", random.nextInt(10000000));
        return prefix + number;
    }

    public static String generateRandomCountry() {
        String[] countries = {"Portugal"};
        Random random = new Random();
        return countries[random.nextInt(countries.length)];
    }
}
