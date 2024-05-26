import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v251.message.ADT_A01;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.model.v251.segment.PID;
import ca.uhn.hl7v2.parser.Parser;

public class HL7messageGenerator {

    public String generateHL7Message(PersonInfo personInfo) {
        try {
            ADT_A01 adt = new ADT_A01();
            adt.initQuickstart("ADT", "A01", "P");

            // Populate the MSH Segment
            MSH mshSegment = adt.getMSH();
            mshSegment.getSendingApplication().getNamespaceID().setValue("TestSendingSystem");
            mshSegment.getSequenceNumber().setValue("123");

            // Populate the PID Segment with personInfo data
            PID pid = adt.getPID();
            pid.getPatientName(0).getFamilyName().getSurname().setValue(personInfo.getLastName());
            pid.getPatientName(0).getGivenName().setValue(personInfo.getFirstName());
            pid.getPatientIdentifierList(0).getIDNumber().setValue(personInfo.getId());

            // Encode the message
            HapiContext context = new DefaultHapiContext();
            Parser parser = context.getPipeParser();
            return parser.encode(adt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
