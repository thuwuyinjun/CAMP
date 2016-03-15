package org.CAMP;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Arrays;
public class create_index_set {
	
	public static int block;
	public static int interv;
	public static int block_size = 1000000;
	
	public static index_set bitmapof(int [] data, int max)
	{
		interv = (int)(Math.sqrt(block_size) * 0.1);
		block = (int)Math.ceil((double)block_size/interv);
		BitSet set = new BitSet ();
		int i = 0;
		int k = 1;
		index_set result = new index_set();
		result.num = block;
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		while(i< data.length)
		{
			while(data[i] >=  k * block_size)
			{
				index1 temp = build(set, block_size);
				
				result.add(temp);
				k++;
				set = new BitSet();
				list.clear();
				if(data[i] <  k * block_size)
				{
					break;
				}
			}
			set.set(data[i] - (k-1) * block_size);
			list.add(data[i] - (k-1) * block_size);
			i++;
		}
		
		index1 temp = build(set, block_size);
		
		
		result.add(temp);
		k++;
		set = new BitSet();
		return result;
	}
	public static index1 build(BitSet set, int length)
	{
		interv = (int)(Math.sqrt(length) * 0.1);
		block = (int)Math.ceil((double)length/interv);
		int alphanum = interv;
		return build_index(set, alphanum, block, interv);
	}
	public static index1 build_index(BitSet set, int alphanum, int block, int interv)
	{
		index1 id = new index1();
		int i;
		int rest = 0;
		int size = 0;
		
		while(rest<interv)
		{
			BitSet alpha = new BitSet();
			BitSet beta = new BitSet();
			while(true)
			{
				alpha = set.get(rest, alphanum+rest);
				for(i = 1;i<block;i++)
				{
					if(set.get(i*interv+rest, i*interv+rest+alphanum).equals(alpha) == true)
					{
						continue;
					}
					else
					{
						alphanum--;
						break;
					}
				}
				if(i>=block)
				{
					for(i = 0;i<block;i++)
					{
						beta.set(i, set.get(i*interv+alphanum+rest));
					}
					break;
				}
			}	
			if(size >= id.count.length)
			{
				id.increase_size();
			}
			id.set[2 * size] = alpha;
			if(alphanum + 1 + rest > interv)
			{
				beta.clear();
				id.set[2 * size + 1] = beta;
			}
			else
			{
				id.set[2 * size + 1] = beta;
			}
			id.count[size]= alphanum;
			int temp = rest;
			rest = alphanum + 1 + rest;
			alphanum = interv - alphanum - 1 - temp;
			
			
			size ++;
		}
		id.size = size;
		return id;
	}
	}
