/**
 * Copyright (c) 2012, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **/

package edu.berkeley.path.beats.simulator.utils;

import edu.berkeley.path.beats.jaxb.Splitratio;
import edu.berkeley.path.beats.simulator.Node;

import java.util.List;
import java.util.StringTokenizer;

// 3D matrix class used for representing time-invariant split ratio matrices.
public final class Double3DMatrix {
	
	protected int nIn;			// [1st dimension] number of input links
    protected int nOut;			// [2nd dimension] number of columns
    protected int nVTypes;		// [3rd dimension] number of slices
    protected boolean isempty;	// true if there is no data;
    protected double [][][] data;
    
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

    public Double3DMatrix(Double3DMatrix that){
        this.nIn = that.nIn;
        this.nOut = that.nOut;
        this.nVTypes = that.nVTypes;
        this.isempty = that.isempty;
        this.data = that.cloneData();
    }


    public Double3DMatrix(int nIn,int nOut,int nVTypes,double val) {
    	this.nIn = nIn;
    	this.nOut = nOut;
    	this.nVTypes = nVTypes;
    	data = new double[nIn][nOut][nVTypes];
    	for(int i=0;i<nIn;i++)
        	for(int j=0;j<nOut;j++)
            	for(int k=0;k<nVTypes;k++)
            		data[i][j][k] = val;
    	this.isempty = nIn==0 || nOut==0 || nVTypes==0;
    }
    
    public Double3DMatrix(String str) {

    	int numtokens,i,j,k;
		boolean issquare = true;
    	nIn = 0;
    	nOut = 0;
    	nVTypes = 0;
    	
    	if ((str.isEmpty()) || (str.equals("\n")) || (str.equals("\r\n"))){
    		isempty = true;
			return;
    	}
    	
    	str.replaceAll("\\s","");
    	
    	// populate data
		StringTokenizer slicesX = new StringTokenizer(str, ";");
		nIn = slicesX.countTokens();
		i=0;
		boolean allnan = true;
		while (slicesX.hasMoreTokens() && issquare) {
			String sliceX = slicesX.nextToken();
			StringTokenizer slicesXY = new StringTokenizer(sliceX, ",");
			
			// evaluate nOut, check squareness
			numtokens = slicesXY.countTokens();
			if(nOut==0) // first time here
				nOut = numtokens;
			else{
				if(nOut!=numtokens){
					issquare = false;
					break;
				}
			}
			
			j=0;
			while (slicesXY.hasMoreTokens() && issquare) {
				String sliceXY = slicesXY.nextToken();
				StringTokenizer slicesXYZ = new StringTokenizer(sliceXY,":");
				
				// evaluate nVTypes, check squareness
				numtokens = slicesXYZ.countTokens();
				if(nVTypes==0){ // first time here
					nVTypes = numtokens;
					data = new double[nIn][nOut][nVTypes];
				}
				else{
					if(nVTypes!=numtokens){
						issquare = false;
						break;
					}
				}
				
				k=0;
				while (slicesXYZ.hasMoreTokens() && issquare) {
					try {
						double value = Double.parseDouble(slicesXYZ.nextToken());
						if(value>=0){
							data[i][j][k] = value;
							allnan = false;
						}
						else
							data[i][j][k] = Double.NaN;
					} catch (NumberFormatException e) {
						data[i][j][k] = Double.NaN;
					}
					k++;
				}
				j++;
			}
			i++;
		}

		if(allnan){
			nIn=0;
			nOut=0;
			nVTypes=0;
			data = null;
		}
		
		if(!issquare){
			BeatsErrorLog.addError("Data is not square.");
			nIn=0;
			nOut=0;
			nVTypes=0;
			data = null;
		}

    	this.isempty = nIn==0 && nOut==0 && nVTypes==0;
		
    }
     
    public Double3DMatrix(double [][][] x){
    	nIn = x.length;
    	if(nIn==0)
    		return;
    	nOut = x[0].length;
    	if(nOut==0)
    		return;
    	nVTypes = x[0][0].length;
    	if(nVTypes==0)
    		return;
    	data = new double[nIn][nOut][nVTypes];
    	for(int i=0;i<nIn;i++)
        	for(int j=0;j<nOut;j++)
            	for(int k=0;k<nVTypes;k++)
            		data[i][j][k] = x[i][j][k];
    	isempty = false;
    }
    
	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////  

