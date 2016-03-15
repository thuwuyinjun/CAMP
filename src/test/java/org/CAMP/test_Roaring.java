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

import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.BitmapContainer;
import org.roaringbitmap.RoaringBitmap;

import junit.framework.Assert;

public class test_Roaring {
	private static UniformDataGenerator uniformDataGeneror=new UniformDataGenerator();
	
	public static double density =0.999;
	public static int max_num = 10000000;
	
	public static void main(String[] args) throws IOException
	{
		int integer_count = (int) (max_num * density);
		int [] data1 = uniformDataGeneror.generateUniform(integer_count,max_num);
		int [] data2 = uniformDataGeneror.generateUniform(integer_count,max_num);
		RoaringBitmap set1 = new RoaringBitmap();
		RoaringBitmap set2 = new RoaringBitmap();
		
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
		

		
		


		bw.close();
		fw.close();
		
		file = new File("data2.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		fw = new FileWriter(file.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		
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
		
		
		
		RoaringBitmap id1= new RoaringBitmap();
		RoaringBitmap id2= new RoaringBitmap();
		
		id1 = RoaringBitmap.bitmapOf(data1);
		id2 = RoaringBitmap.bitmapOf(data2);
			
		RoaringBitmap result_com = RoaringBitmap.and(id1, id2);
		
		RoaringBitmap expect = RoaringBitmap.bitmapOf(expected_result);
		
		int size = result_com.highLowContainer.size;
		for(int i  =0; i<size; i++)
		{
			if(result_com.highLowContainer.keys[i]!= expect.highLowContainer.keys[i])
			{
				Assert.assertEquals(true, false);
			}
			else
			{
				if(result_com.highLowContainer.values[i] instanceof ArrayContainer)
				{
					if(result_com.highLowContainer.values[i] instanceof ArrayContainer)
					{
						ArrayContainer container = (ArrayContainer)expect.highLowContainer.values[i];
						ArrayContainer res_container = (ArrayContainer)result_com.highLowContainer.values[i];
						int card = container.cardinality;
						for(int j = 0; j<card; j++)
						{
							if(container.content[j] != res_container.content[j])
							{
								Assert.assertEquals(false, true);
							}
						}
					}
					else
					{
						Assert.assertEquals(true, false);
					}
				}
				else
				{
					if(result_com.highLowContainer.values[i] instanceof BitmapContainer)
					{
						BitmapContainer container = (BitmapContainer)expect.highLowContainer.values[i];
						BitmapContainer res_container = (BitmapContainer)result_com.highLowContainer.values[i];
						int card = container.bitmap.length;
						for(int j = 0; j<card; j++)
						{
							if(container.bitmap[j] != res_container.bitmap[j])
							{
								Assert.assertEquals(false, true);
							}
						}
					}
					else
					{
						Assert.assertEquals(true, false);
					}
				}
			}
		}
		
	}
}
