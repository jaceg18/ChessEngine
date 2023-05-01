package com.github.jaceg18.chess.identity;

import com.github.jaceg18.chess.Board;

public enum GameState {
    OPENING,
    MIDDLE,
    END;

    public static GameState getGameState(Board board) {
        int pieceCount = board.getTeamPieces(Color.WHITE).size() + board.getTeamPieces(Color.BLACK).size();
        if (pieceCount >= 29) return OPENING;
        else if (pieceCount > 10) return MIDDLE;
        else return END;
    }
}
