import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//represents ICell interface
interface ICell {

  // checks if cell is empty
  boolean isEmpty();

  // gets cell info
  String getCellInfo();

  // returns cell image
  WorldImage cellImage(int cellSize);

  // returns if flooded
  boolean isFlooded();
}

// Represents a single square of the game area
class Cell implements ICell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  String colorName;
  String value;
  boolean flooded;
  WorldImage cellImage;
  Utils util = new Utils();

  // the four adjacent cells to this one
  ICell left;
  ICell top;
  ICell right;
  ICell bottom;

  // constructor for a cell
  void init() {
    this.flooded = false;
    this.left = new MtCell();
    this.top = new MtCell();
    this.right = new MtCell();
    this.bottom = new MtCell();
  }

  // constructor for cell
  Cell(int x, int y, Color color) {
    init();
    this.x = x;
    this.y = y;
    this.color = color;
    this.colorName = util.getColor(color);
    this.value = "(" + x + "," + y + ")";
  }

  // constructor for cell
  Cell(int x, int y, Color color, Cell left, Cell top, Cell right, Cell bottom) {
    init();
    this.x = x;
    this.y = y;
    this.color = color;
    this.colorName = util.getColor(color);
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  // checks if cell is flooded
  public boolean isFlooded() {
    return flooded;
  }

  // checks if cell is empty
  public boolean isEmpty() {
    return false;
  }

  // gets the cell info
  public String getCellInfo() {
    return new String("Cell[" + value + " " + colorName + "]");
  }

  // creating image
  public WorldImage cellImage(int cellSize) {

    WorldImage i = new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, this.color);
    this.cellImage = i;
    return i;
  }
}

//represents an empty cell
class MtCell implements ICell {
  int x;
  int y;
  boolean flooded;

  // constructors for empty cell
  void init() {
    this.flooded = false;
  }

  MtCell() {
    init();
  }

  MtCell(int x, int y) {
    init();
    this.x = x;
    this.y = y;
  }

  // checks if cell is flooded
  public boolean isFlooded() {
    return false;
  }

  // check if cell is empty
  public boolean isEmpty() {
    return true;
  }

  // get cell info
  public String getCellInfo() {
    return new String("Cell[empty]");
  }

  // draw empty cell
  public WorldImage cellImage(int cellSize) {
    return new EmptyImage();
  }
}

// represents floodIt class
class FloodIt extends World {

  // constants
  int gameHeightNum = 2400;
  int gameWidthNum = 4000;

  int windowHeight = 96000;
  int windowWidth = 160000;

  int cellsize = windowWidth / gameWidthNum;

  int gameHeight = gameHeightNum;
  int gameWidth = gameWidthNum;

  int sceneSize = 840;

  WorldScene scene;
  boolean theGameIsOver = false;

  // a two-dimensional grid of these Cells to represent the board
  ArrayList<ArrayList<Cell>> board;
  ArrayList<Color> colorList;
  int boardSize;
  int numberOfColors;
  Random rand;
  int totalTries;
  int userTries = 0;
  int timer = 0;

  Utils util = new Utils();

  // a constructor that takes in two numbers: the size of the board and the number
  // of colors
  FloodIt(int boardSize, int numberOfColors) {

    rand = new Random(5);
    this.board = new ArrayList<ArrayList<Cell>>();
    this.colorList = new ArrayList<Color>();

    if (boardSize < 2 || boardSize > 14) {
      throw new IllegalArgumentException("The size of the board must be between 2 and 14.");
    }
    else {
      this.boardSize = boardSize;
      this.totalTries = (boardSize * boardSize) - boardSize;
    }

    if (numberOfColors < 3 || numberOfColors > 8) {
      throw new IllegalArgumentException("The number of colors must be between 3 and 8.");
    }
    else {
      this.numberOfColors = numberOfColors;
    }
  }

