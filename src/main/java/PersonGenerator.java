import java.util.Random;
import ca.uhn.hl7v2.model.v251.datatype.*;
import ca.uhn.hl7v2.model.v251.segment.PID;


public class PersonGenerator {
    public static PID pidGenerator(Person person, PID pid) throws Exception {
        // Generate HL7 fields for the person
        Random random = new Random();
        String id = Person.generatePatientInternalID();
        String firstName = person.generateRandomFirstName();
        String lastName = person.generateRandomLastName();
        String dob = person.generateRandomDOB();
        String sex = person.generateRandomSex();
        String streetAddress = person.generateRandomStreet();
        String city = person.generateRandomCity();
        String country = person.generateRandomCountry();
        int streetNumber = person.generateRandomStreetNumber();
        String phoneNumber = person.generateRandomPhoneNumber();

        // Setting the generated values to the person's HL7 fields
        CX idNumber = pid.getPid3_PatientIdentifierList(0);
        idNumber.getCx1_IDNumber().setValue(id);

        XPN patientName = pid.getPid5_PatientName(0);
        patientName.getXpn1_FamilyName().getSurname().setValue(lastName);
        patientName.getXpn2_GivenName().setValue(firstName);

        TS dateTimeOfBirth = pid.getPid7_DateTimeOfBirth();
        dateTimeOfBirth.getTs1_Time().setValue(dob);

        pid.getPid8_AdministrativeSex().setValue(sex);

        XAD patientAddress = pid.getPid11_PatientAddress(0);
        patientAddress.getXad1_StreetAddress().getSad2_StreetName().setValue(streetAddress);
        patientAddress.getXad1_StreetAddress().getSad3_DwellingNumber().setValue(String.valueOf(streetNumber));
        patientAddress.getXad3_City().setValue(city);

        XTN phoneNumberHome = pid.getPid13_PhoneNumberHome(0);
        phoneNumberHome.getXtn1_TelephoneNumber().setValue(phoneNumber);

        CE nationality = pid.getPid28_Nationality();
        nationality.getCe2_Text().setValue(country);

        return pid;
    }
}
