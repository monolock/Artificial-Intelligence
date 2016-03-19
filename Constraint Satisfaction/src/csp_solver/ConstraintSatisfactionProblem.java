package csp_solver;

import java.util.*;

import javafx.util.Pair;
import org.omg.PortableInterceptor.INACTIVE;

/**
 * Simple CSP solver
 * Created by gejing on 2/19/16.
 */
public class ConstraintSatisfactionProblem {
    private int nodesExplored;
    private int constraintsChecked;
    private HashMap<Integer, Set<Integer>> variables;
    private HashMap<Pair<Integer, Integer>, Set<Pair<Integer, Integer>>> constraints;
    private HashMap<Integer, Set<Integer>> neighbors;

    static final private boolean isLCV = false;
    static final private boolean isMRV = true;
    static final private boolean isFC = true;
    // To use MAC3, you need to set isFC as false
    static final private boolean isMAC3 = false;


    ConstraintSatisfactionProblem() {
        resetStats();
        variables = new HashMap<>();
        constraints = new HashMap<>();
        neighbors = new HashMap<>();
    }

    /**
     * Solve for the CSP problem
     *
     * @return the mapping from variables to values
     */
    public Map<Integer, Integer> solve() {
        resetStats();
        long before = System.currentTimeMillis();
        if (!enforceConsistency())
            return null;
        Map<Integer, Integer> solution = backtracking(new HashMap<>());
        double duration = (System.currentTimeMillis() - before) / 1000.0;
        printStats();
        System.out.println(String.format("Search time is %.2f second", duration));
        return solution;
    }

    private void resetStats() {
        nodesExplored = 0;
        constraintsChecked = 0;
    }

    private void incrementNodeCount() {
        ++nodesExplored;
    }

    private void incrementConstraintCheck() {
        ++constraintsChecked;
    }

    public int getNodeCount() {
        return nodesExplored;
    }

    public int getConstraintCheck() {
        return constraintsChecked;
    }

    protected void printStats() {
        System.out.println("Nodes explored during last search:  " + nodesExplored);
        System.out.println("Constraints checked during last search " + constraintsChecked);
    }

    /**
     * Add a variable with its domain
     *
     * @param id     the identifier of the variable
     * @param domain the domain of the variable
     */
    public void addVariable(Integer id, Set<Integer> domain) {
        variables.put(id, new HashSet<>(domain));
    }

    /**
     * Add a binary constraint
     *
     * @param id1        the identifier of the first variable
     * @param id2        the identifier of the second variable
     * @param constraint the constraint
     */
    public void addConstraint(Integer id1, Integer id2, Set<Pair<Integer, Integer>> constraint) {
        Pair<Integer, Integer> pair = new Pair<>(id1, id2);
        Set<Pair<Integer, Integer>> clone = new HashSet<>();

        Pair<Integer, Integer> pair_reverse = new Pair<>(id2, id1);
        Set<Pair<Integer, Integer>> clone_reverse = new HashSet<>();

        if(!neighbors.containsKey(id1))
            neighbors.put(id1, new HashSet<>());
        neighbors.get(id1).add(id2);
        if(!neighbors.containsKey(id2))
            neighbors.put(id2, new HashSet<>());
        neighbors.get(id2).add(id1);

        for (Pair<Integer, Integer> cons : constraint) {
            clone.add(new Pair<>(cons.getKey(), cons.getValue()));
            clone_reverse.add(new Pair<>(cons.getValue(), cons.getKey()));
        }

        constraints.put(pair, clone);
        constraints.put(pair_reverse, clone_reverse);
    }

    /**
     * Enforce consistency by AC-3, PC-3.
     */
    private boolean enforceConsistency() {

        Queue<Pair<Integer, Integer>> queue = new LinkedList<>();

        // add all the arcs into queue
        for (Pair<Integer, Integer> pair : constraints.keySet())
            queue.add(pair);

        while (!queue.isEmpty()) {

            incrementNodeCount();

            Pair<Integer, Integer> arc = queue.poll();

            // if the domain of X1 has been changed
            if (revise(arc.getKey(), arc.getValue())) {

                // the domain's size equals 0, can't find a solution
                if (variables.get(arc.getKey()).size() == 0)
                    return false;

                // add all neighbors of X1 into queue
                for(Integer neighbor : neighbors.get(arc.getKey())){
                    Pair<Integer, Integer> pair = new Pair<>(neighbor, arc.getKey());
                    queue.add(pair);
                }
            }
        }

        return true;
    }

