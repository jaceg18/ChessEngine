package com.github.jaceg18.chess;

import com.github.jaceg18.chess.identity.Color;
import com.github.jaceg18.chess.identity.Flag;
import com.github.jaceg18.chess.identity.MoveType;
import com.github.jaceg18.chess.pieces.King;
import com.github.jaceg18.chess.pieces.Piece;
import com.github.jaceg18.chess.ui.GUI;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Board {
    public static final int ROWS = 8;
    public static final int COLS = 8;
    private int skippedMoves;
    private Move lastMove;
    private HashMap<Integer, Integer> positionHistory;
    private static long[][][] zobristTable;
    private Piece[][] pieces;

    public Board() {
        this.pieces = Utility.getDefaultBoard();
        this.positionHistory = new HashMap<>();

        if (zobristTable == null) {
            initZobristTable();
        }

    }

    /**
     * Creates a deep copy of the board
     *
     * @return a deep copy of the board
     */
    public Board getCopy() {
        Board board = new Board();
        board.pieces = new Piece[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Piece piece = getPieceAt(row, col);
                if (piece != null)
                    board.pieces[row][col] = piece.copy();
            }
        }
        board.positionHistory = new HashMap<>(this.positionHistory);
        return board;
    }

    /**
     * Skips the turn for the AI
     */
    public void skipMove() {
        skippedMoves++;
    }

    /**
     * Resets skipped turns
     */

    public void undoSkipMove() {
        if (skippedMoves > 0) {
            skippedMoves--;
        }
    }

    /**
     * Gets the hash code value for this Board.
     *
     * @return The hash code value for this Board.
     */
    public long zobristHashCode() {
        long hash = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Piece piece = getPieceAt(row, col);
                if (piece != null) {
                    int pieceIndex = Utility.pieceToIndex(piece);
                    hash ^= zobristTable[pieceIndex][row][col];
                }
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Board other = (Board) obj;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Piece thisPiece = pieces[row][col];
                Piece otherPiece = other.getPieceAt(row, col);
                if (thisPiece == null && otherPiece != null) {
                    return false;
                } else if (thisPiece != null && !thisPiece.equals(otherPiece)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Initializes the zobrist table
     */

    private void initZobristTable() {
        Random rand = new Random();
        zobristTable = new long[12][8][8]; // 12 unique piece types (6 for each color), 8 rows, 8 cols
        for (int piece = 0; piece < 12; piece++) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    zobristTable[piece][row][col] = rand.nextLong();
                }
            }
        }
    }

    /**
     * Gets the piece list for this board
     *
     * @return the piece list for this board
     */
    public Piece[][] getPieces() {
        return pieces;
    }

    /**
     * Gets the piece at a certain spot
     *
     * @param row The row to search for a piece
     * @param col The col to search for a piece
     * @return The piece at the row and col
     */
    public Piece getPieceAt(int row, int col) {
        return pieces[row][col];
    }

    /**
     * Finds and returns the king
     *
     * @param color The color of the king
     * @return The king, null otherwise.
     */
    public King getKing(Color color) {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Piece piece = getPieceAt(i, j);
                if (piece instanceof King) {
                    if (piece.getColor() == color) {
                        return (King) piece;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the last move played on the board
     *
     * @return the last move played
     */
    public Move getLastMove() {
        return lastMove;
    }

    /**
     * Used to save previous moves and board positions
     * Also used to determine threefold repetition draw
     */
    private void updatePositionHistory() {
        int positionHashCode = this.hashCode();
        positionHistory.put(positionHashCode, positionHistory.getOrDefault(positionHashCode, 0) + 1);
    }

    /**
     * Makes a move on the board
     *
     * @param move The move to make
     */
    public void makeMove(Move move, Flag flag) {
        lastMove = move;

        int toRow = move.getToRow();
        int fromRow = move.getFromRow();
        int toCol = move.getToCol();
        int fromCol = move.getFromCol();
        Piece piece = move.getPiece();

        if (flag == Flag.NORMAL) {
            GUI.gameNotation += move.toNotation() + " ";
            updatePositionHistory();
        }

        if (Utility.isMoveCastle(move)) {
            makeCastleMove(move);
        } else {
            pieces[toRow][toCol] = piece;
            pieces[fromRow][fromCol] = null;

            move.setSourcePieceHasMoved(move.getPiece().hasMoved());

            if (Utility.isMoveCapture(this, move)) {
                move.setCapturePieceHasMoved(move.getCapturedPiece().hasMoved());
                move.getCapturedPiece().setCaptured(true);
            }

            piece.setMoved(true);
            piece.setPosition(toRow, toCol);

            // Handle pawn promotion
            if (Utility.isPromotionMove(move)) {
                pieces[toRow][toCol] = move.getPromotionPiece();
            }

        }
    }

    /**
     * Undo a move on the board
     *
     * @param move The move to undo
     */
    public void undoMove(Move move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();
        Piece movedPiece = move.getPiece();

        if (Utility.isMoveCastle(move)) {
            undoCastleMove(move);
        } else {
            pieces[fromRow][fromCol] = movedPiece;
            pieces[toRow][toCol] = move.getCapturedPiece();

            if (Utility.isMoveCapture(this, move)) {
                move.getCapturedPiece().setMoved(move.getCapturedPieceHasMoved());
                move.getCapturedPiece().setCaptured(false);
                move.getCapturedPiece().setPosition(toRow, toCol);
            }

            movedPiece.setMoved(move.getSourcePieceHasMoved());
            movedPiece.setPosition(fromRow, fromCol);
        }
    }

    /**
     * A helper method for makeMove that handles castle moves
     *
     * @param move The castle move to make
     */
    private void makeCastleMove(Move move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        int rookToCol = (toCol > fromCol) ? toCol - 1 : toCol + 1;
        int rookCol = (toCol > fromCol) ? 7 : 0;

        Piece rook = getPieceAt(fromRow, rookCol);
        Piece king = move.getPiece();

        pieces[toRow][toCol] = king;
        king.setPosition(toRow, toCol);
        pieces[fromRow][fromCol] = null;
        king.setMoved(true);
        ((King) king).setHasCastled(true);

        pieces[fromRow][rookToCol] = rook;
        rook.setPosition(fromRow, rookToCol);
        pieces[fromRow][rookCol] = null;
        rook.setMoved(true);
    }

    /**
     * A helper method for undoMove that handles undoing castle moves
     *
     * @param move The castle move to undo
     */
    private void undoCastleMove(Move move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toCol = move.getToCol();
        int toRow = move.getToRow();

        int rookCol = (toCol > fromCol) ? 7 : 0;
        int rookToCol = (toCol > fromCol) ? toCol - 1 : toCol + 1;

        Piece rook = getPieceAt(fromRow, rookToCol);
        Piece king = move.getPiece();

        pieces[fromRow][fromCol] = king;
        king.setPosition(fromRow, fromCol);
        pieces[toRow][toCol] = null;
        king.setMoved(false);
        ((King) king).setHasCastled(false);

        pieces[fromRow][rookCol] = rook;
        rook.setPosition(fromRow, rookCol);
        pieces[fromRow][rookToCol] = null;
        rook.setMoved(false);
    }

    /**
     * Gets moves for a given team
     *
     * @param type  The type of move to request
     * @param color The team to get the moves from
     * @return The team moves based on given type
     */
    public List<Move> getMoves(MoveType type, Color color) {
        List<Piece> teamPieces = getTeamPieces(color);
        List<Move> teamMoves = teamPieces.stream()
                .flatMap(piece -> getPieceMoves(piece).stream())
                .collect(Collectors.toList());

        if (type == MoveType.LEGAL)
            return teamMoves;

        Map<MoveType, List<Move>> movesByType = teamMoves.stream()
                .collect(Collectors.groupingBy(Move::getMoveType));

        if (type == MoveType.ORDERED) {
            List<Move> orderedMoves = new ArrayList<>();

            for (MoveType moveType : List.of(MoveType.CHECKMATE, MoveType.CAPTURE, MoveType.PROMOTION, MoveType.CHECK, MoveType.CASTLE)) {
                orderedMoves.addAll(movesByType.getOrDefault(moveType, List.of()));
            }

            List<Move> remainingMoves = movesByType.getOrDefault(MoveType.LEGAL, List.of());
            Collections.shuffle(remainingMoves);
            orderedMoves.addAll(remainingMoves);

            return orderedMoves;
        }

        return movesByType.getOrDefault(type, List.of());
    }

    /**
     * Gets legal piece moves. Helper for GUI and getMoves
     *
     * @param piece The piece to get legal moves from
     * @return A list of legal moves for the piece.
     */
    @SuppressWarnings("all")
    public List<Move> getPieceMoves(Piece piece) {
        List<Move> pieceMoves = piece.getSudoLegalMoves(this);
        pieceMoves = organizeMoves(pieceMoves);

        Predicate<Move> illegalMove = move -> move.getMoveType() == MoveType.ILLEGAL;

        pieceMoves.removeIf(illegalMove);

        return pieceMoves;
    }

    /**
     * Helper method to get all pieces for a given team
     *
     * @param color The team
     * @return A list of pieces from the provided color
     */
    public List<Piece> getTeamPieces(Color color) {
        Predicate<Piece> sameColor = piece -> piece != null && piece.getColor() == color;

        return Arrays.stream(pieces)
                .flatMap(Arrays::stream)
                .filter(sameColor)
                .collect(Collectors.toList());
    }

    /**
     * A helper method to organize and identify move types
     *
     * @param moves The list of moves to organize
     * @return The list of organized moves
     */
    private List<Move> organizeMoves(List<Move> moves) {
        for (Move move : moves) {
            if (move.getMoveType() == null) {
                boolean typeSet = Utility.isMoveCapture(this, move);
                if (Utility.doesMoveCheckOpponent(this, move) && !typeSet) typeSet = true;
                if (Utility.doesMoveLeadToCheck(this, move) && !typeSet) typeSet = true;
                if (Utility.isMoveCastle(move) && !typeSet) typeSet = true;
                if (Utility.isPromotionMove(move) && !typeSet) typeSet = true;
                if (!typeSet || move.getMoveType() == null) move.setMoveType(MoveType.LEGAL);
            }
        }
        return moves;
    }

    /**
     * Organizes a single move
     *
     * @param move The move to organize
     * @return The organized move
     */
    public Move organizeMove(Move move) {
        if (move.getMoveType() == null) {
            boolean typeSet = Utility.isMoveCapture(this, move);
            if (Utility.doesMoveCheckOpponent(this, move) && !typeSet) typeSet = true;
            if (Utility.doesMoveLeadToCheck(this, move) && !typeSet) typeSet = true;
            if (Utility.isMoveCastle(move) && !typeSet) typeSet = true;
            if (Utility.isPromotionMove(move) && !typeSet) typeSet = true;
            if (!typeSet || move.getMoveType() == null) move.setMoveType(MoveType.LEGAL);
        }
        return move;
    }

    /**
     * A helper method for gui to determine is a move is legal
     *
     * @param move The move to check for legality
     * @return A boolean stating whether the move is legal
     */
    public boolean isMoveValid(Move move) {
        Piece piece = move.getPiece();
        for (Move pieceMove : getPieceMoves(piece)) {
            if (move.getToRow() == pieceMove.getToRow() && move.getToCol() == pieceMove.getToCol() && pieceMove.getPiece() == move.getPiece()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets ordered moves for MVV-LVA (Method is for AI use only)
     *
     * @param color   The color to get moves for
     * @param history Recent history for AI
     * @return The new ordered moves list
     */
    public List<Move> getSortedMoves(Color color, int[][] history) {
        List<Move> moves = getMoves(MoveType.ORDERED, color);
        moves.sort((move1, move2) -> {
            int move1Score = 0;
            int move2Score = 0;

            // Assign scores based on MVV-LVA
            if (move1.getCapturedPiece() != null) {
                move1Score = move1.getCapturedPiece().getValue() - move1.getPiece().getValue();
            }
            if (move2.getCapturedPiece() != null) {
                move2Score = move2.getCapturedPiece().getValue() - move2.getPiece().getValue();
            }

            // If MVV-LVA scores are the same, compare based on history heuristic scores
            if (move1Score == move2Score) {
                move1Score = history[move1.getFromRow()][move1.getFromCol()];
                move2Score = history[move2.getFromRow()][move2.getFromCol()];
            }

            return Integer.compare(move2Score, move1Score);
        });

        return moves;
    }
}
