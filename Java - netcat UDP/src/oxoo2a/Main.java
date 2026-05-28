package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main {

    private static final int PACKET_SIZE = 4096;

    // **********************************************************************
    // MAIN
    // **********************************************************************
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Usage: java Main <listening-port>");
            System.exit(-1);
        }

        int myPort = Integer.parseInt(args[0]);

        DatagramSocket socket = new DatagramSocket(myPort);

        System.out.println("UDP Chat gestartet auf Port " + myPort);

        // Thread zum Empfangen starten
        Receiver receiver = new Receiver(socket);
        receiver.start();

        // Hauptthread zum Senden
        BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));

        while (true) {

            String input = br.readLine();

            if (input == null)
                continue;

            if (input.equalsIgnoreCase("stop")) {
                socket.close();
                System.exit(0);
            }

            /*
             * Erwartetes Format:
             * send <IP> <PORT> <Nachricht>
             */

            if (input.startsWith("send ")) {

                String[] parts = input.split(" ", 4);

                if (parts.length < 4) {
                    System.out.println(
                            "Format: send <IP> <PORT> <Nachricht>");
                    continue;
                }

                String ip = parts[1];
                int port = Integer.parseInt(parts[2]);
                String message = parts[3];

                byte[] buffer = message.getBytes("UTF-8");

                InetAddress address = InetAddress.getByName(ip);

                DatagramPacket packet =
                        new DatagramPacket(
                                buffer,
                                buffer.length,
                                address,
                                port);

                socket.send(packet);

                System.out.println(
                        "Nachricht gesendet an "
                                + ip + ":" + port);
            }
            else {
                System.out.println(
                        "Ungültiger Befehl!");
                System.out.println(
                        "Benutze: send <IP> <PORT> <Nachricht>");
            }
        }
    }

    // **********************************************************************
    // RECEIVER THREAD
    // **********************************************************************
    static class Receiver extends Thread {

        private DatagramSocket socket;

        public Receiver(DatagramSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            byte[] buffer = new byte[PACKET_SIZE];

            while (true) {

                try {

                    DatagramPacket packet =
                            new DatagramPacket(
                                    buffer,
                                    buffer.length);

                    socket.receive(packet);

                    String message =
                            new String(
                                    packet.getData(),
                                    0,
                                    packet.getLength(),
                                    "UTF-8");

                    System.out.println(
                            "\nEmpfangen von "
                                    + packet.getAddress().getHostAddress()
                                    + ":"
                                    + packet.getPort()
                                    + " -> "
                                    + message);

                } catch (IOException e) {

                    System.out.println("Socket geschlossen.");
                    break;
                }
            }
        }
    }
}