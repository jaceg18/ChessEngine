package com.github.jaceg18.chess.ui;

import com.github.jaceg18.chess.pieces.Piece;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
@SuppressWarnings("all")
public class Graphic {
    private static final String SPRITE_PATH = "resources/chess.png";
    private static final Image[] PIECE_ICONS = new Image[12];
    private static final int SPRITE_HEIGHT = 400;
    private static final int SPRITE_WIDTH = 1200;
    private static final int ADJUSTED_SPRITE_SIZE = 64;
    private static final int DEFAULT_SPRITE_SIZE = 200;

    /**
     * The creation of the PIECE_ICONS array and loading of sprites
     */
    static {
        try {
            BufferedImage all = ImageIO.read(new File(SPRITE_PATH));
            int index = 0;

            for (int y = 0; y < SPRITE_HEIGHT; y+=DEFAULT_SPRITE_SIZE){
                for (int x = 0; x < SPRITE_WIDTH; x+=DEFAULT_SPRITE_SIZE){
                    PIECE_ICONS[index] = all.getSubimage(x, y, DEFAULT_SPRITE_SIZE, DEFAULT_SPRITE_SIZE).getScaledInstance(ADJUSTED_SPRITE_SIZE, ADJUSTED_SPRITE_SIZE, BufferedImage.SCALE_SMOOTH);
                    index++;
                }
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Gets the correct icon for the passed piece
     * @param piece The piece passed for the icon
     * @return The icon for the piece
     */
    public static Image getSprite(Piece piece){
        String fen = piece.toString();
        String[] pieceIcons = {"K", "Q", "B", "N", "R", "P", "k", "q", "b", "n", "r", "p"};

        int index = Arrays.asList(pieceIcons).indexOf(fen);
        if (index == -1) {
            return null; // Return null if the piece is not recognized
        }

        return PIECE_ICONS[index];

    }

}
