package battleship;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class Server extends Thread {

    static int id = 0;
    ServerSocket server;
    public static ArrayList<SClient> clients;
    public static ArrayList<CClient>frames=new ArrayList<>();
    public static ArrayList<SClient> players=new ArrayList<>();
    
    
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port);
        this.clients = new ArrayList<>();

    }

    public void Start() {

        this.start();
    }

    public void SentMsgAll(byte[] msg) throws IOException {
        for (SClient client : clients) {
            client.SendMsg(msg);
        }
    }

    

    @Override
    public void run() {
        try {
            while (!this.server.isClosed()) {
                id++;
                Socket acceptedSocket = this.server.accept();

                SClient nclient = new SClient(acceptedSocket, this, id);
                this.clients.add(nclient);
                nclient.Listen();

// Yeniye kendi ID’sini gönder
                nclient.SendMsg(("My ID: " + id).getBytes());

// Tüm istemcilere güncel oyuncu listesini gönder
                for (SClient client : this.clients) {
                    for (SClient c : this.clients) {
                        client.SendMsg(("Player ID: " + c.id).getBytes());
                    }
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
