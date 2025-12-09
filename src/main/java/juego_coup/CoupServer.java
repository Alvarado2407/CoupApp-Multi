package juego_coup;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CoupServer {

    private static final int PORT = 12345;
    private static final int MAX_PLAYERS = 4;
    private final List<ClientHandler> handlers = new ArrayList();
    private Game game = new Game();
    private int playerCounter = 0;
    private Command currentAction = null;
    private final List<Command> phaseResponses = new ArrayList();
    private final Object phaseLock = new Object();

    public CoupServer() {
    }

    public static void main(String[] args) {
        CoupServer server = new CoupServer();
        server.start();
    }

// Agrega el método start() a CoupServer.java
    public void start() {
        System.out.println("Coup Server is running on port 12345");

        try {
            ServerSocket serverSocket = new ServerSocket(12345);

            try {
                while (this.playerCounter < 4) {
                    System.out.println("Esperando al jugador #" + (this.playerCounter + 1) + "...");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado desde: " + String.valueOf(clientSocket.getInetAddress()));
                    // Nota: Asume que las clases Player, ClientHandler y Command existen.
                    Player newPlayer = new Player("Player " + (this.playerCounter + 1), this.playerCounter);
                    ClientHandler handler = new ClientHandler(clientSocket, this, newPlayer);
                    this.handlers.add(handler);
                    ++this.playerCounter;
                    (new Thread(handler)).start();
                }

                System.out.println("¡Máximo de jugadores alcanzado! Iniciando la partida...");
                this.initializeGame();
            } catch (Throwable var6) {
                try {
                    serverSocket.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }

                throw var6;
            }

            serverSocket.close();
        } catch (IOException var7) {
            System.err.println("Error en el servidor: " + var7.getMessage());
            var7.printStackTrace();
        }
    }
}