  // resets the game
  public void resetGame() {
    this.userTries = 0;
    theGameIsOver = false;
    this.timer = 0;
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        this.board.get(i).get(j).color = this.colorList.get(this.rand.nextInt(this.numberOfColors));
        this.board.get(i).get(j).flooded = false;
      }
    }
    floodBoardCells();
  }

  // ends the world
  public WorldEnd worldEnds() {
    return new WorldEnd(false, this.makeScene());
  }

  // creates world scene
  public WorldScene makeScene() {
    return this.renderGame();
  }

  // renders the game
  public WorldScene renderGame() {
    WorldScene background = new WorldScene(sceneSize, sceneSize);
    background.placeImageXY(renderBoardImage(), cellsize * boardSize, cellsize * boardSize);

    // display timer
    String timerStr = "" + timer;
    TextImage timerImage = new TextImage(timerStr, 24, FontStyle.REGULAR, Color.GRAY);
    background.placeImageXY(timerImage, cellsize, (cellsize * boardSize) + 120);

    // user won or lose
    String text = "";
    boolean isAllFlooded = isAllFlooded();
    if (!isAllFlooded && this.userTries == this.totalTries) {
      text = "You Lose!";
      TextImage userWonOrLoss = new TextImage(text, 32, FontStyle.BOLD, Color.RED);
      background.placeImageXY(userWonOrLoss, cellsize * boardSize, (cellsize * boardSize) + 270);
      theGameIsOver = true;
    }
    else if (isAllFlooded) {
      text = "You Win!";
      TextImage userWonOrLoss = new TextImage(text, 32, FontStyle.BOLD, Color.GREEN);
      background.placeImageXY(userWonOrLoss, cellsize * boardSize, (cellsize * boardSize) + 270);
      theGameIsOver = true;
    }
    else {
      timer++;
    }

    return background;
  }

  // produce an image of the world
  public WorldImage renderBoardImage() {
    return renderAllCells();
  }

  // draw the nodes
  public WorldImage renderAllCells() {
    WorldImage i = new EmptyImage();

    for (int index = 0; index < board.size(); index++) {
      i = new BesideImage(i, this.renderRow(board.get(index)));
    }
    TextImage userTries = new TextImage("" + this.userTries + "/" + this.totalTries, 32,
        FontStyle.BOLD, Color.GRAY);
    i = new AboveImage(i, userTries);

    return i;
  }

  // draw a row of nodes
  public WorldImage renderRow(ArrayList<Cell> row) {
    WorldImage i = new EmptyImage();

    for (int index = 0; index < row.size(); index++) {
      i = new AboveImage(i, row.get(index).cellImage(cellsize));
    }
    return i;
  }

  // creates board scene
  void makeBoard() {
    this.userTries = 0;
    this.timer = 0;
    makeColors();
    buildBoardCells();
    connectBoardCells();
    floodBoardCells();
  }

  // creates cells
  void buildBoardCells() {
    String render = "";
    for (int i = 0; i < boardSize; i++) {
      ArrayList<Cell> row = new ArrayList<Cell>();
      for (int j = 0; j < boardSize; j++) {
        Color color = this.colorList.get(this.rand.nextInt(this.numberOfColors));
        Cell cell = new Cell(i, j, color);
        row.add(cell);
        if (boardSize == 2) {
          System.out
              .println("(" + i + "," + j + ") " + util.getColor(color) + " " + cell.getCellInfo());

        }
      }
      render = render + "\n";
      this.board.add(row);
    }
  }

  // connects cells
  void connectBoardCells() {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (i > 0) {
          this.board.get(i).get(j).top = this.board.get(i - 1).get(j);
        }
        if (j < boardSize - 1) {
          this.board.get(i).get(j).right = this.board.get(i).get(j + 1);
        }
        if (i < boardSize - 1) {
          this.board.get(i).get(j).bottom = this.board.get(i + 1).get(j);
        }
        if (j > 0) {
          this.board.get(i).get(j).left = this.board.get(i).get(j - 1);
        }
      }
    }
  }

  // checks if adjacent cells are flooded
  boolean isCurrentCellAdjacentToFlooded(Cell currentCell) {
    return (currentCell.left.isFlooded() || currentCell.top.isFlooded()
        || currentCell.right.isFlooded() || currentCell.bottom.isFlooded());
  }

  // checks if cells are the same color
  boolean areCellsSameColor(Cell givenCell, Cell currentCell) {
    return (givenCell.color.equals(currentCell.color));
  }

  // mark cells as flooded on top-left corner and any adjacent matching color
  // cells
  void floodBoardCells() {

    Cell floodedCell = null;
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (i == 0 && j == 0) {
          this.board.get(0).get(0).flooded = true;
          floodedCell = this.board.get(0).get(0);
          continue;
        }
        if (isCurrentCellAdjacentToFlooded(this.board.get(i).get(j))
            && areCellsSameColor(floodedCell, this.board.get(i).get(j))) {
          this.board.get(i).get(j).flooded = true;
        }
      }
    }
  }

  // mark all remaining cells to top-left cell as flooded with color
  void floodBoardCellsColorOnTick() {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (this.board.get(i).get(j).isFlooded()
            && !areCellsSameColor(this.board.get(0).get(0), this.board.get(i).get(j))) {
          this.board.get(i).get(j).color = this.board.get(0).get(0).color;
        }
      }
    }
    floodBoardCells();
  }

  // mark top-left cell as flooded with color using given cell's color
  void floodBoardCellsColor(Cell givenCell) {
    if (givenCell.isFlooded() || areCellsSameColor(this.board.get(0).get(0), givenCell)) {
      return;
    }
    this.board.get(0).get(0).color = givenCell.color;
  }

  // check if all cells are flooded
  boolean isAllFlooded() {
    boolean isAllFlooded = true;
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (!this.board.get(i).get(j).isFlooded()) {
          isAllFlooded = false;
        }
      }
    }
    return isAllFlooded;
  }

  // creates text on board
  String renderBoardText() {
    String render = "";
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        render = render + "" + this.board.get(i).get(j).getCellInfo() + " ";
      }
      render = render + "\n";
    }
    return render;
  }

  // prints board
  void printBoard() {
    System.out.println(renderBoardText());
  }

  // create a random list of colors
  public ArrayList<Color> makeColors() {
    ArrayList<Color> availableColors = new ArrayList<Color>(numberOfColors);
    availableColors.add(Color.blue);
    availableColors.add(Color.green);
    availableColors.add(Color.yellow);
    availableColors.add(Color.red);
    availableColors.add(Color.pink);
    availableColors.add(Color.orange);
    availableColors.add(Color.MAGENTA);
    availableColors.add(Color.gray);

    for (int i = 0; i < numberOfColors; i++) {
      int colorIndex = this.rand.nextInt(availableColors.size());
      this.colorList.add(availableColors.get(colorIndex));
      availableColors.remove(colorIndex);
    }
    return this.colorList;
  }

  // displays number of colors
  public void printColorList() {
    System.out.print("Number of Colors " + numberOfColors + " ");
    for (int i = 0; i < numberOfColors; i++) {
      System.out.print(util.getColor(this.colorList.get(i)) + " ");
    }
  }

  // handles mouse clicks and is given the mouse location
  public void onMouseClicked(Posn pos) {

    ICell cellPressed = new MtCell();

    pos.y = pos.y + cellsize / 2;

    if (this.boardSize == 4) {
      pos.x = pos.x + cellsize;
      pos.y = pos.y + cellsize;
    }
    else if (this.boardSize == 8) {
      pos.x = pos.x + cellsize * 3;
      pos.y = pos.y + cellsize * 3;
    }
    else if (this.boardSize == 12) {
      pos.x = pos.x + cellsize * 5;
      pos.y = pos.y + cellsize * 5;
    }

    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {

        if (!this.board.get(i).get(j).isEmpty()
            && pos.x >= this.board.get(i).get(j).x * this.cellsize + cellsize * boardSize
                - this.cellsize
            && pos.y >= this.board.get(i).get(j).y * this.cellsize + cellsize * boardSize
                - this.cellsize
            && pos.x <= (this.board.get(i).get(j).x * this.cellsize + cellsize * boardSize)
            && pos.y <= (this.board.get(i).get(j).y * this.cellsize + cellsize * boardSize)) {
          cellPressed = this.board.get(i).get(j);
          break;
        }
      }
    }
    if (!cellPressed.isEmpty()) {
      if (!theGameIsOver && this.userTries < this.totalTries) {
        this.userTries++;
      }
      else {
        return;
      }
      floodBoardCellsColor((Cell) cellPressed);

    }
  }

  // handles 'r' key press to reset the game and create a new board
  public void onKeyEvent(String ke) {
    if (ke.equals("r")) {
      System.out.println("onKey event occured and r key is pressed to reset the game !");
      this.resetGame();
    }
    return;
  }

  // handles ticking of the clock and updating the world if needed
  public void onTick() {
    floodBoardCellsColorOnTick();
    return;
  }
}

