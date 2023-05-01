package com.github.jaceg18.chess;

import com.github.jaceg18.chess.identity.Color;
import com.github.jaceg18.chess.identity.Flag;
import com.github.jaceg18.chess.identity.MoveType;
import com.github.jaceg18.chess.pieces.*;

import java.util.ArrayList;
import java.util.List;

import static com.github.jaceg18.chess.identity.MoveType.*;


@SuppressWarnings("unused")
public class Utility {

    // Constants for piece values
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 350;
    private static final int BISHOP_VALUE = 375;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 0;
    private static final int[][] knightOffsets = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};

    // Bishops, rooks, and queens
    private static final int[][] slidingOffsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

    /**
     * Gets the value of a piece based on its type.
     *
     * @param piece the piece to get the value of
     * @return the value of the piece
     */
    public static int getValueByPiece(Piece piece) {
        // Use switch statement for better performance
        return switch (getNameByPiece(piece)) {
            case "Pawn" -> PAWN_VALUE;
            case "Knight" -> KNIGHT_VALUE;
            case "Bishop" -> BISHOP_VALUE;
            case "Rook" -> ROOK_VALUE;
            case "Queen" -> QUEEN_VALUE;
            case "King" -> KING_VALUE;
            default -> throw new IllegalArgumentException("Invalid piece type: " + getNameByPiece(piece));
        };
    }

    /**
     * Gets the name of a piece based on its type.
     *
     * @param piece the piece to get the name of
     * @return the name of the piece
     */
    public static String getNameByPiece(Piece piece) {
        if (piece instanceof Pawn) return "Pawn";
        if (piece instanceof Knight) return "Knight";
        if (piece instanceof Bishop) return "Bishop";
        if (piece instanceof Rook) return "Rook";
        if (piece instanceof Queen) return "Queen";
        if (piece instanceof King) return "King";

        throw new IllegalArgumentException("Invalid Piece Type");
    }


    /**
     * Gets piece to index for zobrist hashing
     *
     * @param piece The piece to index
     * @return The index of the piece
     */

    static int pieceToIndex(Piece piece) {
        int base = piece.getColor() == Color.WHITE ? 0 : 6;
        return switch (piece.getClass().getSimpleName()) {
            case "Pawn" -> base;
            case "Knight" -> base + 1;
            case "Bishop" -> base + 2;
            case "Rook" -> base + 3;
            case "Queen" -> base + 4;
            case "King" -> base + 5;
            default -> throw new IllegalArgumentException("Invalid piece type");
        };
    }

    /**
     * Gets the FEN representation of a piece.
     *
     * @param piece the piece to get the FEN of
     * @return the FEN character for the piece
     */
    public static char getFenByPiece(Piece piece) {
        // Use switch statement for better performance
        return switch (getNameByPiece(piece)) {
            case "Pawn" -> piece.getColor() == Color.WHITE ? 'P' : 'p';
            case "Knight" -> piece.getColor() == Color.WHITE ? 'N' : 'n';
            case "Bishop" -> piece.getColor() == Color.WHITE ? 'B' : 'b';
            case "Rook" -> piece.getColor() == Color.WHITE ? 'R' : 'r';
            case "Queen" -> piece.getColor() == Color.WHITE ? 'Q' : 'q';
            case "King" -> piece.getColor() == Color.WHITE ? 'K' : 'k';
            default -> throw new IllegalArgumentException("Invalid piece type: " + getNameByPiece(piece));
        };
    }

    /**
     * Gets a new default chessboard
     *
     * @return A default chess board
     */
    public static Piece[][] getDefaultBoard() {
        Piece[][] board = new Piece[Board.ROWS][Board.COLS];
        board[0][0] = new Rook(Color.BLACK, 0, 0);
        board[0][1] = new Knight(Color.BLACK, 0, 1);
        board[0][2] = new Bishop(Color.BLACK, 0, 2);
        board[0][3] = new Queen(Color.BLACK, 0, 3);
        board[0][4] = new King(Color.BLACK, 0, 4);
        board[0][5] = new Bishop(Color.BLACK, 0, 5);
        board[0][6] = new Knight(Color.BLACK, 0, 6);
        board[0][7] = new Rook(Color.BLACK, 0, 7);

        for (int i = 0; i < Board.COLS; i++) {
            board[1][i] = new Pawn(Color.BLACK, 1, i);
        }

        for (int i = 0; i < Board.COLS; i++) {
            board[6][i] = new Pawn(Color.WHITE, 6, i);
        }

        board[7][0] = new Rook(Color.WHITE, 7, 0);
        board[7][1] = new Knight(Color.WHITE, 7, 1);
        board[7][2] = new Bishop(Color.WHITE, 7, 2);
        board[7][3] = new Queen(Color.WHITE, 7, 3);
        board[7][4] = new King(Color.WHITE, 7, 4);
        board[7][5] = new Bishop(Color.WHITE, 7, 5);
        board[7][6] = new Knight(Color.WHITE, 7, 6);
        board[7][7] = new Rook(Color.WHITE, 7, 7);

        return board;
    }

    /**
     * Determines if a move is a capture, and sets the move type to capture.
     *
     * @return the boolean that states whether a move is a capture
     */
    public static boolean isMoveCapture(Board board, Move move) {
        Color color = move.getPiece().getColor();
        Color opponentsColor = Color.invert(color);

        int toRow = move.getToRow();
        int toCol = move.getToCol();

        Piece capturedPiece = board.getPieceAt(toRow, toCol);
        if (capturedPiece != null) {
            if (capturedPiece.getColor() == opponentsColor) {
                move.setMoveType(MoveType.CAPTURE);
                move.setCapturedPiece(capturedPiece);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a certain square is attacked by the opposite color
     *
     * @param board The board to check
     * @param row   The row to check
     * @param col   The col to check
     * @param color The color of the team being attacked
     * @return A boolean that tells whether a square is attacked by the opposite team.
     */
    public static boolean isAttacked(Board board, int row, int col, Color color) {
        Color opponentsColor = Color.invert(color);

        // Check for opponent's pawn
        int pawnDirection = (color == Color.WHITE) ? -1 : 1;
        if (isOpponentPiece(board, opponentsColor, row + pawnDirection, col - 1, Pawn.class) ||
                isOpponentPiece(board, opponentsColor, row + pawnDirection, col + 1, Pawn.class)) return true;

        // Check for opponent's knight
        for (int[] offset : knightOffsets) {
            if (isOpponentPiece(board, opponentsColor, row + offset[0], col + offset[1], Knight.class)) return true;
        }

        // Check for opponent's sliding pieces (bishops, rooks, and queens)
        for (int[] offset : slidingOffsets) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];

            while (isValidCoordinate(newRow, newCol)) {
                Piece piece = board.getPieceAt(newRow, newCol);
                if (piece != null) {
                    if (piece.getColor() == opponentsColor) {
                        if (piece instanceof Queen ||
                                piece instanceof Bishop && (offset[0] == offset[1] || offset[0] == -offset[1]) ||
                                piece instanceof Rook && (offset[0] == 0 || offset[1] == 0)) return true;
                    }
                    break; // Stop searching in this direction if a piece is encountered
                }
                newRow += offset[0];
                newCol += offset[1];
            }
        }

        // Check for opponent's king
        for (int[] offset : slidingOffsets) {
            if (isOpponentPiece(board, opponentsColor, row + offset[0], col + offset[1], King.class)) return true;
        }

        return false;
    }

    /**
     * Gets move from standard chess notation
     *
     * @param notation The notation
     * @param team     The team
     * @return The move
     */
    public static Move getMoveFromNotation(String notation, Board board, Color team) {
        List<Move> moves = board.getMoves(LEGAL, team);

        for (Move move : moves) {
            if (move.toNotation().equals(notation))
                return move;
        }
        return null;
    }

    /**
     * Returns a list of pieces attacking a certain position on the board.
     *
     * @param board the current state of the board
     * @param row   the row coordinate of the position being attacked
     * @param col   the column coordinate of the position being attacked
     * @param color the color of the attacking team
     * @return the list of pieces attacking the position
     */
    public static List<Piece> getAttackingPieces(Board board, int row, int col, Color color) {
        List<Piece> attackingPieces = new ArrayList<>();
        Color opponentColor = Color.invert(color);

        // Check for opponent's pawn
        int pawnDirection = (color == Color.WHITE) ? -1 : 1;
        if (isOpponentPiece(board, opponentColor, row + pawnDirection, col - 1, Pawn.class)) {
            attackingPieces.add(board.getPieceAt(row + pawnDirection, col - 1));
        }
        if (isOpponentPiece(board, opponentColor, row + pawnDirection, col + 1, Pawn.class)) {
            attackingPieces.add(board.getPieceAt(row + pawnDirection, col + 1));
        }

        // Check for opponent's knight
        for (int[] offset : knightOffsets) {
            if (isOpponentPiece(board, opponentColor, row + offset[0], col + offset[1], Knight.class)) {
                attackingPieces.add(board.getPieceAt(row + offset[0], col + offset[1]));
            }
        }

        // Check for opponent's sliding pieces (bishops, rooks, and queens)
        for (int[] offset : slidingOffsets) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];

            while (isValidCoordinate(newRow, newCol)) {
                Piece piece = board.getPieceAt(newRow, newCol);
                if (piece != null) {
                    if (piece.getColor() == opponentColor) {
                        if (piece instanceof Queen ||
                                piece instanceof Bishop && (offset[0] == offset[1] || offset[0] == -offset[1]) ||
                                piece instanceof Rook && (offset[0] == 0 || offset[1] == 0)) {
                            attackingPieces.add(piece);
                        }
                    }
                    break; // Stop searching in this direction if a piece is encountered
                }
                newRow += offset[0];
                newCol += offset[1];
            }
        }

        // Check for opponent's king
        for (int[] offset : slidingOffsets) {
            if (isOpponentPiece(board, opponentColor, row + offset[0], col + offset[1], King.class)) {
                attackingPieces.add(board.getPieceAt(row + offset[0], col + offset[1]));
            }
        }

        return attackingPieces;
    }

    /**
     * Checks if a certain team is in check
     *
     * @param board The board to check
     * @param color The team to determine in check
     * @return A boolean stating whether the given color is in check.
     */
    public static boolean inCheck(Board board, Color color) {
        King king = board.getKing(color);
        if (king == null) return false;

        int kingRow = king.getRow();
        int kingCol = king.getCol();

        return isAttacked(board, kingRow, kingCol, color);
    }

    /**
     * Helper method for isAttacked and inCheck to determine whether a piece is an opponents
     *
     * @param board         The board to check
     * @param opponentColor The color of the opponent
     * @param row           The row of the opponent
     * @param col           The col of the opponent
     * @param pieceClass    The type of piece ot check for
     * @return A boolean stating whether piece belongs to an opponent.
     */
    private static boolean isOpponentPiece(Board board, Color opponentColor, int row, int col, Class<? extends Piece> pieceClass) {
        if (!isValidCoordinate(row, col)) return false;
        Piece piece = board.getPieceAt(row, col);
        return piece != null && piece.getClass() == pieceClass && piece.getColor() == opponentColor;
    }

    /**
     * Helper method for inCheck, isOpponentPiece, and isAttacked that determines whether a pair of coordinates are in bounds
     *
     * @param row The row to check
     * @param col The col to check
     * @return A boolean stating whether the coordinates are in bounds.
     */
    private static boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS;
    }

    /**
     * Checks if a move gets the opponent into check
     *
     * @param board The board to check
     * @param move  The move to check
     * @return A boolean stating whether the move leads to the opponent in check
     */
    public static boolean doesMoveCheckOpponent(Board board, Move move) {
        board.makeMove(move, Flag.SEARCHING);
        boolean isCheck = inCheck(board, Color.invert(move.getPiece().getColor()));
        board.undoMove(move);

        if (isCheck) move.setMoveType(CHECK);

        return isCheck;
    }

    /**
     * Checks if a move causes the moving team check (Example, a pinned bishop moving)
     *
     * @param board The board to check
     * @param move  The move to check
     * @return A boolean stating whether the move leads to the moving team being in check.
     */
    public static boolean doesMoveLeadToCheck(Board board, Move move) {
        board.makeMove(move, Flag.SEARCHING);
        boolean isCheck = inCheck(board, move.getPiece().getColor());
        board.undoMove(move);

        if (isCheck) move.setMoveType(MoveType.ILLEGAL);

        return isCheck;
    }

    /**
     * Determines if a move is checkmate for the opponents team
     *
     * @param board The board to check
     * @param move  The move to check
     * @return A boolean stating whether the move is checkmate for the opponent
     */
    public boolean isMoveCheckmate(Board board, Move move) {
        board.makeMove(move, Flag.SEARCHING);
        boolean isCheckmate = isCheckmate(board, Color.invert(move.getPiece().getColor()));
        board.undoMove(move);

        return isCheckmate;
    }

    /**
     * Determines if either team is in stalemate
     *
     * @param board The board to check
     * @return A boolean stating whether a team is in stalemate
     */
    public static boolean isStalemate(Board board) {
        return board.getMoves(ORDERED, Color.WHITE).size() == 0 || board.getMoves(ORDERED, Color.BLACK).size() == 0 || isInsufficientMaterial(board);
    }

    public static boolean isInsufficientMaterial(Board board) {
        List<Piece> whitePieces = board.getTeamPieces(Color.WHITE);
        List<Piece> blackPieces = board.getTeamPieces(Color.BLACK);

        int whiteSize = whitePieces.size();
        int blackSize = blackPieces.size();

        boolean insufficientMaterial = false;

        if (whiteSize == 1 && blackSize == 1) {
            insufficientMaterial = true;
        } else if (whiteSize == 2 && blackSize == 1) {
            for (Piece piece : whitePieces) {
                if (piece instanceof Bishop || piece instanceof Knight) {
                    insufficientMaterial = true;
                    break;
                }
            }
        } else if (whiteSize == 1 && blackSize == 2) {
            for (Piece piece : blackPieces) {
                if (piece instanceof Bishop || piece instanceof Knight) {
                    insufficientMaterial = true;
                    break;
                }
            }
        }
        return insufficientMaterial;
    }

    /**
     * Determines if the current state of the board is checkmate for a given color.
     *
     * @param board The board to check
     * @param color The color to check
     * @return A boolean stating whether a team is in checkmate
     */
    public static boolean isCheckmate(Board board, Color color) {
        return inCheck(board, color) && board.getMoves(ORDERED, color).size() == 0;
    }

    /**
     * Determines if a move is a castling move
     *
     * @param move The move to check
     * @return A boolean stating whether a move is a castle move
     */
    public static boolean isMoveCastle(Move move) {
        boolean castleMove = false;
        if (move.getPiece() instanceof King && Math.abs(move.getToCol() - move.getFromCol()) == 2) {
            castleMove = true;
            move.setMoveType(MoveType.CASTLE);
        }
        return castleMove;
    }

    /**
     * Determines if a move is a promotion move
     *
     * @param move The move to check
     * @return A boolean stating whether a move is a castle move
     */
    public static boolean isPromotionMove(Move move) {
        boolean isPromotionMove = false;
        if (move.getPiece() instanceof Pawn && (move.getToRow() == 0 || move.getToRow() == 7)) {
            isPromotionMove = true;
            move.setMoveType(MoveType.PROMOTION);
            Piece promotionPiece = new Queen(move.getPiece().getColor(), move.getToRow(), move.getToCol());
            promotionPiece.setPosition(move.getToRow(), move.getToCol());
            move.setPromotionPiece(promotionPiece);
        }
        return isPromotionMove;
    }

    /**
     * Checks if the game is over
     *
     * @param board The board to see if the game is finished
     * @return A boolean stating whether the game is over
     */
    public static boolean isGameOver(Board board) {
        return Utility.isStalemate(board) || Utility.isCheckmate(board, Color.WHITE) || Utility.isCheckmate(board, Color.BLACK);
    }

}
