# Maze Solver — UCS & A\* with Teleports

A Java console application that finds the shortest path through a randomly
generated `N x N` maze using two classic search algorithms — **Uniform Cost
Search (UCS)** and **A\*** — and compares them side by side.

Movement is allowed in all 8 directions, obstacles are placed at random, and the
maze can contain **teleport** cells that let the agent jump across the grid.

The project ships in two flavours: a **Java command-line solver** (the reference
implementation) and an **interactive web visualizer** that animates the search
cell by cell.

**▶ Live demo:**  https://karras-d47.github.io/maze.lab/

> Originally built as part of an Artificial Intelligence course assignment, then
> cleaned up and extended into a standalone project.

## Features

- **Two algorithms, one engine** — UCS and A\* share the same search routine; A\*
  simply adds a heuristic (UCS is the special case where the heuristic is 0).
- **8-directional movement** with a uniform step cost of 1.
- **Teleports** — fully connected; stepping onto any teleport lets you jump to
  any other teleport for a cost of 2.
- **Teleport-aware, admissible heuristic** — A\* is provably optimal _even with
  teleports_, so it always returns the same path cost as UCS while expanding far
  fewer cells (verified over 5 000 random mazes).
- **Coloured terminal visualisation** of the maze, the path, the endpoints and
  the teleports.
- **Performance comparison** — path cost, number of expanded cells, and runtime
  for each algorithm.

## How it works

**Search.** Both algorithms expand the cell with the lowest priority from a
priority queue: UCS orders by `g` (cost so far), A\* by `f = g + h` (cost so far
plus estimated cost to the goal). The best cost found for each cell is tracked in
a `double[][]` grid, so a cell is re-expanded only if a cheaper path to it is
discovered.

**Heuristic.** The estimate is the Chebyshev (chessboard) distance — the minimum
number of 8-directional steps to the goal. To stay admissible when teleports
exist, the heuristic also considers reaching the goal _through_ a teleport:

```
h(cell) = min( chebyshev(cell, goal),
               min over teleports t of [ chebyshev(cell, t) + 2 + chebyshev(nearest other teleport, goal) ] )
```

Because every distance term ignores obstacles, it can never overestimate the
real cost — which is exactly the condition that keeps A\* optimal.

## Getting started

### Web visualizer (no install)

Open `index.html` in any modern browser, or host it for free on **GitHub Pages**:
push the repo, then in _Settings → Pages_ choose your branch and root folder.
The page lets you draw walls, place the start/goal/teleports, pick an algorithm,
and watch the search explore the grid and trace the path. The "Compare" toggle
runs both algorithms and shows how many fewer cells A\* expands.

### Java command-line solver

Requires a JDK (developed on Java 21).

```bash
# compile
javac *.java

# run
java MazeSolver
```

You will be asked for the grid size, the obstacle probability, any teleport
points (enter `-1 -1` to finish), and the start and goal coordinates.

## Example session

A 10×10 maze with no obstacles and teleports at the corners. UCS and A\* find the
same optimal path (cost 9), but A\* expands far fewer cells:

```
   MAZE
     0  1  2  3  4  5  6  7  8  9
     ------------------------------
0   | S  .  .  .  .  .  .  .  .  T
1   | .  *  .  .  .  .  .  .  .  .
2   | .  .  *  .  .  .  .  .  .  .
3   | .  .  .  *  .  .  .  .  .  .
4   | .  .  .  .  *  .  .  .  .  .
5   | .  .  .  .  .  *  .  .  .  .
6   | .  .  .  .  .  .  *  .  .  .
7   | .  .  .  .  .  .  .  *  .  .
8   | .  .  .  .  .  .  .  .  *  .
9   | T  .  .  .  .  .  .  .  .  G

--- UCS ---
Path cost : 9
Expansions: 95
Time      : 8.157 ms

--- A* ---
Path cost : 9
Expansions: 10
Time      : 0.357 ms
```

## Legend

| Symbol | Meaning   |
| :----: | --------- |
|  `.`   | free cell |
|  `#`   | obstacle  |
|  `*`   | path      |
|  `S`   | start     |
|  `G`   | goal      |
|  `T`   | teleport  |

## Project structure

```
maze-solver-astar/
├── index.html        # interactive web visualizer (GitHub Pages ready)
├── GridNode.java     # a single cell during the search (g, h, parent)
├── Maze.java         # random maze generation and bounds checking
├── Search.java       # UCS and A* + the admissible heuristic
├── MazeSolver.java   # interactive command-line entry point
└── README.md
```

## Possible improvements

- Variable step cost for diagonal moves (e.g. `√2`) with a matching heuristic.
- Read a maze from a file instead of generating it randomly.
- A simple graphical (Swing/JavaFX) front-end.

## Authors
