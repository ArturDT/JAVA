package pl.ibmi.connector;

import com.ibm.as400.access.AS400ConnectionPool;


public class IBMIConnectionPool {
    public AS400ConnectionPool pool;
//??
    public IBMIConnectionPool () {
        pool = new AS400ConnectionPool();
        pool.addConnectionPoolListener(new IBMIConnectionPoolListener());
    }

}
