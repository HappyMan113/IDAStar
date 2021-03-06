package happyman.examples;

import happyman.AStar.*;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AStarTest
{
    static class Coord
    {
        final int x;
        final int y;

        Coord(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        Coord getRelative(int x, int y)
        {
            return new Coord(this.x + x, this.y + y);
        }

        @Override
        public String toString()
        {
            return "(" + x + ", " + y + ")";
        }
    }

    static class BoardState extends State
    {
        static final int length = 3;
        private final int[][] board;

        BoardState(int[][] ints)
        {
            if (ints.length < length || ints[0].length < length)
            {
                throw new IllegalArgumentException("Error! Board must be " + length + "x" + length + " array!");
            }
            this.board = new int[length][length];
            for (int y = 0; y < length; y++)
            {
                for (int x = 0; x < length; x++)
                {
                    board[x][y] = ints[x][y];
                }
            }
        }

        BoardState(BoardState other, Coord coord1, Coord coord2)
        {
            this.board = new int[length][length];
            for (int y = 0; y < length; y++)
            {
                for (int x = 0; x < length; x++)
                {
                    board[x][y] = other.board[x][y];
                }
            }

            int temp = board[coord1.y][coord1.x];
            board[coord1.y][coord1.x] = board[coord2.y][coord2.x];
            board[coord2.y][coord2.x] = temp;
            //                TestClass.sendTestMessage(this);
        }

        Coord coordOfBlank()
        {
            for (int y = 0; y < length; y++)
            {
                for (int x = 0; x < length; x++)
                {
                    if (board[y][x] == 0)
                    {
                        return new Coord(x, y);
                    }
                }
            }
            throw new IllegalArgumentException("Error: bad value: " + 0);
        }

        int getTilesOff()
        {
            int h = 0;
            for (int y = 0; y < length; y++)
            {
                for (int x = 0; x < length; x++)
                {
                    h += Math.abs(board[y][x] % length - x) + Math.abs( board[y][x] / length - y);
                }
            }
            return h;
        }

        @Override
        public String toString()
        {
            StringBuilder result = new StringBuilder("\n");
            for (int y = 0; y < length; y++)
            {
                for (int x = 0; x < length; x++)
                {
                    result.append(board[y][x]);
                }

                result.append('\n');
            }
            return result.toString();
        }

        @Override
        public int hashCode()
        {
            int code = 0;
            for (int y = 0; y < length; y++)
            {
                for (int x = 0; x < length; x++)
                {
                    code = 31 * code + board[y][x];
                }
            }
            return code;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof BoardState)
            {
                BoardState other = (BoardState)o;
                for (int y = 0; y < length; y++)
                {
                    for (int x = 0; x < length; x++)
                    {
                        if (board[y][x] != other.board[y][x])
                        {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    static class BoardAction extends Action<BoardState>
    {
        private final Coord coord1;
        private final Coord coord2;
        BoardAction(Coord coord1, Coord coord2)
        {
            super(1);
            this.coord1 = coord1;
            this.coord2 = coord2;
        }

        @Override
        public BoardState enact(BoardState state)
        {
            return new BoardState(state, coord1, coord2);
        }

        @Override
        public String toString()
        {
            return "move to " + coord2;
        }
    }

    static class BoardProblem extends AStarProblem<BoardState>
    {
        BoardProblem(BoardState initialState)
        {
            super(initialState);
        }

        @Override
        protected float getHeuristicCostToReachGoalFrom(BoardState state)
        {
            return state.getTilesOff();
        }

        @Override
        public boolean isTerminal(BoardState state)
        {
            return state.getTilesOff() == 0;
        }

        @Override
        public List<Action<BoardState>> getActions(final BoardState state)
        {
            final List<Action<BoardState>> result = new LinkedList<>();
            Coord blankCoord = state.coordOfBlank();
            if (blankCoord.x > 0)
            {
                result.add(new BoardAction(blankCoord, blankCoord.getRelative(-1, 0)));
            }
            if (blankCoord.x < BoardState.length - 1)
            {
                result.add(new BoardAction(blankCoord, blankCoord.getRelative(1, 0)));
            }
            if (blankCoord.y > 0)
            {
                result.add(new BoardAction(blankCoord, blankCoord.getRelative(0, -1)));
            }
            if (blankCoord.y < BoardState.length - 1)
            {
                result.add(new BoardAction(blankCoord, blankCoord.getRelative(0, 1)));
            }
            return result;
        }
    }

    @Test
    public void test()
    {
        assertTrue(new BoardState(new int[][] {
                new int[] {0, 1, 2},
                new int[] {3, 4, 5},
                new int[] {6, 7, 8},
//                new int[] {0, 1, 2, 3},
//                new int[] {4, 5, 6, 7},
//                new int[] {8, 9, 10, 11},
//                new int[] {12, 13, 14, 15},
        }).getTilesOff() == 0);

        analyzeSolution(new BoardProblem(new BoardState(new int[][]
                {
                        new int[] {1, 4, 8},
                        new int[] {6, 3, 0},
                        new int[] {5, 2, 7},
                        //                                new int[] {2, 3, 10, 7},
                        //                                new int[] {1, 6, 0, 11},
                        //                                new int[] {5, 12, 9, 15},
                        //                                new int[] {4, 8, 13, 14},
                }
        )));

        analyzeSolution(new BoardProblem(new BoardState(new int[][]
                {
                        new int[]{7, 2, 4},
                        new int[]{5, 0, 6},
                        new int[]{8, 3, 1},
                }
        )));
    }

    static<S extends State> void analyzeSolution(final AStarProblem<S> problem)
    {
        analyzeSolution(problem, false);
        analyzeSolution(problem, true);
    }

    private static <S extends State> void analyzeSolution(AStarProblem<S> problem, boolean limitedMemory)
    {
        final Solution<S> solution = SolutionFinder.findSolution(problem, limitedMemory);
        int executionTime = 0;
        int it = 0;
        long gStart = System.currentTimeMillis();
        while (System.currentTimeMillis() - gStart < 1000)
        {
            long start = System.currentTimeMillis();
            SolutionFinder.findSolution(problem, limitedMemory);
            long stop = System.currentTimeMillis();
            executionTime += stop - start;
            it++;
        }
        if (limitedMemory)
        {
            System.out.println(solution + " to " + problem + " took " + (float)executionTime/it + "ms to find with iterative deepening A*\n");
        }
        else
        {
            System.out.println(solution + " to " + problem + " took " + (float)executionTime/it + "ms to find with good'le A*\n");
        }
    }
}
