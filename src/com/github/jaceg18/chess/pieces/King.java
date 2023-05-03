package com.github.jaceg18.chess.pieces;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.Utility;
import com.github.jaceg18.chess.identity.Color;


import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class King extends Piece {

    private boolean hasCastled;
    public King(Color color, int row, int col) {
        super(color, row, col);

        this.hasCastled = false;
    }

    /**
     * Returns all sudo legal moves for a given king
     * @param board The board to check for moves
     * @return A list of all sudo legal king moves
     */
    @Override
    public List<Move> getSudoLegalMoves(Board board) {
        List<Move> moveSet = new ArrayList<>();
        int row = getRow();
        int col = getCol();
        // Check for all moves in all directions
        for (int newRow=row-1; newRow<=row+1; newRow++)
            for (int newCol=col-1; newCol<=col+1; newCol++)
                if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8 && (newRow != row || newCol != col)) {
                    Move move = new Move(row, col, newRow, newCol, this);
                    Piece destPiece = board.getPieceAt(newRow, newCol);
                    if (destPiece == null || destPiece.getColor() != getColor())
                        moveSet.add(move);
                }

        addCastleMoves(board, moveSet);


        return moveSet;
    }
    /**
     * Creates a deep copy of the piece
     * @return A deep copy of the piece
     */
    @Override
    public Piece copy() {
        King king = new King(getColor(), getRow(), getCol());
        king.setMoved(hasMoved());
        king.setHasCastled(hasCastled());
        king.setCaptured(isCaptured());
        king.setValue(getValue());
        return king;
    }

    /**
     * A helper method to add all legal castle moves
     * @param board The board to check for moves
     * @param legalMoves The pre-existing sudo legal moves list to add-on to
     */
    private void addCastleMoves(Board board, List<Move> legalMoves){
        if (!hasMoved() && !hasCastled && getCol() == 4){
            int row = getRow();
            int col = getCol();
            Piece kingSideRook = board.getPieceAt(row, 7);
            if (kingSideRook instanceof Rook && !kingSideRook.hasMoved()){
                boolean isPathClear = true;
                for (int i=col+1; i<7; i++)
                    if (board.getPieceAt(row, i) != null){
                        isPathClear = false;
                        break;
                    }
                if (isPathClear && !Utility.inCheck(board, getColor()) && !Utility.isAttacked(board, row, col+1, getColor()) && !Utility.isAttacked(board, row, col+2, getColor())){
                    legalMoves.add(new Move(row, col, row, col+2, this));
                }
            }
            Piece queenSideRook = board.getPieceAt(row, 0);
            if (queenSideRook instanceof Rook && !queenSideRook.hasMoved()){
                boolean isPathClear = true;
                for (int i=col-1; i>0; i--)
                    if (board.getPieceAt(row, i) != null){
                        isPathClear = false;
                        break;
                    }
                if (isPathClear && !Utility.inCheck(board, getColor()) && !Utility.isAttacked(board, row, col-1, getColor()) && !Utility.isAttacked(board, row, col-2, getColor())){
                    legalMoves.add(new Move(row, col, row, col-2, this));
                }
            }
        }
    }

    /**
     * A method to set is a king has castled
     * @param hasCastled The boolean value to set hasCastled
     */
    public void setHasCastled(boolean hasCastled){
        this.hasCastled = hasCastled;
    }

    /**
     * Checks if a king has castled already
     * @return The boolean value of hasCastled
     */
    public boolean hasCastled(){
        return hasCastled;
    }
}
