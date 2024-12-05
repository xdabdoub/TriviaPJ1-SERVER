package me.yhamarsheh.trivia.managers;

import me.yhamarsheh.trivia.managers.sub.QuestionsManager;
import me.yhamarsheh.trivia.storage.FileSystem;

import java.io.File;
import java.io.FileNotFoundException;

public class PrimaryManager {

    private final QuestionsManager questionsManager;
    private final FileSystem fileSystem;

    public PrimaryManager() {
        this.questionsManager = new QuestionsManager();
        this.fileSystem = new FileSystem(new File("questions.csv"));
    }

    public QuestionsManager getQuestionsManager() {
        return questionsManager;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }
}
