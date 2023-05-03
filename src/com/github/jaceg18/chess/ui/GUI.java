package com.github.jaceg18.chess.ui;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.Utility;
import com.github.jaceg18.chess.ai.AI;
import com.github.jaceg18.chess.ai.AIThreadSearch;
import com.github.jaceg18.chess.identity.Color;
import com.github.jaceg18.chess.pieces.Piece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("all")
public class GUI extends JPanel implements ActionListener {
    public static final int BOARD_SIZE = 512;
    public static final int SQUARE_SIZE = BOARD_SIZE / 8;
    private final int TICK_SPEED = 40;

    private static Controller controller;

    public Board board;
    private Timer timer;
    private static Console console;

    public static boolean flip = false;

    private final java.awt.Color color1 = new java.awt.Color(255, 255, 255);
    private final java.awt.Color color2 = new java.awt.Color(94, 133, 114);
    private final java.awt.Color color3 = new java.awt.Color(135, 206, 235);
    private AIThreadSearch aiThreadSearch;
    private AI ai;
    private AI ai2;
    private Color playerTeam;
    public boolean AI_THINKING = false;
    public static String gameNotation;
    private int depth = 4;

    /**
     * Constructor for JPanel
     * @param playerTeam The players team
     */
    public GUI(Color playerTeam){
        this.board = new Board();
        this.playerTeam = playerTeam;
        this.controller = new Controller(board, playerTeam);
        this.timer = new Timer(TICK_SPEED, this);
        this.ai = new AI(Color.invert(playerTeam), depth);
        this.ai2 = new AI(playerTeam, depth);
        gameNotation = "";

        flip = playerTeam == Color.BLACK;

        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setFocusable(true);
        addMouseListener(controller);
        addMouseMotionListener(controller);

        this.console = new Console(this);
        console.start();

        timer.start();

    }

    /**
     * Paints the graphical UI on the panel
     * @param g  the <code>Graphics</code> context in which to paint
     */
    @Override
    public void paint(Graphics g){
        super.paintComponent(g);

        // Draw squares
        for (int row = (!flip ? Board.ROWS - 1 : 0); (!flip ? row >= 0 : row < Board.ROWS); row += (!flip ? -1 : 1)){
            for (int col = (!flip ? Board.COLS - 1 : 0); (!flip ? col >= 0 : col < Board.COLS); col += (!flip ? -1 : 1)){
                g.setColor(((row + col) % 2 == 0) ? color1 : color2);
                g.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
        // Draw mouse selector
        if (controller.getSelector() != null) {
            int[] selector = controller.getSelector();
            g.setColor(java.awt.Color.LIGHT_GRAY);
            g.fillRect((!flip ? selector[1] * SQUARE_SIZE : (Board.COLS - selector[1] - 1) * SQUARE_SIZE),
                    (!flip ? selector[0] * SQUARE_SIZE : (Board.ROWS - selector[0] - 1) * SQUARE_SIZE),
                    SQUARE_SIZE, SQUARE_SIZE);
        }
        // Draw moves for piece
        if (controller.getSelectedPiece() != null){
            Piece selectedPiece = controller.getSelectedPiece();
            for (Move move : board.getPieceMoves(selectedPiece)){
                g.setColor(color3);
                g.fillRect((!flip ? move.getToCol() * SQUARE_SIZE : (Board.COLS - move.getToCol() - 1) * SQUARE_SIZE),
                        (!flip ? move.getToRow() * SQUARE_SIZE : (Board.ROWS - move.getToRow() - 1) * SQUARE_SIZE),
                        SQUARE_SIZE, SQUARE_SIZE);
            }
        }
        // Draw pieces
        for (int i = (!flip ? Board.ROWS - 1 : 0); (!flip ? i >= 0 : i < Board.ROWS); i += (!flip ? -1 : 1)){
            for (Piece piece : board.getPieces()[i]){
                if (piece != null){
                    g.drawImage(Graphic.getSprite(piece),
                            (!flip ? piece.getCol() * SQUARE_SIZE : (Board.COLS - piece.getCol() - 1) * SQUARE_SIZE),
                            (!flip ? piece.getRow() * SQUARE_SIZE : (Board.ROWS - piece.getRow() - 1) * SQUARE_SIZE),
                            null);
                }
            }
        }
    }

    public void resetGame(){
        board = new Board();
        controller = new Controller(board, playerTeam);
        ai = new AI(Color.invert(playerTeam), depth);
        ai2 = new AI(playerTeam, depth);
        AI_THINKING = false;

    }

    /**
     * Returns the mouse listener
     * @return the mouse listener
     */
    public static Controller getController(){
        return controller;
    }
    /**
     * Gets the GUI's board
     * @return The gui's current board
     */
    public Board getBoard(){
        return board;
    }

    /**
     * Sets a new board for the controller and gui
     * @param board The replacement board
     */
    public void setBoard(Board board){
        this.board = board;
        this.controller.setBoard(board);
    }

    /**
     * Flips the board around
     */
    public void flip(){
        this.flip = !flip;
    }
    /**
     * Repaints the panel and allows the AI to move if it's the correct turn.
     * @param e the event to be processed
     */

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
        if (!Utility.isGameOver(board)) {
            if (controller.getTurnToMove() != controller.getPlayerTeam() && !AI_THINKING) {
               AI_THINKING = true;
               aiThreadSearch = new AIThreadSearch(ai, this);
               aiThreadSearch.execute();
            }
        } else {
            System.out.println("Game over!");
            timer.stop();
        }

    }
}
