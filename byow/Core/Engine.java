package byow.Core;

import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.awt.*;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private static final int ROOM_ODDS = 40;
    private static final int MAKE_ROOM = 10;
    private long loadedRandCall = 0;
    private long numTimesRandCall = 0;
    private long seed;
    boolean loadedWorld = false;
    private int characterX = 0;
    private int characterY = 0;
    private String movementPath;
    private StringBuilder retainedPath;
    boolean worldStarted = false;
    boolean won = false;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        boolean quit = false;
        StringBuilder seedAsString = new StringBuilder();
        StringBuilder path = new StringBuilder();
        String movement = "wasd";
        ter.initialize(WIDTH, HEIGHT + 1, 0, 1);
        menuscreen();
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }
        while (!quit) {
            if (won){
                drawFrame(WIDTH/2,HEIGHT/2, "You Win!!",true);
                break;
            }
            if (worldStarted) {
                HUD(finalWorldFrame);
            }
            if (StdDraw.hasNextKeyTyped()) {
                char currChar = Character.toLowerCase(StdDraw.nextKeyTyped());
                String curr = String.valueOf(currChar);
                switch (curr) {
                    case ":" -> {
                        while (true) {
                            if (StdDraw.hasNextKeyTyped()) {
                                break;
                            }
                        }
                        if (worldStarted) {
                            curr = String.valueOf(StdDraw.nextKeyTyped());
                            if (curr.equalsIgnoreCase("q")) {
                                if (!loadedWorld) {
                                    seed = Long.parseLong(seedAsString.toString());
                                }
                                movementPath = path.toString();
                                createSaveFile(seed, numTimesRandCall);
                            }
                            quit = true;
                        }
                    }
                    case "l" -> {
                        if (!worldStarted) {
                            loadedWorld = true;
                            loadFile();
                            generateWorld(seed, finalWorldFrame);
                            startingCharacterPlace(finalWorldFrame);
                            readMovementPath(finalWorldFrame);
                            HUD(finalWorldFrame);
                            worldStarted = true;
                        }
                    }
                    case "r" -> {
                        if (!worldStarted) {
                            loadFile();
                            generateWorld(seed, finalWorldFrame);
                            startingCharacterPlace(finalWorldFrame);
                            readMovementPath(finalWorldFrame);
                            loadedWorld = true;
                            worldStarted = true;
                        }
                    }
                    case "q" -> {
                        quit = true;
                        quitGame();
                    }
                    case "n" -> {
                        seedAsString = enterSeed();
                        seed = Long.parseLong(seedAsString.toString());
                        generateWorld(seed, finalWorldFrame);
                        startingCharacterPlace(finalWorldFrame);
                        worldStarted = true;
                    }
                    default -> {
                        if (movement.contains(curr) && worldStarted) {
                            path.append(curr);
                            if (loadedWorld) {
                                retainedPath.append(curr);
                            }
                            switch (curr) {
                                case "w" -> characterMovement(characterX, characterY + 1, finalWorldFrame);
                                case "a" -> characterMovement(characterX - 1, characterY, finalWorldFrame);
                                case "s" -> characterMovement(characterX, characterY - 1, finalWorldFrame);
                                case "d" -> characterMovement(characterX + 1, characterY, finalWorldFrame);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, running both of these:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        ter.initialize(WIDTH, HEIGHT);
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }
        boolean save = readInputString(input);
        generateWorld(seed, finalWorldFrame);
        startingCharacterPlace(finalWorldFrame);
        readMovementPath(finalWorldFrame);
        if (save) {
            createSaveFile(seed, numTimesRandCall);
        }
        ter.renderFrame(finalWorldFrame);
        return finalWorldFrame;
    }

    private void readMovementPath(TETile[][] world) {
        StringInputDevice inputDevice;
        if (!loadedWorld) {
            inputDevice = new StringInputDevice(movementPath);
        } else {
            inputDevice = new StringInputDevice(retainedPath.toString());
        }
        while (inputDevice.possibleNextInput()) {
            char currChar = inputDevice.getNextKey();
            String curr = String.valueOf(currChar);
            switch (curr) {
                case "w" -> characterMovement(characterX, characterY + 1, world);
                case "a" -> characterMovement(characterX - 1, characterY, world);
                case "s" -> characterMovement(characterX, characterY - 1, world);
                case "d" -> characterMovement(characterX + 1, characterY, world);
            }
        }
    }

    private boolean readInputString(String input) {
        boolean save = false;
        String movement = "wasd";
        StringInputDevice inputDevice = new StringInputDevice(input.toLowerCase());
        StringBuilder seedAsString = new StringBuilder();
        StringBuilder path = new StringBuilder();
        while (inputDevice.possibleNextInput()) {
            char currChar = Character.toLowerCase(inputDevice.getNextKey());
            String curr = String.valueOf(currChar);
            switch (curr) {
                case ":" -> {
                    curr = String.valueOf(inputDevice.getNextKey());
                    if (curr.equalsIgnoreCase("q")) {
                        if (!loadedWorld) {
                            seed = Long.parseLong(seedAsString.toString());
                        }
                        save = true;
                    }
                }
                case "l" -> {
                    loadedWorld = true;
                    loadFile();
                }
                case "q" -> quitGame();
                case "n" -> {
                    while (!curr.equalsIgnoreCase("s")) {
                        if (inputDevice.possibleNextInput()) {
                            currChar = inputDevice.getNextKey();
                            curr = String.valueOf(currChar);
                            if (Character.isDigit(currChar)) {
                                seedAsString.append(curr);
                            }
                        }
                    }
                }
                default -> {
                    if (movement.contains(curr)) {
                        path.append(curr);
                        if (loadedWorld) {
                            retainedPath.append(curr);
                        }
                    }
                }
            }
        }
        if (!loadedWorld) {
            seed = Long.parseLong(seedAsString.toString());
        }
        movementPath = path.toString();
        return save;
    }

    private void generateWorld(long seed, TETile[][] world) {
        Random rand = new Random(seed);
        GenerateStructures structures = new GenerateStructures(rand, HEIGHT, WIDTH);
        numTimesRandCall++;
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                if (world[x][y] != Tileset.FLOOR) {
                    int generateRoomHere = RandomUtils.uniform(rand, 1, ROOM_ODDS);
                    numTimesRandCall++;
                    if (generateRoomHere == MAKE_ROOM) {
                        numTimesRandCall += structures.createRoom(x, y, world, numTimesRandCall);
                    }
                }
            }
        }
        structures.fillGaps(world);
        structures.addWalls(world);
        numTimesRandCall++;
        if (loadedWorld) {
            if (numTimesRandCall != loadedRandCall) {
                System.out.println("Generated random state does not match saved random state. \nGenerated: "
                        + numTimesRandCall + ", Saved: " + loadedRandCall);
            }
        }
    }

    private void startingCharacterPlace(TETile[][] world) {
        boolean characterCreated = false;
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                if (world[x][y].equals(Tileset.FLOOR)) {
                    characterX = x;
                    characterY = y;
                    world[x][y] = Tileset.AVATAR;
                    characterCreated = true;
                    break;
                }
            }
            if (characterCreated) {
                break;
            }
        }
    }

    private void characterMovement(int newX, int newY, TETile[][] world) {
        if (newX < WIDTH && newY < HEIGHT) {
            if (world[newX][newY].equals(Tileset.LOCKED_DOOR)){
                won = true;
            }
            if (world[newX][newY].equals(Tileset.FLOOR) || world[newX][newY].equals(Tileset.LOCKED_DOOR)) {
                world[characterX][characterY] = Tileset.FLOOR;
                world[newX][newY] = Tileset.AVATAR;
                if (!loadedWorld) {
                    HUD(world);
                    StdDraw.pause(250);
                }
                characterX = newX;
                characterY = newY;
            }
        }
    }

    //@source: https://www.w3schools.com/java/java_files_create.asp, https://www.w3schools.com/java/java_files_delete.asp
    private void createSaveFile(long seed, long numTimesRandCall) {
        try {
            File saveFile = new File("save-file.txt");
            if (saveFile.createNewFile()) {
                try {
                    FileWriter writer = new FileWriter("save-file.txt");
                    writer.write(Long.toString(seed));
                    writer.write("\n" + numTimesRandCall);
                    if (!loadedWorld) {
                        writer.write("\n" + movementPath);
                    } else {
                        writer.write("\n" + retainedPath);
                    }
                    writer.close();
                } catch (IOException e) {
                    System.out.println("Exception occurred: " + e);
                }
                quitGame();
            } else {
                if (saveFile.delete()) {
                    System.out.println("Previous save deleted");
                    createSaveFile(seed, numTimesRandCall);
                } else {
                    System.out.println("Failed to delete the previous save file.");
                }
            }
        } catch (IOException e) {
            System.out.println("Exception occurred: " + e);
        }
    }

    //@source: https://www.w3schools.com/java/java_files_read.asp
    private void loadFile() {
        try {
            File saveFile = new File("save-file.txt");
            Scanner reader = new Scanner(saveFile);
            int i = 0;
            while (reader.hasNextLine()) {
                String currLine = reader.nextLine();
                if (i == 0) {
                    seed = Long.parseLong(currLine);
                } else if (i == 1) {
                    loadedRandCall = Long.parseLong(currLine);
                } else if (i == 2) {
                    movementPath = currLine;
                    retainedPath = new StringBuilder(currLine);
                }
                i++;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Exception occurred: " + e);
            quitGame();
        }
    }

    //@source: https://www.scaler.com/topics/java/exit-in-java/
    private void quitGame() {
        System.exit(0);
    }
    private void menuscreen() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontbig = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontbig);
        drawFrame(WIDTH / 2, HEIGHT / 2, "New Game. (N)", false);
        drawFrame(WIDTH / 2, HEIGHT / 2 - 2, "Load Game. (L)", false);
        drawFrame(WIDTH / 2, HEIGHT / 2 - 4, "Replay Last Save (R)", false);
        drawFrame(WIDTH / 2, HEIGHT / 2 - 6, "Quit Game. (Q)", false);
        StdDraw.show();
        StdDraw.pause(1000);
    }
    private StringBuilder enterSeed(){
        drawFrame(WIDTH / 2, HEIGHT / 2, "Enter seed: ", true);
        StringBuilder seedAsString = new StringBuilder();
        char currChar = Character.toLowerCase('n');
        String curr = String.valueOf(currChar);
        while (!curr.equalsIgnoreCase("s")) {
            if (StdDraw.hasNextKeyTyped()) {
                currChar = StdDraw.nextKeyTyped();
                curr = String.valueOf(currChar);
                if (Character.isDigit(currChar)) {
                    seedAsString.append(curr);
                    drawFrame(WIDTH / 2, HEIGHT / 2, "Enter seed: ", true);
                    drawFrame(WIDTH / 2, HEIGHT / 2 - 2, seedAsString.toString(), false);
                }
            }
        }
        return seedAsString;
    }

    //@source: https://www.javatpoint.com/java-get-current-date
    private void HUD(TETile[][] world) {
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = ((int) StdDraw.mouseY()) - 1;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        LocalDateTime currTime = LocalDateTime.now();
        ter.renderFrame(world);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 20);
        Font fontSmall = new Font("Monaco", Font.BOLD, 10);
        StdDraw.setFont(fontSmall);
        StdDraw.text(3.75, 0.5, "Type :Q to Save and Quit");
        StdDraw.text(15, 0.5, dtf.format(currTime));
        if (mouseY < HEIGHT && mouseY >= 0) {
            StdDraw.text(10, 0.5, world[mouseX][mouseY].description());
        }
        StdDraw.line(0, 1, WIDTH, 1);
        StdDraw.line(8.5, 0, 8.5, 1);
        StdDraw.line(11.5, 0, 11.5, 1);
        StdDraw.show();
        StdDraw.setFont(fontBig);
    }

    public void drawFrame(int x, int y, String s, boolean clear) {
        /* Take the input string S and display it at the center of the screen,
         * with the pen settings given below. */
        if (clear) {
            StdDraw.clear(Color.BLACK);
        }
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontBig);
        StdDraw.text(x, y, s);
        StdDraw.show();
    }

    public static void main(String[] args) {
        Engine test = new Engine();
        test.interactWithKeyboard();
//        test.interactWithInputString("n123sssww");
//        test.interactWithInputString("n123sss:q");
//        test.interactWithInputString("lww");
    }
}
