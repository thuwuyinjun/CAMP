package org.CAMP;
import java.util.ArrayList;
public class create_index {
	
	public static int block;
	public static int interv;
	
	
	public static index bitmapof(int [] data, int max)
	{
		BitSet set = new BitSet ();
		for(int i = 0; i<data.length; i++)
		{
			set.set(data[i]);
		}
		return build(set, max);
	}
	public static index build(BitSet set, int length)
	{
		interv = (int)(Math.sqrt(length) * 0.1);
		block = (int)Math.ceil((double)length/interv);
		int alphanum = interv;
		return build_index(set, alphanum, block, interv);
	}
	public static index build_index(BitSet set, int alphanum, int block, int interv)
	{
		index id = new index();
		id.num = block;
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
