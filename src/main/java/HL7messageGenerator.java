import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v251.message.ADT_A01;
import ca.uhn.hl7v2.model.v251.segment.EVN;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.model.v251.segment.PID;
import ca.uhn.hl7v2.model.v251.segment.PV1; // Added PV1 segment
import ca.uhn.hl7v2.parser.Parser;

import java.io.IOException;

public class HL7messageGenerator {

    public String generateHL7Message(PersonInfo personInfo) {
        HapiContext context = new DefaultHapiContext();
        try {
            // Create a new ADT_A01 message (without Quickstart)
            ADT_A01 adt = new ADT_A01();

            // Populate the MSH Segment
            MSH mshSegment = adt.getMSH();
            mshSegment.getFieldSeparator().setValue("|");
            mshSegment.getEncodingCharacters().setValue("^~\\&");
            mshSegment.getSendingApplication().getNamespaceID().setValue("TestSendingSystem");
            mshSegment.getSendingFacility().getNamespaceID().setValue("TestSendingFacility");
            mshSegment.getReceivingApplication().getNamespaceID().setValue("TestReceivingSystem");
            mshSegment.getReceivingFacility().getNamespaceID().setValue("TestReceivingFacility");
            mshSegment.getDateTimeOfMessage().getTime().setValue("20230526120000");
            mshSegment.getMessageType().getMessageCode().setValue("ADT");
            mshSegment.getMessageType().getTriggerEvent().setValue("A01");
            mshSegment.getMessageControlID().setValue("12345");
            mshSegment.getProcessingID().getProcessingID().setValue("P");
            mshSegment.getVersionID().getVersionID().setValue("2.5.1");

            // Populate the EVN Segment
            EVN evnSegment = adt.getEVN();
            evnSegment.getEventTypeCode().setValue("A01");
            evnSegment.getRecordedDateTime().getTime().setValue("20230526120000");

            // Populate the PID Segment
            PID pid = adt.getPID();
            pid.getPatientID().getIDNumber().setValue(personInfo.getId());
            pid.getPatientName(0).getFamilyName().getSurname().setValue(personInfo.getLastName());
            pid.getPatientName(0).getGivenName().setValue(personInfo.getFirstName());
            pid.getPatientIdentifierList(0).getIDNumber().setValue(personInfo.getId());
            pid.getPatientIdentifierList(0).getIdentifierTypeCode().setValue("MR");
            pid.getDateTimeOfBirth().getTime().setValue("19700101");
            pid.getAdministrativeSex().setValue("U");

            // Populate the PV1 Segment
            PV1 pv1Segment = adt.getPV1();
            pv1Segment.getSetIDPV1().setValue("1");
            pv1Segment.getPatientClass().setValue("I"); // Inpatient
            pv1Segment.getAssignedPatientLocation().getPointOfCare().setValue("ICU"); // Intensive Care Unit
            pv1Segment.getAdmissionType().setValue("EMER"); // Emergency
            pv1Segment.getAdmitDateTime().getTime().setValue("20230526120000");

            String message = adt.getMSH().encode() + "\n" +
                    adt.getPID().encode() + "\n" +
                    adt.getPV1().encode() + "\n" +
                    adt.getEVN().encode();

            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                context.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
