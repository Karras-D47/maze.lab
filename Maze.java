// Maze Solver — UCS & A* pathfinding with teleports
// Originally built for an Artificial Intelligence course assignment
// Copyright (c) 2025 AbzZ3r0
// Licensed under the MIT License (see LICENSE)

import java.util.List;
import java.util.Random;

/**
 * Builds an N x N maze. Each cell is either free (0) or an obstacle (1),
 * with obstacles placed randomly according to a given probability.
 * Teleport endpoints are always forced to be free so the search can use them.
 */
public class Maze {

    /**
     * Generates a maze of size n x n.
     *
     * @param n                   grid dimension (n &gt; 1)
     * @param obstacleProbability chance that any given cell is an obstacle, in [0, 1]
     * @param teleports           cells that must stay free (teleport endpoints)
     * @return the grid, where 0 = free and 1 = obstacle
     */
    public static int[][] generate(int n, double obstacleProbability, List<int[]> teleports) {
        int[][] grid = new int[n][n];
        Random random = new Random();

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                grid[row][col] = (random.nextDouble() < obstacleProbability) ? 1 : 0;
            }
        }

        // Teleport endpoints must never be blocked.
        for (int[] cell : teleports) {
            grid[cell[0]][cell[1]] = 0;
        }
        return grid;
    }

    /** Returns true if (row, col) lies inside an n x n grid. */
    public static boolean inBounds(int row, int col, int n) {
        return row >= 0 && col >= 0 && row < n && col < n;
    }
}
