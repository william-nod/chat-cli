package com.gummy.corp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {

    // Arraylist untuk menyimpan client aktif dan int untuk id
    static ArrayList<ClientHandler> ar = new ArrayList<>();
    static int idx = 0;

    public static void main(String[] args) throws IOException {
        // Inisialisasi serversocket di port 3000 dan socket
        ServerSocket ss = new ServerSocket(3000);
        Socket s;
        System.out.println("Server Chat-CLI berjalan di port 3000");
        System.out.println("Chat Log dimulai :");
        System.out.println("Format: id | nama | pesan | room / private");

        // Melakukan perulangan untuk menangkap hubungan dengan client
        while (true) {
            s = ss.accept();
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            // Membuat client baru dan diinputkan ke dalam arraylist
            String username = in.readUTF();
            System.out.println("Client baru diterima : "+ idx + ", " + username);
            ClientHandler mtch = new ClientHandler(s, idx, username, in, out);
            Thread t = new Thread(mtch);
            ar.add(mtch);

            // Menjalankan Thread per client dan increment i untuk id client
            t.start();
            idx++;
        }
    }
}



