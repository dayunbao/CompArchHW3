/*
 * Andrew S. Clark
 * CS 3853
 * Computer Architecture
 * Assignment 2
 * 10/23/15
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class Cache 
{
	public int cacheSizeBits;
	public int blockSizeBits;
	public int associativityBit;
	public String replacePolicy;
	public int numBlockBits;
	public int cacheSize;
	public int blockSize;
	public int numBlocks;
	public int setAssociativity;
	public int totalSetsInCache;
	public final int ADDRESS_SIZE = 32;
	public boolean trace;
	public int numHits;
	public int numMisses;
	public int numAccesses;
	public boolean directMapped;
	public boolean fullyAssociative;
	public boolean nWayAssociative;
	//use same data structure for direct mapped and fully associative, since only difference is whether to check tag or not
	public ArrayList<Block> directMappedOrFullyAssociativeBlocks;
	public Block[][] nWayBlocks;
	public String space;
	
	public Cache(int cacheSizeBits, int blockSizeBits, int associativityBit, String replacePolicy, boolean trace)
	{
		this.cacheSizeBits = cacheSizeBits;
		this.blockSizeBits = blockSizeBits;
		this.associativityBit = associativityBit;
		this.replacePolicy = replacePolicy;
		this.cacheSize = (int) Math.pow(2.0, cacheSizeBits);
		this.blockSize = (int) Math.pow(2.0, blockSizeBits);
		this.numBlockBits = cacheSizeBits - blockSizeBits;
		this.numBlocks = (int) Math.pow(2.0, numBlockBits);
		this.setAssociativity = (int) Math.pow(2.0, associativityBit);
		this.totalSetsInCache = numBlocks / setAssociativity;
		
		this.trace = trace;
		this.numHits = 0;
		this.numMisses = 0;
		this.numAccesses = 0;
		
		this.directMapped = false;
		this.fullyAssociative = false;
		this.nWayAssociative = false;
		this.space = "  ";
		
		if(this.setAssociativity == 1)
		{
			this.directMapped = true;
		}
		else if(this.associativityBit == this.numBlockBits)
		{
			this.fullyAssociative = true;
		}
		else
		{
			this.nWayAssociative = true;
		}
		
		//use same data structure for direct mapped and fully associative, since only difference is whether to check tag or not
		if(this.directMapped || this.fullyAssociative)
		{
			//direct mapped cache
			this.directMappedOrFullyAssociativeBlocks = new ArrayList<Block>();
			initializeDirectMappedOrFullyAssociativeBlocks();
		}
		else if(this.nWayAssociative)
		{
			this.nWayBlocks = new Block[totalSetsInCache][setAssociativity];
			initializeNWayBlocks();
		}
		
		if(this.trace == true)
		{
			//System.out.println("Add"  + "\t" + 	"Tag"  + "\t" + "Set"  + "\t" + "H/M"  + "\t" + "#H"  + "\t" + "#M"  + "\t" + 	"#A"  + "\t" + "MR" + "\t" + "\t" + "CTags");
			
			System.out.printf("%-8s", "Addr");
			System.out.printf("%s", space);
			System.out.printf("%5s", "Tag");
			System.out.printf("%s", space);
			System.out.printf("%5s", "Set");
			System.out.printf("%s", space);
			System.out.printf("%5s", "H/M");
			System.out.printf("%s", space);
			System.out.printf("%5s", "Hit");
			System.out.printf("%s", space);
			System.out.printf("%5s", "Miss");
			System.out.printf("%s", space);
			System.out.printf("%5s", "Acc");
			System.out.printf("%s", space);
			System.out.printf("%10s", "MRate");
			System.out.printf("%s", space);
			System.out.printf("%5s\n", "CTag");
			
		}
		
		
	}
	
	public void initializeDirectMappedOrFullyAssociativeBlocks()
	{
		for(int i = 0; i < this.numBlocks; i++)
		{
			Block block = new Block();
			this.directMappedOrFullyAssociativeBlocks.add(block);
		}
	}
	
	
	public void initializeNWayBlocks()
	{
		
		for(int i = 0; i < this.nWayBlocks.length; i++)
		{
			for(int j = 0; j < this.nWayBlocks[i].length; j++)
			{
				Block block = new Block();
				this.nWayBlocks[i][j] = block;
			}
		}
		
	}
		
	public int convertToIntegerDecimal(String address)
	{
		int intAddress;
		
		if(address.startsWith("0x"))
		{
			String subStr = address.substring(2);
			intAddress = Integer.parseInt(subStr, 16);
			
		}
		else
		{
			intAddress = Integer.parseInt(address);
		}
		
		return intAddress;
	}
	
	public static String convertToHex(int addr)
	{
		String address = Integer.toHexString(addr);
		return address;
	}
		
	//returns the block address.
	public int getBlockAddress(int fullAddress)
	{
		int divVal = (int) Math.pow(2.0, this.blockSizeBits);
		int blockAddress = fullAddress / divVal;
		return blockAddress;
		
	}
	
	//returns the index
	public int getIndex(int blockAddress)
	{
		int modVal = 0;
		int index = 0;
		
		if(this.directMapped)
		{	
			modVal = (int) Math.pow(2.0, this.numBlockBits);
			index = blockAddress % modVal;
			
		}
		else if(this.nWayAssociative)
		{
			index = blockAddress % this.totalSetsInCache;
		}
		
		return index;
	}
	
	//returns the tag 
	public int getTag(int blockAddress)
	{
		int tag = 0;
		
		if(this.directMapped)
		{	
			int divideVal = (int)Math.pow(2.0, this.numBlockBits);
			tag = blockAddress / divideVal;
			
			
		}
		else
		{
			tag = blockAddress / this.totalSetsInCache;
		}
		
		return tag;
	}
	
	public void checkCache(String address)
	{
		this.numAccesses++;
		String hitOrMiss = "";
		//breakdown address into usable parts
		int intAddress = convertToIntegerDecimal(address);
		int blockAddress = getBlockAddress(intAddress);
		
		//if direct mapped, proceed as in last homework
		if(this.directMapped)
		{
			int blockIndex = getIndex(blockAddress);
			int tag = getTag(blockAddress);
			
			//actual work starts here
			if(this.directMappedOrFullyAssociativeBlocks.get(blockIndex).getValidBit() == 0)
			{
				hitOrMiss = "miss";
				this.numMisses++;
				
				if(this.trace)
				{
					printTraceInfo(intAddress, tag, blockIndex, hitOrMiss);
				}
				
				this.directMappedOrFullyAssociativeBlocks.get(blockIndex).setValidBit(1);
				this.directMappedOrFullyAssociativeBlocks.get(blockIndex).setTag(tag);
				//first time placed in cache, always set timeAccessed var
				this.directMappedOrFullyAssociativeBlocks.get(blockIndex).setTimeAccessed(numAccesses);
			}
			else if(this.directMappedOrFullyAssociativeBlocks.get(blockIndex).getTag() != tag)
			{
				//always replace, since direct mapped
				hitOrMiss = "miss";
				this.numMisses++;
				
				if(this.trace)
				{
					printTraceInfo(intAddress, tag, blockIndex, hitOrMiss);
				}
				
				this.directMappedOrFullyAssociativeBlocks.get(blockIndex).setValidBit(1);
				this.directMappedOrFullyAssociativeBlocks.get(blockIndex).setTag(tag);
				this.directMappedOrFullyAssociativeBlocks.get(blockIndex).setTimeAccessed(numAccesses);
				
			}
			else if(directMappedOrFullyAssociativeBlocks.get(blockIndex).getTag() == tag)
			{
				hitOrMiss = "hit";
				this.numHits++;
				
				if(this.trace)
				{
					printTraceInfo(intAddress, tag, blockIndex, hitOrMiss);
				}
				
				if(replacePolicy.equals("lru"))
				{
					this.directMappedOrFullyAssociativeBlocks.get(blockIndex).setTimeAccessed(numAccesses);
				}
				
				
			}
		}
		/*	else if fully associative
		 * 	1) no index
		 * 	2) tag is entire block address
		 * 	3) loop through fullyAssociativeBlocks ArrayList
		 * 	4) and check every tag
		 * 	5) set timesAccessed variable with numAccesses value
		 * 	6) replace based on replacePolicy
		 */
		else if(this.fullyAssociative)
		{
			/* check on how to replace!!!
			 * loop through whole array, look for matching tag
			 * 
			 * 1)	if find matching tag, then have hit, 
			 *		update timeAccessed var based on replacement policy
			 *    	done
			 * OR
			 * 
			 * 2)	if no matching tag, and have a block with validBit == 0, then place randomly in block with validBit == 0
			 * 		update timeAccessed var based on replacement policy
			 * 		done
			 * OR
			 * 
			 * 3)	if no matching tag, and all validBits == 1, then replace based on replacement policy
			 * 		update timeAccessed var based on replacement policy
			 */
			int tag = blockAddress;
			int index = -1;
			boolean tagMatch = false;
			boolean foundInvalidBit = false;
			
			//first search through array, looking for matching tag
			for(int i = 0; i < this.directMappedOrFullyAssociativeBlocks.size(); i++)
			{
				//data already in cache, mark as hit, update timeAccessed var based on replacePolicy
				if(this.directMappedOrFullyAssociativeBlocks.get(i).getTag() == tag)
				{
					//if lru, update timeAccessed var when placed in cache, and when accessed
					if(replacePolicy.equals("lru"))
					{
						this.directMappedOrFullyAssociativeBlocks.get(i).setTimeAccessed(numAccesses);
					}
					hitOrMiss = "hit";
					this.numHits++;
					tagMatch = true;
					break;
				}
			}
			
			//if no matching tag, search again for block with validBit == 0, replace block based on policy
			if(!tagMatch)
			{
				for(int i = 0; i < this.directMappedOrFullyAssociativeBlocks.size(); i++)
				{
					//place in first block with validBit == 0, i.e. currently no valid address
					if(this.directMappedOrFullyAssociativeBlocks.get(i).getValidBit() == 0)
					{
						//in this case, no matching tag, so will be placing in cache for first time
						//so timeAccessed var updated regardless of policy		
						this.directMappedOrFullyAssociativeBlocks.get(i).setTimeAccessed(numAccesses);
						this.directMappedOrFullyAssociativeBlocks.get(i).setValidBit(1);
						this.directMappedOrFullyAssociativeBlocks.get(i).setTag(tag);
						this.numMisses++;
						hitOrMiss = "miss";
						foundInvalidBit = true;
						break;
					}
					
				}
			}
			
			//no matching tag found, all valid bits set, so need to replace existing data 
			if(!tagMatch && !foundInvalidBit)
			{
				//temp var to find minimum timeAccessed var value
				int min = Integer.MAX_VALUE;
				int indexOfBlockToReplace = 0;
				//loop through array and find block with minimum timeAccessed var
				//this will be the same for fifo and lru
				for(int i = 0; i < this.directMappedOrFullyAssociativeBlocks.size(); i++)
				{
					if(this.directMappedOrFullyAssociativeBlocks.get(i).getTimeAccessed() < min)
					{
						min = this.directMappedOrFullyAssociativeBlocks.get(i).getTimeAccessed();
						indexOfBlockToReplace = i;
					}
				}
				hitOrMiss = "miss";
				this.numMisses++;
				this.directMappedOrFullyAssociativeBlocks.get(indexOfBlockToReplace).setTag(tag);
				this.directMappedOrFullyAssociativeBlocks.get(indexOfBlockToReplace).setTimeAccessed(numAccesses);
				
			}
			
			
			if(this.trace)
			{
				printTraceInfo(intAddress, tag, index, hitOrMiss);
			}
		}
		/*	else is n-way set associative
		 * 	1) has index, which is set number
		 *	2) go to nWayBlocks ArrayList set number
		 * 	3) check tags of every block in set
		 * 	4) set timesAccessed variable with numAccesses value
		 * 	5) replace based on replacePolicy
		 */
		else if(this.nWayAssociative)
		{
			//check on how to replace!!!
			int setIndex = getIndex(blockAddress);
			int tag = getTag(blockAddress);
			
			boolean tagMatch = false;
			boolean foundInvalidBit = false;
			
			//first check blocks in set for matching tag
			for(int i = 0; i < this.setAssociativity; i++)
			{
				//found matching tag, if LRU update timeAccessed var
				if(this.nWayBlocks[setIndex][i].getTag() == tag)
				{		
					hitOrMiss = "hit";
					tagMatch = true;
					
					if(this.trace)
					{
						printTraceInfo(intAddress, tag, setIndex, hitOrMiss);
					}
					
					if(replacePolicy.equals("lru"))
					{
						this.nWayBlocks[setIndex][i].setTimeAccessed(numAccesses);
					}
					
					this.numHits++;
					break;
				}
			}
			//no matching tag found, so look for block with validBit == 0
			if(!tagMatch)
			{
				for(int i = 0; i < this.setAssociativity; i++)
				{
					//found block with validBit == 0, so put data there
					if(this.nWayBlocks[setIndex][i].getValidBit() == 0)
					{
						hitOrMiss = "miss";
						this.numMisses++;
						
						if(this.trace)
						{
							printTraceInfo(intAddress, tag, setIndex, hitOrMiss);
						}
						
						foundInvalidBit = true;
						this.nWayBlocks[setIndex][i].setValidBit(1);
						this.nWayBlocks[setIndex][i].setTag(tag);
						this.nWayBlocks[setIndex][i].setTimeAccessed(numAccesses);
						break;
					}
				}
			}
			//no matching tag, and no blocks with validBit == 0; so need to choose which to replace
			if(!tagMatch && !foundInvalidBit)
			{
				hitOrMiss = "miss";
				this.numMisses++;
				
				if(this.trace)
				{
					printTraceInfo(intAddress, tag, setIndex, hitOrMiss);
				}
				
				//temp var used to find min timeAccessed var
				int min = Integer.MAX_VALUE;
				//temp var to keep track of block with min timeAccessed var
				int indexOfBlockToReplace = 0;
				
				for(int i = 0; i < this.setAssociativity; i++)
				{
					if(this.nWayBlocks[setIndex][i].getTimeAccessed() < min)
					{
						min = this.nWayBlocks[setIndex][i].getTimeAccessed();
						indexOfBlockToReplace = i;
					}
				}
				//will replace block with min timeAccessed var, regardless of policy
				
				this.nWayBlocks[setIndex][indexOfBlockToReplace].setTag(tag);
				this.nWayBlocks[setIndex][indexOfBlockToReplace].setValidBit(1);
				this.nWayBlocks[setIndex][indexOfBlockToReplace].setTimeAccessed(numAccesses);;
			}	
		}
		
	}
	
	//FINISH THIS FUNCTION!
	public void printTraceInfo(int intAddress, int tag, int index, String hitOrMiss)
	{	
		if(this.directMapped)
		{
			String hexAddress = convertToHex(intAddress);
			String hextag = convertToHex(tag);
			String setNum = "";
			setNum += convertToHex(index);
			
			if(directMappedOrFullyAssociativeBlocks.get(index).getTimeAccessed() != -1)
			{
				ArrayList<String> sortedHexTags = new ArrayList<String>();
				sortedHexTags = sortTags(index);
				
				System.out.printf("%-8s", hexAddress);
				System.out.printf("%s", space);
				System.out.printf("%5s", hextag);
				System.out.printf("%s", space);
				System.out.printf("%5s", setNum);
				System.out.printf("%s", space);
				System.out.printf("%5s", hitOrMiss);
				System.out.printf("%s", space);
				System.out.printf("%5s", numHits);
				System.out.printf("%s", space);
				System.out.printf("%5s", numMisses);
				System.out.printf("%s", space);
				System.out.printf("%5s", numAccesses);
				System.out.printf("%s", space);
				System.out.printf("%10s", getMissRatio());
				System.out.printf("%s%s%s", space, space, space);
				
				for(int i = 0; i < sortedHexTags.size(); i++)
				{
					System.out.print(sortedHexTags.get(i));
					
					if(i != (sortedHexTags.size() - 1 ) )
					{
						System.out.print(",");
					}
				}
				
				System.out.println();
			}
			else
			{
			
				System.out.printf("%-8s", hexAddress);
				System.out.printf("%s", space);
				System.out.printf("%5s", hextag);
				System.out.printf("%s", space);
				System.out.printf("%5s", setNum);
				System.out.printf("%s", space);
				System.out.printf("%5s", hitOrMiss);
				System.out.printf("%s", space);
				System.out.printf("%5s", numHits);
				System.out.printf("%s", space);
				System.out.printf("%5s", numMisses);
				System.out.printf("%s", space);
				System.out.printf("%5s", numAccesses);
				System.out.printf("%s", space);
				System.out.printf("%10s", getMissRatio());
				System.out.printf("%s%s%s", space, space, space);
				System.out.println();
			}
		}
		else if(this.fullyAssociative)
		{
			String hexAddress = convertToHex(intAddress);
			String hextag = convertToHex(tag);
			String setNum = "0";
			
			ArrayList<String> sortedHexTags = new ArrayList<String>();
			sortedHexTags = sortTags(0);
			
			System.out.printf("%-8s", hexAddress);
			System.out.printf("%s", space);
			System.out.printf("%5s", hextag);
			System.out.printf("%s", space);
			System.out.printf("%5s", setNum);
			System.out.printf("%s", space);
			System.out.printf("%5s", hitOrMiss);
			System.out.printf("%s", space);
			System.out.printf("%5s", numHits);
			System.out.printf("%s", space);
			System.out.printf("%5s", numMisses);
			System.out.printf("%s", space);
			System.out.printf("%5s", numAccesses);
			System.out.printf("%s", space);
			System.out.printf("%10s", getMissRatio());
			System.out.printf("%s%s%s", space, space, space);
			
			for(int i = 0; i < sortedHexTags.size(); i++)
			{
				System.out.print(sortedHexTags.get(i));
				
				if(i != (sortedHexTags.size() - 1 ) )
				{
					System.out.print(",");
				}
			}
			
			System.out.println();
			
		}
		else if(this.nWayAssociative)
		{
			String hexAddress = convertToHex(intAddress);
			String hextag = convertToHex(tag);
			String setNum = ""; 
			setNum += convertToHex(index);
			
			ArrayList<String> sortedHexTags = new ArrayList<String>();
			sortedHexTags = sortTags(index);
						
			System.out.printf("%-8s", hexAddress);
			System.out.printf("%s", space);
			System.out.printf("%5s", hextag);
			System.out.printf("%s", space);
			System.out.printf("%5s", setNum);
			System.out.printf("%s", space);
			System.out.printf("%5s", hitOrMiss);
			System.out.printf("%s", space);
			System.out.printf("%5s", numHits);
			System.out.printf("%s", space);
			System.out.printf("%5s", numMisses);
			System.out.printf("%s", space);
			System.out.printf("%5s", numAccesses);
			System.out.printf("%s", space);
			System.out.printf("%10s", getMissRatio());
			System.out.printf("%s%s%s", space, space, space);
			
			for(int i = 0; i < sortedHexTags.size(); i++)
			{
				System.out.print(sortedHexTags.get(i));
				
				if(i != (sortedHexTags.size() - 1 ) )
				{
					System.out.print(",");
				}
			}
			
			System.out.println();
		}
				
	}
	
	public ArrayList<String> sortTags(int setNumber)
	{
		ArrayList<Integer> sortedIntTags = new ArrayList<Integer>();
		ArrayList<String> sortedHexTags = new ArrayList<String>();
		
		if(this.directMapped)
		{
			int intTag = 0;
			
			if(directMappedOrFullyAssociativeBlocks.get(setNumber).getTag() != -1)
			{
				intTag = directMappedOrFullyAssociativeBlocks.get(setNumber).getTag();
			}
			
			String tempAdd = convertToHex(intTag);
			tempAdd += "(" + directMappedOrFullyAssociativeBlocks.get(setNumber).getTimeAccessed() + ")";
			sortedHexTags.add(tempAdd);
		}
		else if(this.fullyAssociative)
		{
			for(int i = 0; i < this.directMappedOrFullyAssociativeBlocks.size(); i++)
			{
				if(directMappedOrFullyAssociativeBlocks.get(i).getTag() == -1)
				{
					continue;
				}
				else
				{
					sortedIntTags.add(directMappedOrFullyAssociativeBlocks.get(i).getTag());
				}
			}
			
			Collections.sort(sortedIntTags);
			
			for(int i = 0; i < sortedIntTags.size(); i++)
			{
				for(int j = 0; j < this.directMappedOrFullyAssociativeBlocks.size(); j++)
				{
					if(this.directMappedOrFullyAssociativeBlocks.get(j).getTag() == sortedIntTags.get(i))
					{
						String tempAdd = convertToHex(sortedIntTags.get(i));
						tempAdd += "(" + directMappedOrFullyAssociativeBlocks.get(j).getTimeAccessed() + ")";
						sortedHexTags.add(tempAdd);
					}
				}
			}

		}

		else
		{
			for(int i = 0; i < this.nWayBlocks[setNumber].length; i++)
			{
				
				if(this.nWayBlocks[setNumber][i].getTag() == -1)
				{
					continue;
				}
				else
				{
					sortedIntTags.add(this.nWayBlocks[setNumber][i].getTag());
				}
				
			}
			
			Collections.sort(sortedIntTags);
			
			for(int i = 0; i < sortedIntTags.size(); i++)
			{
				for(int j = 0; j < this.nWayBlocks[setNumber].length; j++)
				{
					if(this.nWayBlocks[setNumber][j].getTag() == sortedIntTags.get(i))
					{
						String tempAdd = convertToHex(sortedIntTags.get(i));
						tempAdd += "(" + this.nWayBlocks[setNumber][j].getTimeAccessed() + ")";
						sortedHexTags.add(tempAdd);
					}
					
				}
			}
		}
		
				return sortedHexTags;
	}
	
	public String getMissRatio()
	{
		DecimalFormat format = new DecimalFormat("###,###,###,##0.00000000");
		String ratio = format.format( (numMisses / (double)numAccesses) );
		return ratio;
	}
	
	public int getNumAccesses() 
	{
		return numAccesses;
	}

	public void setNumAccesses(int numAccesses) 
	{
		this.numAccesses = numAccesses;
	}

	public int getNumHits() 
	{
		return numHits;
	}

	public void setNumHits(int numHits) 
	{
		this.numHits = numHits;
	}

	public int getNumMisses() 
	{
		return numMisses;
	}

	public void setNumMisses(int numMisses) 
	{
		this.numMisses = numMisses;
	}
	
}
