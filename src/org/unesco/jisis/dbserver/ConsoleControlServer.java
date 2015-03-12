package org.unesco.jisis.dbserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.client.ClientDbProxy;
import org.unesco.jisis.corelib.client.ConnectionNIO;
import org.unesco.jisis.corelib.client.RemoteDatabase;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.server.ConfigProperties;

/**
 * ConsoleControlServer: A console to control a J-ISIS Database Server
 */
public class ConsoleControlServer extends ThreadedServer
    implements Runnable {

    public void start(String[] args)
        throws Exception {
        setRunnable(this);

        //super.start(args);
        super.start();
    }

    public void run() {
        ConsoleThread t = new ConsoleThread();
        t.start();

        try {
            t.join();
        } catch (InterruptedException intEx) {
            // Do nothing but return
            return;
        }
    }

    public ConfigProperties getConfigInfo() {
        return null;
    }

    public void setConfigInfo(ConfigProperties configInfo) {
    }
}

class ConsoleThread extends Thread {

    private JisisDbServer jisisDbServer_;
    
    private IConnection connection_;
    
    private String consolePrompt;

    private final String username = "admin";
    private final String password = "admin";
    private final String port = "1111";
    private final String hostname = "localhost";
    
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConsoleThread.class);

    public ConsoleThread() {
        setDaemon(true);
        consolePrompt = "J-ISIS Server >";
    }
    
    private Map<String, List<String>> getDatabaseNames() throws DbException {

        String[] dbHomes_ = null;
        try {
            dbHomes_ = connection_.getDbHomes();
        } catch (DbException ex) {
            LOGGER.error("Error when getting dbHomes", ex);
            throw new DbException(ex);
        }

        Map<String, List<String>> dbNames = new <String, List<String>> HashMap();

        for (String dbHome : dbHomes_) {
            List<String> list = connection_.getDbNames(dbHome);
            dbNames.put(dbHome, list);
        }
        return dbNames;
    }

    @Override
    public void run() {
        try {
            // Set up
            BufferedReader in
                = new BufferedReader(
                    new InputStreamReader(System.in));

            System.out.print(consolePrompt);
            for (String line = in.readLine();
                !line.equals("quit");
                line = in.readLine()) {
                LOGGER.info(this.toString()
                    + ": '" + line + "'");
                if (line.trim().equals("shutdown")) {
                    if (jisisDbServer_ != null) {
                        jisisDbServer_.close();
                    }
                    //return;
                } else if (line.trim().startsWith("start")) {
                    jisisDbServer_ = new JisisDbServer();
                    jisisDbServer_.start();
                     try {
                        connection_ = ConnectionNIO.connect(hostname, Integer.valueOf(port), username, password);
                    } catch (DbException ex) {
                        LOGGER.error("Cannot establish connection", ex);
                    }

                } else if (line.trim().startsWith("list")) {
                    String[] svcs = ServerManager.instance().getServices();

                    System.out.println("Services: {");
                    for (int i = 0; i < svcs.length; i++) {
                        System.out.println("    " + svcs[i]);
                    }
                    System.out.println("}");
                } else if (line.trim().startsWith("remove ")) {
                    // Parse argument, confirm removal,
                    // call ServerManager.removeService()
                } else if (line.trim().startsWith("test")) {
                 

                    Map<String, List<String>> dbNames = getDatabaseNames();
                    for (Map.Entry entry : dbNames.entrySet()) {
                        System.out.println("dbHome: " + entry.getKey() + "  {");
                        List<String> names = (List<String>) entry.getValue();

                        for (String name : names) {
                            System.out.println("    " + name);
                        }
                        System.out.println("}");
                    }


                    RemoteDatabase db_ = new RemoteDatabase(connection_);
                    String dbHome = "DEF_HOME";
                    String dbName = "ASFAEX";
                    db_.getDatabase(dbHome, dbName, Global.DATABASE_DURABILITY_WRITE);

                    IRecord rec = db_.getFirst();
                    System.out.println(rec.toString());

                } else if (line.trim().startsWith("threads")) {
                    // List all threads running in the JVM
                    //

                    // Find the ultimate ThreadGroup parent
                    ThreadGroup ancestor = Thread.currentThread().getThreadGroup();
                    while (ancestor.getParent() != null) {
                        ancestor = ancestor.getParent();
                    }

                    // List all threads
                    int ct = ancestor.activeCount();
                    ct += ct / 2;
                    Thread[] array = new Thread[ct];
                    ancestor.enumerate(array, true);

                    for (int i = 0; i < array.length; i++) {
                        if (array[i] != null) {
                            String msg = array[i].toString();
                            msg += ": [";
                            if (array[i].isAlive()) {
                                msg += " ALIVE";
                            }
                            if (array[i].isDaemon()) {
                                msg += " DAEMON";
                            }
                            msg += " ]";
                            System.out.println(msg);
                        }
                    }
                } else {
                    System.out.println("Unrecognized command: " + line);
                }

                System.out.print("J-ISIS Server >");
            }
        } catch (DbException ex) {
            LOGGER.error("DBException !", ex);
            if (jisisDbServer_ != null) {
                jisisDbServer_.close();
            }
        } catch (java.io.IOException IOEx) {
             LOGGER.error("IOException !", IOEx);
            if (jisisDbServer_ != null) {
                jisisDbServer_.close();
            }
        }
    }
}
