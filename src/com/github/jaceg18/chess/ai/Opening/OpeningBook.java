package com.github.jaceg18.chess.ai.Opening;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.Utility;
import com.github.jaceg18.chess.identity.Color;
import com.github.jaceg18.chess.ui.GUI;

import java.io.FileReader;
import java.util.*;
import java.util.List;

@SuppressWarnings("all")
public class OpeningBook {
    private final String OPENING_FILE_PATH = "data/openings.txt";
    public final static char[] COLUMNS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    private List<String> openings;
    private final Color AITeam;
    private boolean whitesPlayed;

    /**
     * Constructor for opening book
     * @param AITeam The team thats using the opening book
     */
    public OpeningBook(Color AITeam) {
        initOpenings();
        this.AITeam = AITeam;
        this.whitesPlayed = false;
    }

    /**
     * Gets opening move based on game notation
     * @param board The board to check
     * @return An opening move
     */

    public Move getOpeningMove(Board board) {
        if (!whitesPlayed && AITeam == Color.WHITE){
            String opening = openings.get(new Random().nextInt(openings.size()));
            whitesPlayed = true;
            return Utility.getMoveFromNotation(opening.split(" ")[0], board, AITeam);
        } else {
            for (String opening : openings){
                if (opening.startsWith(GUI.gameNotation)){
                    String nextMoveNotation = opening.split(" ")[GUI.gameNotation.split(" ").length];
                    return Utility.getMoveFromNotation(nextMoveNotation, board, AITeam);
                }
            }
        }
        return null;
    }

    /**
     * Adds 20,000 opening's from a file.
     */
    private void initOpenings() {
        List<String> opening = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new FileReader(OPENING_FILE_PATH));
            while (scanner.hasNextLine()) {
                opening.add(scanner.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.openings = opening;

        Collections.shuffle(opening);
    }

}
