package org.CAMP;
import java.util.ArrayList;
import java.util.Arrays;

import org.roaringbitmap.ArrayContainer;


public class index {
	public int num = 0;
	public int [] count;
	public BitSet[] set;
	public int size;
	index()
	{
		num = 0;
		size = 0;
		set = new BitSet[2];
		count = new int[1];
	}
	
	public void increase_size()
	{
		int newCapacity = count.length + 10;
        count = Arrays.copyOf(count, newCapacity);
        newCapacity = set.length + 20;
        set = Arrays.copyOf(set, newCapacity);
        
	}
}
