package sat4j;


/**
 * Sudoku solver using reduction
 *
 * @author  Wayne Zhang and Eli Segal
 */

import java.util.*;
import java.io.*;
import java.lang.*;


import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

class SATSolver {

    /**
     * Returns literals that satisfy the given formula, if the formula is satisfiable.
     *
     * @param cnfFormulaFileName Path name of the file containing the formula
     * @return An array of literal values denoting a satisfying assignment when one exists; null for unsatisfiable formulae.
     * @throws ContradictionException
     * @throws IOException
     * @throws ParseFormatException
     * @throws FileNotFoundException
     * @throws TimeoutException
     * @throws Exception
     */
    public static int [] solve ( String cnfFormulaFileName )  throws
            FileNotFoundException, ParseFormatException, IOException, ContradictionException, TimeoutException
    {

        ISolver solver = SolverFactory.newDefault();

        Reader reader = new DimacsReader(solver);
        IProblem problem = reader.parseInstance(cnfFormulaFileName);
        return problem.findModel();

    }
}


public class SudokuSolver {
    //file name
    private static String name;
    //comment
    final static char COMMENT = 'c';
    //ex 3 by 3  sudDimX by sudDimY
    private static int sudDimX;
    private static int sudDimY;
    //unsolved puzzle stored in an 2d array
    private static int [][] originalBoard;
    //solved puzzle stored in an 2d array
    private static int [][] solveBoard;
    //size of formula
    private static int numClause;
    //cnf
    private static List<int[]> formula;
    //writer
    private static BufferedWriter writer = null;
    //solver assignment
    private static int [] assignment;



    public static void main(String[]args){
        long start = System.currentTimeMillis();
        //gets the board
        getBoard(args[0]);
        //encodes the board
        encodeBoard(sudDimY*sudDimX);
        //turns the encoding into a text file
        conNForm(formula);
        //tries to solve board
        try{
            SATSolver ss = new SATSolver();

            assignment = ss.solve(name+".cnf");
        }

        catch(Exception e){
            System.out.println(e+"error occured =(");
        }

        //gets the answer
        decode(assignment);
        //verifies solution
        print(originalBoard);
        System.out.println();
        /**
         if(certifer(originalBoard, solveBoard))
         System.out.println("solution exists\n");
         else
         System.out.println("no solutions+\n");
         */
        print(solveBoard);
        System.out.println("\nIt took "+(System.currentTimeMillis()-start)+" milliseconds");
    }
    /**
     * Prints the board
     *
     * @param  theBoard sudoku board
     */
    public static void print(int [][] theBoard){
        for(int i = 0; i< theBoard.length;i++){
            for(int k  = 0; k<theBoard[i].length;k++){
                System.out.print(theBoard[i][k]+" ");
            }
            System.out.println();
        }
    }
    /**
     * Translate the SAT solution back into a sudoku
     * board
     *
     * @param  solution   SAT solution to sudoku
     */
    public static void decode(int [] solution){
        try{
            writer = new BufferedWriter(new FileWriter(name+"(solution).txt"));
            //current row
            int row = 0;
            //current row +1
            int current = 1;
            for(int i = 0; i<solution.length;i++){
                //check if we need to go to the next row
                if(Math.abs(solution[i]/100)!=current){
                    current++;
                    writer.write("\n");
                    row ++;


                }
                //positive number means that that number is the actual number for that cell
                if(solution[i]>0){
                    writer.write(solution[i]%10+" ");
                    //insert number to solveBoard
                    solveBoard[row][solution[i]%100/10-1] = solution[i]%10;
                }

            }
            writer.close();
        }
        catch(Exception e){
            System.out.println(e+" error occured =(");

        }
    }



