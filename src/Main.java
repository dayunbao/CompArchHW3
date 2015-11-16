/*
 * Andrew S. Clark
 * CS 3853
 * Computer Architecture
 * Assignment 2
 * 10/23/15
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class Main 
{
	public static boolean trace;
	public static File file;
	public static String replacePolicy;
	
	public static int cacheSizeBits;
	public static int blockSizeBits;
	public static int associativityBit;
	public static int numBlockBits;
	public static int indexSizeBits;
	
	public static int cacheSize;
	public static int blockSize;
	//public static int tagSize;
	public static int numBlocks;

	
	public static boolean verifyInput(String [] args)
	{
		/*	input: n m p fifo/lru on/off filename
		 * 	n - log2 of cache size
		 *	m - log2 of block size
		 *	p - associativity
		 *	fifo/lru - replacement policy
		 *	on/off - verbose output
		 *	filename - data set
		 */
		
		
		if(args.length < 6 || args.length > 6)
		{
			System.out.println("This program requires 6 arguments.");
			return false;
		}
		
		//size of cache in bits - n
		cacheSizeBits = Integer.parseInt(args[0]);
		//size of blocks in bits - m
		blockSizeBits = Integer.parseInt(args[1]);
		//associativity as an exponent - p
		associativityBit = Integer.parseInt(args[2]);
		//number of bits to calculate the number of blocks in the cache
		numBlockBits = cacheSizeBits - blockSizeBits;
		//size of index in bits
		indexSizeBits = cacheSizeBits - blockSizeBits;
		
		//fifo or lru
		String replaceVal = args[3].toLowerCase();
		
		//off or on
		String traceVal = args[4].toLowerCase();
		
		//file from given filename
		file = new File(args[5]);
		
		cacheSize = (int) Math.pow(2.0, cacheSizeBits);
		blockSize = (int) Math.pow(2.0, blockSizeBits);
		//tagSize = (int) Math.pow(2.0, blockSizeBits);
		numBlocks = (int) Math.pow(2.0, numBlockBits);
				
		if(cacheSizeBits <= 0 || blockSizeBits <= 0 || numBlocks <= 0)
		{
			System.out.println("The cache size, block size and number of blocks must be non-negative and nonzero");
			return false;
		}
		
		if(associativityBit < 0 || associativityBit > (cacheSizeBits - blockSizeBits))
		{
			associativityBit = (cacheSizeBits - blockSizeBits);
		}
		
		if(!file.isFile())
		{
			System.out.println("The file provided is not a valid file.");
			return false;
		}
		
		if((blockSize * numBlocks) != cacheSize || (blockSize * numBlocks) > cacheSize)
		{
			System.out.println("The cache and block size are incorrect");
			return false;
		}
		
		
		if(traceVal.equals("off"))
		{
			trace = false;
		}
		else if (traceVal.equals("on"))
		{
			trace = true;
		}
		else
		{
			System.out.println("Invalid trace argument given");
			return false;
		}
		
		if(replaceVal.equals("fifo"))
		{
			replacePolicy = "fifo";
		}
		else if(replaceVal.equals("lru"))
		{
			replacePolicy = "lru";
		}
		else 
		{
			System.out.println("Invalid replacement policy argument given");
			return false;
		}
		
		return true;
	}

	public static void main(String[] args) 
	{
		Scanner in = null;
		
		if(verifyInput(args))
		{
			Cache simulatedCache = new Cache(cacheSizeBits, blockSizeBits, associativityBit, replacePolicy, trace);
			
			try 
			{
				in = new Scanner(new File(args[5]));
				
			}
			catch (FileNotFoundException exception)
			{
				System.out.println("There was a problem opening the file");
			}
			
			while(in.hasNextLine())
			{
				String address = in.nextLine();
				if(address.equals(""))
				{
					continue;
				}
				String strippedAddress = address.trim();
				simulatedCache.checkCache(strippedAddress);
					
			}
			
			System.out.println("Andrew S. Clark");
			for(String a : args)
			{
				System.out.print(a + " ");
			}
			System.out.println();
			System.out.println("memory accesses: " + simulatedCache.getNumAccesses());
			System.out.println("hits: " + simulatedCache.getNumHits());
			System.out.println("misses: " + simulatedCache.getNumMisses());
			System.out.println("miss ratio: " + simulatedCache.getMissRatio());
		}

	}

}
