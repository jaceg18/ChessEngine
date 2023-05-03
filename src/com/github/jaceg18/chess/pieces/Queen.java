package com.github.jaceg18.chess.pieces;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.identity.Color;


import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {
    public Queen(Color color, int row, int col) {
        super(color, row, col);
    }

    /**
     * Returns all sudo legal moves for a given queen
     * @param board The board to check for moves
     * @return A list of all sudo legal queen moves
     */
    @Override
    public List<Move> getSudoLegalMoves(Board board) {
        List<Move> moveSet = new ArrayList<>();

        // Check for moves in all directions
        moveSet.addAll(getMovesInDirection(board, -1, 0)); // up
        moveSet.addAll(getMovesInDirection(board, 1, 0)); // dowm
        moveSet.addAll(getMovesInDirection(board, 0, -1)); // left
        moveSet.addAll(getMovesInDirection(board, 0, 1)); // right
        moveSet.addAll(getMovesInDirection(board, -1, -1)); // up left
        moveSet.addAll(getMovesInDirection(board, -1, 1)); // up right
        moveSet.addAll(getMovesInDirection(board, 1, -1)); // down left
        moveSet.addAll(getMovesInDirection(board, 1, 1)); // down right

        return moveSet;
    }
    /**
     * Creates a deep copy of the piece
     * @return A deep copy of the piece
     */
    @Override
    public Piece copy() {
        Queen queen = new Queen(getColor(), getRow(), getCol());
        queen.setMoved(hasMoved());
        queen.setCaptured(isCaptured());
        queen.setValue(getValue());

        return queen;
    }

    /**
     * A helper method to get all sudo legal moves in a given direction
     * @param board The board to check for moves
     * @param rowIncrement The row's increment
     * @param colIncrement The col's increment
     * @return A list of all sudo legal queen moves in a given direction
     */
    private List<Move> getMovesInDirection(Board board, int rowIncrement, int colIncrement){
        List<Move> moveSet = new ArrayList<>();
        int row = getRow();
        int col = getCol();
        int newRow = row + rowIncrement;
        int newCol = col + colIncrement;
        while (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8){
            Piece destPiece = board.getPieceAt(newRow, newCol);
            if (destPiece == null)
                moveSet.add(new Move(row, col, newRow, newCol, this));
            else {
                if (destPiece.getColor() != getColor())
                    moveSet.add(new Move(row, col, newRow, newCol, this));
                break;
            }
            newRow += rowIncrement;
            newCol += colIncrement;

        }
        return moveSet;
    }
}