	public boolean isEmpty() {
		return isempty;
	}
	
    public int getnIn() {
		return nIn;
	}

	public int getnOut() {
		return nOut;
	}

	public int getnVTypes() {
		return nVTypes;
	}

	public double [][][] getData() {
		return data;
	}
	
	public double [][][] cloneData() {
		double [][][] cData = new double [nIn][nOut][nVTypes];
		int i,j,k;
		for(i=0;i<nIn;i++)
			for(j=0;j<nOut;j++)
				for(k=0;k<nVTypes;k++)
					cData[i][j][k] = data[i][j][k];
		return cData;
	}

	public double get(int i,int j,int k){
    	if(isempty)
    		return Double.NaN;
		try{
    		return data[i][j][k];
		}
		catch(ArrayIndexOutOfBoundsException e){
			return Double.NaN;
		}
    }
	
	public double getSumOverTypes(int i,int j){
		double sum = 0.0;
		for(int k=0;k<data[i][j].length;k++)
			sum += data[i][j][k];
		return sum;
	}
    
    @Override
	public String toString() {
		String str = new String();
		str = "[";
    	for(int i=0;i<nIn;i++){
        	for(int j=0;j<nOut;j++){
            	for(int k=0;k<nVTypes;k++){
        			str += data[i][j][k];
            		if(k<nVTypes-1)
            			str += ":";
            	}
        		if(j<nOut-1)
        			str += ",";
        	}
    		if(i<nIn-1)
    			str += ";";
    	}
    	str += "]";
		return str;
	}
	
	/////////////////////////////////////////////////////////////////////
	// alter data
	/////////////////////////////////////////////////////////////////////  

	public void set(int i,int j,int k,double f){
    	if(isempty)
    		return;
    	data[i][j][k] = f;
    }
	
    public void multiplyscalar(double value){
    	if(isempty)
    		return;
    	int i,j,k;
    	for(i=0;i<nIn;i++)
    		for(j=0;j<nOut;j++)
    			for(k=0;k<nVTypes;k++)
    				data[i][j][k] *= value;	
    }
    
    public void addscalar(double value){
    	if(isempty)
    		return;
    	int i,j,k;
    	for(i=0;i<nIn;i++)
    		for(j=0;j<nOut;j++)
    			for(k=0;k<nVTypes;k++)
    				data[i][j][k] += value;	
    }
    
    public void copydata(Double3DMatrix in){
    	if(in.nIn!=nIn || in.nOut!=nOut || in.nVTypes!=nVTypes)
    		return;
    	int i,j,k;
    	for(i=0;i<nIn;i++)
    		for(j=0;j<nOut;j++)
    			for(k=0;k<nVTypes;k++)
    				data[i][j][k] = in.data[i][j][k];	  
    }

    public void override_splits(Node node,List<Splitratio> srs){

        for(Splitratio sr : srs){
            int in_index = node.getInputLinkIndex(sr.getLinkIn());
            int out_index = node.getOutputLinkIndex(sr.getLinkOut());
            int vt_index = node.getMyNetwork().getMyScenario().get.vehicleTypeIndexForId(sr.getVehicleTypeId());

            if(in_index<0 || out_index<0 || vt_index<0)
                continue;

            Double val;
            try{
                val = Double.parseDouble(sr.getContent());
            } catch( NumberFormatException e){
                val = Double.NaN;
            }
            if(val.isNaN())
                continue;

            data[in_index][out_index][vt_index] = val;
        }

    }
    
	/////////////////////////////////////////////////////////////////////
	// check data
	/////////////////////////////////////////////////////////////////////  
    
    public boolean hasNaN(){
    	if(isempty)
    		return false;
    	int i,j,k;
    	for(i=0;i<nIn;i++)
    		for(j=0;j<nOut;j++)
    			for(k=0;k<nVTypes;k++)
    				if(Double.isNaN(data[i][j][k]))
    					return true;
    	return false;
    }
    
}
