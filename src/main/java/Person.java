public class Person {
    private String firstName;
    private String lastName;
    private String internalId;
    private String dateOfBirth;
    private String sex;
    private String street;
    private int streetNumber;
    private String city;
    private String phoneNumber;
    private String country;

    // Constructor
    public Person() {
        this.firstName = PersonInfo.generateRandomFirstName();
        this.lastName = PersonInfo.generateRandomLastName();
        this.internalId = PersonInfo.generatePatientInternalID();
        this.dateOfBirth = PersonInfo.generateRandomDOB();
        this.sex = PersonInfo.generateRandomSex();
        this.street = PersonInfo.generateRandomStreet();
        this.streetNumber = PersonInfo.generateRandomStreetNumber();
        this.city = PersonInfo.generateRandomCity();
        this.phoneNumber = PersonInfo.generateRandomPhoneNumber();
        this.country = PersonInfo.generateRandomCountry();
    }

    // Getters and setters
    // You can generate them automatically in most IDEs or manually like this:
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

    // Repeat for other attributes

    // toString method for debugging or display purposes
    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", internalId='" + internalId + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", sex='" + sex + '\'' +
                ", street='" + street + '\'' +
                ", streetNumber=" + streetNumber +
                ", city='" + city + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
