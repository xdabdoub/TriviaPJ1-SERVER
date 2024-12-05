package me.yhamarsheh.trivia;

import me.yhamarsheh.trivia.enums.Operation;
import me.yhamarsheh.trivia.managers.PrimaryManager;
import me.yhamarsheh.trivia.objects.Game;
import java.io.IOException;

public class Trivia {

    public static final PrimaryManager PRIMARY_MANAGER = new PrimaryManager();

    public static void main(String[] args) throws IOException {
        PRIMARY_MANAGER.getFileSystem().readQuestions();

        final int PORT = 5689;
        Game game = new Game(PORT);

        System.out.println("Trivia PJ1 - Launched Successfully (SERVER HOST) and is now listening on PORT: " + game.getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            game.sendOperationToAll(String.format(Operation.QUIT.getFormat(), "The host server has closed and your connection with it will disconnect soon!"));
            System.out.println("\n\nExiting the game.");
        }));

        game.listenForConnections();
    }
}
