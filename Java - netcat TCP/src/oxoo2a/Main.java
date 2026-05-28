package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Main {

    // **********************************************************************
    // MAIN
    // **********************************************************************
    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println(
                    "Usage: java Main <server-ip> <server-port>");
            System.exit(-1);
        }

        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);

        startClient(serverHost, serverPort);
    }

    // **********************************************************************
    // CLIENT
    // **********************************************************************
    private static void startClient(
            String serverHost,
            int serverPort) throws IOException {

        InetAddress serverAddress =
                InetAddress.getByName(serverHost);

        Socket socket =
                new Socket(serverAddress, serverPort);

        System.out.println(
                "Verbunden mit Server "
                        + serverHost
                        + ":"
                        + serverPort);

        // Zum Senden
        PrintWriter writer =
                new PrintWriter(
                        socket.getOutputStream(),
                        true);

        // Zum Empfangen
        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));

        // Thread für Empfang starten
        Receiver receiver =
                new Receiver(reader);

        receiver.start();

        // Tastatureingaben lesen
        BufferedReader keyboard =
                new BufferedReader(
                        new InputStreamReader(System.in));

        String line;

        while (true) {

            line = keyboard.readLine();

            if (line == null)
                continue;

            // Nachricht senden
            writer.println(line);

            // Verbindung beenden
            if (line.equalsIgnoreCase("stop")) {
                socket.close();
                System.exit(0);
            }
        }
    }

    // **********************************************************************
    // RECEIVER THREAD
    // **********************************************************************
    static class Receiver extends Thread {

        private BufferedReader reader;

        public Receiver(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {

            String line;

            try {

                while ((line = reader.readLine()) != null) {

                    System.out.println(
                            "\n[SERVER] " + line);
                }

            } catch (IOException e) {

                System.out.println(
                        "Verbindung zum Server beendet.");
            }
        }
    }
}