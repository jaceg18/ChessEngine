package com.github.jaceg18.chess.pieces;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.identity.Color;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
    public Rook(Color color, int row, int col) {
        super(color, row, col);
    }

    /**
     * Returns all sudo legal moves for a given rook
     * @param board The board to check for moves
     * @return A list of all sudo legal rook moves
     */
    @Override
    public List<Move> getSudoLegalMoves(Board board) {
        List<Move> moveSet = new ArrayList<>();
        int row = getRow();
        int col = getCol();
        // Move up
        for (int r=row-1; r>=0; r--){
            Piece destPiece = board.getPieceAt(r, col);
            if (destPiece == null)
                moveSet.add(new Move(row, col, r, col, this));
            else {
                if (destPiece.getColor() != getColor())
                    moveSet.add(new Move(row, col, r, col, this));
                break;
            }
        }
        // Move down
        for (int r=row+1; r<8; r++){
            Piece destPiece = board.getPieceAt(r, col);
            if (destPiece == null)
                moveSet.add(new Move(row, col, r, col, this));
            else {
                if (destPiece.getColor() != getColor())
                    moveSet.add(new Move(row, col, r, col, this));
                break;
            }
        }
        // Move left
        for (int c=col-1; c>=0; c--){
            Piece destPiece = board.getPieceAt(row, c);
            if (destPiece == null)
                moveSet.add(new Move(row, col, row, c, this));
            else {
                if (destPiece.getColor() != getColor())
                    moveSet.add(new Move(row, col, row, c, this));
                break;
            }
        }
        // Move right
        for (int c=col+1; c<8; c++){
            Piece destPiece = board.getPieceAt(row, c);
            if (destPiece == null)
                moveSet.add(new Move(row, col, row, c, this));
            else {
                if (destPiece.getColor() != getColor())
                    moveSet.add(new Move(row, col, row, c, this));
                break;
            }
        }
        return moveSet;
    }

    /**
     * Creates a deep copy of the piece
     * @return A deep copy of the piece
     */
    @Override
    public Rook copy() {
        Rook rook = new Rook(getColor(), getRow(), getCol());
        rook.setMoved(hasMoved());
        rook.setCaptured(isCaptured());
        rook.setValue(getValue());
        return rook;
    }
}
