// Maze Solver — UCS & A* pathfinding with teleports
// Authors: Karras Dimitris-Kosmas (AM 5247), Theodoropoulos Panagiotis (AM 5230)

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive entry point. Asks the user for a maze configuration, runs both
 * UCS and A*, then prints the discovered path and a performance comparison.
 */
public class MazeSolver {

    // ANSI colour codes for the visualisation (works in any modern terminal).
    private static final String RESET   = "\u001B[0m";
    private static final String GRAY    = "\u001B[90m"; // free cells & obstacles
    private static final String GREEN   = "\u001B[92m"; // path
    private static final String CYAN    = "\u001B[96m"; // start
    private static final String YELLOW  = "\u001B[93m"; // goal
    private static final String MAGENTA = "\u001B[95m"; // teleport

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        printIntro();

        while (true) {
            int n = readGridSize(scanner);
            double obstacleProb = readObstacleProbability(scanner);
            List<int[]> teleports = readTeleports(scanner, n);
            int[][] grid = Maze.generate(n, obstacleProb, teleports);

            int[] start = readCell(scanner, "Start", n);
            int[] goal  = readCell(scanner, "Goal", n);

            long t0 = System.nanoTime();
            Search.SearchResult ucs = Search.solveUCS(grid, n, start[0], start[1], goal[0], goal[1], teleports);
            long ucsTime = System.nanoTime() - t0;

            long t1 = System.nanoTime();
            Search.SearchResult astar = Search.solveAStar(grid, n, start[0], start[1], goal[0], goal[1], teleports);
            long astarTime = System.nanoTime() - t1;

            GridNode pathToDraw = (ucs.goal() != null) ? ucs.goal() : astar.goal();
            printMaze(grid, pathToDraw, start, goal, teleports);

            printStats("UCS", ucs, ucsTime);
            printStats("A*",  astar, astarTime);

            if (!askYesNo(scanner, "\nSolve another maze? (y/n): ")) break;
            System.out.println();
        }
        scanner.close();
    }

    // ----- Input helpers (token-based, locale-independent) -----

    private static int readGridSize(Scanner sc) {
        while (true) {
            System.out.print("  Grid size N (> 1): ");
            try {
                int n = Integer.parseInt(sc.next());
                if (n > 1) return n;
            } catch (NumberFormatException ignored) { }
            System.out.println("  Invalid input. Enter an integer greater than 1.");
        }
    }

    private static double readObstacleProbability(Scanner sc) {
        while (true) {
            System.out.print("  Obstacle probability (0.0 - 1.0): ");
            try {
                double p = Double.parseDouble(sc.next().replace(',', '.'));
                if (p >= 0.0 && p <= 1.0) return p;
            } catch (NumberFormatException ignored) { }
            System.out.println("  Invalid input. Enter a value between 0.0 and 1.0.");
        }
    }

    private static List<int[]> readTeleports(Scanner sc, int n) {
        List<int[]> teleports = new ArrayList<>();
        System.out.println("  Teleport points 'row col' (enter -1 -1 to finish):");
        while (true) {
            System.out.print("    TP: ");
            int[] cell = readPair(sc);
            if (cell == null) {
                System.out.println("    Invalid input. Enter two integers.");
                continue;
            }
            if (cell[0] == -1 && cell[1] == -1) break;
            if (Maze.inBounds(cell[0], cell[1], n)) teleports.add(cell);
            else System.out.println("    Out of bounds. Try again.");
        }
        return teleports;
    }

    private static int[] readCell(Scanner sc, String label, int n) {
        while (true) {
            System.out.print("  " + label + " 'row col': ");
            int[] cell = readPair(sc);
            if (cell != null && Maze.inBounds(cell[0], cell[1], n)) return cell;
            System.out.println("  Invalid coordinates. Enter two integers from 0 to " + (n - 1) + ".");
        }
    }

    /** Reads exactly two whitespace-separated tokens and parses them as ints. */
    private static int[] readPair(Scanner sc) {
        String a = sc.next();
        String b = sc.next();
        try {
            return new int[]{Integer.parseInt(a), Integer.parseInt(b)};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean askYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String ans = sc.next().trim().toLowerCase();
            if (ans.equals("y")) return true;
            if (ans.equals("n")) return false;
            System.out.println("  Please enter 'y' or 'n'.");
        }
    }

    // ----- Output helpers -----

    private static void printMaze(int[][] grid, GridNode goalNode, int[] start, int[] goal, List<int[]> teleports) {
        int n = grid.length;
        char[][] cell = new char[n][n];
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                cell[r][c] = (grid[r][c] == 1) ? '#' : '.';

        for (int[] t : teleports) cell[t[0]][t[1]] = 'T';

        // Mark the reconstructed path, but keep teleport markers visible.
        for (GridNode node = goalNode; node != null; node = node.parent) {
            if (cell[node.row][node.col] != 'T') cell[node.row][node.col] = '*';
        }

        cell[start[0]][start[1]] = 'S';
        cell[goal[0]][goal[1]]   = 'G';

        System.out.println("\n   MAZE");
        System.out.print("     ");
        for (int c = 0; c < n; c++) System.out.printf("%-3d", c);
        System.out.println();
        System.out.print("     ");
        for (int c = 0; c < n; c++) System.out.print("---");
        System.out.println();

        for (int r = 0; r < n; r++) {
            System.out.printf("%-3d |", r);
            for (int c = 0; c < n; c++) {
                System.out.print(" " + colored(cell[r][c]) + " ");
            }
            System.out.println();
        }
    }

    private static String colored(char ch) {
        String color = switch (ch) {
            case '*' -> GREEN;
            case 'S' -> CYAN;
            case 'G' -> YELLOW;
            case 'T' -> MAGENTA;
            default  -> GRAY; // '.' and '#'
        };
        return color + ch + RESET;
    }

    private static void printStats(String name, Search.SearchResult result, long nanos) {
        System.out.printf("%n--- %s ---%n", name);
        if (result.goal() != null) {
            System.out.printf("Path cost : %.0f%n", result.goal().g);
        } else {
            System.out.println("Path cost : no path found");
        }
        System.out.println("Expansions: " + result.expansions());
        System.out.printf("Time      : %.3f ms%n", nanos / 1e6);
    }

    private static void printIntro() {
        System.out.println(
            "+-----------------------------+\n" +
            "|         MAZE SOLVER         |\n" +
            "+-----------------------------+\n" +
            "\nUCS and A* over an N x N maze with 8-directional movement and teleports.\n" +
            "\nLegend:\n" +
            "   . free cell      # obstacle      * path\n" +
            "   S start          G goal          T teleport\n" +
            "\nNotes:\n" +
            "   - Each step costs 1; each teleport jump costs 2.\n" +
            "   - Coordinates are entered as 'row col', both from 0 to N-1.\n" +
            "   - Path cost = total length of the path; Expansions = cells visited.\n"
        );
    }
}
