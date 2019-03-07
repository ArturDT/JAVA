package pl.ibmi.connector;

import com.ibm.as400.access.*;
import com.ibm.as400.data.ProgramCallDocument;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Base64;

@Log
@Component
public class IBMIConnector {

    private String PASSWORD = "XXX";
    private static final String USERNAME = "XXX";
    private static final String IBMIServer = "XX.XX.XX.XX";

    private AS400 as400System;
    private final CommandCall command;
    private final String programPath;
    private String functionName;

    private ProgramCallDocument pcml;
    private final AS400ConnectionPool pool;

    /**
     * Setting a connection to IBMIServer
     *
     * @param serviceProgram service program to work with
     * @param library        library where is locate service program
     */
    public IBMIConnector(String serviceProgram, String library) {
        this.setPassword();
        pool = new AS400ConnectionPool();
        pool.addConnectionPoolListener(new IBMIConnectionPoolListener());
        try {
            this.as400System = pool.getConnection(IBMIServer, USERNAME, PASSWORD);
        } catch (Exception e) {
            log.info("get connection failure!");
            e.printStackTrace();
        }
        this.command = new CommandCall(as400System);
       // this.program = new ServiceProgramCall(as400System);
        this.programPath = "/QSYS.LIB/" + library.trim() + ".LIB/" + serviceProgram.trim() + ".SRVPGM";
    }

