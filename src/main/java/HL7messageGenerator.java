import ca.uhn.hl7v2.DefaultHapiContext;

import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v251.message.ADT_A01;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.model.v251.segment.PID;
import ca.uhn.hl7v2.parser.Parser;

public class HL7messageGenerator {
//    ADT_A01 adt = new ADT_A01();
//    adt.initQuickstart("ADT", "A01", "P");
//
//    // Populate the MSH Segment
//    MSH mshSegment = adt.getMSH();
//         mshSegment.getSendingApplication().getNamespaceID().setValue("TestSendingSystem");
//         mshSegment.getSequenceNumber().setValue("123");
//
//    // Populate the PID Segment
//    PID pid = adt.getPID();
//         pid.getPatientName(0).getFamilyName().getSurname().setValue("Doe");
//         pid.getPatientName(0).getGivenName().setValue("John");
//         pid.getPatientIdentifierList(0).getID().setValue("123456");
//
//    /*
//     * In a real situation, of course, many more segments and fields would be populated
//     */
//
//    // Now, let's encode the message and look at the output
//    HapiContext context = new DefaultHapiContext();
//    Parser parser = context.getPipeParser();
//    String encodedMessage = parser.encode(adt);
//         System.out.println("Printing ER7 Encoded Message:");
//         System.out.println(encodedMessage);
//
//    /*
//     * Prints:
//     *
//     * MSH|^~\&|TestSendingSystem||||200701011539||ADT^A01^ADT A01||||123
//     * PID|||123456||Doe^John
//     */
//
//    // Next, let's use the XML parser to encode as XML
//    parser = context.getXMLParser();
//    encodedMessage = parser.encode(adt);
//         System.out.println("Printing XML Encoded Message:");
//         System.out.println(encodedMessage);
//


}
