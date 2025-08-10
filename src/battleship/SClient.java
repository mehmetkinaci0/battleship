package battleship;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;


public class SClient extends Thread {

    Socket csokcet;
    OutputStream output;
    InputStream input;
    Server server;
    int id;
    boolean pair = false;
    boolean destroy = false;

    public SClient(Socket acceptedSocket, Server server, int id) throws IOException {
        this.csokcet = acceptedSocket;
        this.server = server;
        this.id = id;
        this.output = csokcet.getOutputStream();
        this.input = csokcet.getInputStream();
    }

    public void Listen() throws IOException {
        this.start();
    }

    public void SendMsg(byte[] msg) throws IOException {
        this.output.write(msg.length);

        this.output.write(msg);
    }

    public void removeClient(String myID, String matchId) {
        Iterator<SClient> iterator = Server.clients.iterator();
        while (iterator.hasNext()) {
            SClient client = iterator.next();
            if (Integer.valueOf(myID) == client.id || Integer.valueOf(matchId) == client.id) {
                iterator.remove(); 
            }
        }

    }



    @Override
    public void run() {

        try {
            while (this.csokcet.isConnected()) {

                int rbyte = this.input.read();
                byte buffer[] = new byte[rbyte];
                this.input.read(buffer);
                String rmsg = new String(buffer);

                System.out.println(new String(buffer));
                String[] parts = rmsg.split(":"); 

                String[] numbers = parts[1].trim().split(","); 

                String myID;
                String matchID;
                String coordinat;

                if (rmsg.startsWith("MATCH:")) {
                    myID = numbers[0];
                    matchID = numbers[1];

                    for (SClient client : Server.clients) {
                        if (client.id == Integer.valueOf(matchID)) {
                            client.SendMsg(("MATCH_REQUEST_FROM:" + myID).getBytes());
                            break;
                        }
                    }
                }
                if (rmsg.startsWith("DELETE: ")) {
                    myID = numbers[0];
                    matchID = numbers[1];

                    for (SClient client : Server.clients) {
                        if (client.id == Integer.valueOf(myID) || client.id == Integer.valueOf(matchID)) {
                            client.SendMsg("OK".getBytes());
                            Server.players.add(client);
                        }

                    }
                    removeClient(myID, matchID);

                }
                if (rmsg.startsWith("OK:")) {

                    myID = numbers[0];
                    matchID = numbers[1];

                    for (SClient client : Server.players) {

                        if (client.id == Integer.valueOf(myID)) {

                            client.pair = true;
                            for (SClient client2 : Server.players) {
                                if (client2.id == Integer.valueOf(matchID)) {
                                    if (client.pair && client2.pair) {
                                        client.SendMsg(("Pair: " + 1).getBytes());
                                        client2.SendMsg(("Pair: " + 0).getBytes());
                                    } else {
                                        System.out.println("match no pair");
                                    }
                                }
                            }
                        }

                    }

                }
                if (rmsg.startsWith("ATTACK: ")) {
                    matchID = numbers[0];
                    myID = numbers[1];
                    coordinat = numbers[2];

                    String message = "ATTACK: " + myID + "," + coordinat + "," + matchID;
                    for (SClient client : Server.players) {
                        if (client.id == Integer.valueOf(matchID)) {
                            client.SendMsg(message.getBytes());
                        }
                    }
                }
                if (rmsg.startsWith("STATUS: ")) {
                    myID = numbers[0];
                    String status = numbers[2];
                    matchID = numbers[1];
                    String shooted=numbers[3];
                    for (SClient client : Server.players) {
                        if (client.id == Integer.valueOf(myID)) {
                            client.SendMsg(("STATUS: " + status+","+shooted).getBytes());
                            for (SClient client2 : Server.players) {
                                if (client2.id == Integer.valueOf(matchID)) {
                                    client2.SendMsg(("CHECK: " + myID + "," + matchID).getBytes());
                                }
                            }
                        }
                    }
                }
                if (rmsg.startsWith("BLOCK: ")) {
                    matchID = numbers[0];
                    String status = numbers[1];
                    for (SClient client : Server.players) {
                        if (client.id == Integer.valueOf(matchID)) {
                            client.SendMsg(("BLOCK: " + status).getBytes());
                        }
                    }
                }
                if (rmsg.startsWith("DESTROY: ")) {
                    myID = numbers[0];
                    matchID = numbers[1];
                    String status = numbers[2];
                    for (SClient client : Server.players) {
                        if (client.id == Integer.valueOf(myID)) {
                            if (status.contains("1")) {
                                client.destroy = true;

                                for (SClient client2 : Server.players) {
                                    if (client2.id == Integer.valueOf(matchID)) {
                                        if (client2.destroy == true) {
                                            client2.SendMsg(("FINISH: " + 1).getBytes());
                                            client.SendMsg(("FINISH: " + 0).getBytes());
                                        }
                                        if (client.destroy == true) {
                                            client2.SendMsg(("FINISH: " + 0).getBytes());
                                        }
                                        client.SendMsg(("FINISH: " + 1).getBytes());
                                    }
                                }
                            }else{
                                client.SendMsg(("DESTROY: "+status).getBytes());
                            }
                        }
                    }

                }
                if (rmsg.startsWith("ADD:")) {
                    myID = parts[1];

                    for (SClient client : Server.players) {
                        if (client.id == Integer.valueOf(myID)) {
                            Server.clients.add(client);
                            client.pair = false;
                            client.destroy=false;
                            client.SendMsg(("My ID: " + myID).getBytes());
                            for (SClient c : Server.clients) {
                                client.SendMsg(("Player ID: " + c.id).getBytes());
                            }

                        }
                    }
                }
                if (rmsg.startsWith("REMOVE:")) {
                    myID = parts[1];
                    
                    Iterator<SClient> iterator = Server.players.iterator();
                    while (iterator.hasNext()) {
                        SClient client = iterator.next();
                        if (Integer.valueOf(myID) == client.id ) {
                            iterator.remove(); 
                        }
                    }
                    
                    for(SClient c:Server.clients)
                        System.out.println("client "+c.id);
                }
            }

        } catch (IOException ex) {
            this.server.clients.remove(this);
        }

    }
}
