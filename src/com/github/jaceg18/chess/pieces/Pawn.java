package com.github.jaceg18.chess.pieces;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.identity.Color;

import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("unused")
public class Pawn extends Piece {

    private boolean enPassantAvailable;
    public Pawn(Color color, int row, int col) {
        super(color, row, col);

        this.enPassantAvailable = false;
    }

    /**
     * A method that checks if a pawn can be en passant
     * @return If a pawn can be en passant
     */
    public boolean isEnPassantAvailable() {
        return enPassantAvailable;
    }

    /**
     * A method that sets a pawn's permission to be en passant
     * @param enPassantAvailable The value to give the en passant boolean
     */
    public void setEnPassantAvailable(boolean enPassantAvailable) {
        this.enPassantAvailable = enPassantAvailable;
    }

    /**
     * Returns all sudo legal moves for a given pawn
     * @param board The board to check for moves
     * @return The list of sudo legal pawn moves
     */
    @Override
    public List<Move> getSudoLegalMoves(Board board) {
        List<Move> moveSet = new ArrayList<>();
        int row = getRow();
        int col = getCol();
        boolean isWhite = getColor() == Color.WHITE;
        int direction = (isWhite) ? -1 : 1;

        if ((isWhite && 0 >= row) || (!isWhite && 7 <= row))
            direction = 0;

        if (board.getPieceAt(row + direction, col) == null) {
            moveSet.add(new Move(row, col, row + direction, col, this));

            // Pawn can move two squares forward
            if ((isWhite && row == 6) || (!isWhite && row == 1))
                if (board.getPieceAt(row + (2 * direction), col) == null)
                    moveSet.add(new Move(row, col, row + (2 * direction), col, this));
        }

        Piece capturedPiece;

        if (col > 0) {
            capturedPiece = board.getPieceAt(row + direction, col - 1);
            if (capturedPiece != null && capturedPiece.getColor() != getColor())
                moveSet.add(new Move(row, col, row + direction, col - 1, this));
        }
        if (7 > col) {
            capturedPiece = board.getPieceAt(row + direction, col + 1);
            if (capturedPiece != null && capturedPiece.getColor() != getColor())
                moveSet.add(new Move(row, col, row + direction, col + 1, this));
        }
        addEnPassantMoves(moveSet, board, row, col, direction);

        return moveSet;
    }
    /**
     * Creates a deep copy of the piece
     * @return A deep copy of the piece
     */
    @Override
    public Piece copy() {
        Pawn pawn = new Pawn(getColor(), getRow(), getCol());
        pawn.setMoved(hasMoved());
        pawn.setCaptured(isCaptured());
        pawn.setValue(getValue());
        pawn.setEnPassantAvailable(isEnPassantAvailable());

        return pawn;
    }

    /**
     * A helper method that adds en passant moves to it's moveset
     * @param moveSet The list to add en passant moves to
     * @param board The board to check for en passant moves
     * @param row The row of the pawn
     * @param col The col of the pawn
     * @param direction The direction of the pawn
     */
    private void addEnPassantMoves(List<Move> moveSet, Board board, int row, int col, int direction){
        Piece leftPiece = col > 0 ? board.getPieceAt(row, col - 1) : null;
        Piece rightPiece = col < 7 ? board.getPieceAt(row, col + 1) : null;
        if (leftPiece instanceof Pawn && leftPiece.getColor() != getColor() && ((Pawn) leftPiece).isEnPassantAvailable())
            moveSet.add(new Move(row, col, row + direction, col - 1, this));

        if (rightPiece instanceof Pawn && rightPiece.getColor() != getColor() && ((Pawn) rightPiece).isEnPassantAvailable())
            moveSet.add(new Move(row, col, row + direction, col + 1, this));
    }
}