// represents util class
class Utils {
  public String getColor(Color c) {
    if (c == null) {
      return "Unknown Color";
    }
    else if (c.equals(Color.RED)) {
      return "RED";
    }
    else if (c.equals(Color.GREEN)) {
      return "GREEN";
    }
    else if (c.equals(Color.BLUE)) {
      return "BLUE";
    }
    else if (c.equals(Color.YELLOW)) {
      return "YELLOW";
    }
    else if (c.equals(Color.PINK)) {
      return "PINK";
    }
    else if (c.equals(Color.ORANGE)) {
      return "ORANGE";
    }
    else if (c.equals(Color.MAGENTA)) {
      return "MAGENTA";
    }
    else if (c.equals(Color.GRAY)) {
      return "GRAY";
    }
    else {
      return "Unknown Color";
    }
  }
}

// represents the examples class
class ExamplesFloodItWorld {

  ExamplesFloodItWorld() {
  }

  // represents floodIt worlds
  FloodIt world1;
  FloodIt world2;
  FloodIt world3;
  FloodIt world4;
  FloodIt world5;
  FloodIt world6;
  FloodIt world7;
  FloodIt world8;
  FloodIt world9;
  FloodIt world10;
  FloodIt world11;
  FloodIt world12;
  FloodIt world13;
  FloodIt world14;

