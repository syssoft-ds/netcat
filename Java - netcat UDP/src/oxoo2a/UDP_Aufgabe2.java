package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UDP_Aufgabe2 {

    private static class InstanceInfo {
        String ip;
        int port;

        InstanceInfo(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }

    private static Map<String, InstanceInfo> instances = new HashMap<>();

    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port>\"");
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l"))
            listenAndTalk(port);
        else
            connectAndTalk(args[0],port);
    }

    private static final int packetSize = 4096;

    // ************************************************************************
    // listenAndTalk
    // ************************************************************************
    private static void registerInstance(String name, String ip, int port) {
        System.out.println("Registering instance: " + name + ", " + ip + ", " + port);
        instances.put(name, new InstanceInfo(ip, port));
    }

    private static void sendMessage(String name, String message) throws IOException {
        InstanceInfo info = instances.get(name);
        if (info == null) {
            System.out.println("Unknown instance: " + name);
            return;
        }

        InetAddress other_address = InetAddress.getByName(info.ip);
        DatagramSocket s = new DatagramSocket();
        byte[] buffer = message.getBytes("UTF-8");
        DatagramPacket p = new DatagramPacket(buffer, buffer.length, other_address, info.port);
        s.send(p);
        s.close();
    }

    private static void listenAndTalk(int port) throws IOException {
        DatagramSocket s = new DatagramSocket(port);
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            DatagramPacket p = new DatagramPacket(buffer, buffer.length);
            s.receive(p);
            line = new String(buffer, 0, p.getLength(), "UTF-8");
            System.out.println(line);
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    private static void connectAndTalk(String other_host, int other_port) throws IOException {
        InetAddress other_address = InetAddress.getByName(other_host);
        DatagramSocket s = new DatagramSocket();
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            line = readString();
            if (line.startsWith("register ")) {
                String[] parts = line.split(" ");
                if (parts.length != 4) {
                    System.out.println("Invalid register command");
                } else {
                    registerInstance(parts[1], parts[2], Integer.parseInt(parts[3]));
                }
            } else if (line.startsWith("send ")) {
                String[] parts = line.split(" ", 3);
                if (parts.length != 3) {
                    System.out.println("Invalid send command");
                } else {
                    sendMessage(parts[1], parts[2]);
                }
            } else {
                buffer = line.getBytes("UTF-8");
                DatagramPacket p = new DatagramPacket(buffer, buffer.length, other_address, other_port);
                s.send(p);
            }
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    private static String readString () {
        BufferedReader br = null;
        boolean again = false;
        String input = null;
        do {
            // System.out.print("Input: ");
            try {
                if (br == null)
                    br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
            }
            catch (Exception e) {
                System.out.printf("Exception: %s\n",e.getMessage());
                again = true;
            }
        } while (again);
        return input;
    }

    private BufferedReader br = null;
}
