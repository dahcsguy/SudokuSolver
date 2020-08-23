package backtrack;

import java.io.*;
import java.util.Scanner;
import static java.lang.Math.*;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Iterator;
import java.util.Arrays;
import java.util.*;

/** 
    Represent a CNF problem to solve.  
    This version allows setting and
    unsetting of clauses and variables to implement backtracking.

    @author Wayne Zhang
*/
public class Formula {

    // For easy debug printing.  DO NOT call from final solution
    // as it will slow things down.
    public static void p(boolean b,String x) {if (b) System.out.println(x);}	

    private String name; // problem name
    private int nvar; // number of vars
    private int nclauses; // number of clauses
    private int form[ ][ ]; // formula broken up by clauses

    private int vars[ ]; // variable assignment (true,false,NOTASSIGNED)
	private int tempform [][]; //copy of the original formula
    public static final int MAXCLAUSELEN = 3; // limits us to 3SAT
    public static final int NOTASSIGNED = 0; // unassigned variable
    final char COMMENT = 'c';
    final String PROBLEM =  "p";
    final String CNF =  "cnf";

    private Stack< LinkedList<Integer>> clauseStack; // stack of clauses for backtracking
    private Iterator<Integer> iter;  // re-usable iterator

    /** 
	Assumes file name of DIMAC format 3SAT problem. 
	Loads file into object.  Do not modify this method.
	@param fname file name to open
    */
    public Formula(String fname) {
	Scanner s = null;
	try {
	    this.name = fname;
	    s = new Scanner(new BufferedReader(new FileReader(fname)));
	    String line = null;
	    // Remove comments
	    while (s.hasNextLine()) {
		line = s.nextLine();
		if (line.charAt(0) != COMMENT)  break;
	    }
	    // Extract problem
	    String[ ] token = line.split("\\s+");
	    //System.out.println("problem: " + line);
	    if ( token.length < 4 || !token[0].equals(PROBLEM) || !token[1].equals(CNF)) {
		System.err.println("Error in input; bad problem line found");
		System.err.println(line);
		return;
	    }
	    this.nvar = Integer.parseInt(token[2]);
	    this.nclauses = Integer.parseInt(token[3]);
	    this.form = new int[nclauses][ ];
	    // load integers representing clause using standard DIMACS format
	    // assume  0 at end of each clause
	    int clausenum = 0;
	    int [ ] tmp = new int[MAXCLAUSELEN+1]; // temporary space
	    int i = 0;
	    while (s.hasNextInt()) {
		i = 0; // load one clause
		while (s.hasNextInt() && i < tmp.length) {
		    tmp[i] =  s.nextInt();
		    if (tmp[i] == 0)  break; // end of clause
		    i++;
		}
		this.form[clausenum] = new int[i]; // copy clause to formula
		
		for (int k = 0; k < i; k++)  this.form[clausenum][k] = tmp[k];
		clausenum++;
	    }
	} catch (FileNotFoundException ex) {
	    ex.printStackTrace();
	    return;
	} finally {
	    if (s != null) s.close();
	}
	
	initBacktrack();
    }

	
	
	
    /**
       Print var assignment.
    */
    void printAssignment() {
		for (int i = 1; i < nvar+1; i++) System.out.print(vars[i] + " ");
		System.out.println();
    }

    /** Initialize vars for backtracking:
	vars: t/f/notassigned array for current assignment
	clauseStack: stack of active clauses we backtrack to when assignment fails.
    */
    private void initBacktrack() {
	// TODO
	
		tempform = new int [nclauses][nvar];//copy of the original form
		for(int i = 0; i<form.length;i++){ //transfering values
			for(int k = 0; k<form[i].length;k++)
				tempform[i][k] = form[i][k]; //tempform[i][k] has the value of form[i][k]
		}
		vars = new int[nvar+1]; //initialize vars to length nvar+1
		clauseStack = new Stack<LinkedList<Integer>>();	//initalize stack
		
		LinkedList<Integer> ll = new LinkedList<Integer>();//this is a linkedlist that we'll push to the stack
		
		
		for(int i = 0 ; i< form.length;i++){
			ll.add(i); //add index to linkedlist
		}
		clauseStack.push(ll);//push all the clauses into stack
		
    }


