package com.github.jaceg18.chess.ai;

import com.github.jaceg18.chess.Board;
import com.github.jaceg18.chess.Move;
import com.github.jaceg18.chess.ScoredMove;
import com.github.jaceg18.chess.Utility;
import com.github.jaceg18.chess.ai.Opening.OpeningBook;
import com.github.jaceg18.chess.evaluation.Evaluation;
import com.github.jaceg18.chess.identity.Color;
import com.github.jaceg18.chess.identity.Flag;
import com.github.jaceg18.chess.identity.GameState;
import com.github.jaceg18.chess.identity.MoveType;
import com.github.jaceg18.chess.ui.AudioPlayer;
import com.github.jaceg18.chess.ui.GUI;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


@SuppressWarnings("all")
public class AI {
    private Map<Long, TranspositionEntry> transpositionTable;
    protected final Color AITeam;
    private int[][] history;
    private int depth;
    private boolean depthIncrease;
    private final int numThreads = 4;
    private OpeningBook openingBook;
    private final int openingMax = 5;
    private int openingAmount = 0;
    private final ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    /**
     AI constructor that sets up initial properties of the AI
     @param AITeam The color of the AI
     @param maxSearchTimeMillis The maximum amount of time allowed for the AI to make a move
     */
    public AI(Color AITeam, int depth){
        this.AITeam = AITeam;
        this.depth = depth;
        this.depthIncrease = false;
        this.transpositionTable = new HashMap<>();
        history = new int[8][8];

        this.openingBook = new OpeningBook(AITeam);
    }

    /**
     Makes a move for the AI on the provided board
     @param board The board on which to make a move
     @return The new board with the AI's move made
     */

    public synchronized Board move(Board board){
        boolean openingFailed = true;
        if (openingMax >= openingAmount){
           Move move = openingBook.getOpeningMove(board);
           if (move != null){
               openingFailed = false;
               openingAmount++;
               board.makeMove(move, Flag.NORMAL);
               return board;
           }
        }

        if (openingFailed) {

            if (GameState.getGameState(board) == GameState.END && !depthIncrease) {
                this.depth += 2;
                this.depthIncrease = true;
            }
            Move move = search(board, depth);
            board.makeMove(move, Flag.NORMAL);
            AudioPlayer.playSound(move.getMoveType() == MoveType.CAPTURE);
            return board;
        }
        return board;
    }

    /**

     Uses iterative deepening search to find the best move for the AI within the given time limit

     @param board The board on which to search for the best move

     @return The best move for the AI within the given time limit
     */

    /**
     * Searches the best move using alpha beta pruning
     * @param depth The depth to search at
     * @param startTime The starting time of the search
     * @param searchCompleted The boolean for indicating whether a search was finished
     * @param board The board to search on
     * @return The best move at the provided depth
     */