  // represents cells
  ICell cell0 = new MtCell(10, 10);
  ICell cell1 = new Cell(50, 50, Color.pink);
  ICell cell2 = new Cell(60, 60, Color.blue);
  Cell cell3 = new Cell(20, 20, Color.gray); 
  Cell cell4 = new Cell(40, 40, Color.green); 


  Utils util = new Utils();

  double delay = 0.1;

  // init world data
  void initWorld() {
    world1 = new FloodIt(2, 3);
    world2 = new FloodIt(4, 5);
    world3 = new FloodIt(8, 6);
    world4 = new FloodIt(12, 7);
  }

  // tests for floodIt
  void testFloodIt(Tester t) {
    initWorld();

    this.world1.makeColors();
    this.world1.makeBoard();
    this.world1.printBoard();

    this.world2.makeColors();
    this.world2.makeBoard();
    this.world2.printBoard();

    this.world3.makeColors();
    this.world3.makeBoard();
    this.world3.printBoard();
  }

  // tests for isEmpty()
  boolean testIsEmpty(Tester t) {
    return t.checkExpect(this.cell0.isEmpty(), true) && t.checkExpect(this.cell1.isEmpty(), false);
  }

  // tests for cellImage(int cellSize)
  boolean testCellImage(Tester t) {
    return t.checkExpect(cell0.cellImage(0), new EmptyImage()) && t.checkExpect(cell1.cellImage(20),
        new RectangleImage(20, 20, OutlineMode.SOLID, Color.pink));

  }

