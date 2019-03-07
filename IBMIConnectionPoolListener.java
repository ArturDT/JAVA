package pl.ibmi.connector;

import com.ibm.as400.access.*;

import java.beans.PropertyVetoException;
import java.io.IOException;

public class IBMIConnectionPoolListener implements ConnectionPoolListener
{
    @Override
    public void connectionCreated(ConnectionPoolEvent event) {
        AS400 as400 = (AS400) event.getSource();
        CommandCall command = new CommandCall(as400);
        try {
            String liblCommand = "CHGLIBL LIBL(IASLIBST04 POZST04SVR POZST04DB1 POZST04EFT POZST04PB AIBINTMRQ2 AIBINTMRO2 AIBINTMRO AIB71INTPD AIBV710DQ2 AIBV710D2 AIBV710D AIBV710PD POLV71 EUMDV710 ICBSV710 ICBSSMSMRP QGPL QTEMP)";
            if (!command.run(liblCommand)) {
                AS400Message[] messagelist = command.getMessageList();
                for (AS400Message as400Message : messagelist) {
                    System.out.println("System message: " + as400Message.getText());
                }
            }
        } catch (IOException | ErrorCompletingRequestException | InterruptedException | PropertyVetoException | AS400SecurityException e) {
            e.printStackTrace();
        }
    }
        @Override
        public void connectionExpired(ConnectionPoolEvent event)
        {
        }

        @Override
        public void connectionPoolClosed(ConnectionPoolEvent event)
        {
        }

        @Override
        public void connectionReleased(ConnectionPoolEvent event)
        {
        }

        @Override
        public void connectionReturned(ConnectionPoolEvent event)
        {
        }

        @Override
        public void maintenanceThreadRun(ConnectionPoolEvent event)
        {
        }
    }
