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
}