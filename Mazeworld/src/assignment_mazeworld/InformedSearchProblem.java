package assignment_mazeworld;

import java.util.*;

/**
 * Provided by Dartmouth College CS76 Artificial Intelligence
 */
public class InformedSearchProblem extends SearchProblem {

	public List<SearchNode> astarSearch() {
		resetStats();

		// sort these nodes based on their distances to the goal
		PriorityQueue<SearchNode> fringe = new PriorityQueue<SearchNode>();

		// record nodes we have visited and their priority
		HashMap<SearchNode, Double> visited = new HashMap<SearchNode, Double>();

		// record a node's parent node
		// we could use hashmap to record parent node
		// or add it into node's data structure
		// I implemented both of them and use the last one
		// HashMap<SearchNode, SearchNode> reachedFrom = new HashMap<SearchNode, SearchNode>();

		fringe.add(startNode);
		visited.put(startNode, startNode.priority());
		//reachedFrom.put(startNode, null);

		while (!fringe.isEmpty()){

			// update memory and explored nodes number
			incrementNodeCount();
			updateMemory(fringe.size() + visited.size());

			SearchNode current = fringe.poll();
			if(current.priority() > visited.get(current))
				continue;
			// return path if reach the goal
			if (current.goalTest())
				return newbackchain(current);

			// search next states
			else{
				List<SearchNode> successorsList = current.getSuccessors();
				for (SearchNode node : successorsList) {
					// if we have not visited this node
					if (!visited.containsKey(node)) {
						node.setParent(current);
						fringe.add(node);
						visited.put(node, node.priority());
						//reachedFrom.put(node, current);
					}
				}
			}
		}
		return null;
	}

	//
	protected List<SearchNode> newbackchain(SearchNode node) {
		LinkedList<SearchNode> solution = new LinkedList<SearchNode>();
		while (node != null) {
			solution.addFirst(node);
			node = node.getParent();
		}
		return solution;
	}
}