    /**
       @return formula as string
    */
    public String toString() {
	String s  = "";
	for (int i = 0; i < form.length; i++) {
	    for (int j = 0; j < form[i].length; j++) {
		s += form[i][j] + "\t";
	    }
	    s += "\n";
	}
	return s;
    }

	
    /**
       @return branch variable
       Returns -1 if no variable to try exists.
    */
    int selectBranchVar ( ) {
	// return first unassigned.
	for (int i = 1; i < nvar+1; i++) 
	    if (vars[i] == NOTASSIGNED) return i;
	return -1;
    }

    /**
       @return true if the formula has an empty clause, false otherwise
    */
    boolean hasEmptyClause () {
		// TODO
		LinkedList<Integer> ll = clauseStack.peek(); //get linkedlist
		iter = ll.iterator();
		while(iter.hasNext()){
			int rowNum = iter.next();//row number
			if(isEmptyClause(rowNum))//if rownumber is an empty clause
				return true;
		}
		return false;

    }
	
    /**
       @return true if the formula has no clauses left, false otherwise
    */
    boolean isEmpty ( ) {
	// TODO
		LinkedList<Integer> ll = clauseStack.peek();
		return ll.size()==0; //if the size of the linkedlist is 0 that mean there is no more unsatisfied clauses!
    }

    /**
       @return true if the clause at index c is empty
    */
    boolean isEmptyClause ( int c ) {
	// TODO
		for(int i = 0; i<form[c].length;i++){ //iterate through row c
			if(form[c][i]!=0) //if form[c][i] is !0 
				return false;
		}
		return true; //all the elements in row c are 0
    }
    /**
       @return true iff formula[clz] contains var.
    */
    private boolean inClause(int clz,int var) {
	// TODO
		for(int i = 0; i<tempform[clz].length;i++){//iterate through row clz
			if(tempform[clz][i] ==(var)){ //if there is var in the row
				return true;
				
			}
				
		}
		return false;
    }

    /**
     Set given variable to given true/false value.
     Variable value is positive, but in formula is posivite or negative.
     Will remove clauses containing true var value from consideration.
     Will remove variables false var value from clauses.
     @param var index of var to set.
     @param val t/f value to set var.
    */
    
    void setVar ( int var, boolean val) {
	// TODO
		int tempVar; //-var or var
		if(val){
			vars[var] = 1; //vars[var] is set to true
			tempVar = var; //it's not false so var
		}
		else{
			vars[var] = -1; //vars[var] is set to false
			tempVar = -var; //it's false so -var
		}
			
		
		int opposite = -tempVar;//variable to mark
		
		
		LinkedList<Integer> theList = clauseStack.peek(); //ll to iterate
		LinkedList<Integer> newList = new LinkedList<Integer>(); //new ll to push
		iter = theList.iterator();
		while(iter.hasNext()){ //iterate through the ll at the top of stack
			int rowNum = iter.next(); //row number
			
			if(!inClause(rowNum,tempVar)){//tempvar is not in form[rowNum] so we have to add it to the new ll
				newList.add(rowNum); //add to ll
				if(inClause(rowNum,opposite)){ //opposite variable is in original form
					for(int i = 0; i<form[rowNum].length;i++){
						if(form[rowNum][i]==opposite){
							form[rowNum][i] = 0;//mark opposite variable in form
							
						}
					}
				}
			}

		}
		
		clauseStack.push(newList); //push the linkedlist to the stack
		
    }
	
    /**
       @param variable to unset (only positive values allowed)
       Set given variable to NOTASSIGNED.
       Flagged instances in clauses must be unflagged.
       Must backtrack to set of clauses at point when var was assigned.
    */
    void unset ( int var) {
	// TODO
	
		vars[var] = NOTASSIGNED;
		LinkedList<Integer> thePop = clauseStack.pop(); //variable assignments failed, we have to backtrack
		int opposite = -var; //opposite variable
		iter = thePop.iterator();
		while(iter.hasNext()){ //iterate through the linkedlist at the top of the stack
			int rowNum = iter.next(); //row number
			if(inClause(rowNum,opposite)||inClause(rowNum,var)){//if the opposite or the actual variable is in the clause
				for(int i = 0; i< tempform[rowNum].length;i++){ //iterate through the array 
					if(tempform[rowNum][i]==opposite||tempform[rowNum][i]==var){ //if they match var or negative var (probably could've used absolute value)
						form[rowNum][i] = tempform[rowNum][i]; //backtrack (revert it back to original value)
					}
						
				}
			}
			
		}
		
    }
	//easy deubugging
	void printArray(int [][] a){
		for(int i = 0; i<a.length;i++){
			for(int k = 0; k<a[i].length;k++){
				System.out.print(a[i][k]+" ");
			}
			System.out.println();
		}
	}
	

}
