package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenerateStructures {

    private final int worldH;

    private final int worldW;

    private final Random rand;

    public List<Room> allRooms = new ArrayList<>();

    public GenerateStructures(Random rand, int worldH, int worldW) {
        this.rand = rand;
        this.worldH = worldH;
        this.worldW = worldW;
    }

    public void generateHallway(Room currRoom, TETile[][] world) {
        Room prevRoom = allRooms.get(allRooms.indexOf(currRoom) - 1);
        int leftmostX = Math.min(currRoom.centerX, prevRoom.centerX);
        int rightmostX = Math.max(currRoom.centerX, prevRoom.centerX);
        int leftmostY;
        int rightmostY;
        if (leftmostX == currRoom.centerX) {
            leftmostY = currRoom.centerY;
            rightmostY = prevRoom.centerY;
        } else {
            leftmostY = prevRoom.centerY;
            rightmostY = currRoom.centerY;
        }
        for (int x = leftmostX; x <= rightmostX; x++) {
            world[x][leftmostY] = Tileset.FLOOR;
        }
        for (int y = Math.min(leftmostY, rightmostY); y <= Math.max(leftmostY, rightmostY); y++) {
            world[rightmostX][y] = Tileset.FLOOR;
        }
    }

    public void addWalls(TETile[][] world) {
        List<int[]> wallCoords = new ArrayList<>();
        for (int x = 0; x < worldW; x++) {
            for (int y = 0; y < worldH; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    if (x == worldW - 1 || y == worldH - 1 || x == 0 || y == 0) {
                        world[x][y] = Tileset.WALL;
                        wallCoords.add(new int[] {x, y});
                    } else {
                        if (world[x - 1][y] == Tileset.NOTHING) {
                            world[x - 1][y] = Tileset.WALL;
                            wallCoords.add(new int[] {x - 1, y});
                        }
                        if (world[x + 1][y] == Tileset.NOTHING) {
                            world[x + 1][y] = Tileset.WALL;
                            wallCoords.add(new int[] {x + 1, y});
                        }
                        if (world[x][y - 1] == Tileset.NOTHING) {
                            world[x][y - 1] = Tileset.WALL;
                            wallCoords.add(new int[] {x, y - 1});
                        }
                        if (world[x][y + 1] == Tileset.NOTHING) {
                            world[x][y + 1] = Tileset.WALL;
                            wallCoords.add(new int[] {x, y + 1});
                        }
                    }
                }
            }
        }
        int[] gateCoord = wallCoords.get(rand.nextInt(wallCoords.size()));
        world[gateCoord[0]][gateCoord[1]] = Tileset.LOCKED_DOOR;
    }


    public void fillGaps(TETile[][] world) {
        for (int x = 1; x < worldW - 1; x++) {
            for (int y = 1; y < worldH - 1; y++) {
                if (world[x][y] == Tileset.NOTHING) {
                    if (world[x - 1][y] == Tileset.FLOOR && world[x + 1][y] == Tileset.FLOOR && world[x][y - 1] == Tileset.FLOOR && world[x][y + 1] == Tileset.FLOOR) {
                        world[x][y] = Tileset.FLOOR;
                    }
                }
            }
        }
    }

    public long createRoom(int bottomCornerX, int bottomCornerY, TETile[][] world, long numTimesRandCall) {
        int height = RandomUtils.uniform(rand, 3, 6);
        numTimesRandCall++;
        int width = RandomUtils.uniform(rand, 3, 6);
        numTimesRandCall++;
        Room temp = new Room(bottomCornerX, bottomCornerY, width, height);
        if (!checkIntersect(bottomCornerX, bottomCornerY, height, width, world) && temp.validRoom) {
            allRooms.add(temp);
            for (int x = bottomCornerX; x < (bottomCornerX + width) && x < worldW; x++) {
                for (int y = bottomCornerY; y < (bottomCornerY + height) && y < worldH; y++) {
                    world[x][y] = Tileset.FLOOR;
                }
            }
            if (allRooms.size() > 1) {
                generateHallway(temp, world);
            }
        }
        return numTimesRandCall;
    }

    private boolean checkIntersect(int bottomCornerX, int bottomCornerY, int height, int width, TETile[][] world) {
        boolean intersectExists = false;
        for (int x = bottomCornerX; x < (bottomCornerX + width) && x < worldW; x++) {
            for (int y = bottomCornerY; y < (bottomCornerY + height) && y < worldH; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    intersectExists = true;
                    break;
                }
            }
        }

        return intersectExists;
    }

    private class Room {
        private int centerX;
        private int centerY;
        private boolean validRoom = true;

        private Room(int bottomCornerX, int bottomCornerY, int width, int height) {
            centerX = (width / 2) + bottomCornerX;
            centerY = (height / 2) + bottomCornerY;
            if (bottomCornerX + width - 1 >= worldW - 1 || bottomCornerX == 0) {
                validRoom = false;
            }
            if (bottomCornerY + height - 1 >= worldH - 1 || bottomCornerY == 0) {
                validRoom = false;
            }
            if (centerX >= worldW) {
                centerX = worldW - 1;
            }
            if (centerY >= worldH) {
                centerY = worldH - 1;
            }
        }
    }
}