import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Minesweeper extends PApplet {

///////////////////////////////////////////// global variables stated

cell[][] board;
int boardSize = 40;//adjust to make large board x by x board
float mineRatio = .20f; //between 0-1; higher = more difficult --> default: 0.20
PFont font;
float offsetX = 7;
float offsetY = 16.5f;
int mines;
boolean lock = false; //ease of use for quick-revealing
ArrayList<PVector> cellList = new ArrayList<PVector>(); //implement for optimization of mine assignment

///////////////////////////////////////////////////

public void setup() { //assign variables, construct objects/values
   //multiply boardSize by 20; MUST ADJUST THIS WHEN YOU ADJUST BOARDSIZE
  background(255);
  
  for(int x = 0; x < boardSize; x += 1) { //set up grid
    for(int y = 0; y < boardSize; y += 1) {
      rect(x*20, y*20, 20, 20);
    }
  }
  
  font = loadFont("Serif-16.vlw"); //load font
  textFont(font, 16); //font size
  
  board = new cell[boardSize][boardSize]; //construct array
  for(int x = 0; x < boardSize; x ++) { //construct cell objects; no longer "null"
    for(int y = 0; y < boardSize; y ++) {
      board[x][y] = new cell(x*20, y*20);
      cellList.add(new PVector(x, y));
    }
  }
  
  mines = PApplet.parseInt(boardSize * boardSize * mineRatio);
  for(int x = 1; x <= mines; x ++) { //set up mines
    setMine();
  }
  displayMineCount(); //post-initialization
  
  for(int j = 0; j < boardSize; j ++) { //assign number clues for non-mine cells (how many cells are adjacent)
    for(int k = 0; k < boardSize; k ++) {
      if(!board[j][k].isMine) {
        int adjacent; //number of adjacent mines
        adjacent = tm(j-1, k-1) + tm(j, k-1) + tm(j+1, k-1) + tm(j-1, k) + tm(j+1, k) + tm(j-1, k+1) + tm(j, k+1) + tm(j+1, k+1);
        board[j][k].numMine = adjacent;
      } else {
        board[j][k].numMine = -1;
      }
    }
  }
  println("Done initializing");
}

///////////////////////////////// check for events/updating gamestate

public void draw() { //loop
}

public void mousePressed() { //test for revealing
  int x, y;
  x = detCell(mouseX);
  y = detCell(mouseY);
  if(keyPressed && key == 32) { //is 'space' being held? if so, reveal all neighbor spots
    int flagAdjacent;
    flagAdjacent = tf(x-1, y-1) + tf(x, y-1) + tf(x+1, y-1) + tf(x-1, y) + tf(x+1, y) + tf(x-1, y+1) + tf(x, y+1) + tf(x+1, y+1);
    if(board[x][y].isRevealed && flagAdjacent == board[x][y].numMine) {
      revealNeighbors(x, y);
    }
    lock = true;
  }
  if(!lock) { //cannot quick-reveal around unrevealed cells
    board[x][y].reveal();
  }
  lock = false;
}

public void keyReleased() { //CONTROLS: 'space' = flag/unflag, 'space + click' = quick reveal
  if(key == 32) { //was 'space' released? if so, flag the cell
    int x, y;
    x = detCell(mouseX);
    y = detCell(mouseY);
    board[x][y].flag();
  }
}

//////////////////////////////we love object oriented programming

class cell {
  boolean isMine = false;
  int numMine;
  boolean isRevealed = false;
  boolean isFlagged = false;
  PVector position;
  
  cell(int x, int y) {
    numMine = 0; //placeholder value
    position = new PVector(x, y);
  }
  
  public void reveal() { //REVEALED!
    if(!isFlagged && !isRevealed) {
      if(isMine) {
        fill(0);
        text('X', position.x + offsetX, position.y + offsetY);
        endGame(); //lose
        println("You Lose!");
      } else {
        isRevealed = true; //can no longer be flagged
        fill(200); //gray rectangles = revealed
        rect(position.x, position.y, 20, 20);
        fill(0); //reset; black text
        if(numMine != 0) {
          text(numMine, position.x + offsetX, position.y + offsetY); //reveal number
        }
        if(numMine == 0) {
          revealNeighbors(PApplet.parseInt(position.x/20), PApplet.parseInt(position.y/20)); //reveal neighbors automatically if 0 neighbor mines
        }
      }
    }
  }
  
  public void flag() { //FLAGGED!
    if(!isRevealed) {
      if(isFlagged) {
        isFlagged = false; //unflagged
        fill(255); //set color to white to match background
        rect(position.x, position.y, 20, 20); //reset to white cell (cover up F)
        mines ++;
        displayMineCount();
      } else {
        isFlagged = true;
        fill(0);
        text('F', position.x + offsetX, position.y + offsetY); //F stands for flag: can no longer reveal
        mines --;
        displayMineCount();
      }
    }
  }
}


//////////////////////////////////// basic processes for Minesweeper

public void setMine() { //place mine under cell
  int index = PApplet.parseInt(random(0, cellList.size() - .6f));
  PVector boardIndex = cellList.get(index);
  board[PApplet.parseInt(boardIndex.x)][PApplet.parseInt(boardIndex.y)].isMine = true;
  cellList.remove(index);
}

public void endGame() { //someone lost...
  noLoop();
}

public int detCell(float pos) { //determine which cell the mouse is hovering over
  int x;
  x = floor(pos/20); //pos is either mouseX or mouseY
  return x;
}
public void displayMineCount() { //keep track of how many mines are left
  println("number of mines:" + mines);
}

public void revealNeighbors(int x, int y) { //quick reveal functionality
  rn(x-1, y-1);
  rn(x, y-1);
  rn(x+1, y-1);
  rn(x-1, y);
  rn(x+1, y);
  rn(x-1, y+1);
  rn(x, y+1);
  rn(x+1, y+1);
}

public boolean testDomain(int x, int y) { //make sure index is within bounds of array
  boolean test = false;
  if( 0 <= x && x < boardSize) {
    if(0 <= y && y < boardSize) {
      test = true;
    }
  }
  return test;
}

public int tm(int x, int y) { //facilitate assigning number clues
  int val = 0;
  if(testDomain(x, y) && board[x][y].isMine) {
      val = 1;
  }
  return val;
}

public int tf(int x, int y) { //facilitate revealing neighbor cells
  int val = 0;
  if(testDomain(x, y) && board[x][y].isFlagged) {
      val = 1;
  }
  return val;
}

public void rn(int x, int y) { //facilitate revealing neighbor cells
  if(testDomain(x, y)) {
    board[x][y].reveal();
  }
}
  public void settings() {  size(800, 800); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Minesweeper" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
