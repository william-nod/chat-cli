package com.gummy.corp;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {
    // Inisialisasi ip dan port
    public static final int port = 3000;
    public static final String ip = "127.0.0.1";

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        // Membuat socket agar terhubung dengan server lokal di port 3000
        Socket socket = new Socket(ip, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

//        String opening = "Welcome to chat-cli - anda terhubung dengan client";
//        out.println(opening);
//        out.flush();
//        String opener = in.readLine();
//        System.out.println(opener);

        // Multithread objek untuk mengirim dan menerima pesan
        Thread kirim = new Thread(() -> {
            while (true) {
                String pesan = sc.nextLine();
                try {
                    out.writeUTF(pesan);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread terima = new Thread(() -> {
            try {
                String pesan = in.readUTF();
                System.out.println(pesan);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Menjalankan Thread
        kirim.start();
        terima.start();
    }
}
