package backtrack;

/**
 * This class is a top level class for a backtracking SAT solver.
 * No need to modify this class.
 */
public class SATBacktrack {
    Formula formula; // a CNF problem to solve (you implement methods in this class)
	
    /**
       Solve fileName problem 
    */
    public void solve ( String fileName ) {
 	
	formula = new Formula(fileName);
	
	if (dp ( formula ) )
	    success ( formula );
	else
	    failure ( formula );
    }
	
    // Formula is satisfiable
    void success (Formula f) {
	System.out.println ( "Formula is satisfiable");
	// Print satisfying assignment
	formula.printAssignment();
    }
	
    // Formula is unsatisfiable
    void failure (Formula f) {
	System.out.println ("Formula is unsatisfiable");
    }
	
    /**
       Recursive backtracking solution
       @param formula 3sat formula to solve.
       @returns true iff formula is satisfiable
    */
    public boolean dp ( Formula formula ) {
		
	if ( formula.isEmpty()) // First base case: solution found
	    return true;
	else if (formula.hasEmptyClause ()){ // Second base case: dead end found
		
	    return false;
	}
	else {
	    // Pick a branch variable
	    int var = formula.selectBranchVar (  );
	    formula.setVar ( var, true );
	    if (dp(formula)) 
			return true;
	    else {
			// Unset var in the formula 
			formula.unset ( var );
			// Setting var to true did not work, so now try var = false
			formula.setVar ( var, false );

			if (dp (formula))
				return true;
			else {
				// Neither true nor false worked, so unset the branch 
				// variable and backtrack
				formula.unset ( var );
				return false;			
		}
	    }
	}	
    }
	

    public static void main(String[] args) {
	long start = System.currentTimeMillis();
	if (args.length < 1) {
	    System.err.println ("Usage: java SATBacktrack cnf-formula");
	    System.exit(0);
	}
	new SATBacktrack().solve ( args[0] );
	System.out.println("It took "+ (System.currentTimeMillis()-start));
    }


}
