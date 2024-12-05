package me.yhamarsheh.trivia.objects;

import java.util.ArrayList;

public class Question {

    private long startTime;
    private final String question;
    private final String answer;

    private ArrayList<Player> answered;
    public Question(String question, String answer, long startTime) {
        this.answered = new ArrayList<>();
        this.startTime = startTime;
        this.question = question;
        this.answer = answer;
    }

    public Question(String question, String answer) {
        this.answered = new ArrayList<>();
        this.question = question;
        this.answer = answer;
    }

    public boolean answer(Player player, String answer) {
        if (answered.contains(player)) return true;

        answered.add(player);
        return answer.equalsIgnoreCase(this.answer);
    }

    public long getStartTime() {
        return startTime;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public ArrayList<Player> getAnswered() {
        return answered;
    }

    public String askQuestion(int questionNumber) {
        return "Question " + questionNumber + ": " + question;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