  // tests for world 3 flood it game
  void testFloodItWorld3Game(Tester t) {
    initWorld();

    this.world3.makeBoard();

    world3.scene = world3.makeScene();
    world3.bigBang(world3.sceneSize, world3.sceneSize, delay);
  }

  // tests for makeColors()
  boolean testMakeColors(Tester t) {
    initWorld();
    return t.checkExpect(world1.makeColors(),
        new ArrayList<Color>(Arrays.asList(Color.ORANGE, Color.PINK, Color.YELLOW)))
        && t.checkExpect(world2.makeColors(), new ArrayList<Color>(
            Arrays.asList(Color.ORANGE, Color.PINK, Color.YELLOW, Color.GRAY, Color.GREEN)));
  }

  // test getCellInfo()
  boolean testGetCellInfo(Tester t) {
    return t.checkExpect(cell0.getCellInfo(), "Cell[empty]")
        && t.checkExpect(cell1.getCellInfo(), "Cell[(" + 50 + "," + 50 + ") " + "PINK" + "]")
        && t.checkExpect(cell2.getCellInfo(), "Cell[(" + 60 + "," + 60 + ") " + "BLUE" + "]");

  }

  // test renderBoardImage()
  boolean testRenderBoardImage(Tester t) {
    initWorld();

    this.world4.makeBoard();

    WorldImage image = world4.renderBoardImage();
    return t.checkExpect(image.getHeight(), 480.0) && t.checkExpect(image.getWidth(), 480.0);
  }

  // test renderAllCells()
  boolean testRenderAllCells(Tester t) {
    initWorld();

    this.world4.makeBoard();

    WorldImage image = world4.renderAllCells();
    return t.checkExpect(image.getHeight(), 480.0) && t.checkExpect(image.getWidth(), 480.0);
  }

  // test renderRow(ArrayList<Cell> row)
  boolean testRenderRow(Tester t) {
    initWorld();

    this.world4.makeBoard();

    WorldImage image = world4.renderRow(this.world4.board.get(0));
    return t.checkExpect(image.getHeight(), 480.0) && t.checkExpect(image.getWidth(), 40.0);
  }

  // test renderGame()
  boolean testRenderGame(Tester t) {
    initWorld();

    this.world4.makeBoard();

    this.world4.scene = world4.renderGame();
    return t.checkExpect(this.world4.scene.height, world4.sceneSize)
        && t.checkExpect(this.world4.scene.width, world4.sceneSize);
  }

  // test buildBoardCells()
  boolean testBuildBoardCells(Tester t) {
    initWorld();

    this.world1.makeColors();
    this.world1.buildBoardCells();

    return t.checkExpect(util.getColor(world1.board.get(0).get(0).color), "YELLOW")
        && t.checkExpect(util.getColor(world1.board.get(1).get(1).color), "PINK");
  }

  // test connectBoardCells()
  boolean testConnectBoardCells(Tester t) {
    initWorld();

    this.world1.makeColors();
    this.world1.buildBoardCells();
    this.world1.connectBoardCells();

    return t.checkExpect(this.world1.board.get(0).get(0).top.isEmpty(), true)
        && t.checkExpect(this.world1.board.get(0).get(0).left.isEmpty(), true)
        && t.checkExpect(this.world1.board.get(0).get(0).right.isEmpty(), false)
        && t.checkExpect(this.world1.board.get(0).get(0).bottom.isEmpty(), false)
        && t.checkExpect(this.world1.board.get(1).get(1).top.isEmpty(), false)
        && t.checkExpect(this.world1.board.get(1).get(1).left.isEmpty(), false)
        && t.checkExpect(this.world1.board.get(1).get(1).right.isEmpty(), true)
        && t.checkExpect(this.world1.board.get(1).get(1).bottom.isEmpty(), true);
  }

  // testgetColor()
  void testGetColor(Tester t) {
    t.checkExpect(util.getColor(Color.gray), "GRAY");
  }

