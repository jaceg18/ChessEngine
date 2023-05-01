package com.github.jaceg18.chess;

public class ScoredMove extends Move {
    private int score;

    public ScoredMove(Move move, int score) {
        super(move);
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}