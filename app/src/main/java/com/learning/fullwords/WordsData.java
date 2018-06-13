package com.learning.fullwords;

class WordsData {

    String word;
    String meaning;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public WordsData(String word, String meaning) {
        this.word=word;
        this.meaning=meaning;
    }
}
