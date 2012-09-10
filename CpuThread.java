import java.io.*;

/*
 * This thread takes one of the possible moves by the computer
 * then scores possible futures moves to find what it thinks
 * is the value of making the original move.
 */
public final class CpuThread extends Thread {

    public static int mMaxDepth;
    private boolean   mThinking = true;

    private Board     mBoard;
    private int       mLowestScore;
    private int       mBestScore;

    private int       mDepth;

    private int       mMove;
    private int       mPlayerTurn;

    private boolean   isRunning = true;

    private Othello   mOthello;

    /*
     * Constructor
     * @param a board which has already had it move identified
     * but not yet taken
     */
    public CpuThread(Board aBoard,Othello obj) {
        mBoard = aBoard;
        mOthello = obj;
        mMove = mBoard.getMove();
        mPlayerTurn = mBoard.getPlayerTurn();
    }

    /*
     * Thread run method
     * Calls the recursive method possibleMoves();
     */
    public void run() {
        mLowestScore = 1000;
        mBestScore   = -1000;

        mBoard.makeMove();
        mDepth = 0;

        this.possibleMoves(mBoard,mBoard.getScore());

        isRunning = false;
    }

    /*
     * Accessor method
     * @return if thread has finished
     */
    public boolean isRunning() {
		return isRunning;
	}
    /*
     * Accessor method
     * @return int value which represents the original move
     * Used by javascript to redraw only the required cells
     */
    public int getMove() {
        return mMove;
    }

    /*
     * Accessor method
     * Used to stop this thread from working to allow it to quickly exit
     * The affect is to reduce the AI of the computer, forcing it to move.
     */
    public void stopThinking() {
        mThinking = false;
    }

    /*
     * Accessor method
     * This is not used anymore
     * @return this threads original board object
     */
    public Board getBoard() {
        return mBoard;
    }

    /*
     * Accessor method
     * @return int gets the current bestScore for this move
     * This is a measure of the value in taking the original move for this thread
     * It is accurate limited to te max depth into the future analysed and given a
     * correct scoring metic. Ideally max depth == 60 and score = flipped only.
     */
    public int getValue() {
        return mBestScore;
    }

    /*
     * This is a recursive method. (complicated!)
     * Each time it is called the next move into the future is applied
     * until mDepth reaches mMaxDepth, then all possible moves are
     * scored. The scoring of subsequent moves is cumulative.
     */
    public void possibleMoves(Board board,int lastScore)  {
		// If forced to move will unroll the the stack and return.
        if (!mThinking) { return; }
        mDepth++;
        // System.out.println("Depth = "+mDepth);

        if (mPlayerTurn != board.getPlayerTurn() ) { lastScore = -lastScore; }

        board.togglePlayerTurn();
        boolean gameOver = false;
        // If not at last depth then look for moves and make them
        if (mDepth < mMaxDepth) {

            boolean noMove = true;
            // This is not require as to reach this point implies makeMove just happen
            // board.resetNextMove();

            // Loop while we find new valid moves to make
            Board aBoard;
            while(board.nextMove()) {
                noMove = false;

                try { aBoard = (Board)board.clone();}
                catch (CloneNotSupportedException e) {
					System.out.println("CloneNotSupportedException2");
					return; }
                aBoard.makeMove();
                this.possibleMoves(aBoard,lastScore+aBoard.getScore());
            }
            if (noMove) {
                board.togglePlayerTurn();

                // We have switched to other player so have to start considering
                // all possible empty cells from beginning of the board again.
                board.resetNextMove();
                gameOver = true;
                while(board.nextMove()) {
                    gameOver = false;

                    try { aBoard = (Board)board.clone();}
					catch (CloneNotSupportedException e) {
						System.out.println("CloneNotSupportedException3");
						return; }
                    aBoard.makeMove();
                    this.possibleMoves(aBoard,lastScore+aBoard.getScore());
                }
            }
        }
        
        // If at last depth then look for moves and only score them
        if (mDepth == mMaxDepth || gameOver == true) {
            board.setScoring(true);

            // If you opponent could not go you got 1 or more extra goes
            // If you are ending on your go you are trying to maximise your score
            if (mPlayerTurn != board.getPlayerTurn()) {
                while(board.nextMove()) {
                    int score = lastScore + board.getScore();
                    if (score < mLowestScore) {
                        mLowestScore = score;
                    }
                }
            }
            else {
                mLowestScore = -1000;
                while(board.nextMove()) {
                    int score = lastScore + board.getScore();
                    if (score > mLowestScore) {
                        mLowestScore = score;
                    }
                }
            }

            // Get the best lowest score
            // Because you move to increase score and your opponent
            // attempts to make moves to decrase the score.
            if (mLowestScore > mBestScore) {
                mBestScore = mLowestScore;
            }
        }

        // If not at first move then go back and score other possible futures
        if (--mDepth > 0) {
            mLowestScore = 1000;
            return;
        }

        // Finished scoring all paths
    }
}