package juego_coup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CoupClient {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Game localGame;
    private int myPlayerNum = -1;
    private final Scanner scanner;

    public CoupClient() {
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        CoupClient client = new CoupClient();
        client.start();
    }

    public void start() {
        try {
            this.socket = new Socket("127.0.0.1", 12345);
            System.out.println("Conectado al servidor Coup en 127.0.0.1:12345");
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.in = new ObjectInputStream(this.socket.getInputStream());
            this.listenToServer();
        } catch (IOException var5) {
            System.err.println("Error de conexión: Asegúrate de que CoupServer esté ejecutándose.");
            System.err.println(var5.getMessage());
        } finally {
            this.closeConnection();
        }

    }

    private void listenToServer() throws IOException {
        while (true) {
            try {
                if (this.socket.isConnected()) {
                    Object obj = this.in.readObject();
                    if (obj instanceof Command) {
                        this.processServerCommand((Command) obj);
                    }
                    continue;
                }
            } catch (ClassNotFoundException var2) {
                System.err.println("Objeto recibido no reconocido: " + var2.getMessage());
            }

            return;
        }
    }

    private void processServerCommand(Command command) throws IOException, ClassNotFoundException {
        switch (command.getType()) {
            case 1:
                System.out.println(">>> [INFO] " + command.getMessage());
                break;
            case 2:
                Game receivedGame = (Game) this.in.readObject();
                this.updateGameState(receivedGame);
                break;
            case 3:
                System.out.println("\n*** JUEGO TERMINADO ***");
                PrintStream var10000 = System.out;
                String var10001 = this.localGame.getWinner() != null ? this.localGame.getWinner().getName() : "Desconocido";
                var10000.println("El ganador es: " + var10001);
                this.socket.close();
                break;
            case 4:
                System.out.println(">>> " + command.getMessage());
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 13:
            case 14:
            default:
                System.out.println("Comando desconocido: " + command.getType());
                break;
            case 10:
                this.handleActionPrompt();
                break;
            case 11:
            case 12:
                this.handleDecisionPrompt(command);
                break;
            case 15:
                this.handleInfluenceLossPrompt(command);
        }

    }

    private void closeConnection() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void updateGameState(Game receivedGame) {
        this.localGame = receivedGame;
        if (this.myPlayerNum == -1) {
            this.findMyPlayerNumber(receivedGame);
        }

        System.out.println("\n--- ESTADO ACTUAL DEL JUEGO (Turno: " + receivedGame.getCurrentPlayerIndex() + ") ---");
        Player[] var2 = this.localGame.getPlayers();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Player p = var2[var4];
            String marker = p.getPlayerNum() == this.localGame.getCurrentPlayerIndex() ? " [TURNO]" : "";
            System.out.printf("[%d] %s (Fichas: %d, Influencias: %d)%s\n", p.getPlayerNum(), p.getName(), p.getTokens(), p.getNumInfluence(), marker);
            if (p.getPlayerNum() == this.myPlayerNum) {
                System.out.print("   Tus cartas: ");
                PrintStream var10000 = System.out;
                String var10001 = String.valueOf(p.getInfluence1());
                var10000.println("[" + var10001 + "], [" + String.valueOf(p.getInfluence2()) + "]");
            }
        }

        System.out.println("-----------------------------------------------------------------");
    }

    private void findMyPlayerNumber(Game receivedGame) {
        if (this.myPlayerNum == -1) {
            for (int i = 0; i < receivedGame.getPlayers().length; ++i) {
                if (receivedGame.getPlayers()[i].getName().contains("Player")) {
                    this.myPlayerNum = i;
                    System.out.println("Asignado PlayerNum: " + this.myPlayerNum);
                    break;
                }
            }
        }
    }

    private void handleActionPrompt() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void handleDecisionPrompt(Command command) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void handleInfluenceLossPrompt(Command command) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
