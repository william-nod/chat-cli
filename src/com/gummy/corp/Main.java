package com.gummy.corp;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    // Inisialisasi ip dan port
    public static final int port = 3000;
    public static final String ip = "127.0.0.1";
    public static Boolean isLoggedOut = false;

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        // Membuat socket agar terhubung dengan server lokal di port 3000
        Socket socket = new Socket(ip, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Opening Aplikasi
        String opening = "Welcome to chat-cli - anda akan terhubung dengan server";
        String unamePrompt = "Mohon masukkan username chat-cli : ";
        System.out.println(opening);
        System.out.println("Ketikkan #hint untuk melihat command");
        System.out.print(unamePrompt);
        String username = sc.nextLine();



        // Multithread objek untuk mengirim dan menerima pesan
        // Thread untuk mengirim pesan
        Thread kirim = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out.writeUTF(username);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (true) {
                    if (isLoggedOut) {
                        break;
                    }
                    String pesan = sc.nextLine();
                    try {
                        out.writeUTF(pesan);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Thread untuk menerima Pesan
        Thread terima = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        String msg = in.readUTF();
                        if (msg.equals("Close Connection")) {
                            System.out.println("Anda telah keluar aplikasi");
                            socket.close();
                            in.close();
                            out.close();
                            System.exit(0);
                            isLoggedOut = true;
                            break;
                        }
                        System.out.println(msg);
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }
            }
        });

        // Menjalankan Thread
        kirim.start();
        terima.start();
    }
}
