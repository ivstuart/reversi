import java.io.*;

/*
 * This class represents the state of the othello board
 * It is used to score moves and to execute placement
 * of disc which in turn flip opponent discs
 */
public final class Board implements Cloneable {

    private static final int EMPTY = 0;

    private boolean mScoring = false;

    private int[][] mCell;
    private int     mPlayerTurn ;

    private int     mRandomMove;

    private int     mFlipped;
    private int     mFlipScore;
    private int     mPosScore;

    private int     mX;
    private int     mY;

    private int     mTimeIndex;

    private int[][] mScore = {
    {9,4,5,5,5,5,4,9},
    {4,0,1,1,1,1,0,4},
    {5,1,3,2,2,3,1,5},
    {5,1,2,3,3,2,1,5},
    {5,1,2,3,3,2,1,5},
    {5,1,3,2,2,3,1,5},
    {4,0,1,1,1,1,0,4},
    {9,4,5,5,5,5,4,9}};

    /*
     * Constructor
     *
     */
    public Board() {
		mCell = new int[8][8];
		}

    /*
     * This sets the state of mCell back to the beginning of
     * the game. However it is not currently used by anything.
     */
    public void resetBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                mCell[i][j]=0;
            }
        }
        mCell [3][3] = 1;
        mCell [4][4] = 1;
        mCell [3][4] = 2;
        mCell [4][3] = 2;
        mX = 0;
        mY = -1;
    }

    /*
     * This method clones this object.
     * A new 2D array of int is created as the new Board object
     * requires cell object with new references
     */
    public Object clone() throws CloneNotSupportedException {
        Object myBoard = super.clone();
        int[][] newCell = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                newCell[i][j] = mCell[i][j];
            }
        }
        mCell = newCell;
        return myBoard;
    }

    /*
     * Accessor method to mScoring
     */
    public void setScoring(boolean bool) {
        mScoring = bool;
    }

    /*
     * Accessor get method for mScoring
     */
    public boolean isScoring() {
        return mScoring;
    }

    /*
     * Accessor method
     * Get the state of a single cell on the board
     */
    public int getCell(int x,int y) {
        return mCell[x][y];
    }

    /*
     * This resets the position over the board that the pointer
     * has reached, which looking for valid next moves.
     *
     */
    public void resetNextMove() {
        mX = 0;
        mY = -1;
    }

    /*
     * Accessor method which sets the game turn time period
     * for this board, so that the scoring metric can determine
     * the correct score for the board at a certain point in the game
     */
    public void setTime(int depth) {
        mTimeIndex = Othello.mGameTurn + depth;
    }

    /*
     * Accessor method
     * @return the id number of the cell in the board
     * which was analysed last.
     */
    public int getMove()  { return mX + (mY * 8); }


    /*
	 * Scans across the board looking for valid moves
	 * @return if there is a next move or not
     */
    public boolean nextMove() {
        // Go to next cell
        if (++mY > 7) {
			mY=0;
			mX++;
            }
        // Carries on where is left off last time
        for (; mX < 8; mX++,mY=0) {
            for (; mY < 8; mY++) {
                if (mCell [mX][mY] == EMPTY && isValidMove(mX,mY)) {
                    this.flipDiscs(mX,mY);
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * Wrapper method to execute actually flipping discs
     * This is run only after a valid move has been found
     */
    public void makeMove() {
        mScoring = false;
        this.flipDiscs(mX,mY);
        this.resetNextMove();
    }

    /*
     * This method will not change the state of the current board
     * @return if there is a move to make for the current player
     */
    public boolean hasMove() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(mCell [i][j] == EMPTY && this.isValidMove(i,j)) {
					return true;
					}
            }
        }
        return false;
    }

    /*
     * Main method for flipping / scoring flipping of discs
     */
    public void flipDiscs(int x,int y) {
        mFlipped = 0;
        mFlipScore = 0;
        if (mScoring == false) {
            mCell[x][y] = mPlayerTurn;
        }
        else {
			mPosScore = mScore[x][y];
		}
        if (this.checkRow(x,y, 1, 0)) { this.flipRow(x,y, 1, 0);}
        if (this.checkRow(x,y, 0, 1)) { this.flipRow(x,y, 0, 1);}
        if (this.checkRow(x,y,-1, 0)) { this.flipRow(x,y,-1, 0);}
        if (this.checkRow(x,y, 0,-1)) { this.flipRow(x,y, 0,-1);}
        if (this.checkRow(x,y, 1, 1)) { this.flipRow(x,y, 1, 1);}
        if (this.checkRow(x,y, 1,-1)) { this.flipRow(x,y, 1,-1);}
        if (this.checkRow(x,y,-1, 1)) { this.flipRow(x,y,-1, 1);}
        if (this.checkRow(x,y,-1,-1)) { this.flipRow(x,y,-1,-1);}
    }

    /*
     * Checks all 8 possible directions for disc which would be flipped
     * @return true or false if there is at least one disc that will be flipped
     */
    public boolean isValidMove(int x,int y) {
        if (mCell[x][y] != EMPTY) { return false; }
        if (this.checkRow(x,y,1,0)  ||
        	this.checkRow(x,y,0,1)  ||
        	this.checkRow(x,y,-1,0) ||
        	this.checkRow(x,y,0,-1) ||
        	this.checkRow(x,y,1,1)  ||
        	this.checkRow(x,y,1,-1) ||
        	this.checkRow(x,y,-1,1) ||
        	this.checkRow(x,y,-1,-1)) {
	            return true;
        }
        return false;
    }

    /*
     * Looks for opponent disc next to the one your just placed
     * @return is oppponent disc next to you in this direction
     */
    public boolean checkRow(int x,int y,int dx,int dy) {
        x += dx;
        y += dy;

        if (x < 0 || x > 7 || y < 0 || y > 7) {
			return false;
			}

        if (mCell[x][y] == mPlayerTurn || mCell[x][y] == EMPTY) {
            return false;
        }

        return checkRowAfter(x,y,dx,dy);
    }

    /*
     * Looks for your disc somewhere along this line
     * @return is your disc somewhere along this line
     */
    public boolean checkRowAfter(int x,int y,int dx,int dy) {
        x += dx;
        y += dy;

        if (x < 0 || x > 7 || y < 0 || y > 7) { return false; }

        if (mCell[x][y] == mPlayerTurn) { return true; }
        if (mCell[x][y] == EMPTY) { return false; }

        return checkRowAfter(x,y,dx,dy);
    }

    /*
     * Flips a row (unless scoring true)
     * @return if this row has disc which where flipped
     */
    public boolean flipRow(int x,int y,int dx,int dy) {
        x += dx;
        y += dy;

        if (x < 0 || x > 7 || y < 0 || y > 7) { return false; }

        if (mCell[x][y] == mPlayerTurn) { return true; }

        if (mScoring == false) { mCell[x][y] = mPlayerTurn; }

        mFlipped++;
        mFlipScore += mScore[x][y];

        return flipRow(x,y,dx,dy);
    }

    /*
     * Selects a random move which is valid on the board
     * @return cell id of the random location to move in
     */
    public int selectRandomMove() {
        mRandomMove = (int)(64*Math.random());
        this.setRandomMove();
        return mRandomMove;
    }

    /*
     * Sets up the random move, skipping any already
     * taken cells on the board, so this method is quick.
     */
    private void setRandomMove() {
        if (++mRandomMove > 63) { mRandomMove = 0;}
        int x = mRandomMove % 8;
        int y = (int)(mRandomMove / 8);

        if (mCell[x][y] != EMPTY || !isValidMove(x,y)) { setRandomMove(); }
        return;
    }

    /*
     * Scoring matrix which is dependant on the game turn
     * This is affectively the personnality of the computer player
     * Playing first to limit the number of moves of the opponent
     * Then playing for position on the board
     * Then ending the game by going for the most number of discs
     */
    public int getScore() {
        if (mTimeIndex > 52) {
            return mFlipped;
        }
        if (mTimeIndex > 48) {
            return mPosScore + (3*mFlipped) + mFlipScore;
        }
        if (mTimeIndex > 40) {
            return mPosScore + (2*mFlipped) + mFlipScore;
        }
        if (mTimeIndex > 20) {
            return mPosScore + mFlipped + mFlipScore;
        }
        if (mTimeIndex > 10) {
            return mPosScore + mFlipScore;
        }
        return mPosScore - mFlipped + mFlipScore;
    }

    /*
     * Counter a players disc
     * Used by gameOver to work out who has won
     */
    public int getCounters(int player) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(mCell [i][j] == player) { count++; }
            }
        }
        return count;
    }

    /*
     * Accessor method
     */
    public int getPlayerTurn() {
        return mPlayerTurn;
    }

    /*
     * Accessor method
     */
    public void setPlayerTurn(int player) {
        mPlayerTurn = player;
    }

    /*
     * Switch whos players turn it is
     */
    public void togglePlayerTurn() {
        mPlayerTurn = 1 + (mPlayerTurn % 2);
    }

    /*
     * Tracing purposes only
     * This method dumps content of cell to standard output
     */
    public synchronized void dump() {
        System.out.println("Tracing of board!");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(mCell [i][j] + " ");
            }
            System.out.println("");
        }
    }
}