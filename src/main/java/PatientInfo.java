import java.util.Random;

public class PatientInfo extends Person {
    private String admissionDateTime;
    private String dischargeDateTime;


    public PatientInfo(String firstName, String lastName, String id, String DOB, String street, String city, String phoneNumber, String country, String sex, String admissionDateTime, String dischargeDateTime) {
        super(firstName, lastName, id, DOB, street, city, phoneNumber, country, sex);
        this.admissionDateTime = admissionDateTime;
        this.dischargeDateTime = dischargeDateTime;
    }

    public String getAdmissionDateTime() {
        return admissionDateTime;
    }

    public void setAdmissionDateTime(String admissionDateTime) {
        this.admissionDateTime = admissionDateTime;
    }

    public String getDischargeDateTime() {
        return dischargeDateTime;
    }

    public void setDischargeDateTime(String dischargeDateTime) {
        this.dischargeDateTime = dischargeDateTime;
    }

}
