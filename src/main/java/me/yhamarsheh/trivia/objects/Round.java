package me.yhamarsheh.trivia.objects;

import me.yhamarsheh.trivia.Trivia;
import me.yhamarsheh.trivia.enums.Operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Round {

    private final Game game;
    private Question currentQuestion;

    private int questionNumber;

    private final int TIME_PER_QUESTION = 30;
    private final int QUESTIONS_PER_ROUND = 3;
    private final int TIME_BETWEEN_QUESTIONS = 25;

    private final Map<Player, Double> points;

    public Round(Game game) {
        this.points = new HashMap<>();
        this.game = game;

        for (Player player : game.getContestants()) {
            if (!points.containsKey(player)) points.put(player, -1.0);
        }
    }

    public void startRound() {
        Runnable task = () -> {
        do {
            nextQuestion();
            String s = currentQuestion.askQuestion(questionNumber);
            game.broadcast(s);
            System.out.println(s);

            game.sendOperationToAll(String.format(Operation.REQUEST_INPUT.getFormat(), "  Your answer (or type 'exit' to quit): "));

            try {
                Thread.sleep(TIME_PER_QUESTION * 1000);
            } catch (InterruptedException ex) {
                System.out.println("An error has occurred: " + ex.getMessage());
                return;
            }

            System.out.println("Time's up!");
            game.broadcast("TIME'S UP! The correct answer was: " + currentQuestion.getAnswer());
            game.broadcast("BLANK");

            distributePoints();
            displayLeaderboard();

            if (questionNumber != 3) {
                game.broadcast("That was it for question #" + questionNumber + ". Next question will start soon! Get ready!");
                game.broadcast("BLANK");
                try {
                    Thread.sleep(TIME_BETWEEN_QUESTIONS * 1000);
                } catch (InterruptedException ex) {
                    System.out.println("An error has occurred: " + ex.getMessage());
                    return;
                }
            } else {
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException ex) {
                    System.out.println("An error has occurred: " + ex.getMessage());
                    return;
                }
            }
        } while (questionNumber < QUESTIONS_PER_ROUND);
        Player winner = end();

        game.nextRound(winner);
    };

        new Thread(task).start();
    }

    public boolean answer(Player player, String answer) {
        if (points.get(player) == -1.0) points.replace(player, 0.0);

        boolean correctness = currentQuestion.answer(player, answer);
        if (correctness) points.replace(player, 3.0);

        return correctness;
    }

    public void distributePoints() {
        for (Player player : currentQuestion.getAnswered()) {
            double p = ((double) currentQuestion.getAnswered().size() - currentQuestion.getAnswered().indexOf(player)) /
                    ((double) currentQuestion.getAnswered().size());

            if (points.get(player) == 3.0)
                points.replace(player, p);
            else if (points.get(player) != -1.0 && points.get(player) != 0.0)
                points.replace(player, points.get(player) + p);
        }
    }

    public void nextQuestion() {
        if (questionNumber == 3) {
            return;
        }

        this.currentQuestion = Trivia.PRIMARY_MANAGER.getQuestionsManager().getRandomQuestion();
        this.questionNumber++;
    }

    public Player end() {
        // END OF ROUND LOGIC - MUST RETURN WINNER
        ArrayList<Map.Entry<Player, Double>> entryList = new ArrayList<>(points.entrySet());

        entryList.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        return entryList.get(0).getKey();
    }

    public void displayLeaderboard() {
        game.broadcast("(ROUND) Current Scores and Standings:");
        if (points.isEmpty()) {
            game.broadcast("  * No participants :(");
            game.broadcast("BLANK");
            return;
        }

        ArrayList<Map.Entry<Player, Double>> entryList = new ArrayList<>(points.entrySet());

        entryList.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        int pos = 1;
        for (Map.Entry<Player, Double> entry : entryList) {
            game.broadcast("  #" + pos++ + ". " + entry.getKey() + ": " + (entry.getValue() == -1.0 ? 0.0 : entry.getValue()));
        }

        game.broadcast("BLANK");
    }

    public ArrayList<Player> getPlayersWhoDidNotParticipate() {
        ArrayList<Player> players = new ArrayList<>();
        for (Player player : points.keySet()) {
            if (points.get(player) == -1.0) players.add(player);
        }

        return players;
    }
}
