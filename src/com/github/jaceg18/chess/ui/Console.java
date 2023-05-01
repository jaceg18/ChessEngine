package com.github.jaceg18.chess.ui;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.evaluation.Evaluation;
import com.github.jaceg18.chess.identity.Color;

import java.util.Scanner;
@SuppressWarnings("all")
public class Console extends Thread {

    GUI gui;

    /**
     * Constructor for in game console
     *
     * @param gui The game GUI
     */
    public Console(GUI gui) {
        this.gui = gui;
    }

    /**
     * The thread and commands
     */

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine().toLowerCase();

            if (input.equals("evaluate") || input.equals("eval")) {
                Board board = gui.getBoard();
                System.out.println("Whites Evaluation: " + Evaluation.evaluate(board, Color.WHITE));
                System.out.println("Blacks Evaluation: " + Evaluation.evaluate(board, Color.BLACK));
            }
            if (input.equals("flip")) {
                gui.flip();
                System.out.println("Board has been flipped");
            }
            if (input.equals("restart") || input.equals("new") || input.equals("reset")){
                gui.resetGame();
            }
        }
    }

}
