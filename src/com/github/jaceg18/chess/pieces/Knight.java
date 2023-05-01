package com.github.jaceg18.chess.pieces;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.identity.Color;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    private final int[][] offsets = {{-1,-2}, {-2,-1}, {-2,1}, {-1,2}, {1,-2}, {2,-1}, {2,1}, {1,2}};
    public Knight(Color color, int row, int col) {
        super(color, row, col);
    }

    /**
     * Returns all sudo legal moves for a given knight
     * @param board The board to check for moves
     * @return A list of all sudo legal moves for a given knight
     */
    @Override
    public List<Move> getSudoLegalMoves(Board board) {
        int row = getRow();
        int col = getCol();
        List<Move> moveSet = new ArrayList<>();
        for (int[] offset : offsets){
            int newRow = row + offset[0];
            int newCol = col + offset[1];

            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8){
                Piece destPiece = board.getPieceAt(newRow, newCol);

                if (destPiece == null || destPiece.getColor() != getColor())
                    moveSet.add(new Move(row, col, newRow, newCol, this));
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
        Knight knight = new Knight(getColor(), getRow(), getCol());
        knight.setMoved(hasMoved());
        knight.setCaptured(isCaptured());
        knight.setValue(getValue());
        return knight;
    }
}
