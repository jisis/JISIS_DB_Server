/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dbserver;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jc dauphin
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
      try {
         
       
         ConsoleControlServer console = new ConsoleControlServer();
         console.start();
         console.run();
      } catch (Exception ex) {
         ex.printStackTrace();
         //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

}
