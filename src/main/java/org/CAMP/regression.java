package org.CAMP;
import java.util.BitSet;
import java.util.ArrayList;

public class regression {
	public void regress(BitSet set)
	{
		int index = 0 ;
		int position = set.nextSetBit(index);
		ArrayList<Integer> list = new ArrayList<Integer>();
		ArrayList<Integer> err = new ArrayList<Integer>();
		int max = 0;
		while(position != -1)
		{
			list.add(position);
			position = set.nextSetBit(position + 1);
		}
		int sum_x = 0;
		int sum_y = 0;
		int cross = 0;
		int square_x = 0;
		int square_y = 0;
		double average_x, average_y;
		for(int i = 0 ;i<list.size();i++)
		{
			sum_y += list.get(i);
			sum_x += i;
			cross += list.get(i)*i;
			square_x += i*i;
			square_y += list.get(i)*list.get(i);
		}
		average_y = sum_y/list.size();
		average_x = sum_x/list.size();
		int a1 = 0;
		int a2 = 0;
		for(int i = 0 ;i<list.size();i++)
		{
			a1 += (i - average_x)*(list.get(i)-average_y);
			a2 += (i - average_x)*(i - average_x);
		}
		int a, b;
		a = a1/a2;
		b = (int)(average_y - a * average_x);
		for(int i = 0;i<list.size();i++)
		{
			err.add((int)(a*i + b - list.get(i)));
			if(Math.abs(err.get(i)) > max) max = err.get(i);
		}
		int m = 0;
		m = 1;
	}
}
