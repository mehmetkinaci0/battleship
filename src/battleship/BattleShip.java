/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mehmet
 */
public class BattleShip {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try {
           Server s1 =new Server(58);
           s1.Start();

        } catch (IOException ex) {
            Logger.getLogger(BattleShip.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
}
