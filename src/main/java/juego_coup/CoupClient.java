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
}