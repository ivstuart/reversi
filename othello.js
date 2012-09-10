 
var Player;
var Computer;

var gameState;
var gameOver;

var x_last;
var y_last;

var playerTurn;

var ready;

function init() {
   Player    = new Array(3);
   Computer  = new Array(3);

   Player[0]     = new Image();
   Player[1]     = new Image();
   Player[2]     = new Image();
   
   Player[0].src = "blank.gif";
   Player[1].src = "black.gif";
   Player[2].src = "white.gif";
   
   Computer[1]   = false;
   Computer[2]   = false;

   window.document.setgame.players[0].checked =false;
   window.document.setgame.players[1].checked =false;
   window.document.setgame.players[2].checked =true;
   
   setgame.difficulty.selectedIndex = 2;

   resetGame();
   }

function breakOut() {
   othello.stopThinking();
}

function placeDisc(x,y) {
   if (Computer[playerTurn]) { return; }
   if (gameOver) { return; }
   if (gameState == false) {
     return;
   }   
   var validMove = othello.playerMove(x,y);
   if (validMove == false) {
      return;
   }
   x_last = x;
   y_last = y;
   // redrawAll();
   redrawBoard();
   nextTurn();
}

function nextTurn() {
   if (gameState == false) { return; }
   gameOver = othello.isGameOver();
   if (gameOver) {
     whoWon();
     return;
   }

   playerTurn = othello.getPlayerTurn();

   TurnIcon.src = Player[playerTurn].src;
    
   if (Computer[playerTurn]) {
      othello.computerMove();
      
      ready = false;
      while (ready == false) {
        ready = othello.hasMove();        
        setTimeout(";",500);
        //reset = othello.shouldReset();
        //if (reset) {
        //   resetGame();
        //   return;
        //}
        // setTimeout(";",100);
        
      } 
      othello.makeBestMove();
      var lastMove = othello.getLastMove();  
      x_last = lastMove % 8;
      y_last = Math.floor(lastMove / 8); 
      // redrawAll();
      redrawBoard();
      playerTurn = othello.getPlayerTurn();
   }
   
   if (Computer[playerTurn] == false) {
      gameState = true;
      TurnIcon.src = Player[playerTurn].src;
   }
   else {
      nextTurn();
   }
   
}

function whoWon() {
   var one= othello.getCounters(1);
   var two= othello.getCounters(2);

   if (one > two) {
     playersturn.innerHTML = "Winner!<br>"+one+" to "+two+"<br>";
     TurnIcon.src = Player[1].src;
     return;
   }
   if (two > one) {
     playersturn.innerHTML = "Winner!<br>"+two+" to "+one+"<br>";
     TurnIcon.src = Player[2].src;
     return;
   }
   if (one == two) {
     playersturn.innerHTML = "Draw!<br>"+one+" each.";
     TurnIcon.src = Player[0].src;
     return;
   }
}

function showAI() { ai.style.display = "block"; }

function hideAI() { ai.style.display = "none"; }

function startGame() {
  
   playerTurnWindow.style.display = "block";
   
   startstop.innerHTML = "Reset";

   if(window.document.setgame.players[0].checked == true) {
      Computer[1] = true;
      Computer[2] = true;
   }
   if(window.document.setgame.players[1].checked == true) {
      Computer[1] = false;
      Computer[2] = true;
   }
   var level = setgame.difficulty.selectedIndex;
   othello.startGame();
   othello.setLevel(level);
   setTimeout(";",100);
   playerTurn = othello.getPlayerTurn();
   gameState = true;
   nextTurn();
}

function resetGame() {
  gameState = false;
  reset = othello.shouldReset();
  othello.startGame();
  startstop.innerHTML = "Start";
  playersturn.innerHTML = "Player's Turn";
  playerTurnWindow.style.display = "none";
  Computer[1] = false;
  Computer[2] = false;
  redrawAll();
}


function redrawAll() {
   var k;
   var myCounter=0;
   for (var j = 0; j < 8; j++) {
      for (var i = 0; i < 8; i++) {
         k = othello.getCell(i,j);
         document.images[myCounter++].src = Player[k].src;   
         }
      }
}

function redrawBoard() {
  redrawRow(0,-1);
  redrawRow(1,-1);
  redrawRow(1,0);
  redrawRow(1,1);
  redrawRow(0,1);
  redrawRow(-1,1);
  redrawRow(-1,0);
  redrawRow(-1,-1);
}

function redrawRow(dx,dy) {
  var x = x_last;
  var y = y_last;
  var result = playerTurn;
  while (result == playerTurn) {
    result = othello.getCell(x,y);
    lastMove = x + (y*8);
    document.images[lastMove].src = Player[result].src;
    x += dx;
    y += dy;
    if ( x < 0 || x > 7 || y < 0 || y > 7 ) {
      result = 0;
    }
  }
}

function toggleGame() {
if (gameState == false) { startGame(); }
else { resetGame(); }
}