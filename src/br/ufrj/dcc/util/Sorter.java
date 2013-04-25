package br.ufrj.dcc.util;

import java.util.Vector;

import br.ufrj.dcc.routing.proc.RoutingEntry;

public class Sorter {
	private Vector vector;

	public Sorter(Vector vector) {
		this.vector = vector;
	}
	
	public void sort(boolean coord) {
		if(vector == null)
			return;
		
		quicksort(0, vector.size()-1, coord);
	}
	
	private int compare(int i, RoutingEntry entry, boolean coord) {
		return ((RoutingEntry)vector.elementAt(i)).compare(entry, coord);
	}

	private void swap(int i, int j) {
		Object oi = vector.elementAt(i);
		Object oj = vector.elementAt(j);
		vector.setElementAt(oj, i);
		vector.setElementAt(oi, j);
	}
	
	private void quicksort(int begin, int end, boolean coord) {
		int i = begin;
		int j = end;
		RoutingEntry middle;

		if (end > begin) {
			int index = (begin + end) / 2; 
			middle = (RoutingEntry)vector.elementAt(index);

			while (i <= j) {
				while (i < end && compare(i, middle, coord) < 0)
					i++;

				while (j > begin && compare(j, middle, coord) > 0)
					--j;

				if (i <= j) {
					swap(i, j);
					++i;
					--j;
				}
			}
			
			if (begin < j)
				quicksort(begin, j, coord);

			if(i < end)
				quicksort(i, end, coord);

		}
	}
}