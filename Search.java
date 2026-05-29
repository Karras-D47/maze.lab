// Maze Solver — UCS & A* pathfinding with teleports
// Originally built for an Artificial Intelligence course assignment
// Copyright (c) 2025 AbzZ3r0
// Licensed under the MIT License (see LICENSE)

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Pathfinding over a maze using Uniform Cost Search (UCS) and A*.
 *
 * <p>Movement is allowed in all 8 directions, each step costing 1.
 * Teleport cells are fully connected: stepping onto one lets you jump to any
 * other teleport for a cost of 2.
 *
 * <p>The A* heuristic is teleport-aware and admissible, so A* is guaranteed to
 * return an optimal path — identical in cost to UCS, but expanding fewer cells.
 */
public class Search {

    /** Outcome of a search: the goal node (null if unreachable) and how many cells were expanded. */
    public record SearchResult(GridNode goal, int expansions) {}

    // The 8 neighbouring moves (orthogonal + diagonal).
    private static final int[] DR = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] DC = {-1, 0, 1, -1, 1, -1, 0, 1};

    private static final double STEP_COST = 1.0;
    private static final double TELEPORT_COST = 2.0;

    public static SearchResult solveUCS(int[][] grid, int n, int startRow, int startCol,
                                        int goalRow, int goalCol, List<int[]> teleports) {
        return search(grid, n, startRow, startCol, goalRow, goalCol, false, teleports);
    }

    public static SearchResult solveAStar(int[][] grid, int n, int startRow, int startCol,
                                        int goalRow, int goalCol, List<int[]> teleports) {
        return search(grid, n, startRow, startCol, goalRow, goalCol, true, teleports);
    }

    private static SearchResult search(int[][] grid, int n, int startRow, int startCol,
                                    int goalRow, int goalCol, boolean useHeuristic,
                                    List<int[]> teleports) {
        // A* orders the frontier by f = g + h; UCS orders by g (i.e. h = 0).
        PriorityQueue<GridNode> open = new PriorityQueue<>(
            (a, b) -> useHeuristic ? Double.compare(a.f(), b.f()) : Double.compare(a.g, b.g)
        );

        // Best known cost to reach each cell; INFINITY means "not reached yet".
        double[][] bestG = new double[n][n];
        for (double[] rowCosts : bestG) {
            Arrays.fill(rowCosts, Double.POSITIVE_INFINITY);
        }

        // For each teleport, the cheapest "jump out + walk to the goal" lower bound (A* only).
        double[] teleportExit = useHeuristic ? teleportExitBounds(teleports, goalRow, goalCol) : null;

        int expansions = 0;
        double startH = useHeuristic ? heuristic(startRow, startCol, goalRow, goalCol, teleports, teleportExit) : 0;
        bestG[startRow][startCol] = 0;
        open.add(new GridNode(startRow, startCol, 0, startH, null));

        while (!open.isEmpty()) {
            GridNode current = open.poll();

            // Skip stale entries left behind by an earlier, worse path to this cell.
            if (current.g > bestG[current.row][current.col]) continue;

            expansions++;
            if (current.row == goalRow && current.col == goalCol) {
                return new SearchResult(current, expansions);
            }

            // Teleport edges: if standing on a teleport, jump to every other teleport (cost 2).
            if (isTeleport(current.row, current.col, teleports)) {
                for (int[] dest : teleports) {
                    if (dest[0] == current.row && dest[1] == current.col) continue;
                    relax(open, bestG, grid, current, dest[0], dest[1], TELEPORT_COST, useHeuristic, goalRow, goalCol, teleports, teleportExit);
                }
            }

            // Walking edges: the 8 neighbouring cells (cost 1 each).
            for (int d = 0; d < DR.length; d++) {
                int nr = current.row + DR[d];
                int nc = current.col + DC[d];
                if (Maze.inBounds(nr, nc, n)) {
                    relax(open, bestG, grid, current, nr, nc, STEP_COST,
                        useHeuristic, goalRow, goalCol, teleports, teleportExit);
                }
            }
        }
        return new SearchResult(null, expansions); // goal unreachable
    }

    /** Adds a successor to the frontier if it improves the best known cost to that cell. */
    private static void relax(PriorityQueue<GridNode> open, double[][] bestG, int[][] grid,
                            GridNode from, int row, int col, double stepCost,
                            boolean useHeuristic, int goalRow, int goalCol,
                            List<int[]> teleports, double[] teleportExit) {
        if (grid[row][col] != 0) return; // obstacle
        double g = from.g + stepCost;
        if (g < bestG[row][col]) {
            bestG[row][col] = g;
            double h = useHeuristic ? heuristic(row, col, goalRow, goalCol, teleports, teleportExit) : 0;
            open.add(new GridNode(row, col, g, h, from));
        }
    }

    private static boolean isTeleport(int row, int col, List<int[]> teleports) {
        for (int[] t : teleports) {
            if (t[0] == row && t[1] == col) return true;
        }
        return false;
    }

    /**
     * Teleport-aware admissible heuristic.
     *
     * <p>From a cell you can either walk straight to the goal, or walk to a
     * teleport, jump (cost 2) and walk on from another teleport. We return the
     * cheapest lower bound among these options. Every walking term is a Chebyshev
     * distance, which ignores obstacles and so never exceeds the real cost — hence
     * the heuristic never overestimates and A* stays optimal even with teleports.
     */
    private static double heuristic(int row, int col, int goalRow, int goalCol,
                                    List<int[]> teleports, double[] teleportExit) {
        double best = chebyshev(row, col, goalRow, goalCol);
        for (int i = 0; i < teleports.size(); i++) {
            int[] entry = teleports.get(i);
            double viaTeleport = chebyshev(row, col, entry[0], entry[1]) + teleportExit[i];
            if (viaTeleport < best) best = viaTeleport;
        }
        return best;
    }

    /** For each teleport, the lower bound of "pay 2 to jump out, then walk to the goal". */
    private static double[] teleportExitBounds(List<int[]> teleports, int goalRow, int goalCol) {
        double[] bounds = new double[teleports.size()];
        for (int i = 0; i < teleports.size(); i++) {
            double nearestExit = Double.POSITIVE_INFINITY;
            for (int j = 0; j < teleports.size(); j++) {
                if (i == j) continue;
                int[] exit = teleports.get(j);
                nearestExit = Math.min(nearestExit, chebyshev(exit[0], exit[1], goalRow, goalCol));
            }
            // A lone teleport leads nowhere, so jumping is never worthwhile.
            bounds[i] = (nearestExit == Double.POSITIVE_INFINITY)
                    ? Double.POSITIVE_INFINITY
                    : TELEPORT_COST + nearestExit;
        }
        return bounds;
    }

    /**
     * Chebyshev (chessboard) distance: the minimum number of 8-directional steps
     * between two cells when every step costs 1. Admissible for this movement model.
     */
    private static double chebyshev(int row, int col, int goalRow, int goalCol) {
        return Math.max(Math.abs(row - goalRow), Math.abs(col - goalCol));
    }
}
