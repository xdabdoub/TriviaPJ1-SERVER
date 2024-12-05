package me.yhamarsheh.trivia.objects;

import me.yhamarsheh.trivia.enums.Operation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Game {

    // Initializations
    private final int PORT;
    private final DatagramSocket SOCKET;

    private ArrayList<Player> contestants;

    private final int MAX_ROUND = 3;
    private final int TIMER_IN_SECONDS = 90;
    private int CURRENT_ROUND = 0;

    private final int MIN_PLAYERS = 2;

    private Round currentRound;

    private final Map<Player, Integer> leaderboard;

    private boolean hasStarted;

    public Game(int hostPort) throws IOException {
        this.PORT = hostPort;
        this.SOCKET = new DatagramSocket(PORT);

        this.contestants = new ArrayList<>();
        this.leaderboard = new HashMap<>();
    }

    public void listenForConnections() throws IOException {
        while (true) {
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            SOCKET.receive(packet);

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(receiveBuffer, receiveBuffer.length, address, port);
            String received = new String(receiveBuffer, 0, packet.getLength(), StandardCharsets.UTF_8).trim();

            String[] data = received.split(";");
            Operation operation;
            try {
                operation = Operation.valueOf(data[0]);
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid Operation Type: " + data[0] + ". More: " + exception.getMessage());
                return;
            }

            if (hasStarted()) {
                // ANSWER LOGIC
                Player player = getPlayerByAddressAndPort(address, port);
                if (player == null) {
                    System.out.println("Is null");
                    continue;
                }

                switch (operation) {
                    case ANSWER -> {
                        String answer = data[1];
                        boolean correctness = currentRound.answer(player, answer);
                        System.out.println("Received an answer from " + player.toString() + ": " + answer + " - " + (correctness ? "Correct" : "Incorrect") + "!");
                        break;
                    }
                    case QUIT -> {
                        contestants.remove(player);
                        leaderboard.remove(player);

                        broadcast(player.getName() + " found the question extremely hard and has quit!");
                        broadcast("  Current number of players: " + contestants.size());
                        broadcast("BLANK");

                        System.out.println(player.getName() + " has left the game from ('" + address.getHostAddress() + "', " + port + ")");
                    }
                }
            } else {
                    switch (operation) {
                        case JOIN -> {
                            // JOIN;NAME

                            Player player = new Player(address, port, data[1].trim());
                            contestants.add(player);
                            leaderboard.put(player, 0);

                            if (contestants.size() == MIN_PLAYERS) {
                                // START TIMER LOGIC
                                broadcast(player.getName() + " has joined the game!");
                                broadcast("  Current number of players: " + contestants.size());
                                broadcast("BLANK");
                                System.out.println(player.getName() + " joined the game from ('" + address.getHostAddress() + "', " + port + ")");

                                start();
                            } else {
                                final String waitingForPlayers = "Waiting for the game to start...";
                                if (isWaitingForPlayers()) sendMessageToPlayer(player, waitingForPlayers);
                                sendMessageToPlayer(player, "BLANK");

                                broadcast(player.getName() + " has joined the game!");
                                broadcast("  Current number of players: " + contestants.size());
                                broadcast("BLANK");

                                System.out.println(player.getName() + " joined the game from ('" + address.getHostAddress() + "', " + port + ")");
                            }
                        }
                        case QUIT -> {
                            Player player = getPlayerByAddressAndPort(address, port);
                            if (player == null) {
                                System.out.println("Is null");
                                continue;
                            }

                            contestants.remove(player);
                            leaderboard.remove(player);

                            broadcast(player.getName() + " left the game!");
                            broadcast("  Current number of players: " + contestants.size());
                            broadcast("BLANK");

                            System.out.println(player.getName() + " has left the game from ('" + address.getHostAddress() + "', " + port + ")");
                        }
                    }
            }
        }
    }

    public void start() {
        final String STARTING = "Starting the Trivia Game Round #" + ++CURRENT_ROUND + " in " + TIMER_IN_SECONDS + " seconds!";
        broadcast(STARTING + " Get ready!");
        broadcast("BLANK");

        System.out.println(STARTING);
        try {
            Thread.sleep(TIMER_IN_SECONDS * 1000);
        } catch (InterruptedException e) {
            System.out.println("An error has occurred: " + e.getMessage());
            return;
        }

        if (contestants.size() < MIN_PLAYERS) {
            broadcast("There aren't enough players to begin the next round!");
            broadcast("BLANK");
            return;
        }

        currentRound = new Round(this);

        currentRound.startRound();
        hasStarted = true;
    }

    public void nextRound(Player previousRoundWinner) {
        boolean winnerAvailable = previousRoundWinner != null;

        if (winnerAvailable) leaderboard.replace(previousRoundWinner, leaderboard.get(previousRoundWinner) + 1);

        broadcast("GAME OVER!");

        ArrayList<Player> didntParticipate = currentRound.getPlayersWhoDidNotParticipate();
        for (Player player : didntParticipate) {
            contestants.remove(player);
            leaderboard.remove(player);

            sendMessageToPlayer(player, "BLANK");
            sendMessageToPlayer(player, "You were kicked from the game due to inactivity!");
        }

        if (!winnerAvailable) broadcast("Game has ended with no participants! :(");
        else {
            final String winnerMessage = "The winner is " + previousRoundWinner.getName() + " with " + leaderboard.get(previousRoundWinner) + " rounds! Congratulations!";
            System.out.println(winnerMessage);
            broadcast(winnerMessage);
            broadcast("BLANK");

            displayLeaderboard();
        }

        if (contestants.size() < MIN_PLAYERS) {
            broadcast("There aren't enough players to begin the next round!");
            broadcast("BLANK");
            hasStarted = false;
            return;
        }

        start();
    }

    public void broadcast(String message) {
        broadcastTo(contestants, message);
    }

    private void broadcastTo(ArrayList<Player> players, String message) {
        String finalMessage = String.format(Operation.MESSAGE.getFormat(), message);
        byte[] sendBuffer = finalMessage.getBytes(StandardCharsets.UTF_8);

        for (Player player : players) {
            try {
                SOCKET.send(new DatagramPacket(sendBuffer, sendBuffer.length, player.getIpAddress(), player.getPort()));
            } catch (IOException ex) {
                System.out.println("Couldn't BROADCAST the message '" + message + "' to the player '" + player.getName() + "' More:\n" + ex.getMessage());
            }
        }
    }

    public void sendMessageToPlayer(Player player, String message) {
        String finalMessage = String.format(Operation.MESSAGE.getFormat(), message);
        byte[] sendBuffer = finalMessage.getBytes(StandardCharsets.UTF_8);

        try {
            SOCKET.send(new DatagramPacket(sendBuffer, sendBuffer.length, player.getIpAddress(), player.getPort()));
        } catch (IOException ex) {
            System.out.println("Couldn't SEND the message '" + message + "' to the player '" + player.getName() + "' More:\n" + ex.getMessage());
        }
    }


    public void sendOperationTo(ArrayList<Player> players, String s) {
        byte[] sendBuffer = s.getBytes(StandardCharsets.UTF_8);

        for (Player player : players) {
            try {
                SOCKET.send(new DatagramPacket(sendBuffer, sendBuffer.length, player.getIpAddress(), player.getPort()));
            } catch (IOException ex) {
                System.out.println("Couldn't BROADCAST the message '" + s + "' to the player '" + player.getName() + "' More:\n" + ex.getMessage());
            }
        }
    }

    public void sendOperationToAll(String s) {
        byte[] sendBuffer = s.getBytes(StandardCharsets.UTF_8);

        for (Player player : contestants) {
            try {
                SOCKET.send(new DatagramPacket(sendBuffer, sendBuffer.length, player.getIpAddress(), player.getPort()));
            } catch (IOException ex) {
                System.out.println("Couldn't BROADCAST the message '" + s + "' to the player '" + player.getName() + "' More:\n" + ex.getMessage());
            }
        }
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public boolean isWaitingForPlayers() {
        return contestants.size() < MIN_PLAYERS;
    }

    public Player getPlayerByAddressAndPort(InetAddress address, int port) {
        for (Player player : contestants) {
            if (player.getIpAddress().getHostAddress().equals(address.getHostAddress()) && player.getPort() == port)
                return player;
        }

        return null; // Not found
    }

    public ArrayList<Player> getContestants() {
        return contestants;
    }

    public void displayLeaderboard() {
        broadcast("(OVERALL) Current Scores and Standings:");
        if (leaderboard.isEmpty()) {
            broadcast("  * No participants :(");
            broadcast("BLANK");
            return;
        }

        ArrayList<Map.Entry<Player, Integer>> entryList = new ArrayList<>(leaderboard.entrySet());

        entryList.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));

        int pos = 1;
        for (Map.Entry<Player, Integer> entry : entryList) {
            broadcast("  #" + pos++ + ". " + entry.getKey() + ": " + entry.getValue());
        }

        broadcast("BLANK");
    }

    public int getPort() {
        return PORT;
    }
}
