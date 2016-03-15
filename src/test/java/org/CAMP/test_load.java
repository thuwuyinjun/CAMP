package org.CAMP;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;

public class test_load {

	private static UniformDataGenerator uniformDataGeneror=new UniformDataGenerator();
	
	public static double [] density = {0.00001,0.005,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.1};
	public static int [] max_num = { 2000000};
	
	public static void main(String[] args) throws IOException
	{
		
	int dencnt = 0;
	int maxcnt = 0;
	for(dencnt = 0; dencnt < density.length; dencnt++)
	{
	for(maxcnt = 0; maxcnt < max_num.length; maxcnt++)
	{
	
	 byte [] array = new byte[2];
	 array[0] = 6;
	 array[1] = 10;
	 BitSet s = new BitSet();
	 s = BitSet.valueOf(array);
		int integer_count = (int) (max_num[maxcnt] * density[dencnt]);
		int [] data = uniformDataGeneror.generateUniform(integer_count,max_num[maxcnt]);
		BitSet set = new BitSet();
		for(int i = 0 ; i<data.length; i++)
		{
			set.set(data[i]);
		}

		index_set result_id= new index_set();
		result_id = create_index_set.bitmapof(data, max_num[maxcnt]);
//		index expect = create_index.build(set, max_num[maxcnt]);
		
		
		CAMP_IO_set.output(result_id, "1.txt");
		
		index_set  id  =  CAMP_IO_set.input("1.txt");
		
		
		
		//check input-output
		
		for(int p =0; p < result_id.size; p++)
		{
			index1 temp= result_id.id[p];
			index1 ep = id.id[p];
		for(int j = 0; j<temp.size; j++)
		{
			byte [] b1 = temp.set[2 * j + 1].toByteArray() ;
			byte [] b2 = ep.set[2 * j + 1].toByteArray() ;
			for(int k = 0; k<b1.length; k++)
			{
				if(b1[k] != b2[k])
				{
					int y = 0;
					y++;
					Assert.assertEquals(true, false);
				}
			}
			
			b1 = temp.set[2 * j].toByteArray();
			b2 = ep.set[2 * j ].toByteArray();
			
			for(int k = 0; k<b1.length; k++)
			{
				if(b1[k] != b2[k])
				{
					int y = 0;
					y++;
					Assert.assertEquals(true, false);
				}
			}
			
			if( temp.count[j] != ep.count[j])
			{
				int y = 0;
				y++;
				Assert.assertEquals(true, false);
			}
			
		}
	}
		
		
		BitSet alpha = new BitSet();
		BitSet beta = new BitSet();
		ArrayList<Integer> arr = new ArrayList<Integer>();
		int num = 0;
		for(int p =0; p < result_id.size; p++)
		{
			num = 0;
			index1 temp= result_id.id[p];
			index1 ep = id.id[p];
		for(int i = 0; i<temp.size; i++)
		{
			alpha =temp.set[2 * i];
			beta = temp.set[2 * i + 1];
			int pos = 0;
			while(true)
			{
				pos = alpha.nextSetBit(pos);
				
				if(pos < 0)
					break;
				for(int j = 0; j < create_index_set.block; j++)
				{
					int dt = (num + pos) + j * create_index_set.interv  + create_index_set.block_size * p;
					if(Arrays.binarySearch(data, dt) < 0)
					{
						int y = 0;
						y++;
						Assert.assertEquals(true, false);
					}
					arr.add(dt);
				}
				pos ++;
			}
			num += temp.count[i];
			pos = 0;
			if(beta!= null)
			{
				while(true)
				{
					pos = beta.nextSetBit(pos);
					
					if(pos < 0)
						break;
					int dt = pos * create_index_set.interv + num + create_index_set.block_size * p;
					arr.add(dt);
					if(Arrays.binarySearch(data, dt) < 0)
					{
						int y = 0;
						y++;
						Assert.assertEquals(true, false);
					}
					pos ++;
				}
				num +=  1;
			}
			
			
		}
		}
		
		result_id = create_index_set.bitmapof(data, max_num[maxcnt]);
		
		BitSet tp_set = new BitSet();
		for(int t = 0; t<data.length; t++)
		{
			if(data[t] >= create_index_set.block_size && data[t] < create_index_set.block_size * 2)
				tp_set.set(data[t] - create_index_set.block_size);
		}
		index1 exp = create_index_set.build(tp_set,max_num[maxcnt]);
		
		int [] list = new int[arr.size()];
		int c = 0;
		
		for(int x : arr)
		{
			list[c++] = x;
		}
		Arrays.sort(list);
		
//		if(!Arrays.equals(data, list))
//		{
//			int y = 0;
//			y++;
//			Assert.assertEquals(true, false);
//		}
		
		
		for(int j = 0;j<data.length;j++)
		{
			if(data[j] != list[j])
			{
				int y = 0;
				y++;
				
//				result_id = create_index.build(set, max_num[maxcnt]);
//				for(int i = 0; i<id.size; i++)
//				{
//					alpha =id.set[2 * i];
//					beta = id.set[2 * i + 1];
//					int pos = 0;
//					while(true)
//					{
//						pos = alpha.nextSetBit(pos);
//						
//						if(pos < 0)
//							break;
//						for(int k = 0; k < create_index.block; k++)
//						{
//							int dt = (num + pos) + k * create_index.interv ;
//							if(Arrays.binarySearch(data, dt) < 0)
//							{
//								y++;
//								Assert.assertEquals(true, false);
//							}
//							arr.add(dt);
//						}
//						pos ++;
//					}
//					num += id.count[i];
//					pos = 0;
//					if(beta!= null)
//					{
//						while(true)
//						{
//							pos = beta.nextSetBit(pos);
//							
//							if(pos < 0)
//								break;
//							int dt = pos * create_index.interv + num;
//							arr.add(dt);
////							if(Arrays.binarySearch(data, dt) < 0)
////							{
////								int y = 0;
////								y++;
////								Assert.assertEquals(true, false);
////							}
//							pos ++;
//						}
//						num +=  1;
//					}
//					
//					
//				}
				Assert.assertEquals(true, false);
			}
		}
		
	}
	}
	}
}
