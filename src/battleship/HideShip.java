/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Mehmet
 */
public class HideShip extends javax.swing.JFrame {

    /**
     * Creates new form HideShip
     */
    //cbox secilen gemiye gore buton atama
    private static ArrayList<JButton> amiralButtons = new ArrayList<>();
    private static ArrayList<JButton> kruvazorButtons = new ArrayList<>();
    private static ArrayList<JButton> muhripButtons = new ArrayList<>();
    private static ArrayList<JButton> denizaltiButtons = new ArrayList<>();

    //attack yapilidiginda aranacak koordinatların listesi
    public static ArrayList<String> amiralCoordinats = new ArrayList<>();
    public static ArrayList<String> kruvazorCoordinats = new ArrayList<>();
    public static ArrayList<String> muhripCoordinats = new ArrayList<>();
    public static ArrayList<String> denizaltiCoordinats = new ArrayList<>();

    private JButton[][] buttons = new JButton[10][10];//oyun icin butonlar

    public static JPanel jPanel1 = new JPanel(); //gemi saklanacak butonlar
    public static JPanel jPanel2 = new JPanel(); //cbox gemiler
    public static JPanel jPanel3 = new JPanel(); //start butonu
    public static JPanel jPanel4 = new JPanel(new BorderLayout()); //players list

    JButton btnStart = new JButton();
    private JComboBox cBoxShips = new JComboBox();
    private JList listPlayers = new JList();
    //dikey yatay ayarlamak icin koordinat tutma
    private static ArrayList<Character> selectedRow = new ArrayList<>();
    private static ArrayList<String> selectedCol = new ArrayList<>();

    int count = 0;//butun gemiler yerlesti mi kontrolu
    public static DefaultListModel lst_msgs_model;
    CClient client;

    private static ArrayList<String> selectedCoordinats = new ArrayList<>();

    public HideShip() throws IOException {

        initComponents();

        try {
            this.jPanel4.setVisible(true);
            this.jPanel1.setVisible(false);
            this.jPanel2.setVisible(false);
            this.jPanel3.setVisible(false);

            this.client = new CClient();
            this.client.Connect("13.53.124.168", 58, this);
            this.client.Listen();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Layout'u ayarla
        this.setLayout(new BorderLayout());

        lst_msgs_model = new DefaultListModel();
        listPlayers.setModel(lst_msgs_model);

        JButton btnMatch = new JButton("Play");
        btnMatch.setSize(750, 750);
        btnMatch.setEnabled(false);

        listPlayers.addListSelectionListener(new ListSelectionListener() {//rakip id secme
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    btnMatch.setEnabled(!listPlayers.isSelectionEmpty());
                }
            }
        });

