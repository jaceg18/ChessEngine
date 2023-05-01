package com.github.jaceg18.chess.identity;

public enum Color {
    WHITE,
    BLACK;
    public static Color invert(Color color){
        return color == (WHITE) ? BLACK : WHITE;
    }
}
