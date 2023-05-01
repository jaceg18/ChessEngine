package com.github.jaceg18.chess.evaluation;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Utility;
import com.github.jaceg18.chess.identity.Color;
import com.github.jaceg18.chess.identity.GameState;
import com.github.jaceg18.chess.pieces.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class Evaluation {

    public static final Map<Class<? extends Piece>, Integer> COORDINATION_WEIGHTS = Map.of(
            Queen.class, 10,
            Rook.class, 5,
            Bishop.class, 5,
            Knight.class, 5
    );
    public static final Map<Class<? extends Piece>, Integer> TROPISM_WEIGHTS = Map.of(
            Queen.class, 2,
            Rook.class, 1,
            Bishop.class, 1,
            Knight.class, 1
    );
    public static final int MATERIAL_WEIGHT = 2;
    public static int PAWN_WEIGHT = 1;
    public static final int DEVELOPMENT_WEIGHT = 1;
    public static final int QUEEN_OPENING_WEIGHT = 1;
    public static final  int BISHOP_VISION_WEIGHT = 1;
    public static final int KING_SAFETY_WEIGHT = 2;
    public static final int PROMOTION_WEIGHT = 2;
    public static final int CENTER_WEIGHT = 2;
    public static final int KING_TO_CORNER_WEIGHT = 2;
    public static final int MOBILITY_WEIGHT = 1;
    public static final  int ROOK_OPEN_FILE_WEIGHT = 1;
    public static final int CHECK_WEIGHT = 1;
    public static final int KING_TROPISM_WEIGHT = 1;
    public static final int PIECE_COORDINATION_WEIGHT = 2;

    private static final int QUEEN_STARTING_SCORE = 50;
    private static final int CHECK_SCORE = 50;
    private static final int BISHOP_OPEN_DIAGONAL_SCORE = 35;
    private static final int POSITIVE_DEVELOPMENT_SCORE = 30;
    private static final int NEGATIVE_DEVELOPMENT_SCORE = 25;
    private static final int KING_CASTLED_SCORE = 100;
    private static final int KING_EDGE_SCORE = 25;
    private static final int KING_COVERAGE_SCORE = 15;
    private static final int KING_COVERAGE_OTHER_PIECE_SCORE = 10;
    private static final int CENTER_CONTROL_PAWN_SCORE = 35;
    private static final int CENTER_CONTROL_OTHER_PIECE_SCORE = 10;
    private static final int CENTER_ATTACK_SCORE = 15;
    private static final int KING_EDGE_END_GAME_SCORE = 200;
    private static final int MOBILITY_SCORE_MULTIPLIER = 5;
    private static final int ROOK_OPEN_FILE_SCORE = 30;
    private static final int KNIGHT_CENTER_CONTROL_SCORE = 20;
    private static final int PASSED_PAWN_SCORE = 25;
    private static final int DOUBLED_PAWN_SCORE = 25;
    private static final int PAWN_STRUCTURE_SCORE = 10;
    private static final int BASE_PAWN_PROMOTION_SCORE = 10;
    private static final int[][] BAD_KNIGHT_SQUARES = {
            {0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7},
            {1, 0}, {1, 7}, {2, 0}, {2, 7}, {3, 0}, {3, 7},
            {4, 0}, {4, 7}, {5, 0}, {5, 7}, {6, 0}, {6, 7},
            {7, 0}, {7, 1}, {7, 2}, {7, 3}, {7, 4}, {7, 5}, {7, 6}, {7, 7}
    };
    private static final int[][] GOOD_PAWN_SQUARES = {{3, 4}, {3, 3}, {4, 3}, {4, 4}};
    private static final int[] WHITE_QUEEN_STARTING_SQUARE = {7, 3};
    private static final int[] BLACK_QUEEN_STARTING_SQUARE = {0, 3};
    private static final int[][] centerSquares = {{3, 3}, {3, 4}, {4, 3}, {4, 4}};

    /**
     * General method for evaluations, called by AI class.
     *
     * @param board  The board to evaluate
     * @param AITeam The team of the AI
     * @return The total evaluation score while maximizing AI
     */
    public static int evaluate(Board board, Color AITeam) {
        Color opponentsTeam = Color.invert(AITeam);

        if (GameState.getGameState(board) == GameState.END){
            PAWN_WEIGHT = 2;
        }

        if (Utility.isCheckmate(board, AITeam)) return Integer.MIN_VALUE;
        if (Utility.isCheckmate(board, opponentsTeam)) return Integer.MAX_VALUE;
        if (Utility.isStalemate(board)) return 0;

        return getScoreDifference(board, AITeam, opponentsTeam, MATERIAL_WEIGHT, Evaluation::getMaterialScore) +
                getScoreDifference(board, AITeam, opponentsTeam, PAWN_WEIGHT, Evaluation::getPawnScore) +
                getScoreDifference(board, AITeam, opponentsTeam, DEVELOPMENT_WEIGHT, Evaluation::getDevelopmentScore) +
                getScoreDifference(board, AITeam, opponentsTeam, QUEEN_OPENING_WEIGHT, Evaluation::getQueenOpeningScore) +
                getScoreDifference(board, AITeam, opponentsTeam, BISHOP_VISION_WEIGHT, Evaluation::getBishopVisionScore) +
                getScoreDifference(board, AITeam, opponentsTeam, KING_SAFETY_WEIGHT, Evaluation::getKingSafetyScore) +
                getScoreDifference(board, AITeam, opponentsTeam, PROMOTION_WEIGHT, Evaluation::getPromotionScore) +
                getScoreDifference(board, AITeam, opponentsTeam, CENTER_WEIGHT, Evaluation::getCenterScore) +
                getScoreDifference(board, AITeam, opponentsTeam, KING_TO_CORNER_WEIGHT, Evaluation::getKingToCornerScore) +
                getScoreDifference(board, AITeam, opponentsTeam, MOBILITY_WEIGHT, Evaluation::getMobilityScore) +
                getScoreDifference(board, AITeam, opponentsTeam, ROOK_OPEN_FILE_WEIGHT, Evaluation::getRookOpenFileScore) +
                getScoreDifference(board, AITeam, opponentsTeam, CHECK_WEIGHT, Evaluation::getCheckScore) +
                getScoreDifference(board, AITeam, opponentsTeam, KING_TROPISM_WEIGHT, Evaluation::getKingTropismScore) +
                getScoreDifference(board, AITeam, opponentsTeam, PIECE_COORDINATION_WEIGHT, Evaluation::getPieceCoordinationScore)+
                getScoreDifference(board, AITeam, opponentsTeam, KNIGHT_CENTER_CONTROL_SCORE, Evaluation::getKnightCenterControlScore);
    }

    private static int getScoreDifference(Board board, Color AITeam, Color opponentsTeam, int weight, BiFunction<Board, Color, Integer> scoreFunction) {
        return weight * (scoreFunction.apply(board, AITeam) - scoreFunction.apply(board, opponentsTeam));
    }
    /**
     * Evaluates material score
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @return The total material score for a given team
     */
    public static int getMaterialScore(Board board, Color color) {
        int score = 0;
        List<Piece> pieces = board.getTeamPieces(color);

        for (Piece piece : pieces)
            score += piece.getValue();


        return score;
    }

    /**
     * Evaluates if the opponents team is in check
     *
     * @param board The board to evaluate
     * @param color The team that is checking
     * @return The evaluation of checks
     */
    public static int getCheckScore(Board board, Color color) {
        return Utility.inCheck(board, Color.invert(color)) ? CHECK_SCORE : 0;
    }


    /**
     * Predicate for knight center control
     */
    private static final Predicate<Knight> IS_KNIGHT_IN_CENTER =
            knight -> knight.getRow() >= 2 && knight.getRow() <= 5 && knight.getCol() >= 2 && knight.getCol() <= 5;

    /**
     * Evaluates center control for knights
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @return The evaluation for knight center control
     */

    private static int getKnightCenterControlScore(Board board, Color color) {
        return board.getTeamPieces(color).stream().filter(p -> p instanceof Knight).map(p -> (Knight) p).toList()
                .stream()
                .filter(IS_KNIGHT_IN_CENTER)
                .mapToInt(k -> KNIGHT_CENTER_CONTROL_SCORE)
                .sum();
    }
    /**
     * Evaluates and encourages Knight and Pawn development in the opening
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @return The evaluation of early game development
     */

    public static int getDevelopmentScore(Board board, Color color) {
        int score = 0;

        for (int[] goodPawnSquare : GOOD_PAWN_SQUARES) {
            Piece piece = board.getPieceAt(goodPawnSquare[0], goodPawnSquare[1]);
            if (piece instanceof Pawn && piece.getColor() == color) score += POSITIVE_DEVELOPMENT_SCORE;
        }

        for (int[] badKnightSquare : BAD_KNIGHT_SQUARES) {
            Piece piece = board.getPieceAt(badKnightSquare[0], badKnightSquare[1]);
            if (piece instanceof Knight && piece.getColor() == color) score -= NEGATIVE_DEVELOPMENT_SCORE;
        }


        return score;
    }

    /**
     * Evaluates and discourages moving the queen too early in the opening
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @return the evaluation of queens opening score
     */

    public static int getQueenOpeningScore(Board board, Color color) {
        GameState stage = GameState.getGameState(board);
        int score = 0;
        if (stage == GameState.OPENING) {
            int[] queenSquare = (color == Color.WHITE) ? WHITE_QUEEN_STARTING_SQUARE : BLACK_QUEEN_STARTING_SQUARE;
            Piece queen = board.getPieceAt(queenSquare[0], queenSquare[1]);
            if (queen instanceof Queen) score += QUEEN_STARTING_SCORE;
        }

        return score;
    }

    /**
     * Evaluates open diagonals for bishops, similar to open files for rooks.
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @return The evaluation of bishop open diagonals
     */

    public static int getBishopVisionScore(Board board, Color color) {
        List<Bishop> bishops = board.getTeamPieces(color).stream().filter(p -> p instanceof Bishop).map(p -> (Bishop) p).toList();
        int score = 0;
        for (Bishop bishop : bishops) {
            if (Arrays.stream(new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}})
                    .allMatch(dir -> IntStream.range(1, 8).noneMatch(n -> {
                        int newRow = bishop.getRow() + n * dir[0];
                        int newCol = bishop.getCol() + n * dir[1];
                        return newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8 && board.getPieceAt(newRow, newCol) != null;
                    }))) {
                score += BISHOP_OPEN_DIAGONAL_SCORE;
            }
        }
        return score;
    }

    /**
     * Returns a score based on the king tropism of a team's pieces.
     *
     * @param board the current state of the board
     * @param color the color of the team
     * @return the score based on the team's king tropism
     */
    public static int getKingTropismScore(Board board, Color color) {
        King opponentKing = board.getKing(Color.invert(color));
        if (opponentKing != null) {
            return board.getTeamPieces(color).stream().mapToInt(piece -> (7 - Math.max(Math.abs(piece.getRow() - opponentKing.getRow()), Math.abs(piece.getCol() - opponentKing.getCol()))) * TROPISM_WEIGHTS.getOrDefault(piece.getClass(), 0)).sum();
        }
        return 0;
    }


    /**
     * Returns a score based on the coordination of a team's pieces.
     *
     * @param board the current state of the board
     * @param color the color of the team
     * @return the score based on the team's piece coordination
     */
    public static int getPieceCoordinationScore(Board board, Color color) {
        List<Piece> pieces = board.getTeamPieces(color);
        return pieces.stream().mapToInt(piece -> Utility.getAttackingPieces(board, piece.getRow(), piece.getCol(), color).stream().filter(attacker -> attacker.getColor() == color).mapToInt(attacker -> COORDINATION_WEIGHTS.getOrDefault(attacker.getClass(), 0)).sum()).sum();
    }

    /**
     * Evaluates and encourages king safety
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @return The evaluation for king safety
     */

    public static int getKingSafetyScore(Board board, Color color) {
        int score = 0;
        GameState stage = GameState.getGameState(board);
        King king = board.getKing(color);
        if (king != null && stage != GameState.END) {
            int row = king.getRow();
            int col = king.getCol();
            int rowIncrease = (color == Color.BLACK) ? 1 : -1;
            int[][] offsets = {{rowIncrease, 0}, {rowIncrease, -1}, {rowIncrease, 1}};

            score += king.hasCastled() ? KING_CASTLED_SCORE : 0;
            score += !king.hasCastled() && king.hasMoved() ? -100 : 0;
            score += (row == ((color == Color.WHITE) ? 7 : 0)) ? KING_EDGE_SCORE : 0;
            score += (col < 2 || col > 5) ? KING_EDGE_SCORE : 0;

            for (int[] offset : offsets) {
                int newRow = row + offset[0];
                int newCol = col + offset[1];
                if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                    Piece piece = board.getPieceAt(newRow, newCol);
                    if (piece != null && piece.getColor() == color) {
                        score += piece instanceof Pawn ? KING_COVERAGE_SCORE : KING_COVERAGE_OTHER_PIECE_SCORE;
                    }
                }
            }
        }
        return score;
    }

    /**
     * Evaluates and encourages pawn promotion
     *
     * @param board The board to evaluate
     * @param color The color to evaluate
     * @return The evaluation for promoting pawns
     */

    public static int getPromotionScore(Board board, Color color) {
        int score = 0;
        int[][] promotionSquares = getPromotionSquares(color);

        for (int[] promotionSquare : promotionSquares) {
            Piece promotionPawn = board.getPieceAt(promotionSquare[0], promotionSquare[1]);
            if (promotionPawn instanceof Pawn && promotionPawn.getColor() == color) score += 900;
        }


        return score;
    }

    /**
     * Evaluates and encourages center control
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @return the evaluation for center control of a given team
     */
    public static int getCenterScore(Board board, Color color) {
        int score = 0;
        int multiplier = ((GameState.getGameState(board) == GameState.OPENING) ? 2 : 1);
        Color opponentTeam = Color.invert(color);
        for (int[] centerSquare : centerSquares) {
            Piece centerPiece = board.getPieceAt(centerSquare[0], centerSquare[1]);
            if (centerPiece != null && centerPiece.getColor() == color) {
                score += (centerPiece instanceof Pawn ? CENTER_CONTROL_PAWN_SCORE : CENTER_CONTROL_OTHER_PIECE_SCORE) * multiplier;
            }
            if (Utility.isAttacked(board, centerSquare[0], centerSquare[1], opponentTeam)) score += (CENTER_ATTACK_SCORE * multiplier);
        }
        return score;
    }

    /**
     * Evaluates and encourages pushing king to edges of board in end game
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @return the evaluation for pushing king to corner
     */
    public static int getKingToCornerScore(Board board, Color color) {
        if (GameState.getGameState(board) == GameState.END) {
            King king = board.getKing(Color.invert(color));
            if (king != null) {
                int row = king.getRow();
                int col = king.getCol();
                if (row == 0 || row == 7 || col == 0 || col == 7) return KING_EDGE_END_GAME_SCORE;
            }
        }
        return 0;
    }

    /**
     * Evaluates general mobility of all pieces on a team
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @return the evaluation for piece mobility
     */
    public static int getMobilityScore(Board board, Color color) {
        List<Piece> pieces = board.getTeamPieces(color);
        int mobility = pieces.stream().mapToInt(p -> p.getSudoLegalMoves(board).size()).sum();
        return mobility * MOBILITY_SCORE_MULTIPLIER;
    }

    /**
     * Evaluates rook open files
     *
     * @param board the board to evaluate
     * @param color the team to evaluate
     * @return the evaluation for rook open files
     */
    public static int getRookOpenFileScore(Board board, Color color) {
        int score = 0;
        List<Rook> rooks = board.getTeamPieces(color).stream().filter(p -> p instanceof Rook).map(p -> (Rook) p).toList();
        for (Rook rook : rooks) {
            int row = rook.getRow();
            int col = rook.getCol();
            boolean isOpenFile = true;
            for (int i = 0; i < Board.ROWS; i++) {
                if (board.getPieceAt(i, col) != null && i != row) {
                    isOpenFile = false;
                    break;
                }
            }
            if (isOpenFile) score += ROOK_OPEN_FILE_SCORE;
        }


        return score;
    }

    // PAWN EVALUATIONS

    /**
     * Returns the score for all pawn evaluations
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @return the evaluation for pawns
     */
    public static int getPawnScore(Board board, Color color) {
        List<Pawn> pawns = board.getTeamPieces(color).stream().filter(p -> p instanceof Pawn).map(p -> (Pawn) p).toList();
        GameState state = GameState.getGameState(board);
        return getPawnStructureScore(board, color, pawns) + getPassedPawnScore(board, color, pawns) + getDoubledPawnsScore(board, color, pawns) + getPawnPromotionScore(color, pawns, state);
    }


    /**
     * A helper method for getPawnScore
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @param pawns The list of pawns to evaluate
     * @return the evaluation for passed pawns
     */

    public static int getPassedPawnScore(Board board, Color color, List<Pawn> pawns) {
        int score = 0;
        for (Pawn pawn : pawns) {
            int row = pawn.getRow();
            int col = pawn.getCol();
            boolean isPassed = true;

            if (color == Color.WHITE) {
                for (int i = row - 1; i >= 0; i--) {
                    if (board.getPieceAt(i, col) != null || (col > 0 && board.getPieceAt(i, col - 1) instanceof Pawn) || (col < 7 && board.getPieceAt(i, col + 1) instanceof Pawn)) {
                        isPassed = false;
                        break;
                    }
                }

            } else {
                for (int i = row + 1; i <= 7; i++) {
                    if (board.getPieceAt(i, col) != null || (col > 0 && board.getPieceAt(i, col - 1) instanceof Pawn) || (col < 7 && board.getPieceAt(i, col + 1) instanceof Pawn)) {
                        isPassed = false;
                        break;
                    }
                }
            }
            if (isPassed) score += PASSED_PAWN_SCORE;
        }
        return score;
    }

    /**
     * A helper method for getPawnScore
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @param pawns The list of pawns to evaluate
     * @return the evaluation for doubled pawns
     */
    public static int getDoubledPawnsScore(Board board, Color color, List<Pawn> pawns) {
        int score = 0;

        for (Pawn pawn : pawns) {
            int row = pawn.getRow();
            int col = pawn.getCol();
            boolean isDoubled = false;

            if (color == Color.WHITE) {
                for (int i = row - 1; i >= 0; i--) {
                    Piece piece = board.getPieceAt(i, col);
                    if (piece instanceof Pawn && piece.getColor() == color) {
                        isDoubled = true;
                        break;
                    }
                }
            } else {
                for (int i = row + 1; i < Board.ROWS; i++) {
                    Piece piece = board.getPieceAt(i, col);
                    if (piece instanceof Pawn && piece.getColor() == color) {
                        isDoubled = true;
                        break;
                    }
                }
            }
            if (isDoubled) score -= DOUBLED_PAWN_SCORE;
        }
        return score;
    }

    /**
     * A helper method for getPawnScore
     *
     * @param board The board to evaluate
     * @param color The team to evaluate
     * @param pawns The list of pawns to evaluate
     * @return the evaluation for pawn structure
     */
    public static int getPawnStructureScore(Board board, Color color, List<Pawn> pawns) {
        int score = 0;

        for (Pawn pawn : pawns) {
            int row = pawn.getRow();
            int col = pawn.getCol();
            int connectedPawns = 0;

            connectedPawns += countConnectedPawns(board, row, col, color);

            score += connectedPawns * PAWN_STRUCTURE_SCORE;
        }
        return score;
    }
    /**
     * A helper method for getPawnScore
     * @param color The team to evaluate
     * @param pawns The list of pawns to evaluate
     * @param state The current game state
     * @return the evaluation for pawn promotion
     */
    public static int getPawnPromotionScore(Color color, List<Pawn> pawns, GameState state) {
        if (state != GameState.END) {
            return 0;
        }

        int score = 0;
        for (Pawn pawn : pawns) {
            int row = pawn.getRow();
            int distance = pawnPromotionDistance(row, color);
            score += (7 - distance) * BASE_PAWN_PROMOTION_SCORE;
        }
        return score;
    }

    /**
     * A helper method to calculate the distance of a pawn to the promotion rank
     *
     * @param row   the row of the pawn
     * @param color the color of the pawn
     * @return the distance to the promotion rank
     */
    public static int pawnPromotionDistance(int row, Color color) {
        return color == Color.WHITE ? 7 - row : row;
    }

    /**
     * A helper method for inner helper methods in getPawnScore
     *
     * @param board The board to evaluate
     * @param row   the row of a pawn
     * @param col   the col of a pawn
     * @param color The team to evaluate
     * @return the number of connected pawns
     */
    public static int countConnectedPawns(Board board, int row, int col, Color color) {
        int connectedPawns = 0;
        int[] rowOffsets = {1, -1};
        int[] colOffsets = {1, -1};

        for (int rowOffset : rowOffsets) {
            for (int colOffset : colOffsets) {
                int newRow = row + rowOffset;
                int newCol = col + colOffset;

                if (isInBounds(newRow, newCol)) {
                    Piece piece = board.getPieceAt(newRow, newCol);
                    if (piece instanceof Pawn && piece.getColor() == color) connectedPawns++;
                }
            }
        }
        return connectedPawns;
    }

    /**
     * Helper method for all evaluations
     *
     * @param row the row to check
     * @param col the col to check
     * @return a boolean stating whether the coordinates are in bounds
     */
    public static boolean isInBounds(int row, int col) {
        return row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS;
    }

    /**
     * A helper method to get all promotion squares
     *
     * @param color The team
     * @return All promotion squares for a given team
     */
    public static int[][] getPromotionSquares(Color color) {
        return (color == Color.WHITE) ? new int[][]{{0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}} :
                new int[][]{{7, 0}, {7, 1}, {7, 2}, {7, 3}, {7, 4}, {7, 5}, {7, 6}, {7, 7}};
    }



}
