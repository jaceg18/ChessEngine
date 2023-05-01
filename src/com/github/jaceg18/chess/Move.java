package com.github.jaceg18.chess;

import com.github.jaceg18.chess.ai.Opening.OpeningBook;
import com.github.jaceg18.chess.identity.MoveType;
import com.github.jaceg18.chess.pieces.Pawn;
import com.github.jaceg18.chess.pieces.Piece;

@SuppressWarnings("unused")
public class Move {
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private final Piece piece;
    private MoveType moveType;
    private Piece capturedPiece;
    private boolean capturePieceHasMoved;
    private boolean sourcePieceHasMoved;
    private Piece promotionPiece;
    private boolean promoted;

    /**
     * Constructor for the Move class. Sets up the move with the given row and column values, and sets the Piece that is
     * being moved.
     *
     * @param toRow The row the Piece is being moved to.
     * @param fromRow The row the Piece is moving from.
     * @param toCol The column the Piece is being moved to.
     * @param fromCol The column the Piece is moving from.
     * @param piece The Piece that is being moved.
     */

    public Move(int fromRow, int fromCol, int toRow, int toCol, Piece piece){
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.piece = piece;

        this.sourcePieceHasMoved = piece.hasMoved();
        this.promoted = false;
    }
    public Move(Move move){
        this.fromRow = move.getFromRow();
        this.fromCol = move.getFromCol();
        this.toRow = move.getToRow();
        this.toCol = move.getToCol();
        this.piece = move.getPiece();
        this.promoted = move.isPromoted();
        this.promotionPiece = move.getPromotionPiece();
        this.capturedPiece = move.getCapturedPiece();
        this.moveType = move.getMoveType();
        this.capturePieceHasMoved = move.getCapturedPieceHasMoved();
        this.sourcePieceHasMoved = move.getSourcePieceHasMoved();
    }


    /**
     * Returns the starting row of the Piece.
     *
     * @return The starting row of the Piece.
     */
    public int getFromRow(){
        return fromRow;
    }
    /**
     * Returns the ending row of the Piece.
     *
     * @return The ending row of the Piece.
     */
    public int getToRow(){
        return toRow;
    }
    /**
     * Returns the starting column of the Piece.
     *
     * @return The starting column of the Piece.
     */
    public int getFromCol(){
        return fromCol;
    }
    /**
     * Returns the ending column of the Piece.
     *
     * @return The ending column of the Piece.
     */
    public int getToCol(){
        return toCol;
    }
    /**
     * Returns the Piece that is being moved.
     *
     * @return The Piece that is being moved.
     */
    public Piece getPiece(){
        return piece;
    }
    /**
     * Returns the captured Piece.
     *
     * @return The captured Piece.
     */
    public Piece getCapturedPiece(){
        return capturedPiece;
    }
    /**
     * Returns the promotion Piece.
     *
     * @return The promotion Piece.
     */
    public Piece getPromotionPiece(){
        return promotionPiece;
    }
    /**
     * Returns whether the move results in a promotion.
     *
     * @return True if the move results in a promotion, false otherwise.
     */
    public boolean isPromoted(){
        return promoted;
    }
    /**
     * Returns the type of move.
     *
     * @return The type of move.
     */
    public MoveType getMoveType(){
        return moveType;
    }
    /**
     * Sets the type of move.
     *
     * @param moveType The type of move to set.
     */
    public void setMoveType(MoveType moveType){
        this.moveType = moveType;
    }
    /**
     * Returns whether the source Piece has moved.
     *
     * @return True if the source Piece has moved, false otherwise.
     */
    public boolean getSourcePieceHasMoved(){
        return sourcePieceHasMoved;
    }
    /**
     * Returns whether the captured Piece has moved.
     *
     * @return True if the captured Piece has moved, false otherwise.
     */
    public boolean getCapturedPieceHasMoved(){
        return capturePieceHasMoved;
    }
    /**
     * Sets whether the source Piece has moved.
     *
     * @param hasMoved Whether the source Piece has moved.
     */
    public void setSourcePieceHasMoved(boolean hasMoved){
        this.sourcePieceHasMoved = hasMoved;
    }
    /**
     * Sets whether the captured Piece has moved.
     *
     * @param hasMoved Whether the captured Piece has moved.
     */
    public void setCapturePieceHasMoved(boolean hasMoved){
        this.capturePieceHasMoved = hasMoved;
    }
    /**
     * Sets the captured Piece.
     *
     * @param capturedPiece The Piece that has been captured.
     */
    public void setCapturedPiece(Piece capturedPiece){
        this.capturedPiece = capturedPiece;
        this.capturePieceHasMoved = capturedPiece.hasMoved();
    }
    /**
     * Sets the promotion Piece.
     *
     * @param promotionPiece The Piece that the pawn is being promoted to.
     */
    public void setPromotionPiece(Piece promotionPiece){
        this.promotionPiece = promotionPiece;
    }
    /**
     * Sets whether the pawn has been promoted.
     *
     * @param promoted True if the pawn has been promoted, false otherwise.
     */
    public void setPromoted(boolean promoted){
        this.promoted = promoted;
    }

    @Override
    public String toString(){
        return "To Row: " + toRow + " To Col: " + toCol + " From Row: " + fromRow + " From Col: " + fromCol + " Piece: " + Utility.getNameByPiece(piece);
    }
    public String toNotation(){
        boolean pawn = (getPiece() instanceof Pawn);
        String pieceNotation = (!pawn) ? getPiece().toString().toUpperCase() : "";
        char toCol = '_';
        char fromCol = '_';
        for (int i=0; OpeningBook.COLUMNS.length > i; i++){
            if (i == getToCol())
                toCol = OpeningBook.COLUMNS[i];
            if (i == getFromCol())
                fromCol = OpeningBook.COLUMNS[i];
        }
        if (moveType == MoveType.CAPTURE){
            if (pawn)
                return fromCol + "x" + toCol + (7-getToRow());
           return pieceNotation + "x" + toCol + "" + (7-getToRow());
        }
        if (moveType == MoveType.CASTLE){
            return "0-0";
        }
        if (moveType == MoveType.CHECK){
            return pieceNotation + toCol + "" + (7-getToRow()) + "+";
        }


        return pieceNotation + toCol + "" + (8-getToRow());
    }

}