    /**
     * call program on IBMIServer - program must be specified at constructor of IBMIConnector
     *
     * @return true if program is called without errors
     */
    public boolean callProgram() {
        try {
            this.pcml.callProgram(functionName);
        } catch (Exception e) {
            log.info("call program failure!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * setting PCML file as actual file
     *
     * @param pcmlFile name of PCML file
     * @return true if everything is done - else (when end with errors) return false
     */
    public boolean setPCML(String pcmlFile) {
        try {
            this.pcml = new ProgramCallDocument(as400System, pcmlFile.trim() + ".pcml");
            this.pcml.setPath(pcmlFile.trim(), this.programPath);
            this.functionName = pcmlFile.trim();
        } catch (Exception e) {
            log.info("setting PCML failure!");
            return false;
        }
        return true;
    }

    /**
     * setting value on PCML field
     *
     * @param field name of field from PCML
     * @param value Object value to setting on this field
     */
    private void setPCMLValue(String field, Object value) {
        try {
            this.pcml.setValue(field.trim(), value);
        } catch (Exception e) {
            log.info("setting " + field + " Error!");
        }
    }

    /**
     * setting value on PCML field with multiple
     *
     * @param field name of field from PCML
     * @param index index of field
     * @param value Object value to setting on this field
     */
    private void setPCMLValue(String field, int index, Object value) {
        int[] ind_value = new int[1];
        ind_value[0] = index;
        try {
            this.pcml.setValue(field, ind_value, value.toString().trim());
        } catch (Exception e) {
            log.info("setting " + field + " Error!");
        }
    }

    /**
     * get a string value from RPG response (PCML)
     *
     * @param field field in PCML name
     * @return string value of requested field
     */
    public String getReturnStringValue(String field) {
        String returnValue;
        try {
            returnValue = this.pcml.getStringValue(field.trim());
        } catch (Exception e) {
            log.info("getting value error: " + field);
            returnValue = "";
        }
        return returnValue;
    }

    /**
     * get a string value from RPG response (PCML) from multiple field
     *
     * @param field field in PCML name
     * @param index index of field
     * @return string value of requested field
     */
    public String getReturnStringValue(String field, int index) {
        String returnValue;
        int[] ind_value = new int[1];
        ind_value[0] = index;
        try {
            returnValue = this.pcml.getStringValue(field.trim(), ind_value);
        } catch (Exception e) {
            log.info("getting value error: " + field);
            returnValue = "";
        }
        return returnValue;
    }

    /**
     * get a Integer value from RPG response (PCML)
     *
     * @param field field in PCML name
     * @return Integer value of requested field
     */
    public Integer getReturnIntegerValue(String field) {
        int returnValue;
        try {
            returnValue = this.pcml.getIntValue(field.trim());
        } catch (Exception e) {
            log.info("getting value error: " + field);
            returnValue = 0;
        }
        return returnValue;
    }

    /**
     * get a Integer value from RPG response (PCML) from multiple field
     *
     * @param field field in PCML name
     * @param index index of field
     * @return Integer value of requested field
     */
    public Integer getReturnIntegerValue(String field, int index) {
        int returnValue;
        int[] ind_value = new int[1];
        ind_value[0] = index;
        try {
            returnValue = this.pcml.getIntValue(field.trim(), ind_value);
        } catch (Exception e) {
            log.info("getting value error: " + field);
            returnValue = 0;
        }
        return returnValue;
    }

    /**
     * get a Double value from RPG response (PCML)
     *
     * @param field field in PCML name
     * @return Double value of requested field
     */
    public Double getReturnDoubleValue(String field) {
        double returnValue;
        BigDecimal temp;
        try {
            temp = (BigDecimal) this.pcml.getValue(field.trim());
            returnValue = temp.doubleValue();
        } catch (Exception e) {
            log.info("getting value error: " + field);
            returnValue = 0.0;
            e.printStackTrace();
        }
        return returnValue;
    }

    /**
     * get a Double value from RPG response (PCML) from multiple field
     *
     * @param field field in PCML name
     * @param index index of field
     * @return Double value of requested field
     */
    public Double getReturnDoubleValue(String field, int index) {
        double returnValue;
        BigDecimal temp;
        int[] ind_value = new int[1];
        ind_value[0] = index;
        try {
            temp = (BigDecimal) this.pcml.getValue(field.trim(), ind_value);
            returnValue = temp.doubleValue();
        } catch (Exception e) {
            log.info("getting value error (index): " + field);
            returnValue = 0.0;
            e.printStackTrace();
        }
        return returnValue;
    }

    /**
     * disconnect AS400 session
     */
    public void AS400Disconnect() {
        as400System.disconnectAllServices();
        pool.returnConnectionToPool(as400System);
    }

    /**
     * execute command on IBMI
     *
     * @param myCommand command to execute
     * @return true command executive without errors
     */
    private boolean executeCommand(String myCommand) {
        try {
            if (!command.run(myCommand)) {
                AS400Message[] messagelist = command.getMessageList();
                for (AS400Message as400Message : messagelist) {
                    log.info("System message: " + as400Message.getText());
                }
            }
        } catch (Exception e) {
            log.info("Execute command failed!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * set library list in IBMI
     *
     * @param libraryList library list as string (ex: "TESTLIB TESTLIB01 TESTLIB3")
     * @return true if library list is setting
     */
    public boolean setMyOwnLibraryList(String libraryList) {
        String liblCommand = "CHGLIBL LIBL(" + libraryList + ")";
        return executeCommand(liblCommand);
    }

    /**
     * set developer library list (simply list)
     *
     * @return true if library list setting successful
     */
    public boolean setDevLibraryList() {
        String liblCommand = "CHGLIBL LIBL(TESTLIB TESTLIB01 TESTLIB3)";
        return executeCommand(liblCommand);
    }

    /**
     * set ZT002 environment library list (basic ZT002 list)
     *
     * @return true if library list setting successful
     */
    public boolean setZT002LibraryList() {
        String liblCommand = "CHGLIBL LIBL(TESTLIB TESTLIB01 TESTLIB3 QTEMP)";
        return executeCommand(liblCommand);
    }

    /**
     * Temporary function to hide password in this class
     */
    private void setPassword() {
        byte[] decodedBytes = Base64.getDecoder().decode(this.PASSWORD);
        this.PASSWORD = new String(decodedBytes);
    }

    /**
     * Fill PCML for primitive components in object
     * ex:
     *  DisbursementSchedule class have 2 fields: disbursementDate and disbursementAmount
     *  so we should use setPCMLFileValue(disbursementSchedule, "AmortisationSchedule.DisbursementSchedule")
     *  function fill in PCML file fields: AmortisationSchedule.DisbursementSchedule.disbursementDate and
     *  AmortisationSchedule.DisbursementSchedule.disbursementAmount
     * @param object object from we get values for PCML
     * @param name name of entity in PCML ex AmortisationSchedule.DisbursementSchedule
     * @return true if PCML value is setting without errors
     */
    public boolean setPCMLFileValue(Object object, String name){
        Field[] field = object.getClass().getDeclaredFields();
        String objectContent = object.toString();
        for (Field field1 : field) {
                if (field1.getType().getName().contains("String")) {
                    try {
                        int index1 = objectContent.indexOf(field1.getName()) + field1.getName().length() + 1;
                        int index2 = objectContent.indexOf(",", index1);
                        if (index2 > index1)
                            setPCMLValue(name.concat(".").concat(field1.getName()), objectContent.substring(index1, index2));
                        else setPCMLValue(name.concat(".").concat(field1.getName()), "");
                    } catch (Exception e) {
                        log.info("setPCMLFileValue failed!");
                        e.printStackTrace();
                        return false;
                    }
                } else if (field1.getType().getName().equals("double")) {
                    try {
                        int index1 = objectContent.indexOf(field1.getName()) + field1.getName().length() + 1;
                        int index2 = objectContent.indexOf(",", index1);
                        if (index2 > index1)
                            setPCMLValue(name.concat(".").concat(field1.getName()), Double.valueOf(objectContent.substring(index1, index2)));
                        else setPCMLValue(name.concat(".").concat(field1.getName()), 0);
                    } catch (Exception e) {
                        log.info("setPCMLFileValue failed!");
                        e.printStackTrace();
                        return false;
                    }
                }
                else if (field1.getType().getName().equals("int")) {
                    try {
                        int index1 = objectContent.indexOf(field1.getName()) + field1.getName().length() + 1;
                        int index2 = objectContent.indexOf(",", index1);
                        if (index2 > index1)
                            setPCMLValue(name.concat(".").concat(field1.getName()), Integer.parseInt(objectContent.substring(index1, index2)));
                        else setPCMLValue(name.concat(".").concat(field1.getName()), 0);
                    } catch (Exception e) {
                        log.info("setPCMLFileValue failed!");
                        e.printStackTrace();
                        return false;
                    }
                }
        }
        return true;
    }

    /**
     * Fill PCML for primitive components in object (with multiple)
     * ex:
     *  DisbursementSchedule class have 2 fields: disbursementDate and disbursementAmount
     *  so we should use setPCMLFileValue(disbursementSchedule, "AmortisationSchedule.DisbursementSchedule")
     *  function fill in PCML file fields: AmortisationSchedule.DisbursementSchedule.disbursementDate and
     *  AmortisationSchedule.DisbursementSchedule.disbursementAmount
     * @param object object from we get values for PCML
     * @param name name of entity in PCML ex AmortisationSchedule.DisbursementSchedule
     * @param index index of element to fill
     * @return true if PCML value is setting without errors
     */
    public boolean setPCMLFileValue(Object object, String name, int index){
        Field[] field = object.getClass().getDeclaredFields();
        String objectContent = object.toString();
        for (Field field1 : field)
                if (field1.getType().getName().contains("String")){
                    try {
                        int index1 = objectContent.indexOf(field1.getName()) + field1.getName().length() + 1;
                        int index2 = objectContent.indexOf(",", index1);
                        if (index2 > index1)
                            setPCMLValue(name.concat(".").concat(field1.getName()), index, objectContent.substring(index1, index2));
                        else setPCMLValue(name.concat(".").concat(field1.getName()), index, "");
                    } catch (Exception e) {
                        log.info("setPCMLFileValue failed!");
                        e.printStackTrace();
                        return false;
                    }
                } else if (field1.getType().getName().equals("double")){
                    try {
                        int index1 = objectContent.indexOf(field1.getName()) + field1.getName().length() + 1;
                        int index2 = objectContent.indexOf(",", index1);
                        if (index2 > index1)
                            setPCMLValue(name.concat(".").concat(field1.getName()), index, Double.valueOf(objectContent.substring(index1, index2)));
                        else setPCMLValue(name.concat(".").concat(field1.getName()), index, 0);
                    } catch (Exception e) {
                        log.info("setPCMLFileValue failed!");
                        e.printStackTrace();
                        return false;
                    }
                }
                else if (field1.getType().getName().equals("int")) {
                    try {
                        int index1 = objectContent.indexOf(field1.getName()) + field1.getName().length() + 1;
                        int index2 = objectContent.indexOf(",", index1);
                        if (index2 > index1)
                            setPCMLValue(name.concat(".").concat(field1.getName()), index, Integer.parseInt(objectContent.substring(index1, index2)));
                        else setPCMLValue(name.concat(".").concat(field1.getName()), index, 0);
                    } catch (Exception e) {
                        log.info("setPCMLFileValue failed!");
                        e.printStackTrace();
                        return false;
                    }
                }
        return true;
    }
}