    private boolean revise(Integer id1, Integer id2) {

        incrementNodeCount();

        boolean revised = false;
        Pair<Integer, Integer> pair = new Pair<>(id1, id2);

        Iterator<Integer> itFirst = variables.get(id1).iterator();
        while (itFirst.hasNext()) {

            Integer m = itFirst.next();
            boolean satisfied = false;

            Iterator<Integer> itSecond = variables.get(id2).iterator();
            while (itSecond.hasNext()) {

                Integer n = itSecond.next();
                Pair<Integer, Integer> cons = new Pair<>(m, n);

                incrementConstraintCheck();
                // if m, n could satisfy constraint
                if (constraints.get(pair).contains(cons)) {
                    satisfied = true;
                    break;
                }
            }

            // if for a specific m in D1
            // can't find any n in D2 to satisfy constraint
            // then remove m from D1
            if (!satisfied) {
                itFirst.remove();
                revised = true;
            }
        }

        return revised;
    }

    /**
     * Backtracking algorithm
     *
     * @param partialSolution a partial solution
     * @return a solution if found, null otherwise.
     */
    private Map<Integer, Integer> backtracking(Map<Integer, Integer> partialSolution) {

        // every var has been assigned, means we find a solution
        if (partialSolution.size() == variables.size())
            return partialSolution;

        // select a var has not been assigned
        Integer var = selectUnassignedVariable(partialSolution);

        // get its domain
        Iterable<Integer> domain = orderDomainValues(var, partialSolution);
        Iterator<Integer> it = domain.iterator();

        while (it.hasNext()) {

            // select a value
            Integer value = it.next();
            incrementNodeCount();

            boolean isValid = true;
            Map<Integer, Set<Integer>> removed = new HashMap<>();

            //check if this value is valid or not
            for (Map.Entry<Integer, Integer> entry : partialSolution.entrySet()) {

                incrementConstraintCheck();

                Integer k = entry.getKey();
                Integer v = entry.getValue();

                Pair<Integer, Integer> pair = new Pair<>(var, k);
                Pair<Integer, Integer> cons = new Pair<>(value, v);

                // if these two vars have constraint
                // but this values pair does not satisfy it
                if (constraints.containsKey(pair) &&
                        !constraints.get(pair).contains(cons)) {

                    isValid = false;
                    break;
                }
            }

            partialSolution.put(var, value);
            if (isValid) {
                if (inference(var, value, partialSolution, removed)) {
                    Map<Integer, Integer> solution = backtracking(partialSolution);
                    if (solution != null)
                        return solution;
                }
            }
            partialSolution.remove(var);

            // if can't find a solution
            // add all we have removed to original set
            for (Map.Entry<Integer, Set<Integer>> entry :
                    removed.entrySet()) {
                variables.get(entry.getKey()).addAll(entry.getValue());
            }
        }

        return null;
    }