    /**
     * Gets the board from text file
     * @param  fname file name
     */
    public static void getBoard(String fname){
        //name becomes a global variable
        name = fname;
        Scanner s = null;
        //gets the dimension of the individual boxes
        boolean firstLine = true;
        //havent declare board yet
        boolean declareBoard  = false;

        int row = 0;
        try{
            s = new Scanner(new BufferedReader(new FileReader(fname) ));
            String line = null;

            while(s.hasNextLine()){
                line = s.nextLine();
                //ignore comments
                if(line.charAt(0)==COMMENT)
                    break;
                    //gets dimensions
                else if(firstLine){
                    //looked at first line
                    firstLine = false;
                    // get the two numbers
                    String[] splited = line.split("\\s+");

                    sudDimX = Integer.parseInt(splited[0]);
                    sudDimY = Integer.parseInt(splited[1]);
                }

                else{
                    //declare the board
                    if(!firstLine && !declareBoard){

                        originalBoard = new int [sudDimX*sudDimY][sudDimY*sudDimX];
                        solveBoard = new int[sudDimX*sudDimY][sudDimY*sudDimX];
                        declareBoard = true;
                    }
                    //no leading and trailing white spaces
                    line.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
                    String [] tokens = line.split(" ");

                    //parse each line
                    for(int i = 0; i< sudDimX*sudDimY;i++){
                        originalBoard[row][i] = Integer.parseInt(tokens[i]);
                    }
                    row ++;
                }
            }
        }
        catch(FileNotFoundException ex){
            System.out.println("No files with that name =(");

        }
        finally{
            if(s!= null)
                s.close();
        }
    }
    /**
     * Translate the encoding to an cnf file
     * @param  encode a list which contains the cnf of our sudoku board
     */
    public static void conNForm(List<int []> encode){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(name+".cnf"));
            writer.write("p cnf 999 "+encode.size()+"\n");
            for(int i = 0; i< encode.size();i++){
                for(int j = 0; j<encode.get(i).length;j++){
                    writer.write(encode.get(i)[j]+" ");
                }
                writer.write("0");
                writer.write("\n");
            }
            writer.close();
        }
        catch(Exception e){
            System.out.println("Exception occured :(");
        }

    }
    /**
     * Checks if our solution for a 9x9 board is valid or not
     * @param  problem 	the original problem
     * @param  solution 	the solution to the problem
     */
    public static boolean certifer(int [][] problem, int[][] solution){
        //checks if all the original numbers in problem are in solution
        for(int i = 0; i< problem.length;i++){
            for(int j = 0; j<problem[i].length;j++){
                if(problem[i][j]!=0){
                    if(problem[i][j] != solution[i][j])
                        return false;
                }
            }
        }

        //iterate through board
        for (int i = 0; i < 9; i++) {

            int[] row = new int[9];
            int[] square = new int[9];
            int[] column = solution[i].clone();

            for (int j = 0; j < 9; j ++) {
                row[j] = solution[j][i];
                square[j] = solution[(i / 3) * 3 + j / 3][i * 3 % 9 + j % 3];
            }
            if (!(validate(column) && validate(row) && validate(square)))
                return false;
        }
        return true;


    }
    /**
     * check if every number is in the row, column or box
     * @param  check 	the original problem
     */
    private static boolean validate(int[] check) {
        int i = 0;
        Arrays.sort(check);
        for (int number : check) {
            if (number != ++i)
                return false;
        }
        return true;
    }
    public static int varStringToInt(int first, int second, int third){
        String f = first + "";
        String s = second + "";
        String t = third + "";
        String var_str = f + s + t;
        int var_int = Integer.parseInt(var_str);
        return var_int;
    }

    public static void encodeBoard(int boardsize){
        List<int[]> clz_list = new ArrayList<int[]>();

        // PREEXISTING NUMBERS CLAUSES, EG. [113] IF ROW 1 COL 1 CONTAINS '3'
        for (int row = 1; row <= originalBoard.length; row ++) {
            for (int col = 1; col <= originalBoard.length; col ++) {
                int cell = originalBoard[row-1][col-1];
                if (cell != 0) {
                    int[] f_clz = new int[1];
                    f_clz[0] = varStringToInt(row, col, cell);
                    clz_list.add(f_clz);
                }
            }
        }


        // ROW CLAUSES
        for (int row = 1; row <= boardsize; row ++){
            for (int val = 1; val <= boardsize; val ++) {
                int [] row_clz = new int[9];
                int [] col_clz = new int[9];
                for (int col = 1; col <= boardsize; col ++){
                    int var = varStringToInt(row, col, val); //111, 121, 131, ...
                    int col_var = varStringToInt(col, row, val);
                    row_clz[col-1] = var;
                    col_clz[col-1] = col_var;
                }
                clz_list.add(row_clz);
                clz_list.add(col_clz);
            }

            // CELL CLAUSES
            for (int col = 1; col <= boardsize; col ++){
                int[] base_clz = new int[9];
                for (int m = 1; m <= boardsize; m++) {
                    base_clz[m-1] = varStringToInt(row, col, m);
                }
                clz_list.add(base_clz);
                for (int m = 1; m <= boardsize; m ++){
                    int var_1 = -1 * varStringToInt(row, col, m);
                    int var_r1 = -1 * varStringToInt(row, m, col);
                    int var_c1 = -1 * varStringToInt(m, row, col);
                    int var_b1;

                    for (int n = 1; n <= boardsize; n ++){
                        int var_2 = -1 * varStringToInt(row, col, n);
                        int var_r2 = -1 * varStringToInt(row, n, col);
                        int var_c2 = -1 * varStringToInt(n, row, col);
                        if (var_1 != var_2){
                            int [] cell_clz = {var_1, var_2};
                            clz_list.add(cell_clz);
                        }
                        if (var_r1 != var_r2) {
                            int [] r_clz = {var_r1, var_r2};
                            clz_list.add(r_clz);
                        }
                        if (var_c1 != var_c2) {
                            int [] c_clz = {var_c1, var_c2};
                            clz_list.add(c_clz);
                        }
                    }
                }
            }
        }

        // BOX CLAUSES
        int box_w = sudDimX;
        int box_h = sudDimY;
        for (int val = 1; val <= boardsize; val ++){
            int[][] b = new int[box_w * box_h][box_w * box_h];
            for (int i = 1; i <= box_w; i ++){
                for (int j = 1; j <= box_w; j ++){


                    // each var represents one box in puzzle
                    int [] all_v1 = new int[box_w * box_h];
                    int count = 0;
                    for (int p = 0; p < box_w; p++){
                        for (int o = 0; o < box_h; o++){
                            int v_gen = varStringToInt(i + (p * box_w), j + (o * box_h), val);
                            all_v1[count] = v_gen;
                            count += 1;
                        }
                    }

                    // CREATES BOX CLAUSES, EG. THERE MUST BE A '1', '2', ... IN EACH BOX
                    for (int r = 0; r < (box_w * box_h); r ++){
                        b[r][((i-1)*box_w) + (j-1)] = all_v1[r];
                    }

                    // BOX CLAUSES, EG. THERE CANNOT BE MORE THAN ONE '1' IN A GIVEN BOX, &c.
                    for (int e = 1; e <= box_h; e ++){
                        for (int t = 1; t <= box_h; t ++) {
                            int [] all_v2 = new int[box_w * box_h];
                            int count2 = 0;
                            for (int p = 0; p < box_w; p++){
                                for (int o = 0; o < box_h; o++){
                                    int v2_gen = varStringToInt(e + (p * box_w), t + (o * box_h), val);
                                    all_v2[count2] = v2_gen;
                                    count2 += 1;
                                }
                            }

                            for (int y = 0; y < (box_w * box_h); y++){
                                int [] b_clz = new int[2];
                                if (all_v1[y] != all_v2[y]){
                                    b_clz = new int[] {-1 * all_v1[y], -1 * all_v2[y]};
                                    clz_list.add(b_clz);
                                }
                            }

                        }
                    }


                }
            }
            for (int k = 0; k < (box_w * box_h); k ++){
                clz_list.add(b[k]);
            }
        }

        formula = clz_list;
        boolean VERBOSE = false;
        if (VERBOSE == true){
            int counter = 0;
            for (int[] c : clz_list){
                String cl = "";
                for (int val : c){
                    cl += (val + " ");
                }
                System.out.println(cl);
                counter += 1;
            }
            String cnt = counter + "";
            System.out.println("Number of Clauses: " + cnt);
        }
    }





}
