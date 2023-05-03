
import com.github.jaceg18.chess.identity.Color;
import com.github.jaceg18.chess.ui.GUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        JFrame frame = new JFrame("Chess V2");

        Color playerTeam = Color.BLACK;
        GUI gui = new GUI(playerTeam);

        frame.add(gui);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}