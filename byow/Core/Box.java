package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import org.junit.jupiter.api.MethodOrderer;

import java.util.*;

public class Box {
    private int holeSize = 1;
    TETile[][] world;
    private ArrayList<Room> rooms = new ArrayList<Room>();
    public Player avatar;

    private int w = 2;
    private int h = 3;

    public static void main(String[] args) {
        Box world = new Box(80,60,2023);
        TERenderer ter = new TERenderer();
        ter.initialize(80, 60);
        ter.renderFrame(world.world);
    }

    public Box(int width, int height, long seed) {
        this.world = new TETile[width][height];
        this.w = width;
        this.h = height;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.world[x][y] = Tileset.NOTHING;
            }
        }

        //get room coordinates
        Random r = new Random(seed);
        int roomnumber = r.nextInt(10,30);
        for (int i = 0; i < roomnumber; i++) {
            int roomw = r.nextInt(2,7);
            int roomh = r.nextInt(2,7);
            int tilex = r.nextInt(1, world.length - roomw - 1);
            int tiley = r.nextInt(1, world[0].length - roomh - 1);

            //check overlap
            Room checkover = new Room(roomw, roomh, tilex, tiley);
            boolean overlap = false;
            for (Room ro : rooms) {
                if (ro.isOverlap(checkover)) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) {
                create2room(roomw, roomh, tilex, tiley);
            }
        }
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room r1 = rooms.get(i);
            Room r2 = rooms.get(i+1);
            connectrooms(r1, r2);
        }
        createwall();
        Room randomstart = rooms.get(r.nextInt(0,rooms.size()));
        avatar = new Player(randomstart.centerx, randomstart.centery);
        this.world[avatar.tilex][avatar.tiley] = Tileset.AVATAR;
    }


    private class Room {
        int centerx;
        int centery;
        int tilex;
        int tiley;
        int height;
        int width;
        private Room(int w, int h, int x, int y) {
            height = h;
            width = w;
            tilex = x;
            tiley = y;
            centerx = x + width / 2;
            centery = y + height / 2;
        }
        private boolean isOverlap(Room r) {
            return (this.tilex + this.width >= r.tilex && r.tilex + r.width >= this.tilex &&
                    this.tiley + this.height >= r.tiley && r.tiley + r.height >= this.tiley);
        }
    }

    private void connectrooms(Room r1, Room r2) {
        if (r1.tilex == r2.tilex) {
            for (int i = Math.min(r1.tiley, r2.tiley); i <= Math.max(r1.tiley, r2.tiley); i++){
                world[r1.tilex][i] = Tileset.FLOOR;
            }
        }

        if (r1.tiley == r2.tiley) {
            for (int i = Math.min(r1.tilex, r2.tilex); i <= Math.max(r1.tilex, r2.tilex); i++){
                world[i][r1.tiley] = Tileset.FLOOR;
            }
        }

        Room source = null;
        Room destination = null;

        if (r1.tiley > r2.tiley){
            source = r2;
            destination = r1;
        }
        else {
            source = r1;
            destination = r2;
        }

        for (int i = source.tiley; i <= destination.tiley; i++) {
            world[source.tilex][i] = Tileset.FLOOR;
        }

        for (int i = Math.min(source.tilex, destination.tilex); i <= Math.max(source.tilex, destination.tilex); i++) {
            world[i][destination.tiley] = Tileset.FLOOR;
        }
    }

    private void create2room(int roomw, int roomh, int tilex, int tiley) {
        for (int x = tilex - 1; x <= tilex + roomw; x++) {
            for (int y = tiley - 1; y <= tiley + roomh; y++) {
                if (x == tilex - 1 || x == tilex + roomw || y == tiley - 1 || y == tiley + roomh) {
                    world[x][y] = Tileset.WALL;
                } else {
                    world[x][y] = Tileset.FLOOR;
                }
            }
        }
        Room temp = new Room(roomw, roomh, tilex, tiley);
        rooms.add(temp);
    }
    private void createwall() {
        for (int x = 0; x < world.length; x++){
            for (int y =0; y < world[0].length;y++){
                if (world[x][y] == Tileset.FLOOR){
                    if (world[x+1][y] == Tileset.NOTHING){
                        world[x+1][y] = Tileset.WALL;
                    }
                    if (world[x-1][y] == Tileset.NOTHING){
                        world[x-1][y] = Tileset.WALL;
                    }
                    if (world[x][y+1] == Tileset.NOTHING){
                        world[x][y+1] = Tileset.WALL;
                    }
                    if (world[x][y-1] == Tileset.NOTHING){
                        world[x][y-1] = Tileset.WALL;
                    }
                }
            }
        }
    }

    public class Player{
        int tilex;
        int tiley;
        public Player(int x, int y){
            this.tilex = x;
            this.tiley = y;
        }

        public void move(String direction){
            direction = direction.toUpperCase();
            switch (direction){
                case "W" -> {
                    int newpos = avatar.tiley + 1;
                    if (world[avatar.tilex][newpos] != Tileset.WALL){
                        world[avatar.tilex][avatar.tiley] = Tileset.FLOOR;
                        avatar.tiley = newpos;
                        world[avatar.tilex][avatar.tiley] = Tileset.AVATAR;
                    }
                }
                case "A" -> {
                    int newpos = avatar.tilex - 1;
                    if (world[newpos][avatar.tiley] != Tileset.WALL){
                        world[avatar.tilex][avatar.tiley] = Tileset.FLOOR;
                        avatar.tilex = newpos;
                        world[avatar.tilex][avatar.tiley] = Tileset.AVATAR;
                    }
                }
                case "S" -> {
                    int newpos = avatar.tiley - 1;
                    if (world[avatar.tilex][newpos] != Tileset.WALL){
                        world[avatar.tilex][avatar.tiley] = Tileset.FLOOR;
                        avatar.tiley = newpos;
                        world[avatar.tilex][avatar.tiley] = Tileset.AVATAR;
                    }
                }
                case "D" -> {
                    int newpos = avatar.tilex + 1;
                    if (world[newpos][avatar.tiley] != Tileset.WALL){
                        world[avatar.tilex][avatar.tiley] = Tileset.FLOOR;
                        avatar.tilex = newpos;
                        world[avatar.tilex][avatar.tiley] = Tileset.AVATAR;
                    }
                }
            }
        }
    }

    public void mouse(){

    }
}