  // tests flood it
  boolean testFloodItRenderText(Tester t) {
    initWorld();

    this.world1.makeBoard();
    this.world2.makeBoard();

    return t.checkExpect(this.world1.renderBoardText(),
        "Cell[(0,0) YELLOW] Cell[(0,1) ORANGE] \nCell[(1,0) YELLOW] Cell[(1,1) PINK] \n")
        && t.checkExpect(this.world2.renderBoardText(),
            "Cell[(0,0) ORANGE] Cell[(0,1) GREEN] Cell[(0,2) PINK] Cell[(0,3) YELLOW] \n"
                + "Cell[(1,0) PINK] Cell[(1,1) PINK] Cell[(1,2) GRAY] Cell[(1,3) YELLOW] \n"
                + "Cell[(2,0) ORANGE] Cell[(2,1) GRAY] Cell[(2,2) ORANGE] Cell[(2,3) GRAY] \n"
                + "Cell[(3,0) ORANGE] Cell[(3,1) YELLOW] Cell[(3,2) YELLOW] Cell[(3,3) GRAY] \n");
  }

  // test makeBoard()
  boolean testMakeBoard(Tester t) {
    initWorld();

    this.world1.makeBoard();

    return t.checkExpect(this.world1.board.get(0).get(0).top.isEmpty(), true)
        && t.checkExpect(this.world1.board.get(0).get(0).left.isEmpty(), true)
        && t.checkExpect(this.world1.board.get(0).get(0).right.isEmpty(), false)
        && t.checkExpect(this.world1.board.get(0).get(0).bottom.isEmpty(), false)
        && t.checkExpect(this.world1.board.get(1).get(1).top.isEmpty(), false)
        && t.checkExpect(this.world1.board.get(1).get(1).left.isEmpty(), false)
        && t.checkExpect(this.world1.board.get(1).get(1).right.isEmpty(), true)
        && t.checkExpect(this.world1.board.get(1).get(1).bottom.isEmpty(), true)
        && t.checkExpect(this.world1.renderBoardText(),
            "Cell[(0,0) YELLOW] Cell[(0,1) ORANGE] \nCell[(1,0) YELLOW] Cell[(1,1) PINK] \n");
  }

  // test printBoard()
  void testPrintBoard(Tester t) {
    initWorld();

    this.world1.makeColors();
    this.world1.makeBoard();
    this.world1.printBoard();

    this.world2.makeColors();
    this.world2.makeBoard();
    this.world2.printBoard();

    this.world3.makeColors();
    this.world3.makeBoard();
    this.world3.printBoard();
  }

  // test makeScene()
  void testMakeScene(Tester t) {
    initWorld();
    this.world4.makeBoard();
    this.world4.scene = world4.makeScene();
    world4.bigBang(world4.sceneSize, world4.sceneSize, delay);
  }

  // tests for world 1 flood it game
  void testFloodItWorldGame(Tester t) {
    initWorld();

    System.out.println("Building world1");
    this.world1.makeBoard();
    t.checkExpect(util.getColor(world1.board.get(0).get(0).color), "YELLOW");
    t.checkExpect(util.getColor(world1.board.get(0).get(1).color), "ORANGE");
    t.checkExpect(util.getColor(world1.board.get(1).get(0).color), "YELLOW");
    t.checkExpect(util.getColor(world1.board.get(1).get(1).color), "PINK");

    world1.scene = world1.makeScene();
    world1.bigBang(world1.sceneSize, world1.sceneSize, delay);
  }

  // tests for world 2 flood it game
  void testFloodItWorld2Game(Tester t) {
    initWorld();

    this.world2.makeBoard();

    world2.scene = world2.makeScene();
    world2.bigBang(world2.sceneSize, world2.sceneSize, delay);
  }

  // test resetGame()
  boolean testResetGame(Tester t) {
    initWorld();

    this.world4.makeBoard();

    this.world4.scene = world4.renderGame();
    this.world4.board.get(0).get(0).flooded = true;
    this.world4.board.get(0).get(1).flooded = true;
    this.world4.resetGame();
    return t.checkExpect(this.world4.board.get(0).get(0).flooded, true)
        && t.checkExpect(this.world4.board.get(0).get(1).flooded, false);
  }

