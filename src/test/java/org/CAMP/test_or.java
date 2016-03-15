package org.CAMP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.Assert;

public class test_or {

	private static UniformDataGenerator uniformDataGeneror=new UniformDataGenerator();
	
	public static double density = 0.9999;
	public static int max_num = 5000000;
	
	public static void main(String[] args) throws IOException
	{
		BitSet set1 = new BitSet();
		BitSet set2 = new BitSet();
		int integer_count = (int) (max_num * density);
		int [] data1 = uniformDataGeneror.generateUniform(integer_count,max_num);
		int [] data2 = uniformDataGeneror.generateUniform(integer_count,max_num);

		
		ArrayList<Integer> result_list = new ArrayList<Integer>();
		
		HashSet<Integer> dt = new HashSet<Integer>();
		for(int i  = 0; i<data1.length; i++)
		{
			dt.add(data1[i]);
		}
		for(int i  = 0; i<data2.length; i++)
		{
			dt.add(data2[i]);
		}
		
		int [] expected_result = new int[dt.size()];
		int r = 0;
		for(int x : dt)
		{
			expected_result[r++] = x;
		}
		Arrays.sort(expected_result);
		
		
		File file = new File("data1.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		

		
		

		for(int i = 0 ; i<data1.length; i++)
		{
			set1.set(data1[i]);
			bw.write(String.valueOf(data1[i])+"\r");
		}
		bw.close();
		fw.close();
		
		file = new File("data2.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		fw = new FileWriter(file.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		
		for(int i = 0 ; i<data2.length; i++)
		{
			set2.set(data2[i]);
			bw.write(String.valueOf(data2[i])+"\r");
		}
		bw.close();
		fw.close();
		
		file = new File("expected.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		fw = new FileWriter(file.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		
		for(int x:expected_result)
		{
			bw.write(String.valueOf(x)+"\r");
		}
		
		int max1 = 0;
		int max2 = 0;
		max1 = data1[data1.length - 1];
		max2 = data2[data2.length - 1];
		bw.close();
		fw.close();
		
		
//		ArrayList<Integer> list1 = new ArrayList<Integer>();
//		ArrayList<Integer> list2 = new ArrayList<Integer>();
//		ArrayList<Integer> r = new ArrayList<Integer>();
//		String line = new String();
//		
//		int max1 = 0;
//		int max2 = 0;
//		BufferedReader bufferedReader = new BufferedReader(new FileReader("data1.txt"));
//		while ((line = bufferedReader.readLine()) != null)
//	    {
//	        int y = (Integer.parseInt(line));
//	        list1.add(y);
//	        set1.set(y);
//	        max1 = y;
//	    }
//		
//		int [] data1 = new int[list1.size()];
//		for(int j = 0; j<data1.length; j++)
//		{
//			data1[j] = list1.get(j);
//		}
//		bufferedReader.close();
//		
//		bufferedReader = new BufferedReader(new FileReader("data2.txt"));
//		ArrayList<Integer> t1 = new ArrayList<Integer>();
//		ArrayList<Integer> t2 = new ArrayList<Integer>();
//		while ((line = bufferedReader.readLine()) != null)
//	    {
//	        int y = (Integer.parseInt(line));
//	        list2.add(y);
//	        set2.set(y);
//	        max2 = y;
//	    }
//		int [] data2 = new int[list2.size()];
//		for(int j = 0; j<data2.length; j++)
//		{
//			data2[j] = list2.get(j);
//		}
//		bufferedReader.close();
//		
//		bufferedReader = new BufferedReader(new FileReader("expected.txt"));
//		while ((line = bufferedReader.readLine()) != null)
//	    {
//	        int y = (Integer.parseInt(line));
//	        r.add(y);
//	    }
//		
//		int []expected_result = new int[r.size()];
//		for(int y = 0; y<r.size(); y++)
//		{
//			expected_result[y] = r.get(y);
//		}
//		
//		bufferedReader.close();
		
		
		
		index_set id1= new index_set();
		index_set id2= new index_set();
		
		id1 = create_index_set.bitmapof(data1, max_num);
		id2 = create_index_set.bitmapof(data2, max_num);
		
			
		index_set result_com = operation_set.union(id1, id2);
		
		
		BitSet alpha = new BitSet();
		BitSet beta = new BitSet();
		ArrayList<Integer> arr = new ArrayList<Integer>();
		int num = 0;
		
		for(int p = 0 ; p<result_com.size; p++)
		{
			num = 0;
			index1 temp = result_com.id[p];
		for(int i = 0; i<temp.size; i++)
		{
			alpha = temp.set[2 * i];
			beta = temp.set[2 * i+ 1];
			int pos = 0;
			while(true)
			{
				pos = alpha.nextSetBit(pos);
				
				if(pos < 0)
					break;
				for(int j = 0; j < create_index_set.block; j++)
				{
					arr.add((num + pos) + j * create_index_set.interv  + p*create_index_set.block_size);
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
					arr.add(pos * create_index_set.interv + num+ p*create_index_set.block_size);
					pos ++;
				}
				num +=  1;
			}
			
		}
		}
		int [] list = new int[arr.size()];
		int c = 0;
		
		for(int x : arr)
		{
			list[c++] = x;
		}
		Arrays.sort(list);
		
		
		BitSet result_set = new BitSet();
		for(int i = 0; i<expected_result.length; i++)
		{
			result_set.set(expected_result[i]);
		}
		index1 result_index = new index1();
		result_index = create_index_set.build(result_set, max_num);
		
//		if(!Arrays.equals(expected_result, list))
//		{
//			Assert.assertEquals(true, false);
//		}
		
		for(int i = 0; i<list.length; i++)
		{
			if(list[i] != expected_result[i])
			{
				
				int y = 0;
				y++;
				Assert.assertEquals(true, false);
			}
		}
		
	}
}
