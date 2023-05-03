package com.github.jaceg18.chess.ai;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.ui.GUI;

import javax.swing.*;


public class AIThreadSearch extends SwingWorker<Void, Void> {

    private final AI ai;
    private final GUI gui;

    /**
     * The AI's worker thread
     *
     * @param ai  The AI object
     * @param gui The GUI object
     */
    public AIThreadSearch(AI ai, GUI gui) {
        this.ai = ai;
        this.gui = gui;
    }

    /**
     * The task the thread will perform in the background
     *
     * @return null
     */

    @Override
    protected Void doInBackground() {
        Board board = ai.move(gui.getBoard().getCopy());
        gui.setBoard(board);
        return null;
    }

    /**
     * The task the thread will perform once finished
     */
    @Override
    protected void done() {
        GUI.getController().switchTurns();
        gui.AI_THINKING = false;
    }
}
