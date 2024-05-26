import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v251.message.*;
import ca.uhn.hl7v2.model.v251.segment.*;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

public class HL7messageGenerator {


    public class HL7EventMethodMapper {
        private static final Map<String, String> eventToMethodMap = new HashMap<>();

        static {
            eventToMethodMap.put("ADT_A01", "generateADT_A01Message");
            eventToMethodMap.put("ADT_A02", "generateADT_A02Message");
            eventToMethodMap.put("ADT_A03", "generateADT_A03Message");
            eventToMethodMap.put("ORM_O01", "generateORM_O01Message");
            eventToMethodMap.put("ORU_R01", "generateORU_R01Message");
        }

        public static String getMethodName(String event) {
            return eventToMethodMap.get(event);
        }
    }

    public String generateADT_A01Message(PersonInfo personInfo) {
        HapiContext context = new DefaultHapiContext();
        try {
            // Create a new ADT_A01 message (without Quickstart)
            ADT_A01 adt = new ADT_A01();

            populateCommonSegments(adt.getMSH(), adt.getEVN(), adt.getPID(), personInfo);
            MSH msh = adt.getMSH();
            msh.getMessageType().getMessageCode().setValue("ADT");
            msh.getMessageType().getTriggerEvent().setValue("A01");

            PV1 pv1Segment = adt.getPV1();
            pv1Segment.getSetIDPV1().setValue("1");
            pv1Segment.getPatientClass().setValue("I"); // Inpatient
            pv1Segment.getAssignedPatientLocation().getPointOfCare().setValue("ICU"); // Intensive Care Unit
            pv1Segment.getAdmissionType().setValue("EMER"); // Emergency
            pv1Segment.getAdmitDateTime().getTime().setValue("20230526120000");

            String messageA01 = adt.getMSH().encode() + "\n" +
                    adt.getPID().encode() + "\n" +
                    adt.getPV1().encode() + "\n" +
                    adt.getEVN().encode();

            return messageA01;
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

    public String generateADT_A02Message(PersonInfo personInfo) {
        HapiContext context = new DefaultHapiContext();
        try {
            ADT_A02 adt = new ADT_A02();
            populateCommonSegments(adt.getMSH(), adt.getEVN(), adt.getPID(), personInfo);
            MSH msh = adt.getMSH();
            msh.getMessageType().getMessageCode().setValue("ADT");
            msh.getMessageType().getTriggerEvent().setValue("A02");
            PV1 pv1 = adt.getPV1();
            pv1.getPatientClass().setValue("I"); // Inpatient
            pv1.getAssignedPatientLocation().getPointOfCare().setValue("Ward1");
            pv1.getAdmissionType().setValue("E"); // Emergency
            pv1.getAdmitDateTime().getTime().setValue("20230526120000");
            pv1.getDischargeDateTime(0).getTime().setValue("20230526180000");

            String messageA02 = adt.getMSH().encode() + "\n" +
                    adt.getPID().encode() + "\n" +
                    adt.getPV1().encode() + "\n" +
                    adt.getEVN().encode();

            return messageA02;
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

    public String generateADT_A03Message(PersonInfo personInfo) {
        HapiContext context = new DefaultHapiContext();
        try {
            ADT_A03 adt = new ADT_A03();
            populateCommonSegments(adt.getMSH(), adt.getEVN(), adt.getPID(), personInfo);
            MSH msh = adt.getMSH();
            msh.getMessageType().getMessageCode().setValue("ADT");
            msh.getMessageType().getTriggerEvent().setValue("A03");

            EVN evn = adt.getEVN();
            evn.getEventTypeCode().setValue("A03");

            PV1 pv1 = adt.getPV1();
            pv1.getPatientClass().setValue("I"); // Inpatient
            pv1.getAssignedPatientLocation().getPointOfCare().setValue("Ward1");
            pv1.getAdmissionType().setValue("E"); // Emergency
            pv1.getAdmitDateTime().getTime().setValue("20230526120000");
            pv1.getDischargeDateTime(0).getTime().setValue("20230526180000");

            String messageA03 = adt.getMSH().encode() + "\n" +
                    adt.getPID().encode() + "\n" +
                    adt.getPV1().encode() + "\n" +
                    adt.getEVN().encode();

            return messageA03;
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

    public String generateORM_O01Message(PersonInfo personInfo) {
        HapiContext context = new DefaultHapiContext();
        try {
            ORM_O01 orm = new ORM_O01();

            MSH msh = orm.getMSH();
            populateMSH(msh);
            msh.getMessageType().getMessageCode().setValue("ORM");
            msh.getMessageType().getTriggerEvent().setValue("O01");

            PID pid = orm.getPATIENT().getPID();
            populatePID(pid, personInfo);


            PV1 pv1 = orm.getPATIENT().getPATIENT_VISIT().getPV1();
            pv1.getPatientClass().setValue("I"); // Inpatient

            ORC orc = orm.getORDER().getORC();
            orc.getOrderControl().setValue("NW"); // New order
            orc.getPlacerOrderNumber().getEntityIdentifier().setValue("12345");
            orc.getFillerOrderNumber().getEntityIdentifier().setValue("54321");

            OBR obr = orm.getORDER().getORDER_DETAIL().getOBR();
            obr.getUniversalServiceIdentifier().getIdentifier().setValue("Test");
            obr.getUniversalServiceIdentifier().getText().setValue("Test Description");

            String messageORM = orm.getMSH().encode() + "\n" +
                    orm.getPATIENT().getPID() + "\n" +
                    orm.getPATIENT().getPATIENT_VISIT().getPV1() + "\n" +
                    orm.getORDER().getORC().encode() + "\n" +
                    orm.getORDER().getORDER_DETAIL().getOBR();

            return messageORM;
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

    public String generateORU_R01Message(PersonInfo personInfo) {
        HapiContext context = new DefaultHapiContext();
        try {
            ORU_R01 oru = new ORU_R01();

            MSH msh = oru.getMSH();
            populateMSH(msh);
            msh.getMessageType().getMessageCode().setValue("ORU");
            msh.getMessageType().getTriggerEvent().setValue("R01");

            PID pid = oru.getPATIENT_RESULT().getPATIENT().getPID();
            populatePID(pid, personInfo);

            ORC orc = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC();
            orc.getOrderControl().setValue("RE"); // Result

            OBR obr = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getOBR();
            obr.getUniversalServiceIdentifier().getIdentifier().setValue("Test");
            obr.getUniversalServiceIdentifier().getText().setValue("Test Description");

            OBX obx = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(0).getOBX();
            obx.getSetIDOBX().setValue("1");
            obx.getValueType().setValue("TX");
            obx.getObservationIdentifier().getIdentifier().setValue("12345");
            obx.getObservationIdentifier().getText().setValue("Observation");
            obx.getObservationValue(0).getData().parse("Observation Value");

            String messageORU = oru.getMSH().encode() + "\n" +
                    oru.getPATIENT_RESULT().getPATIENT().getPID().encode() + "a\n" +
                    oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC().encode() + "\n" +
                    oru.getPATIENT_RESULT().getORDER_OBSERVATION().getOBR().encode() + "\n" +
                    oru.getPATIENT_RESULT().getORDER_OBSERVATION().getOBR().encode() + "\n" +
                    oru.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(0).getOBX().encode();

            return messageORU;
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

    private void populateCommonSegments(MSH msh, EVN evn, PID pid, PersonInfo personInfo) throws DataTypeException {
        populateMSH(msh);
        populateEVN(evn);
        populatePID(pid, personInfo);
    }

    private void populateMSH(MSH msh) throws DataTypeException {
        msh.getFieldSeparator().setValue("|");
        msh.getEncodingCharacters().setValue("^~\\&");
        msh.getSendingApplication().getNamespaceID().setValue("TestSendingSystem");
        msh.getSendingFacility().getNamespaceID().setValue("TestSendingFacility");
        msh.getReceivingApplication().getNamespaceID().setValue("TestReceivingSystem");
        msh.getReceivingFacility().getNamespaceID().setValue("TestReceivingFacility");
        msh.getDateTimeOfMessage().getTime().setValue("20230526120000");
        msh.getMessageControlID().setValue("12345");
        msh.getProcessingID().getProcessingID().setValue("P");
        msh.getVersionID().getVersionID().setValue("2.5.1");
    }

    private void populateEVN(EVN evn) throws DataTypeException {
        evn.getRecordedDateTime().getTime().setValue("20230526120000");
    }

    private void populatePID(PID pid, PersonInfo personInfo) throws DataTypeException {
        pid.getPatientID().getIDNumber().setValue(personInfo.getId());
        pid.getPatientName(0).getFamilyName().getSurname().setValue(personInfo.getLastName());
        pid.getPatientName(0).getGivenName().setValue(personInfo.getFirstName());
        pid.getDateTimeOfBirth().getTime().setValue(personInfo.generateRandomDOB());
        pid.getAdministrativeSex().setValue(personInfo.generateRandomSex());
    }
}