        btnMatch.addActionListener(e -> {//secilen id eslesme istegi gonderme

            String id = (String) listPlayers.getSelectedValue();
            int playerId = Integer.parseInt(id.substring(11).trim()); //Player ID: 11 gibi gelen id yi kesip alma
            String msg = "MATCH:" + String.valueOf(this.client.myId) + "," + String.valueOf(playerId);
            this.client.matchid = playerId;//client rakip id belirle
            try {
                this.client.SendMsg(msg);
            } catch (IOException ex) {
                Logger.getLogger(HideShip.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        JLabel lblInfo = new JLabel("Choose a player");
        JLabel lblMessage = new JLabel("MY ID: " + String.valueOf(this.client.myId));

        jPanel4.add(lblInfo, BorderLayout.NORTH);
        jPanel4.add(listPlayers);
        jPanel4.add(btnMatch, BorderLayout.SOUTH);

        // Panelleri ayarla
        jPanel1.setLayout(new GridLayout(11, 11)); // 10x10 buton + başlık satırı/kolonu
        jPanel1.setPreferredSize(new Dimension(600, 600));

        // Satir ve sutun basliklari 
        String[] sutunlar = {"", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        char[] satirlar = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};

        // sutun numaralari ekleme
        for (String s : sutunlar) {
            JButton headerBtn = new JButton(s);
            headerBtn.setEnabled(false);
            jPanel1.add(headerBtn);
        }

        char[] selectedRow = null;
        int[] selectedCol = null;

        // comboBox ekle (gemi seçimi)
        cBoxShips.addItem("Amiral");
        cBoxShips.addItem("Kruvazör");
        cBoxShips.addItem("Muhrip");
        cBoxShips.addItem("Denizaltı");
        jPanel2.add(new JLabel("Gemi Seç:"));
        jPanel2.add(cBoxShips);
        jPanel2.add(lblMessage);

        // butonlari ve satır basliklari ekleme
        for (int i = 0; i < 10; i++) {
            // satir basliklari
            JButton rowHeader = new JButton(String.valueOf(satirlar[i]));
            rowHeader.setEnabled(false);
            jPanel1.add(rowHeader);

            // butonlar
            for (int j = 0; j < 10; j++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(50, 50));
                btn.setBackground(Color.white);
                btn.setOpaque(true);
                btn.setBorderPainted(false);
                buttons[i][j] = btn;

                // eklenen butona gemi saklamak icin tıklandiginda ilgili koordinatı alma
                btn.addActionListener(e -> {

                    JButton clickedButton = (JButton) e.getSource();
                    int row = -1, col = -1;//satir sutun bilgisi icin
                    int m = 0;
                    // tiklanan butonun satir ve sutun bilgisini bulma
                    for (int r = 0; r < 10; r++) {
                        for (int c = 0; c < 10; c++) {
                            if (buttons[r][c] == clickedButton) {
                                row = r;
                                col = c;
                                break;
                            }
                        }
                        if (row != -1) {
                            break;
                        }
                    }
                    setShipsType(satirlar, sutunlar, btn, row, col); //gemi turu belirleme

                });

                jPanel1.add(btn);
            }
        }

        btnStart.setSize(750, 750);
        btnStart.setText("READY");
        btnStart.setEnabled(false);

        btnStart.addActionListener(e -> {//gemilerin hepsi saklandiginda enabled olur ve ok mesaji gonderir

            try {
                String msg = "OK:" + this.client.myId + "," + this.client.matchid;
                this.client.SendMsg(msg);
            } catch (IOException ex) {
                Logger.getLogger(HideShip.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        jPanel3.add(btnStart);

        // Panelleri frame'e yerleştir
        this.add(jPanel1, BorderLayout.CENTER);
        this.add(jPanel2, BorderLayout.EAST);
        this.add(jPanel3, BorderLayout.PAGE_END);
        this.add(jPanel4, BorderLayout.WEST);

        this.setSize(800, 700);
        this.setVisible(true);

    }

    public void restart() throws IOException {//oyun tekrar baslamasi icin gecmis silme
        this.jPanel4.setVisible(true);
        this.jPanel1.setVisible(false);
        this.jPanel2.setVisible(false);
        this.jPanel3.setVisible(false);

        this.amiralButtons.clear();
        this.kruvazorButtons.clear();
        this.muhripButtons.clear();
        this.denizaltiButtons.clear();

        this.amiralCoordinats.clear();
        this.kruvazorCoordinats.clear();
        this.muhripCoordinats.clear();
        this.denizaltiCoordinats.clear();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                buttons[i][j].setBackground(Color.WHITE);
            }
        }

    }

    public void setShipsButton(char[] satirlar, String[] sutunlar, JButton btn, ArrayList ship, int length, int row, int col, Color color, String shipName, ArrayList<String> coordinats) {
        //butonun dikey yatay ve uzunluga gore secim yapilmasi saglanir
        int m = ship.size();

        String coordinat = satirlar[row] + String.valueOf(col + 1);

        if (!selectedCoordinats.contains(coordinat)) {//ust uste gelmemesi icin kontrol
            if (ship.size() != length) {
                if (m == 0) {//daha once hic secilmemisse
                    selectedRow.add(satirlar[row]);
                    selectedCol.add(String.valueOf(col + 1));
                    selectedCoordinats.add(coordinat);//tum secilen koordinatlar tutulur ve ust uste gelmesini engeller
                    coordinats.add(satirlar[row] + String.valueOf(col + 1));//gonderilen gemi turunun koordinatlarinin tutldugu yer
                    ship.add(btn);//gonderilen gemi arrayliste buton ekleme
                    btn.setBackground(color);
                } else if (m == 1) {//onceki secilen koordinata gore dikey yatay secme

                    if ((selectedRow.get(m - 1) == satirlar[row])) {

                        if (Integer.valueOf(selectedCol.get(m - 1)) - 1 == col + 1 || Integer.valueOf(selectedCol.get(m - 1)) + 1 == col + 1) {
                            selectedRow.add(satirlar[row]);
                            selectedCol.add(String.valueOf(col + 1));
                            selectedCoordinats.add(coordinat);
                            coordinats.add(satirlar[row] + String.valueOf(col + 1));
                            ship.add(btn);
                            btn.setBackground(color);
                        } else {
                            System.out.println("Button should be near the past selected!");
                        }

                    } else if ((selectedCol.get(m - 1).equals(sutunlar[col + 1]))) {
                        if (row != 9 && row != 0) {
                            if (selectedRow.get(m - 1).equals(satirlar[row - 1]) || selectedRow.get(m - 1).equals(satirlar[row + 1])) {
                                selectedCol.add(String.valueOf(col + 1));
                                selectedRow.add(satirlar[row]);
                                selectedCoordinats.add(coordinat);
                                coordinats.add(satirlar[row] + String.valueOf(col + 1));
                                ship.add(btn);
                                btn.setBackground(color);
                            } else {
                                System.out.println("Button should be near the past selected!");
                            }
                        }

                    }
                } else {

                    if (selectedRow.get(m - 2).equals(selectedRow.get(m - 1)) && selectedRow.get(m - 1).equals(satirlar[row])) {
                        if (Integer.valueOf(selectedCol.get(m - 2)) - 1 == col + 1 || Integer.valueOf(selectedCol.get(m - 2)) + 1 == col + 1 || Integer.valueOf(selectedCol.get(m - 1)) - 1 == col + 1 || Integer.valueOf(selectedCol.get(m - 1)) + 1 == col + 1) {
                            selectedRow.add(satirlar[row]);
                            selectedCol.add(String.valueOf(col + 1));
                            selectedCoordinats.add(coordinat);
                            coordinats.add(satirlar[row] + String.valueOf(col + 1));
                            ship.add(btn);
                            btn.setBackground(color);
                        } else {
                            System.out.println("Button should be near the past selected!");
                        }

                    } else if (selectedCol.get(m - 2).equals(selectedCol.get(m - 1)) && selectedCol.get(m - 1).equals(sutunlar[col + 1])) {
                        if (row != 9 && row != 0) {
                            if (selectedRow.get(m - 2).equals(satirlar[row + 1]) || selectedRow.get(m - 2).equals(satirlar[row - 1]) || selectedRow.get(m - 1).equals(satirlar[row + 1]) || selectedRow.get(m - 1).equals(satirlar[row - 1])) {
                                selectedCol.add(String.valueOf(col + 1));
                                selectedRow.add(satirlar[row]);
                                selectedCoordinats.add(coordinat);
                                coordinats.add(satirlar[row] + String.valueOf(col + 1));
                                ship.add(btn);
                                btn.setBackground(color);
                            } else {
                                System.out.println("Button should be near the past selected!");
                            }
                        } else {
                            if (selectedRow.get(m - 2).equals(satirlar[row - 1]) || selectedRow.get(m - 1).equals(satirlar[row - 1])) {
                                selectedCol.add(String.valueOf(col + 1));
                                selectedRow.add(satirlar[row]);
                                selectedCoordinats.add(coordinat);
                                coordinats.add(satirlar[row] + String.valueOf(col + 1));
                                ship.add(btn);
                                btn.setBackground(color);
                            } else {
                                System.out.println("Button should be near the past selected!");
                            }
                        }

                    }
                }
            }
        }else{
            System.out.println("This coordinat is settled for another ship!");
        }

        if (ship.size() == length) {
            System.out.println("Ship " + shipName + " is settled!");
            selectedRow.clear();
            selectedCol.clear();
            count++;
            if (count == 4) {
                btnStart.setEnabled(true);
            }
        }
    }

    public void setShipsType(char[] satirlar, String[] sutunlar, JButton btn, int row, int col) {//secilen ture gore uzunluk bilgisi ile arraylistle gonderilir
        String selectedShip = cBoxShips.getSelectedItem().toString();

        int uzunluk = 1;

        switch (selectedShip) {
            case "Amiral":
                uzunluk = 4;
                setShipsButton(satirlar, sutunlar, btn, amiralButtons, uzunluk, row, col, Color.BLUE, "Amiral", amiralCoordinats);
                break;
            case "Kruvazör":
                uzunluk = 3;
                setShipsButton(satirlar, sutunlar, btn, kruvazorButtons, uzunluk, row, col, Color.GREEN, "Kruvazör", kruvazorCoordinats);
                break;
            case "Muhrip":
                uzunluk = 2;
                setShipsButton(satirlar, sutunlar, btn, muhripButtons, uzunluk, row, col, Color.RED, "Muhrip", muhripCoordinats);
                break;
            case "Denizaltı":
                uzunluk = 1;
                setShipsButton(satirlar, sutunlar, btn, denizaltiButtons, uzunluk, row, col, Color.PINK, "Denizaltı", denizaltiCoordinats);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1408, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 863, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleName("jframe");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HideShip.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HideShip.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HideShip.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HideShip.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new HideShip().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(HideShip.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
