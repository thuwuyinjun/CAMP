package org.CAMP;
import java.util.ArrayList;
import java.util.Arrays;

public class index_set {
	public index1[]  id;
	public int size;
	public int num;
	public index_set()
	{
		id = new index1[1];
		size = 0;
	}
	public void increase_size()
	{
		int newCapacity = id.length + 10;
        id = Arrays.copyOf(id, newCapacity);
        
	}
	
	public void add(index1 temp)
	{
		if(size >= id.length)
		{
			increase_size();
		}
		id[size++] = temp;
	}
}
