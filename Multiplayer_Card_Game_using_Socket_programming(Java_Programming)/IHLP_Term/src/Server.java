import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    private static final String[] CARD_VALUES = {"ace","2","3","4","5","6","7","8","9","10","jack","queen","king"};
    private static final int NUM_CARDS = CARD_VALUES.length;

    private static List<String> cardList = new ArrayList<>(Arrays.asList(CARD_VALUES));
    private static Set<Integer> generatedNumbers = new HashSet<>();
    private static Map<Socket, String> clientPicks = new HashMap<>();
    private static int numCardsPicked = 0;
    private static Map<Socket, Integer> totalScores = new HashMap<>();
    private static Map<Integer, String> clientNames = new HashMap<>();

    public static void main(String[] args) throws IOException {
        int portNumber = 4000;
        int round = 1;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server started on port " + portNumber);

            Socket[] clientSockets = new Socket[3];
            for(int i=0; i<3; i++) {
                clientSockets[i] = serverSocket.accept();
                System.out.println("Client " + (i+1) + " connected: " + clientSockets[i]);
                int c = i+1;
                clientNames.put(clientSockets[i].getPort(), "Client" + String.valueOf(c));
            }
            while(numCardsPicked < NUM_CARDS) {
                startRound(round, clientSockets, numCardsPicked, totalScores, generatedNumbers);
                round++;
                numCardsPicked++;
            }
            int max = 0;
            Socket won = new Socket();
            for (Map.Entry<Socket, Integer> win : totalScores.entrySet()) {
                if (win.getValue() > max) {
                    max = win.getValue();
                    won = win.getKey();
                }
            }
            String winnerClient = clientNames.get(won.getPort());
            System.out.println("Final Result: The winner is " + winnerClient + ". Score: " + max);

            String winnerMessage = "Final Result: The winner is ";
            for (Map.Entry<Socket, Integer> client : totalScores.entrySet()) {
                PrintWriter out = new PrintWriter(client.getKey().getOutputStream(), true);
                out.println("Final Result: The winner is " + winnerClient);
            }

        }
        catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(1);
        }
    }

    private static void startRound(int round, Socket[] clientSockets, int numCardsPicked, Map<Socket, Integer> totalScores, Set<Integer> generatedNumbers) throws IOException {
        int selectedCard = randomNumber(generatedNumbers);
        System.out.println("Round " + round + ": The server selected card is " + selectedCard);

        // Map to keep track of client picks and scores
        Map<Socket, String> clientPicks = new HashMap<>();
        Map<Socket, Integer> clientScores = new HashMap<>();
        List<Socket> currentWinners = new ArrayList<>();

        for(Socket clientSocket : clientSockets) {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Round " + round + ": The server selected card is " + selectedCard + ". Please pick a card.");
            String pick = in.readLine();
            clientPicks.put(clientSocket, pick);
            int score = getCardScore(pick);
            clientScores.put(clientSocket, score);
        }

        // Print out each client's score
        System.out.println("Round " + round + " scores:");
        int maxScore = -1;
        for(Socket clientSocket : clientSockets) {
            int score = clientScores.get(clientSocket);
            System.out.println(clientNames.get(clientSocket.getPort()) + " score: " + score);
            if(score > maxScore) {
                maxScore = score;
            }
        }

        for(Socket clientSocket : clientSockets) {
            int score = clientScores.get(clientSocket);
            if(score == maxScore) {
                currentWinners.add(clientSocket);
                System.out.println("This round winner is: " + clientNames.get(clientSocket.getPort()));
            }
            if (totalScores.containsKey(clientSocket)) {
                totalScores.put(clientSocket, totalScores.get(clientSocket) + score);
            } else {
                totalScores.put(clientSocket, score);
            }
        }
        String winnerMessage = "Round " + round + ": The winners are ";
        for(Socket clientSocket : clientSockets) {
        	PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            if(currentWinners.contains(clientSocket)) {
               out.println("Result: "+ winnerMessage + "you! Your pick was " + clientPicks.get(clientSocket));
            } else {
                out.println("Result: you are not the winner");
            }
        }
    }
    private static int getCardScore(String cardValue) {
        switch(cardValue) {
            case "ace":
                return 1;
            case "jack":
                return 11;
            case "queen":
                return 12;
            case "king":
                return 13;
            default:
                return Integer.parseInt(cardValue);
        }
    }
    private static int randomNumber(Set<Integer> generatedNumbers) {
        Random random = new Random();
        int randomNumber;

        do {
            randomNumber = random.nextInt(13) + 1;
        } while (!generatedNumbers.add(randomNumber));

        return randomNumber;
    }
}