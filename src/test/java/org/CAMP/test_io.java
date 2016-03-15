package org.CAMP;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;

public class test_io{
	private static UniformDataGenerator uniformDataGeneror=new UniformDataGenerator();
	
	public static double density = 0.001;
	public static int max_num = 10000000;
	public static void main(String[] args) throws IOException
	{
		int integer_count = (int) (max_num * density);
		int [] data1 = uniformDataGeneror.generateUniform(integer_count,max_num);
		int [] data2 = uniformDataGeneror.generateUniform(integer_count,max_num);
		BitSet set1 = new BitSet();
		BitSet set2 = new BitSet();
		
		ArrayList<Integer> result_list = new ArrayList<Integer>();
		
		
		for(int i  = 0; i<data1.length; i++)
		{
			if(Arrays.binarySearch(data2, data1[i]) >= 0)
			{
				result_list.add(data1[i]);
			}
		}
		int [] expected_result = new int[result_list.size()];
		int r = 0;
		for(int x : result_list)
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
//	        set1.set(y);
//	        max1 = y;
//	    }
//		bufferedReader.close();
//		
//		bufferedReader = new BufferedReader(new FileReader("data2.txt"));
//		ArrayList<Integer> t1 = new ArrayList<Integer>();
//		ArrayList<Integer> t2 = new ArrayList<Integer>();
//		while ((line = bufferedReader.readLine()) != null)
//	    {
//	        int y = (Integer.parseInt(line));
//	        set2.set(y);
//	        max2 = y;
//	    }
//
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
		
		
		
		
		index id1 = create_index.bitmapof(data1, max_num);
		index id2 = create_index.bitmapof(data2, max_num);
		CAMP_IO.output(id1, "1.txt");
		CAMP_IO.output(id2, "2.txt");
		
		index new_id1 = CAMP_IO.input("1.txt");
		index new_id2 = CAMP_IO.input("2.txt");
				
			
		index result_com = operation.intersection(new_id1, new_id2);
		
		
		BitSet alpha = new BitSet();
		BitSet beta = new BitSet();
		ArrayList<Integer> arr = new ArrayList<Integer>();
		int num = 0;
		index temp = result_com;
		for(int i = 0; i<temp.size; i++)
		{
			alpha = temp.set[2 * i];
			beta = temp.set[2 * i+1];
			int pos = 0;
			while(true)
			{
				pos = alpha.nextSetBit(pos);
				
				if(pos < 0)
					break;
				for(int j = 0; j < create_index.block; j++)
				{
					arr.add((num + pos) + j * create_index.interv );
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
					arr.add(pos * create_index.interv + num);
					pos ++;
				}
				num +=  1;
			}
			
		
		}
		int [] list = new int[arr.size()];
		int c = 0;
		
		for(int x : arr)
		{
			list[c++] = x;
		}
		Arrays.sort(list);
		
		if(!Arrays.equals(list, expected_result))
		{
			int y = 0;
			y++;
			Assert.assertEquals(true, false);
		}
		
		
		BitSet result_set = new BitSet();
		for(int i = 0; i<expected_result.length; i++)
		{
			result_set.set(expected_result[i]);
		}
		index1 result_index = new index1();
		result_index = create_index_set.build(result_set, max_num);
		
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
