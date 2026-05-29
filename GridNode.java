// Maze Solver — UCS & A* pathfinding with teleports
// Originally built for an Artificial Intelligence course assignment
// Copyright (c) 2025 AbzZ3r0
// Licensed under the MIT License (see LICENSE)


/**
 * A single cell of the maze as seen by the search.
 * Stores the cost of the best path found so far (g), the heuristic estimate to
 * the goal (h), and the parent cell so the final path can be reconstructed.
 */
public class GridNode {
    public final int row;
    public final int col;
    public double g;          // cost of the best known path from the start to this cell
    public double h;          // heuristic estimate from this cell to the goal (0 for UCS)
    public GridNode parent;   // previous cell on the path (null for the start)

    public GridNode(int row, int col, double g, double h, GridNode parent) {
        this.row = row;
        this.col = col;
        this.g = g;
        this.h = h;
        this.parent = parent;
    }

    /** Total estimated cost of a path through this cell: f = g + h. */
    public double f() {
        return g + h;
    }
}
