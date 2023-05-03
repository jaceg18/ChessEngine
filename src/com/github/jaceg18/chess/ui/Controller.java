package com.github.jaceg18.chess.ui;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.evaluation.Evaluation;
import com.github.jaceg18.chess.identity.Color;
import com.github.jaceg18.chess.identity.Flag;
import com.github.jaceg18.chess.identity.MoveType;
import com.github.jaceg18.chess.pieces.Piece;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

@SuppressWarnings("all")
public class Controller implements MouseMotionListener, MouseListener {

    private int selectedRow = -1;
    private int selectedCol = -1;
    private Piece selectedPiece;
    private Color turnToMove = Color.WHITE;
    private Board board;
    private int[] selector;
    Color playerTeam;

    /**
     * Creates a new Controller object with the given Board and player color.
     */
    public Controller(Board board, Color playerTeam){
        this.board = board;
        this.playerTeam = playerTeam;
    }

    /**
     * Sets the board to a different board
     * @param board The replacement board
     */
    public void setBoard(Board board){
        this.board = board;
    }
    /**
     * Returns the selected row on the Board.
     */
    public int getSelectedRow(){
        return selectedRow;
    }
    /**
     * Returns the selected column on the Board.
     */
    public int getSelectedCol(){
        return selectedCol;
    }
    /**
     * Returns the selected Piece on the Board.
     */
    public Piece getSelectedPiece(){
        return selectedPiece;
    }
    /**
     * Returns the Color of the player whose turn it is to move.
     */
    public Color getTurnToMove(){
        return turnToMove;
    }
    /**
     * Switches the turn to move to the other player.
     */
    public void switchTurns(){
        this.turnToMove = Color.invert(turnToMove);
    }

    /**
     * Returns the color of the player
     * @return The team of the player
     */
    public Color getPlayerTeam(){
        return playerTeam;
    }
    /**
     * Resets the selector to null.
     */
    public void resetSelector(){
        this.selector = null;
    }
    /**
     * Returns the current selector as an array of [row, col] coordinates.
     */
    public int[] getSelector(){
        return selector;
    }

    /**
     * Responsible for selecting and picking up a piece
     * @param e the event to be processed
     */
    @Override
    public void mousePressed(MouseEvent e) {
        selectedPiece = null;
        if (turnToMove == playerTeam) {
            int row = e.getY() / GUI.SQUARE_SIZE;
            int col = e.getX() / GUI.SQUARE_SIZE;

            if (GUI.flip){
                row = Board.ROWS - row - 1;
                col = Board.COLS - col - 1;
            }

            Piece piece = board.getPieceAt(row, col);
            if (piece != null) {
                    selectedPiece = piece;
                    selectedRow = row;
                    selectedCol = col;
                if (board.getPieceMoves(selectedPiece).size() == 0) {
                    selectedPiece = null;
                    selectedRow = -1;
                    selectedCol = -1;
                }
            }
        }
    }

    /**
     * Responsible for releasing a piece and making a move
     * @param e the event to be processed
     */

    @Override
    public void mouseReleased(MouseEvent e) {
        if (turnToMove == playerTeam) {
            if (selectedPiece != null) {
                int row = e.getY() / GUI.SQUARE_SIZE;
                int col = e.getX() / GUI.SQUARE_SIZE;

                if (GUI.flip){
                    row = Board.ROWS - row - 1;
                    col = Board.COLS - col - 1;
                }
                    Move move = board.organizeMove(new Move(selectedRow, selectedCol, row, col, selectedPiece));
                    if (board.isMoveValid(move)) {
                        board.makeMove(move, Flag.NORMAL);
                        Evaluation.evaluate(board, Color.WHITE);
                        AudioPlayer.playSound((move.getMoveType() == MoveType.CAPTURE));
                        switchTurns();
                    }

                selectedPiece = null;
                selectedRow = -1;
                selectedCol = -1;
            }
        }
    }

    /**
     * Responsible for changing the location of the selector
     * @param e the event to be processed
     */

    @Override
    public void mouseMoved(MouseEvent e) {
        int row = e.getY() / GUI.SQUARE_SIZE;
        int col = e.getX() / GUI.SQUARE_SIZE;

        if (GUI.flip){
            row = Board.ROWS - row - 1;
            col = Board.COLS - col - 1;
        }

        this.selector = new int[]{row, col};
    }

    // Other irrelevant mouse methods

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

}