  // test isFlooded()
  boolean testIsFlooded(Tester t) {
    initWorld();

    this.world4.makeBoard();

    this.world4.scene = world4.renderGame();
    this.world4.board.get(0).get(0).flooded = true;
    this.world4.board.get(0).get(1).flooded = false;
    return t.checkExpect(this.world4.board.get(0).get(0).isFlooded(), true)
        && t.checkExpect(this.world4.board.get(0).get(1).isFlooded(), false);
  }

  // test floodBoardCells()
  boolean testfloodBoardCells(Tester t) {
    initWorld();
    this.world4.userTries = 0;
    this.world4.makeColors();
    this.world4.buildBoardCells();
    this.world4.connectBoardCells();
    this.world4.floodBoardCells();
    return t.checkExpect(this.world4.board.get(0).get(0).flooded, true);
  }

  // test floodBoardCellsColor()
  boolean testFloodBoardCellsColor(Tester t) {
    initWorld();
    this.world4.userTries = 0;
    this.world4.makeColors();
    this.world4.buildBoardCells();
    this.world4.connectBoardCells();
    this.world4.floodBoardCells();
    this.world4.floodBoardCellsColor(this.world4.board.get(0).get(1));
    return t.checkExpect(this.world4.board.get(0).get(0).flooded, true)
        && t.checkExpect(this.world4.board.get(0).get(1).flooded, true) && t.checkExpect(
            this.world4.board.get(0).get(0).color, this.world4.board.get(0).get(1).color);
  }

  // test isAllFlooded()
  boolean testisAllFlooded(Tester t) {
    initWorld();
    this.world4.userTries = 0;
    this.world4.makeColors();
    this.world4.buildBoardCells();
    this.world4.connectBoardCells();
    this.world4.floodBoardCells();
    this.world4.floodBoardCellsColor(this.world4.board.get(0).get(1));
    return t.checkExpect(this.world4.isAllFlooded(), false);
  }

  // test floodBoardCellsColorOnTick()
  boolean testFloodBoardCellsColorOnTick(Tester t) {
    initWorld();
    this.world4.userTries = 0;
    this.world4.makeColors();
    this.world4.buildBoardCells();
    this.world4.connectBoardCells();
    this.world4.floodBoardCells();
    this.world4.floodBoardCellsColor(this.world4.board.get(0).get(1));
    this.world4.floodBoardCellsColorOnTick();
    return t.checkExpect(this.world4.board.get(0).get(0).flooded, true)
        && t.checkExpect(this.world4.board.get(0).get(1).flooded, true) && t.checkExpect(
            this.world4.board.get(0).get(0).color, this.world4.board.get(0).get(1).color);
  }

  //test WorldEnds() 
  void testWorldEnds(Tester t) {
    t.checkExpect(world2.worldEnds(), new WorldEnd(false, world2.makeScene()));
    t.checkExpect(world4.worldEnds(), new WorldEnd(false, world4.makeScene()));
  }

  //test isCurrentCellAdjacentToFlooded(Cell) 
  void testIsCurrentCellAdjacentToFlooded(Tester t) {
    t.checkExpect(world1.isCurrentCellAdjacentToFlooded(cell3), false);
    t.checkExpect(world3.isCurrentCellAdjacentToFlooded(cell4), false);
    t.checkExpect(world4.isCurrentCellAdjacentToFlooded(cell3), false);
  }

  //test areCellsSameColor(Cell, Cell) 
  void testAreCellsSameColor(Tester t) {
    t.checkExpect(world1.areCellsSameColor(cell4, cell3), false);
    t.checkExpect(world1.areCellsSameColor(cell4, cell4), true);
  }

  //test isAllFlooded() 
  void testIsAllFlooded(Tester t) {
    initWorld();
    t.checkExpect(world1.isAllFlooded(), false);
    t.checkExpect(world4.isAllFlooded(), false);
  }
}
