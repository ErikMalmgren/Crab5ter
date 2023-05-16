package main.crab5ter;

public class Maze {
    private int[][] easyMaze = {
            {2,0,0,1,1,1,1,1,1,1},
            {0,0,0,0,0,0,0,0,0,1},
            {1,0,0,1,1,1,1,0,0,1},
            {1,0,0,0,0,0,1,0,-1,0},
            {1,0,0,1,0,1,1,1,1,1},
            {1,0,0,0,0,0,1,0,0,3},
            {1,0,0,-1,0,0,1,0,0,1},
            {1,0,0,1,0,0,1,0,0,1},
            {1,0,0,1,0,0,1,0,0,1},
            {1,0,0,1,0,0,0,0,0,0},
            {1,1,1,1,1,1,1,0,-1,0}
    };

    private int[][] mediumMaze = {
            {2,0,0,1,1,1,1,1,1,1},
            {0,0,0,0,0,0,0,0,0,1},
            {1,0,0,1,1,1,1,0,0,1},
            {1,0,0,0,0,0,1,0,-1,0},
            {1,0,0,1,0,1,1,1,1,1},
            {1,0,0,0,0,0,1,0,0,3},
            {1,0,0,-1,0,0,1,0,0,1},
            {1,0,0,1,0,0,1,0,0,1},
            {1,0,0,1,0,0,1,0,0,1},
            {1,0,0,1,0,0,0,0,0,0},
            {1,1,1,1,1,1,1,0,-1,0}
    };

    private int[][] hardMaze = {
            {2,0,0,1,0,0,0,0,4,1,4,0,0,-1,0},
            {1,0,0,1,-1,0,1,-1,0,1,0,1,0,0,0},
            {1,0,-1,1,0,0,1,0,0,1,0,1,0,-1,0},
            {1,0,0,1,-1,0,1,0,-1,0,0,1,0,0,0},
            {1,-1,0,1,0,0,1,1,1,1,1,1,-1,0,-1},
            {1,0,0,0,0,-1,1,0,0,0,0,0,0,0,0},
            {1,1,1,1,1,1,1,-1,0,1,1,1,1,1,1},
            {1,0,0,0,0,-1,1,0,0,1,-1,0,0,-1,0},
            {1,0,0,-1,1,0,0,0,-1,1,0,0,1,0,0},
            {1,1,0,0,1,1,1,1,1,1,0,-1,1,-1,0},
            {-1,0,0,-1,1,0,0,0,0,0,0,0,1,0,0},
            {-1,0,1,1,1,0,1,1,1,1,1,1,1,0,-1},
            {0,0,1,-1,0,0,1,0,0,0,0,0,0,0,0},
            {0,0,1,0,0,1,1,-1,0,1,1,1,1,1,1},
            {0,-1,1,-1,0,1,0,0,0,1,-1,0,0,0,-1},
            {0,0,1,0,0,1,-1,0,-1,1,0,0,1,-1,0},
            {-1,0,0,0,-1,1,0,0,0,0,0,-1,1,0,3}
    };

    public int[][] getEasyMaze() {
        return easyMaze;
    }

    public int[][] getMediumMaze() {
        return mediumMaze;
    }

    public int[][] getHardMaze(){
        return hardMaze;
    }

    public int [][] getRandomMaze() {
        return null;
    }
}