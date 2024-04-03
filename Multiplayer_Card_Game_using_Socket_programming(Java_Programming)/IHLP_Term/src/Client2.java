import java.io.*;
import java.net.*;
import java.util.*;

public class Client2 {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 4000;
    private static final String[] CARD_VALUES = {"ace","2","3","4","5","6","7","8","9","10","jack","queen","king"};
    private static String pick = "";
    private static Set<Integer> generatedNumbers = new HashSet<>();

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("client2 Connected to server");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
                if (!message.contains("Result")) {
                    pick = CARD_VALUES[randomNumber(generatedNumbers)];
                    System.out.println("client2 Picked " + pick);
                    out.println(pick);    
                }
            }
                
            
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    private static int randomNumber(Set<Integer> generatedNumbers) {
        Random random = new Random();
        int randomNumber;

        do {
            randomNumber = random.nextInt(13);
        } while (!generatedNumbers.add(randomNumber));

        return randomNumber;
    }
}
