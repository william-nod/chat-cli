package com.gummy.corp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

class ClientHandler implements Runnable {
    private String name;
    private String room;
    private String connected;
    private int id;
    final DataInputStream in;
    final DataOutputStream out;
    Socket s;

    public ClientHandler(Socket s, int id, String name,
                         DataInputStream dis, DataOutputStream dos) {
        this.id = id;
        this.in = dis;
        this.out = dos;
        this.name = name;
        this.s = s;
        this.room = "";
        this.connected = "";
    }

    @Override
    public void run() {

        String received;
        while (true) {
            try {
                // Menerima string dari client dan melakukan print log di server
                received = in.readUTF();
                String connect = (this.room.isBlank() && this.connected.isBlank())
                        ? ""
                        : !this.room.isBlank()
                        ? this.room
                        : this.connected;
                System.out.println(this.id + " | " + this.name + " | " + received + " | " + connect);

                // Handle client ketika logout aplikasi
                if (received.equals(Command.commandKeluar)) {
                    this.out.writeUTF("Close Connection");
                    for (ClientHandler client : Main.ar) {
                        if (client.id == this.id) {
                            client = null;
                        }
                    }
                    this.s.close();
                    break;
                }

                // Menapilkan command - command yang tersedia pada aplikasi
                else if (received.equals(Command.commandHint)) {
                    String command = """
                            1. #chgUname - Merubah username
                            2. #listRoom - Menampilkan daftar room yang tersedia
                            3. #listUser - Menampilkan daftar peserta di lobby atau room
                            4. #private - Chat private dengan username tujuan
                            5. #joinRoom - Masuk ke dalam chatroom
                            6. #leave - Keluar chatroom atau keluar DM
                            7. #hint - Memberikan command aplikasi
                            8. #logout - Keluar dari aplikasi""";
                    this.out.writeUTF(command);
                }

                // Fitur untuk merubah nama client
                else if (received.equals(Command.commandChngName)) {
                    this.out.writeUTF("Masukkan username baru : ");
                    String recievedUname = in.readUTF();
                    this.name = recievedUname;
                }

                // Fitur untuk melihat room yang sedang online
                else if (received.equals(Command.commandListRoom)) {
                    HashSet<String> roomName = new HashSet<>();
                    for (ClientHandler mc : Main.ar) {
                        if (!mc.room.isBlank()) {
                            roomName.add(mc.room);
                        }
                    }
                    for (String i : roomName) {
                       this.out.writeUTF(i);
                    }
                }

                // Fitur untuk melihat user dalam satu room atau lobby
                else if (received.equals(Command.commandListUser)) {
                    String position = this.room.isBlank() ? "Lobby" : ("Room " + this.room);
                    this.out.writeUTF("Peserta " + position);
                    for (ClientHandler mc : Main.ar) {
                        if (mc.room.equals(this.room)) {
                            this.out.writeUTF(mc.name);
                        }
                    }
                }

                // Menghandle client ketika ingin memasuki chat room
                else if (received.equals(Command.commandRoom)) {
                    if (this.connected != "" || this.room != "") {
                        this.out.writeUTF("Anda sedang berada di private chat atau room!");
                    } else {
                        this.out.writeUTF("Masukkan kode chat room : ");
                        String recievedRoom = in.readUTF();
                        this.room = recievedRoom;
                        for (ClientHandler mc : Main.ar) {
                            if (mc.room.equals(this.room)) {
                                mc.out.writeUTF("SERVER : " + this.name + " telah bergabung");
                            }
                        }
                    }
                }

                // Menghandle client ketika ingin berkomunikasi secara private
                else if (received.equals(Command.commandPrivate)) {
                    boolean ada = false;
                    if (this.connected != "") {
                        this.out.writeUTF("Anda sedang berada di private chat!");
                    } else {
                        this.out.writeUTF("Masukkan username tujuan : ");
                        String recievedUname = in.readUTF();

                        for (ClientHandler mc : Main.ar) {
                            if (mc.name.equals(recievedUname) && mc.connected == "") {
                                this.connected = recievedUname;
                                this.out.writeUTF("SERVER : Anda terhubung dengan " + this.connected);
                                mc.connected = this.name;
                                ada = true;
                                mc.out.writeUTF("SERVER : Anda terhubung dengan " + this.name);
                            }
                        }

                        if (!ada) {
                            this.out.writeUTF("SERVER : Client " + recievedUname + " tidak ditemukan / sedang dalam private dengan client lain" );
                        }
                    }
                }

                // Fitur untuk keluar dari private atau chat room
                else if (received.equals(Command.commandLeave)) {
                    this.out.writeUTF("Anda keluar dari room atau chat");
                    if (!this.room.isBlank()) {
                        for (ClientHandler mc : Main.ar) {
                            if (mc.room.equals(this.room) && !mc.name.equals(this.name)) {
                                mc.out.writeUTF("SERVER : " + this.name + " telah keluar");
                            }
                        }
                        this.room = "";
                    } else if (!this.connected.isBlank()) {
                        for (ClientHandler mc : Main.ar) {
                            if (mc.connected.equals(this.name) && !mc.name.equals(this.name)) {
                                mc.out.writeUTF("SERVER : " + this.name + " telah keluar");
                                mc.connected = "";
                            }
                        }
                        this.connected = "";
                    }
                }

                // Manghandle pengiriman pesan dari client ke client lain dalam chat room yang sama
                else if (!room.isBlank()) {
                    for (ClientHandler mc : Main.ar) {
                        if (mc.room.equals(this.room) && !mc.name.equals(this.name)) {
                            mc.out.writeUTF(this.name + " : " + received);
                        }
                    }
                }

                // Manghandle pengiriman pesan dari client ke client lain dalam private chat
                else if (!connected.isBlank()) {
                    for (ClientHandler mc : Main.ar) {
                        if (mc.connected.equals(this.name)) {
                            mc.out.writeUTF(this.name+ " : " + received);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Melakukan penutupan input dan output stream pada client
        try {
            this.in.close();
            this.out.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}