    public Move search(Board board, int depth) {
        AtomicInteger alpha = new AtomicInteger(Integer.MIN_VALUE);
        AtomicInteger beta = new AtomicInteger(Integer.MAX_VALUE);
        ConcurrentLinkedQueue<ScoredMove> bestMoves = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                Board localBoard = board.getCopy();
                ScoredMove bestMove = searchHelper(localBoard, depth, alpha, beta);
                bestMoves.add(bestMove);
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Determine the best move from the moves in the bestMoves queue
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (ScoredMove move : bestMoves) {
            if (move.getScore() > bestScore) {
                bestScore = move.getScore();
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Helper method for search for multiple threads
     * @param board The board to search on
     * @param depth The depth to search to
     * @param alpha alpha value
     * @param beta beta value
     * @return The best move
     */

    private ScoredMove searchHelper(Board board, int depth, AtomicInteger alpha, AtomicInteger beta) {
        Color currentPlayer = AITeam; // Use AITeam instead of GUI.getController().getPlayerTeam();
        List<Move> moves = board.getSortedMoves(currentPlayer, history);

        int localAlpha = alpha.get();
        int localBeta = beta.get();
        Move bestMove = null;
        boolean first = true;

        for (Move move : moves) {
            board.makeMove(move, Flag.SEARCHING);

            int score;
            if (currentPlayer == AITeam) {
                score = min(localAlpha, localBeta, depth - 1, board, 1, true);
            } else {
                score = max(localAlpha, localBeta, depth - 1, board, 1, true);
            }

            board.undoMove(move);

            if (first || (currentPlayer == AITeam && score > localAlpha) || (currentPlayer != AITeam && score < localBeta)) {
                first = false;
                bestMove = move;

                if (currentPlayer == AITeam) {
                    localAlpha = Math.max(localAlpha, score);
                    alpha.set(localAlpha);
                } else {
                    localBeta = Math.min(localBeta, score);
                    beta.set(localBeta);
                }
            }

            if (localAlpha >= localBeta) {
                break;
            }
        }

        if (bestMove != null) {
            return new ScoredMove(bestMove, (currentPlayer == AITeam) ? localAlpha : localBeta);
        }
        return null;
    }

    /**

     This method is used as the min function in the alpha-beta pruning algorithm. It searches for the minimum score
     available in a game state by calling the max function on the resulting game states from each move.
     It evaluates the board if the maximum depth is reached or the game is over, otherwise it generates and
     evaluates resulting game states from each move and returns the minimum score available.
     @param alpha the alpha value for alpha-beta pruning
     @param beta the beta value for alpha-beta pruning
     @param depth the current depth of the search
     @param startTime the start time of the search
     @param searchCompleted an AtomicBoolean flag indicating if the search was completed
     @param board the current game board state to search
     @return the minimum score available from the resulting game states
     */
    public int min(int alpha, int beta, int depth, Board board, int moveCount, boolean allowNullMove){
        if (depth <= 0 || Utility.isGameOver(board)){
            return Evaluation.evaluate(board, AITeam);
        }


        long boardHash = board.zobristHashCode();
        TranspositionEntry entry = transpositionTable.get(boardHash);
        if (entry != null && entry.depth() >= depth){
            return (int) entry.score();
        }

        if (allowNullMove && moveCount >= 4){
            board.skipMove();
            int score = -max(-beta, -beta + 1, depth - 2, board, moveCount + 1, false);
            board.undoSkipMove();

            if (score >= beta) {
                return beta;
            }
        }


        int minScore = Integer.MAX_VALUE;
        for (Move move : board.getSortedMoves(Color.invert(AITeam), history)){
            board.makeMove(move, Flag.SEARCHING);
            int score = max(alpha, beta, depth - 1, board, moveCount + 1, true);
            board.undoMove(move);
            minScore = Math.min(minScore, score);
            beta = Math.min(beta, minScore);
            if (beta <= alpha){
                break;
            }
        }

        TranspositionEntry newEntry = new TranspositionEntry(minScore, depth);
        if (!transpositionTable.containsValue(newEntry)){
            transpositionTable.put(boardHash, newEntry);
        }

        if (depth <= 0 && isQuietPosition(board)){
            return quiescenceSearch(alpha, beta, board);
        }

        return minScore;
    }
    /**

     This method is used as the max function in the alpha-beta pruning algorithm. It searches for the maximum score
     available in a game state by calling the min function on the resulting game states from each move.
     It evaluates the board if the maximum depth is reached or the game is over, otherwise it generates and
     evaluates resulting game states from each move and returns the maximum score available.
     @param alpha the alpha value for alpha-beta pruning
     @param beta the beta value for alpha-beta pruning
     @param depth the current depth of the search
     @param startTime the start time of the search
     @param searchCompleted an AtomicBoolean flag indicating if the search was completed
     @param board the current game board state to search
     @return the maximum score available from the resulting game states
     */
    public int max(int alpha, int beta, int depth, Board board, int moveCount, boolean allowNullMove){
        if (depth <= 0 || Utility.isGameOver(board)){
            return Evaluation.evaluate(board, AITeam);
        }
        long boardHash = board.zobristHashCode();
        TranspositionEntry entry = transpositionTable.get(boardHash);
        if (entry != null && entry.depth() >= depth){
            return (int) entry.score();
        }

        if (allowNullMove && moveCount >= 4){
            board.skipMove();
            int score = -min(-beta, -beta + 1, depth - 2, board, moveCount, false);
            board.undoSkipMove();

            if (score >= beta) {
                return beta;
            }
        }

        int maxScore = Integer.MIN_VALUE;
        for (Move move : board.getSortedMoves(AITeam, history)){
            board.makeMove(move, Flag.SEARCHING);
            int score = min(alpha, beta, depth - 1, board, moveCount + 1, true);
            board.undoMove(move);

            maxScore = Math.max(maxScore, score);
            alpha = Math.max(alpha, maxScore);

            if (beta <= alpha){
                break;
            }
        }
        TranspositionEntry newEntry = new TranspositionEntry(maxScore, depth);
        if (!transpositionTable.containsValue(newEntry)){
            transpositionTable.put(boardHash, newEntry);
        }
        return maxScore;
    }
    /**

     This helper method checks if a board state is a quiet position or not, meaning there are no available captures
     @param board the game board state to check
     @return true if the board state is a quiet position, false otherwise
     */
    private boolean isQuietPosition(Board board){
        List<Move> captures = board.getMoves(MoveType.CAPTURE, AITeam);
        captures.addAll(board.getMoves(MoveType.CAPTURE, Color.invert(AITeam)));

        return captures.isEmpty();
    }
    /**

     This helper method performs a quiescence search on a game board state.
     A quiescence search is performed when the current state of the game is considered a quiet position,
     which means there are no available captures on the board.
     It evaluates the board if the maximum depth is reached, otherwise it generates and evaluates resulting
     game states from each capture move and returns the maximum score available.
     @param alpha the alpha value for alpha-beta pruning
     @param beta the beta value for alpha-beta pruning
     @param board the current game board state to search
     @return the maximum score available from the resulting game states
     */
    private int quiescenceSearch(int alpha, int beta, Board board){
        int standPat = Evaluation.evaluate(board, Color.invert(AITeam));

        if (standPat >= beta){
            return beta;
        }

        if (alpha < standPat){
            alpha = standPat;
        }

        for (Move move : board.getSortedMoves(Color.invert(AITeam), history)){
            if (move.getCapturedPiece() == null){
                continue;
            }
            board.makeMove(move, Flag.SEARCHING);
            int score = -quiescenceSearch(-beta, -alpha, board);
            board.undoMove(move);

            if (score >= beta){
                return beta;
            }
            if (score > alpha){
                alpha = score;
            }
        }

        return alpha;
    }
    /**

     This helper method updates the history table with the move made and the depth of the search
     @param move the move that was made
     @param depth the depth of the search when the move was made
     */
    private void updateHistory(Move move, int depth){
        history[move.getFromRow()][move.getFromCol()] += depth * depth;
    }
}
