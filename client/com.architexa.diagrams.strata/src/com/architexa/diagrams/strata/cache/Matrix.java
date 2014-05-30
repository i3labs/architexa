/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.cache;

public class Matrix {

	public int[][] data = null;

	public Matrix(int sz) {
		data = new int[sz][sz];
	}
	public Matrix(int[][] _data) {
		data = _data;
	}
	public int getSize() {
		return data.length;
	}
	@Override
	public Matrix clone() {
		return new Matrix(this.data.clone());
		
	}
	public Matrix getMatrixWithDeletedIndex(int ndxToEliminate) {
		Matrix newDep = new Matrix(this.getSize()-1);
		for (int r = 0; r < this.getSize(); r++) {
			if (r==ndxToEliminate) continue;
			for (int c = 0; c < this.getSize(); c++) {
				if (c==ndxToEliminate) continue;
				int dst_r = r;
				int dst_c = c;
				if (dst_r>ndxToEliminate) dst_r--;
				if (dst_c>ndxToEliminate) dst_c--;
				newDep.data[dst_r][dst_c] = this.data[r][c];
			}
		}
		return newDep;
	}
}
