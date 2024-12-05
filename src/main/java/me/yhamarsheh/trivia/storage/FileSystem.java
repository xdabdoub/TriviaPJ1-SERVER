package me.yhamarsheh.trivia.storage;

import me.yhamarsheh.trivia.Trivia;
import me.yhamarsheh.trivia.objects.Question;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileSystem {

    private final File questionsFile;
    public FileSystem(File questionsFile) {
        this.questionsFile = questionsFile;
    }

    public void readQuestions() throws FileNotFoundException {
        try (Scanner scanner = new Scanner(questionsFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("question,answer")) continue;

                String[] data = line.split(",");
                if (data.length != 2) continue;

                String question = data[0];
                String answer = data[1];
                Trivia.PRIMARY_MANAGER.getQuestionsManager().addQuestion(new Question(question, answer));
            }
        }
    }
}
