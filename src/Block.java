/*
 * Andrew S. Clark
 * CS 3853
 * Computer Architecture
 * Assignment 2
 * 10/23/15
 */

public class Block 
{
	public int blockSize;
	public int validBit;
	public int tag;
	//use memory access number for the time
	public int timeAccessed;
	
	public Block()
	{
		validBit = 0;
		tag = -1;
		timeAccessed = -1;
	}
	
	public int getValidBit() {
		return validBit;
	}

	public void setValidBit(int validBit) {
		this.validBit = validBit;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getTimeAccessed() {
		return timeAccessed;
	}

	public void setTimeAccessed(int timeAccessed) {
		this.timeAccessed = timeAccessed;
	}
	
	

}
