import java.applet.*;
import java.awt.*;
import java.io.*;

/*
 * This is the main java class
 * Made final to improve performance
 */
public final class Othello extends Applet {

    public static int mGameTurn;

    private CpuThread[] mThreads;

    private int      mThreadCounter;
    private Board    mGrid;

    private int      mLevel;

    private int      mLastMove;

    private boolean  mGameOver = false;
    private boolean  mNoMove = true;

    private boolean  mReset = false;

    // Constructor (not called)
    public Othello() {}

    // Applet start method
    public void start() {}

    // Applet init method
    public void init() {}

    // Applet stop method
    public void stop() {}

    // Applet destory method
    public void destory() {}

    /*
     * This starts the game and resets the board state
     * It also chooses which player at random goes first
     */
    public void startGame() {
        int player = 1+(int)(2*Math.random());
        mGrid = new Board();
        mThreadCounter =0;
        mGrid.resetBoard();
        mGrid.setPlayerTurn(player);
        mGameOver = false;
    }

    /*
     * This causes the computer to work out a move
     * Then actually make that move
     */
    public void computerMove() {
        mThreadCounter = 1;
        if (mLevel == 0) {
            mLastMove = mGrid.selectRandomMove();
            int x = mLastMove % 8;
            int y = (int)(mLastMove / 8);
            this.playerMove(x,y);
            mThreadCounter--;
            return;
        }
        if (mLevel > 0) {
            this.cleverAI();
            return;
        }
    }

    /*
     * This is a complex method responsible for creating
     * a thread for each possible first move that can be
     * made on the board.
     */
    private void cleverAI() {
        // System.out.println("DECL:cleverAI()");

        // Board will not change during this loop
        mGrid.resetNextMove();
        mGrid.setScoring(true);

        mThreads = new CpuThread[60];
        mThreadCounter=0;

        mNoMove = true;
        while(mGrid.nextMove()) {
			// System.out.println("Trying to crash computer with thread counter = "+mThreadCounter);
            mNoMove = false;

            Board aBoard;

            try {
                aBoard = (Board)mGrid.clone();

            } catch (CloneNotSupportedException e) {
				System.out.println("CloneNotSupportedException1");
                return;
            }

            CpuThread aCpuThread = new CpuThread(aBoard,this);

            // System.out.println("Starting thread "+mThreadCounter);

            mThreads[mThreadCounter++] = aCpuThread;

            aCpuThread.start();
        }
    }

    /*
     * This method is polled by the javascript to
     * determine when the computer had thought of
     * its move
     */
    public boolean hasMove() {
        if (mLevel == 0) { return true; }
        if (mThreadCounter == 0) { return false; }
        for (int i=0;i<mThreadCounter;i++) {
            if (mThreads[i].isRunning()) {
				return false;
			}
        }
        return true;
    }

    /*
     * This method chooses the best move to make based on
     * how it has scored the first selection of possible moves
     * If their are equally scored moves it selects at random.
     * It then executes that move.
     */
    public void makeBestMove() {
        // System.out.println("DECL:makeBestMove()");
        // System.out.println("makeBestMove() with thread counter = "+mThreadCounter);
        if (mLevel == 0) { return;}
        if (mNoMove) {
            this.nextTurn();
            return;
        }
        int bestScore = -1000;

        // Check all threads and find the bestScore
        for (int i=0;i<mThreadCounter;i++) {
            int score = mThreads[i].getValue();
            if (score > bestScore) {
                bestScore = score;
            }
        }

        int[] bestMoves = new int[60];
        int moveCounter = 0;

        // List all threads indexes with the bestScore
        for (int i=0;i<mThreadCounter;i++) {
            int score = mThreads[i].getValue();
            if (score == bestScore) {
                bestMoves[moveCounter++] = i;

            }
        }

        // select at random one of the best moves
        int choice = (int)(moveCounter*Math.random());
        int index = bestMoves[choice];
        mLastMove = mThreads[index].getMove();

        int x = mLastMove % 8;
        int y = (int)mLastMove / 8;


        mGrid.resetNextMove();
	    mGrid.setScoring(false);
	    if (mGrid.isValidMove(x,y) == false) {
			System.out.println("Bug in computer AI - trying to move on invalid cell!");
        }

        this.playerMove(x,y);

        mThreads = null;
        System.gc();
    }

    /*
     * Accessor method for mThreadCounter
     * Used by CpuThread to reduce counter to indicate when
     * all threads have finished.
     */
    public synchronized void threadDone() {
		System.out.println("Finishing thread "+mThreadCounter);
        mThreadCounter--;
    }

    /*
     * Accessor method for Othello javascript
     * @return True when game is over
     */
    public boolean isGameOver() {
        return mGameOver;
    }

    /*
     * Counts number of counters
     * Used by javascript to count up disc to see who has won
     * @param number of the player
     * @return number of counters of specified player type
     */
    public int getCounters(int player) {
        return mGrid.getCounters(player);
    }

    /*
     *
     * Used by javascript to only redraw disc in line with
     * the cell which had a disc placed on the board
     */
    public int getLastMove() { return mLastMove; }

    /*
     * This sets a flag in each CpuThread objects which unwides
     * their thinking loop early
     */
    public void stopThinking() {
        for (int i=0;i<mThreadCounter;i++) {
			if (mThreads[i] != null) {
                mThreads[i].stopThinking();
		    }
        }
    }

    /*
     * Accessor method to set the computer difficulty
     * level from the GUI during the start of the game
     */
    public void setLevel(int level) {
        mLevel = level;
        CpuThread.mMaxDepth = (mLevel * 2)-1;
    }

    /*
     * Used by human players and computer players
     * to make moves on the board
     * @return if the move was a valid one to make
     */
    public boolean playerMove(int x, int y) {
        // System.out.println("DECL:playerMove(x = " + x + ", y = "+y+")");
        mGrid.resetNextMove();
        mGrid.setScoring(false);
        if (mGrid.isValidMove(x,y)) {
            mGrid.flipDiscs(x,y);
            this.nextTurn();
            return true;
        }
        return false;
    }

    /*
     * Used by playerMove() to switch to next players turn
     * If they can not go you get to go again
     * If you both can not go the game is over
     */
    private void nextTurn() {
        mGrid.togglePlayerTurn();
        mGrid.resetNextMove();
        if (mGrid.hasMove()) {
			mGrid.resetNextMove();
            return;
        }
        mGrid.togglePlayerTurn();
        mGrid.resetNextMove();
        if (mGrid.hasMove()) {
        	mGrid.resetNextMove();
            return;
        }
        mGameOver = true;
    }

    /*
     * This is an accessor method
     * @param The x co-ordinate of a cell on the board
     * @param The y co-ordinate of the cell on the board
     * @return The value of a cell on the board
     */
    public int getCell(int x,int y) {
        return mGrid.getCell(x,y);
    }

    /*
     * This is an accessor method
     * @return The player turn, which is either 1 or 2
     */
    public int getPlayerTurn() {
        return mGrid.getPlayerTurn();
    }

    /*
     * This is a wrapper for the sleep method of the class Thread
     * Used by Othello javascript to properly sleep javascript
     * Used by program during debugging of multi-threads to get accurate
     * dump of board state.
     */
    public void sleep(float timeInMillis) throws InterruptedException {
        Thread.sleep((long)timeInMillis );
    }

    /*
     * Set by the reset button in the GUI
     */
    public void reset() {
        mReset = true;
    }

    /*
     * Used by javascript to know if it should exit one of its loops
     */
    public boolean shouldReset() {
        if (mReset) {
            mReset = false;
            return true;
        }
        return false;
    }
}
