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

    public void sendToPlayer(int targetPlayerNum, Command command) {
        if (targetPlayerNum >= 0 && targetPlayerNum < this.handlers.size()) {
            ((ClientHandler) this.handlers.get(targetPlayerNum)).send(command);
        }

    }

    public void broadcast(Command command) {
        Iterator var2 = this.handlers.iterator();

        while (var2.hasNext()) {
            ClientHandler handler = (ClientHandler) var2.next();
            handler.send(command);
        }

    }

    private void initializeGame() {
        List<Player> joinedPlayers = new ArrayList();
        Iterator var2 = this.handlers.iterator();

        while (var2.hasNext()) {
            ClientHandler handler = (ClientHandler) var2.next();
            joinedPlayers.add(handler.getPlayer());
        }

        this.game.setPlayersAtStart(joinedPlayers);
        this.broadcast(new Command(1, "¡El juego ha comenzado! Distribuyendo influencias."));
        this.broadcastGameState();
        ((ClientHandler) this.handlers.get(this.game.getCurrentPlayerIndex())).notifyTurnStart();
    }

    public synchronized void broadcastGameState() {
        Iterator var1 = this.handlers.iterator();

        while (var1.hasNext()) {
            ClientHandler handler = (ClientHandler) var1.next();
            handler.sendGameState(this.game);
        }

    }

    public synchronized Game getGame() {
        return this.game;
    }

    public List<ClientHandler> getHandlers() {
        return this.handlers;
    }

    public Object getPhaseLock() {
        return this.phaseLock;
    }

    public void setCurrentAction(Command action) {
        synchronized (this.phaseLock) {
            this.currentAction = action;
            this.phaseResponses.clear();
        }
    }

    public Command getCurrentAction() {
        synchronized (this.phaseLock) {
            return this.currentAction;
        }
    }

    public void clearCurrentAction() {
        synchronized (this.phaseLock) {
            this.currentAction = null;
        }
    }

    public void clearPhaseResponses() {
        synchronized (this.phaseLock) {
            this.phaseResponses.clear();
        }
    }

    public void registerPhaseResponse(Command response) {
        synchronized (this.phaseLock) {
            if (this.currentAction != null) {
                this.phaseResponses.add(response);
                this.phaseLock.notifyAll();
                System.out.println("Respuesta de fase registrada. Notificando hilo de fase.");
            }

        }
    }

    public List<Command> getPhaseResponses() {
        synchronized (this.phaseLock) {
            return new ArrayList(this.phaseResponses);
        }
    }
}
