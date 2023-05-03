package com.github.jaceg18.chess.identity;

public enum MoveType {
    ILLEGAL(-1),
    LEGAL(0),
    CAPTURE(1),
    PROMOTION(2),
    CASTLE(3),
    CHECK(4),
    CHECKMATE(5),
    ORDERED(6),
    ENPASSANT(-2);

    private final int priority;

    MoveType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
