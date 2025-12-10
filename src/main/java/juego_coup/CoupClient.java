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
    while(true) {
        try {
            if (this.socket.isConnected()) {
                Object obj = this.in.readObject();
                if (obj instanceof Command) {
                    this.processServerCommand((Command)obj);
                }
                continue;
            }
        } catch (ClassNotFoundException var2) {
            System.err.println("Objeto recibido no reconocido: " + var2.getMessage());
        }

        return;
    }
}

    private void processServerCommand(Command command) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void closeConnection() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}