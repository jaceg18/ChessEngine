package com.github.jaceg18.chess.pieces;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.Utility;
import com.github.jaceg18.chess.identity.Color;

import java.util.List;

/**
 * An abstract class representing a chess piece on the board.
 */
@SuppressWarnings("unused")
public abstract class Piece {
    private final Color color;
    private int row;
    private int col;
    private int value;
    private boolean hasMoved;
    private boolean isCaptured;

    /**
     * Constructs a new Piece object with the given color, row, and column.
     *
     * @param color the color of the piece
     * @param row the row of the piece on the board
     * @param col the column of the piece on the board
     */
    public Piece(Color color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;

        this.value = Utility.getValueByPiece(this);
    }
    /**
     * Returns the row of the piece on the board.
     *
     * @return the row of the piece
     */
    public int getRow(){
        return row;
    }
    /**
     * Returns the column of the piece on the board.
     *
     * @return the column of the piece
     */
    public int getCol(){
        return col;
    }
    /**
     * Returns the value of the piece.
     *
     * @return the value of the piece
     */
    public int getValue(){
        return value;
    }
    /**
     * Returns whether the piece has moved.
     *
     * @return true if the piece has moved, false otherwise
     */
    public boolean hasMoved(){
        return hasMoved;
    }
    /**
     * Returns whether the piece has been captured.
     *
     * @return true if the piece has been captured, false otherwise
     */
    public boolean isCaptured(){
        return isCaptured;
    }
    /**
     * Returns the color of the piece.
     *
     * @return the color of the piece
     */
    public Color getColor(){
        return color;
    }
    /**
     * Sets the row and column of the piece on the board.
     *
     * @param row the new row of the piece
     * @param col the new column of the piece
     */
    public void setPosition(int row, int col){
        this.row = row;
        this.col = col;
    }
    /**
     * Sets whether the piece has been captured.
     *
     * @param isCaptured true if the piece has been captured, false otherwise
     */
    public void setCaptured(boolean isCaptured){
        this.isCaptured = isCaptured;
    }
    /**
     * Sets whether the piece has moved.
     *
     * @param hasMoved true if the piece has moved, false otherwise
     */
    public void setMoved(boolean hasMoved){
        this.hasMoved = hasMoved;
    }
    /**
     * Sets the value of the piece.
     *
     * @param value the value of the piece
     */
    public void setValue(int value){
        this.value = value;
    }
    /**
     * Returns a list of sudo-legal moves for the piece.
     *
     * @return a list of sudo-legal moves for the piece
     */
    public abstract List<Move> getSudoLegalMoves(Board board);

    /**
     * Creates a deep copy of the piece
     * @return returns a deep copy of the piece
     */
    public abstract Piece copy();

    /**
     * Returns the hash code value for this piece.
     * @return The hash code value for this piece.
     */
    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + col;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + (hasMoved ? 1231 : 1237);
        result = prime * result + (isCaptured ? 1231 : 1237);
        result = prime * result + row;
        result = prime * result + value;
        return result;
    }
    /**

     Overrides the equals method to compare two Piece objects based on their row, column, and color.
     @param obj the object to be compared with this Piece object
     @return true if the objects are equal based on the row, column, and color, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Piece piece = (Piece) obj;
        return row == piece.row && col == piece.col && color == piece.color;
    }

    /**
     * A method to return fen ID by piece
     * @return The pieces corresponding fen ID
     */
    @Override
    public String toString(){
        if (this instanceof Pawn) return (this.getColor() == Color.WHITE) ? "P" : "p";
        if (this instanceof Knight) return (this.getColor() == Color.WHITE) ? "N" : "n";
        if (this instanceof Bishop) return (this.getColor() == Color.WHITE) ? "B" : "b";
        if (this instanceof Rook) return (this.getColor() == Color.WHITE) ? "R" : "r";
        if (this instanceof Queen) return (this.getColor() == Color.WHITE) ? "Q" : "q";
        if (this instanceof King) return (this.getColor() == Color.WHITE) ? "K" : "k";
        return ""; // Return empty string if not a recognized chess piece
    }

}
