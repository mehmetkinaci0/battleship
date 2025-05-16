package battleship;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author samet
 */
public class CClient extends Thread {

    JPanel panel;
    Socket csocket;
    OutputStream output;
    InputStream input;
    int myId = -1;
    int matchid = -1;
    Server server;
    HideShip hideShip;
    public boolean attackStatus;
    AttackShip as;

    public void Connect(String ip, int port, HideShip hideShip) throws IOException {
        this.csocket = new Socket(ip, port);//connection
        this.output = this.csocket.getOutputStream();
        this.input = this.csocket.getInputStream();
        this.hideShip = hideShip;
    }

    public void SendMsg(String msg) throws IOException {

        byte[] messageBytes = msg.getBytes();
        this.output.write(messageBytes.length);

        this.output.write(messageBytes);
    }

    public void Listen() throws IOException {
        this.start();
    }

    public void Check(String message, int status) {

        if (status == 1) {
            as.list_model.addElement(message);
        } else {
            if (!as.list_model.contains(message)) {
                as.list_model.addElement(message);
            }
        }

    }

    public void run() {

        while (this.csocket.isConnected()) {
            try {

                int rsize = this.input.read();
                byte buffer[] = new byte[rsize];
                this.input.read(buffer);
                String msg = new String(buffer);
                System.out.println(msg);

                if (msg.startsWith("My ID: ")) {
                    myId = Integer.parseInt(msg.substring(7).trim());

                }

                if (msg.startsWith("Player ID: ")) {
                    int incomingId = Integer.parseInt(msg.substring(11).trim());
                    this.matchid = incomingId;
                    // Eğer gelen ID kendine ait değilse listeye ekle
                    if (incomingId != myId) {
                        String listEntry = "Player ID: " + incomingId;

                        if (!HideShip.lst_msgs_model.contains(listEntry)) {
                            HideShip.lst_msgs_model.addElement(listEntry);
                        }
                    }
                }
                if (msg.startsWith("MATCH_REQUEST_FROM:")) {
                    int matchID = Integer.parseInt(msg.split(":")[1]);
                    this.matchid = matchID;
                    int response = JOptionPane.showConfirmDialog(
                            null,
                            String.valueOf(matchID) + " ID wants to play with you! Do you accept?",
                            "Game Request",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (response == JOptionPane.YES_OPTION) {
                        matchid = matchID;

                        String message = "DELETE: " + myId + "," + matchID;

                        this.SendMsg(message);

                    } else {
                        // Reddedildiyse sunucuya mesaj gönder   
                    }
                }
                if (msg.startsWith("OK")) {
                    HideShip.jPanel4.setVisible(false);
                    HideShip.jPanel1.setVisible(true);
                    HideShip.jPanel2.setVisible(true);
                    HideShip.jPanel3.setVisible(true);
                    //HideShip.jPanel5.setVisible(true);
                }
                if (msg.startsWith("Pair: ")) {
                    String status = msg.split(":")[1];
                    if (as == null) {
                        as = new AttackShip(this);
                    } else {
                        if (status.contains("1")) {
                            as.btnAttack.setEnabled(true);
                        }
                    }

                    if (status.contains("1")) {
                        as.setVisible(true);

                    } else {
                        as.setVisible(true);
                        as.btnAttack.setEnabled(false);
                    }

                }
                if (msg.startsWith("ATTACK: ")) {

                    String[] parts = msg.split(":"); // ["MATCH", " 1,2"]

                    String[] numbers = parts[1].trim().split(","); // ["1", "2"]

                    String id = numbers[0];
                    String coordinat = numbers[1];
                    String matchID = numbers[2];

                    if (!this.hideShip.amiralCoordinats.isEmpty() || !this.hideShip.kruvazorCoordinats.isEmpty() || !this.hideShip.muhripCoordinats.isEmpty() || !this.hideShip.denizaltiCoordinats.isEmpty()) {
                        if (this.hideShip.amiralCoordinats.remove(coordinat)) {
                            //as.list_model.addElement("Ship Amiral shooted!");
                            // JOptionPane.showMessageDialog(null, "Ship Amiral shooted!");
                            this.SendMsg("STATUS: " + id + "," + matchID + "," + 1 + "," + "Ship Amiral shooted!");
                        } else if (this.hideShip.kruvazorCoordinats.remove(coordinat)) {
                            //as.list_model.addElement("Ship Kruvazor shooted!");
                            // JOptionPane.showMessageDialog(null, "Ship Kruvazor shooted!");
                            this.SendMsg("STATUS: " + id + "," + matchID + "," + 1 + "," + "Ship Kruvazor shooted!");
                        } else if (this.hideShip.muhripCoordinats.remove(coordinat)) {
                            //as.list_model.addElement("Ship Muhrip shooted!");
                            //JOptionPane.showMessageDialog(null, "Ship Muhrip shooted!");
                            this.SendMsg("STATUS: " + id + "," + matchID + "," + 1 + "," + "Ship Muhrip shooted!");
                        } else if (this.hideShip.denizaltiCoordinats.remove(coordinat)) {
                            //as.list_model.addElement("Ship Denizalti shooted!");
                            //JOptionPane.showMessageDialog(null, "Ship Denizalti shooted!");
                            this.SendMsg("STATUS: " + id + "," + matchID + "," + 1 + "," + "Ship Denizalti shooted!");
                        } else {
                            this.SendMsg("STATUS: " + id + "," + matchID + "," + 0 + "," + "not shoot");
                        }
                    } else {

                    }

                }
                if (msg.startsWith("CHECK: ")) {
                    String[] parts = msg.split(":"); // ["MATCH", " 1,2"]

                    String[] numbers = parts[1].trim().split(","); // ["1", "2"]

                    String id = numbers[0];
                    String matchID = numbers[1];

                    if (this.hideShip.amiralCoordinats.isEmpty() && this.hideShip.kruvazorCoordinats.isEmpty() && this.hideShip.muhripCoordinats.isEmpty() && this.hideShip.denizaltiCoordinats.isEmpty()) {
                        this.SendMsg("DESTROY: " + id + "," + matchID + "," + 1);
                    } else if (this.hideShip.amiralCoordinats.isEmpty()) {
                        this.SendMsg("DESTROY: " + id + "," + matchID + "," + "Ship Amiral Destroyed!");
                    } else if (this.hideShip.kruvazorCoordinats.isEmpty()) {
                        this.SendMsg("DESTROY: " + id + "," + matchID + "," + "Ship Kruvazor Destroyed!");
                    } else if (this.hideShip.muhripCoordinats.isEmpty()) {
                        this.SendMsg("DESTROY: " + id + "," + matchID + "," + "Ship Muhrip Destroyed!");
                    } else if (this.hideShip.denizaltiCoordinats.isEmpty()) {
                        this.SendMsg("DESTROY: " + id + "," + matchID + "," + "Ship Denizalti Destroyed!");
                    }
                }

                if (msg.startsWith("DESTROY: ")) {
                    String destroy = msg.split(":")[1];

                    this.Check(destroy,0);
                }

                if (msg.startsWith("FINISH: ")) {
                    String status = msg.split(":")[1];
                    if (status.contains("1")) {
                        JOptionPane.showMessageDialog(null, "YOU WON!");
                        as.btnAttack.setEnabled(false);
                        this.as.restart();
                        as.setVisible(false);

                        this.SendMsg("ADD:" + myId);
                        this.hideShip.restart();
                        this.SendMsg("REMOVE:" + myId);

                    } else {
                        JOptionPane.showMessageDialog(null, "OTHER PLAYER WON!");
                        as.btnAttack.setEnabled(false);
                        this.as.restart();
                        as.setVisible(false);
                        this.SendMsg("ADD:" + myId);
                        this.hideShip.restart();
                        this.SendMsg("REMOVE:" + myId);
                    }
                }

                if (msg.startsWith("STATUS: ")) {
                    String[] parts = msg.split(":"); // ["MATCH", " 1,2"]

                    String[] numbers = parts[1].trim().split(","); // ["1", "2"]

                    String status = numbers[0];
                    String shooted = numbers[1];

                    if (!shooted.contains("not shoot")) {
                        this.Check(shooted,1);
                    }

                    if (status.contains("1")) {
                        AttackShip.clickedAttack.setBackground(Color.green);
                        this.attackStatus = true;
                        this.SendMsg("BLOCK: " + this.matchid + "," + 1);
                    } else {
                        AttackShip.clickedAttack.setBackground(Color.red);
                        this.attackStatus = false;
                        this.SendMsg("BLOCK: " + this.matchid + "," + 0);
                        this.SendMsg("BLOCK: " + this.myId + "," + 1);
                    }

                }
                if (msg.startsWith("BLOCK: ")) {
                    String status = msg.split(":")[1];

                    if (status.contains("1")) {
                        AttackShip.btnAttack.setEnabled(false);

                    } else {
                        AttackShip.btnAttack.setEnabled(true);
                    }
                }

            } catch (IOException ex) {

            }
        }

    }

}
