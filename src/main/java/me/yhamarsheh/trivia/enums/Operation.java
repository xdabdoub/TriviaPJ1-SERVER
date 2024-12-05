package me.yhamarsheh.trivia.enums;

public enum Operation {

    JOIN("JOIN;%s"),
    QUIT("QUIT;%s"),
    ANSWER("ANSWER;%s"),
    REQUEST_INPUT("REQUEST_INPUT;%s"),
    MESSAGE("MESSAGE;%s");

    final String format;
    Operation(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
