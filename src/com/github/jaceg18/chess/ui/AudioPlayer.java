package com.github.jaceg18.chess.ui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class AudioPlayer {

    private static final String captureSoundPath = "resources/capture.wav";
    private static final String moveSoundPath = "resources/move.wav";

    /**
     * Plays sound on move
     * @param capture Whether the move was a capture
     */
    public static void playSound(boolean capture) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream((capture) ? new File(captureSoundPath) : new File(moveSoundPath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
