import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v251.message.ADT_A01;
import ca.uhn.hl7v2.model.v251.segment.EVN;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.model.v251.segment.PID;
import ca.uhn.hl7v2.model.v251.segment.PV1;
import ca.uhn.hl7v2.parser.Parser;

public class testCreateMessage {
    public static void main(String[] args) throws Exception {

            HapiContext context = new DefaultHapiContext();
            Parser parser = context.getPipeParser();

            ADT_A01 adt = new ADT_A01();
            adt.initQuickstart("ADT", "A01", "P");

            // Populate the MSH Segment
            MSH mshSegment = adt.getMSH();
            mshSegment.getMsh3_SendingApplication().getNamespaceID().setValue("TestSendingSystem");
            mshSegment.getMsh7_DateTimeOfMessage().getTs1_Time().setValue("20241202120000");
            mshSegment.getMsh9_MessageType().getMsg1_MessageCode().setValue("123134500");
            mshSegment.getMsh9_MessageType().getMsg2_TriggerEvent().setValue("ADT_A01");
            mshSegment.getMsh9_MessageType().getMsg3_MessageStructure().setValue("A01");
            mshSegment.getMsh10_MessageControlID().getMessage().generateACK();
            mshSegment.getMsh11_ProcessingID().getPt1_ProcessingID().setValue("123454656456");
            mshSegment.getMsh12_VersionID().getVid1_VersionID().setValue("251");

            String encodedMessage = parser.encode(adt);
            System.out.println("Printing ER7 Encoded Message");
            System.out.println(encodedMessage);

            // Populate the EVN Segment
            EVN evn = adt.getEVN();
            evn.getEvn2_RecordedDateTime().getTs1_Time().setValue("11241202120000");
            encodedMessage = parser.encode(adt);
            System.out.println(encodedMessage);

            // Populate the PID Segment
            PID pid = adt.getPID();
            pid.getPid3_PatientIdentifierList(0).getCx1_IDNumber().setValue("14564564");
            pid.getPid5_PatientName(0).getXpn1_FamilyName().getFn1_Surname().setValue("Costa");
            pid.getPid5_PatientName(0).getXpn2_GivenName().setValue("Rita");
            pid.getPid8_AdministrativeSex().setValue("F");
            encodedMessage = parser.encode(adt);
            System.out.println(encodedMessage);

            // Populate the PV1 Segment
            PV1 pv1 = adt.getPV1();
            pv1.getPv12_PatientClass().setValue("O");
            encodedMessage = parser.encode(adt);
            System.out.println(encodedMessage);
        }

    }

