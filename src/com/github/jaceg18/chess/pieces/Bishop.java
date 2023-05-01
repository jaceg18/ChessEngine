package com.github.jaceg18.chess.pieces;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.identity.Color;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

    private final int[][] DIR_OFFSETS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    public Bishop(Color color, int row, int col) {
        super(color, row, col);
    }

    /**
     * Returns all sudo legal moves for a given bishop
     * @param board The board to check for moves
     * @return A list of all sudo legal bishop moves
     */
    @Override
    public List<Move> getSudoLegalMoves(Board board) {
        List<Move> moveSet = new ArrayList<>();
        int row = getRow();
        int col = getCol();
        for (int dir=0; dir < 4; dir++) {
            int newRow = row;
            int newCol = col;
            while (true) {
                newRow += DIR_OFFSETS[dir][0];
                newCol += DIR_OFFSETS[dir][1];
                if (newRow < 0 || newRow >= 8 || newCol < 0 || newCol >= 8)
                    break;
                Piece destPiece = board.getPieceAt(newRow, newCol);
                if (destPiece == null)
                    moveSet.add(new Move(row, col, newRow, newCol, this));
                else {
                    if (destPiece.getColor() != getColor())
                        moveSet.add(new Move(row, col, newRow, newCol, this));
                    break;
                }
            }
        }
        return moveSet;
    }
    /**
     * Creates a deep copy of the piece
     * @return A deep copy of the piece
     */
    @Override
    public Piece copy() {
        Bishop bishop = new Bishop(getColor(), getRow(), getCol());
        bishop.setMoved(hasMoved());
        bishop.setCaptured(isCaptured());
        bishop.setValue(getValue());
        return bishop;
    }
}
