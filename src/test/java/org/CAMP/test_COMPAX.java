package org.CAMP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import junit.framework.Assert;

public class test_COMPAX {
	private static UniformDataGenerator uniformDataGeneror=new UniformDataGenerator();
	
	public static double density =0.1;
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
//	        list1.add(y);
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
//	        list2.add(y);
//	        max2 = y;
//	    }
//		
//		int []data1 = new int[list1.size()];
//		int []data2 = new int[list2.size()];
//		for(int i = 0; i<data1.length; i++)
//		{
//			data1[i] = list1.get(i);
//		}
//		
//		for(int i = 0; i<data2.length; i++)
//		{
//			data2[i] = list2.get(i);
//		}
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
		
		
		
		COMPAXSet id1= new COMPAXSet();
		COMPAXSet id2= new COMPAXSet();
		
		id1 = COMPAXSet.bitmapof(data1);
		id2 = COMPAXSet.bitmapof(data2);
		int [] dt1 = id1.getSeq(0);
		for(int k = 0; k<dt1.length; k++)
		{
			if(dt1[k] != data1[k])
			{
				int h = 0;
				h++;
			}
		}
		
		if(!Arrays.equals(data1, dt1))
		{
			int l = 0;
			l++;
			Assert.assertEquals(false, true);
		}
		
			
		COMPAXSet result_com = COMPAXSet.intersection(id1, id2);
		
		COMPAXSet expect = COMPAXSet.bitmapof(expected_result);
		
		int []res1 = result_com.getSeq(0);
		int [] res2 = expect.getSeq(0);
		
		for(int i = 0; i<res2.length;i++)
		{
			int w1 = res1[i];
			int w2 = res2[i];
			if(w1 != w2)
			{
				int y = 0;
				y++;
				for(int j = 0; j<result_com.getWords().length; j++)
				{
					if(result_com.getWords()[j] != expect.getWords()[j])
					{
						int k = 0;
						k = 0;
						COMPAXSet.intersection(id1, id2);
					}
				}
				
				
				result_com.getSeq(i);
				Assert.assertEquals(false, true);
			}
		}
		
	}
}