    /**
     * Inference for backtracking
     * Implement FC and MAC3
     *
     * @param var             the new assigned variable
     * @param value           the new assigned value
     * @param partialSolution the partialSolution
     * @param removed         the values removed from other variables' domains
     * @return true if the partial solution may lead to a solution, false otherwise.
     */
    private boolean inference(Integer var, Integer value, Map<Integer, Integer> partialSolution, Map<Integer, Set<Integer>> removed) {

        // use FC
        if (isFC) {

            for (Integer neighbor : neighbors.get(var)) {

                incrementConstraintCheck();

                if(partialSolution.containsKey(neighbor))
                    continue;

                Pair<Integer, Integer> pair = new Pair<>(var, neighbor);
                Iterator<Integer> it = variables.get(neighbor).iterator();

                while (it.hasNext()) {

                    incrementConstraintCheck();

                    Integer neiValue = it.next();
                    Pair<Integer, Integer> cons = new Pair<>(value, neiValue);

                    if (constraints.containsKey(pair) && !constraints.get(pair).contains(cons)) {
                        if (!removed.containsKey(neighbor))
                            removed.put(neighbor, new HashSet<>());
                        removed.get(neighbor).add(neiValue);
                        it.remove();
                    }
                }

                if (variables.get(neighbor).size() == 0) {
                    //System.out.println("removed size in"+removed.size());
                    return false;
                }
            }
            //System.out.println("removed size in"+removed.size());
            return true;
        }

        // use MAC3
        else if (isMAC3) {

            Queue<Pair<Integer, Integer>> queue = new LinkedList<>();

            for (Integer neighbor : neighbors.get(var)) {

                incrementNodeCount();

                if(partialSolution.containsKey(neighbor))
                    continue;

                // add all var's neighbors into the queue
                Pair<Integer, Integer> pair = new Pair<>(neighbor, var);
                queue.add(pair);
            }

            // enforce AC3
            while (!queue.isEmpty()) {

                Pair<Integer, Integer> arc = queue.poll();
                boolean isrevised = false;

                if (partialSolution.containsKey(arc.getValue())) {
                    Iterator<Integer> it = variables.get(arc.getKey()).iterator();

                    while (it.hasNext()) {

                        incrementConstraintCheck();

                        Integer neiValue = it.next();
                        Pair<Integer, Integer> cons = new Pair<>(neiValue, value);

                        if (constraints.containsKey(arc) && !constraints.get(arc).contains(cons)) {
                            isrevised = true;
                            if (!removed.containsKey(arc.getKey()))
                                removed.put(arc.getKey(), new HashSet<>());
                            removed.get(arc.getKey()).add(neiValue);
                            it.remove();
                        }
                    }
                } else {

                    Iterator<Integer> itFirst = variables.get(arc.getKey()).iterator();
                    while (itFirst.hasNext()) {

                        Integer m = itFirst.next();
                        boolean satisfied = false;

                        Iterator<Integer> itSecond = variables.get(arc.getValue()).iterator();
                        while (itSecond.hasNext()) {

                            incrementConstraintCheck();

                            Integer n = itSecond.next();
                            Pair<Integer, Integer> cons = new Pair<>(m, n);

                            // if m, n could satisfy constraint
                            if (constraints.get(arc).contains(cons)) {
                                satisfied = true;
                                break;
                            }
                        }

                        if (!satisfied) {
                            isrevised = true;
                            if (!removed.containsKey(arc.getKey()))
                                removed.put(arc.getKey(), new HashSet<>());
                            removed.get(arc.getKey()).add(m);
                            itFirst.remove();
                        }
                    }

                }

                if(isrevised) {

                    if (variables.get(arc.getKey()).size() == 0)
                        return false;

                    for (Integer neighbor : neighbors.get(arc.getKey())) {

                        if (partialSolution.containsKey(neighbor))
                            continue;

                        // add all var's neighbors into the queue
                        Pair<Integer, Integer> pair = new Pair<>(neighbor, arc.getKey());
                        queue.add(pair);
                    }
                }

            }
            return true;
        }

        else
            return true;
    }

    /**
     * Look-ahead value ordering
     * Pick the least constraining value (min-conflicts)
     *
     * @param var             the variable to be assigned
     * @param partialSolution the partial solution
     * @return an order of values in var's domain
     */
    private Iterable<Integer> orderDomainValues(Integer var, Map<Integer, Integer> partialSolution) {
        if(isLCV) {
            Map<Integer, Integer> elimateMap = new HashMap<>();
            for (Integer value : variables.get(var)) {
                int clashed = 0;
                for (Integer neighbor : neighbors.get(var)) {
                    Pair<Integer, Integer> pair = new Pair<>(var, neighbor);
                    for (Integer neiValue : variables.get(neighbor)) {
                        Pair<Integer, Integer> cons = new Pair<>(value, neiValue);
                        if (constraints.containsKey(pair) && !constraints.get(pair).contains(cons)) {
                            clashed++;
                        }
                    }
                }
                elimateMap.put(value, clashed);
            }

            List<Map.Entry<Integer, Integer>> usingForSorting = new ArrayList<>(elimateMap.entrySet());
            Collections.sort(usingForSorting, new Comparator<Map.Entry<Integer, Integer>>() {
                @Override
                public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                    return o1.getValue() - o2.getValue();
                }
            });
            List<Integer> answer = new ArrayList<>();
            for (Map.Entry<Integer, Integer> item : usingForSorting) {
                answer.add(item.getKey());
            }
            return answer;
        } else {
            List<Integer> answer = new ArrayList<>(variables.get(var));
            return answer;
        }
    }

    /**
     * Dynamic variable ordering
     * Pick the variable with the minimum remaining values or the variable with the max degree.
     * Or pick the variable with the minimum ratio of remaining values to degree.
     *
     * @param partialSolution the partial solution
     * @return one unassigned variable
     */
    private Integer selectUnassignedVariable(Map<Integer, Integer> partialSolution) {
        if (isMRV) {
            int minId = 0;
            int minSize = Integer.MAX_VALUE;
            for (Integer variable : variables.keySet()) {
                if (!partialSolution.containsKey(variable)) {
                    if (variables.get(variable).size() < minSize)  {
                        minSize = variables.get(variable).size();
                        minId = variable;
                    }
                }
            }
            return minId;
        } else {
            // follow the order we added elements
            for (Integer variable : variables.keySet()) {
                if (!partialSolution.containsKey(variable)) {
                    Integer var = variable;
                    return var;
                }
            }
        }
        return -1;
    }
}
