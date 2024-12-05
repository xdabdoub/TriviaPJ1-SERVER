package me.yhamarsheh.trivia.managers.sub;

import me.yhamarsheh.trivia.Trivia;
import me.yhamarsheh.trivia.objects.Question;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

public class QuestionsManager {

    private final ArrayList<Question> questions;
    private final Random random;

    public QuestionsManager() {
        this.questions = new ArrayList<>();
        this.random = new Random();
    }

    public Question getRandomQuestion() {
        if (questions.isEmpty()) {
            try {
                Trivia.PRIMARY_MANAGER.getFileSystem().readQuestions();
            } catch (FileNotFoundException e) {
                System.out.println("File not found!");
            }
        }

        int i = random.nextInt(questions.size());
        Question question = questions.get(i);
        questions.remove(i);

        return question;
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }
}
