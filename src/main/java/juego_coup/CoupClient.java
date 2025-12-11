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
        System.out.println("\n>>> ¡ES TU TURNO DE ACTUAR! <<<");
        System.out.println("Opciones de Acción:");
        System.out.println(" 1. Ingreso (Income)          | 2. Ayuda Extranjera (Foreign Aid)");
        System.out.println(" 3. Coup (7 fichas)           | 4. Duque (Tax)");
        System.out.println(" 5. Asesino (Assassinate, 3) | 6. Capitán (Steal)");
        System.out.println(" 7. Embajador (Exchange)     |");
        int actionCode = this.getUserInput("Elige acción (1-7): ", 1, 7);
        int targetPlayerNum = -1;
        // Nota: Los códigos de acción 103, 105, 106 corresponden a Coup, Assassinate, Steal
        if (actionCode == 103 || actionCode == 105 || actionCode == 106) {
            System.out.println("Jugadores disponibles: " + this.localGame.getAvailablePlayersList(this.myPlayerNum));
            targetPlayerNum = this.getUserInput("Introduce el número de jugador objetivo: ", 0, this.localGame.getPlayers().length - 1);
            Player me = this.localGame.getPlayers()[this.myPlayerNum];
            if (actionCode == 103 && me.getTokens() < 7) {
                System.out.println("¡Advertencia! Necesitas 7 fichas para Coup.");
            }

            if (actionCode == 105 && me.getTokens() < 3) {
                System.out.println("¡Advertencia! Necesitas 3 fichas para Asesinar.");
            }
        }

        Command actionCommand = new Command(20, this.myPlayerNum, actionCode, targetPlayerNum);
        this.sendToServer(actionCommand);
    }

    private void handleDecisionPrompt(Command prompt) {
        String decisionType = prompt.getType() == 11 ? "DESAFIAR" : "BLOQUEAR";
        // Nota: Asume que la clase ActionsPrinter existe
        String actionName = ActionsPrinter.actionToString(prompt.getActionCode());
        System.out.println("\n>>> Tienes un tiempo limitado para responder. <<<");
        System.out.printf("Jugador %d realizó %s. ¿Deseas %s?\n", prompt.getSenderPlayerNum(), actionName, decisionType);
        System.out.print("Introduce '1' para " + decisionType + " o '0' para PASAR: ");
        int choice = this.getUserInput("", 0, 1);
        Command response;
        if (choice == 1) {
            int responseType = prompt.getType() == 11 ? 21 : 22;
            response = new Command(responseType, this.myPlayerNum, prompt.getActionCode(), prompt.getTargetPlayerNum());
            System.out.println("Tu respuesta: " + decisionType);
        } else {
            response = new Command(23, this.myPlayerNum, 0, -1);
            System.out.println("Tu respuesta: PASAR");
        }

        this.sendToServer(response);
    }

    private void handleInfluenceLossPrompt(Command prompt) {
        Player me = this.localGame.getPlayers()[this.myPlayerNum];
        System.out.println("\n*** ¡PERDISTE UNA INFLUENCIA! ***");
        System.out.println("Elige qué carta revelar y perder:");
        System.out.printf(" 1. %s\n 2. %s\n", me.getInfluence1(), me.getInfluence2());
        int choice = this.getUserInput("Elige carta (1 o 2): ", 1, 2);
        Command response = new Command(26, this.myPlayerNum, choice);
        this.sendToServer(response);
    }

    private void sendToServer(Command actionCommand) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private int getUserInput(String introduce_el_número_de_jugador_objetivo_, int i, int i0) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
