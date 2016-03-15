package org.CAMP;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.*;

public class CAMP_IO {

	public static double THRESHOLD = 0.04;
	public  static void output(index set, String str) throws IOException
	{
		int num = set.size;
		String alpha = new String();
		String beta = new String();
		String tmp = new String();

		File myFile=new File(str);
        if( ! myFile. exists() )
        {     
        	myFile.createNewFile();
        }
		 try {
             FileOutputStream out=new FileOutputStream(str);
             PrintStream p=new PrintStream(out);
             String s = new String ();
             s += set.num;
             p.println(s);
             s = new String ();
             s += set.size;
             p.println(s);
             
             int length_alpha = 0;
             int length_beta = 0;
             int bit = 0;
             for(int i=0;i<num;i++)
             {
            	 static_combination st_com = static_combination.convert(set.set[2 * i], set.set[2 * i + 1], set.count[i] ,set.num);
            	 
            	 length_alpha = st_com.alphanum.size();
            	 
            	 boolean special = false;
            	 for(int j = 0;j<length_alpha; j++)
            	 {
            		 if(set.set[2 * i].get(j))
            			 bit = 1;
            		 else
            			 bit = 0;
            		 if(j == 0)
            		 {
            			 alpha += st_com.alphanum.get(j).toString();
            			 if(bit == 1)
            			 {
            				 alpha = "0 " + alpha; 
            				 special = true;
            			 }
            		 }
            		 else
            			 alpha += " " + st_com.alphanum.get(j).toString();
            	 }
            	 
            	 if(special == true)
            		 length_alpha += 1;
            	 if(length_alpha % 2 == 1)
            	 {
            		 alpha += " " + 0; 
            	 }
            	 
            	 BitSet temp = set.set[2 * i + 1];
            	 
            	 if(temp.cardinality() * 1.0/set.num < THRESHOLD)
            	 {
            		 length_beta = st_com.betanum.size();
                	 for(int j = 0; j<length_beta; j++)
                	 {
                		 if(j == 0)
                		 	beta += "0" + "," +  st_com.betanum.get(j);
                		 else
                			beta += " " + st_com.betanum.get(j);
                	 }
            	 }
            	 
            	 else
            	 {
            		 long [] tp_arr = temp.toLongArray();
                	 boolean start = false;
                	 
                	 for(int j = 0; j<tp_arr.length; j++)
                	 {
                		 {
                			 if(!start)
                     		 {
                				 beta += "1" + "," + tp_arr[j];
                				 start = true;
                     		 }
                     		 else
                     			beta += " "  + tp_arr[j];
                		 }
                		 
                	 }
            	 }
            	 
            	
            	 tmp = alpha + "," + beta;
            	 p.println(tmp);
            	 alpha = new String();
            	 beta = new String();
             }
         } catch (FileNotFoundException e) 
         	{
             e.printStackTrace();
         	}
	}
	
	public  static void output_roaring(RoaringBitmap set, String str) throws IOException
	{
		int length = set.highLowContainer.size;
		String type;
		String output;
		File myFile=new File(str);
        if( ! myFile. exists() )
        {     
        	myFile.createNewFile();
        }
		 try {
			 FileOutputStream out=new FileOutputStream(str);
             PrintStream p=new PrintStream(out);
             p.println(length);
		for(int i = 0; i<length; i++)
		{
			if(set.highLowContainer.values[i] instanceof org.roaringbitmap.ArrayContainer)
			{
				type = "0";
				org.roaringbitmap.ArrayContainer container = (org.roaringbitmap.ArrayContainer)set.highLowContainer.values[i];
				int len = container.cardinality;
				output = type + " " + String.valueOf(set.highLowContainer.keys[i]) + "," + len + ",";
				for(int j = 0; j<len - 1; j++)
				{
					output = output + String.valueOf(container.content[j]) + " ";
				}
				output = output + String.valueOf(container.content[len - 1]);
				p.println(output);
			}
			else
			{
				type = "1";
				org.roaringbitmap.BitmapContainer container = (org.roaringbitmap.BitmapContainer)set.highLowContainer.values[i];
				output = type + " " + String.valueOf(set.highLowContainer.keys[i]) + "," + container.cardinality + ",";
				int len = container.bitmap.length;
				for(int j = 0; j<len - 1; j++)
				{
					output = output + String.valueOf(container.bitmap[j]) + " ";
				}
				output = output + String.valueOf(container.bitmap[len - 1]);
				p.println(output);
			}
		}
		 } catch (FileNotFoundException e) 
      	{
          e.printStackTrace();
      	}
	}
	
	public static index  input(String string) throws IOException
	{
		FileReader fr = new FileReader(string); 
		BufferedReader br = new BufferedReader(fr); 
        BitSet bitset;
        int block = 0;
        String s = br.readLine();
        block = Integer.valueOf(s);
        s = br.readLine();
        int size = Integer.valueOf(s);
        s = br.readLine();
        BitSet[] bitseq = new BitSet[2 * size];
        int [] count = new int[size];
        
        int oldnum = 0;
        int num = 0;
        int seq = 0;
        String[] str;
    	String[] beta_str;
    	String[] alpha_str;
    	 index id ;
    	 int len;
    	 int r = 0;
    	 int k = 0;
    	 long [] array;
        while(s!= null)
        {
	        	
	        	str = s.split(",");
	        	num = 0;
        		bitset = new BitSet();
	        	if(str[0].length()!=0)
	        	{
	        		alpha_str = str[0].split(" ");
	        		len = alpha_str.length;
	        		for(r = 0 ;r<len; r+= 2)
	        		{
	        			num += Integer.valueOf(alpha_str[r]);
	        			
	        			oldnum = num;
	        			num += Integer.valueOf(alpha_str[r+1]);
	        			for(k = oldnum; k<num; k++)
    						bitset.set(k, true);
	        		}
	        	}
	        
	        	bitseq[2 * seq] = bitset;
	        	count[seq] = num;
	        	num = 0;
        		
	        	if(str.length >= 2)
	        	{
	        	if(Integer.valueOf(str[1]) == 1)
	        	{
	        	array = new long [(block + 63)/64];
	        	
	        		
		        		beta_str = str[2].split(" ");
		        		for(k = 0 ;k< beta_str.length;k++)
		        		{
		        			array[k] = Long.valueOf(beta_str[k]);
		        		}
		        		bitset = new BitSet(array);
	        		
	        	}
	        	else
	        	{
	        		beta_str = str[2].split(" ");
	        		bitset = new BitSet(Integer.valueOf(beta_str[beta_str.length - 1]));
	        		for(k = 0 ;k< beta_str.length;k++)
	        		{
	        			bitset.set2(Integer.valueOf(beta_str[k]));
	        		}
	        	}
	        	}
	        	bitseq[2 * seq + 1] = bitset;
	        	seq ++;
        	s = br.readLine();
        }
        
        id = new index();
        id.set = bitseq;
        id.count = count;
        id.num = block;
        id.size = size;
        fr.close();
        br.close();
        return id;
	}
	
	public static RoaringBitmap  input_roaring(String string) throws IOException
	{
		FileReader fr = new FileReader(string); 
		BufferedReader br = new BufferedReader(fr); 
        BitSet bitset = new BitSet();
        int number = 0;
        String s = br.readLine();
        number = Integer.valueOf(s);
        String [] str;
        String [] str1;
        String [] str2;
        RoaringBitmap bt = new RoaringBitmap();
        bt.highLowContainer.values = new Container[number];
        bt.highLowContainer.keys = new short[number];
        bt.highLowContainer.size = number;
        ArrayContainer arr = new ArrayContainer();
        BitmapContainer bit = new BitmapContainer ();
        int cardinality = 0;
        for(int i = 0; i<number; i++)
        {
            s = br.readLine();
            str = s.split(",");
            str1 = str[0].split(" ");
            cardinality = Integer.valueOf(str[1]);
            str2 = str[2].split(" ");
            
            if(Integer.valueOf(str1[0]) == 0)
            {
            	arr = new ArrayContainer();
            	bt.highLowContainer.keys[i] = Short.valueOf(str1[1]);
            	arr.cardinality = cardinality;
            	arr.content = new short[str2.length];
            	for(int j = 0; j<cardinality; j++)
            	{
            		arr.content[j] = Short.valueOf(str2[j]);
            	}
            	bt.highLowContainer.values[i] = arr;
            }
            else
            {
            	bit = new BitmapContainer();
            	bt.highLowContainer.keys[i] = Short.valueOf(str1[1]);
            	bit.cardinality = cardinality;
            	bit.bitmap = new long[str2.length];
            	for(int j = 0; j<str2.length; j++)
            	{
            		bit.bitmap[j] = Long.valueOf(str2[j]);
            	}
            	bt.highLowContainer.values[i] = bit;
            }
        }
        return bt;
	}

	public  static COMPAXSet in_compax(String fin) throws IOException
	{
		
		FileReader fr = new FileReader(fin); 
		BufferedReader br = new BufferedReader(fr); 
		String s = br.readLine();
		int []seq = null;
		ArrayList<Integer> num = new ArrayList<Integer>();
		while(s!=null)
		{
			num.add(Integer.valueOf(s).intValue());

			s = br.readLine();
		}
		seq = new int[num.size()];
		for(int i = 0 ;i<num.size();i++)
		{
			seq[i] = (int)num.get(i);
		}
		IntBuffer buffer = IntBuffer.wrap(seq);
		COMPAXSet set = new COMPAXSet(buffer,true);
		fr.close();
		br.close();
		return set;
	}
	
	public static ImmutableConciseSet in_concise(String fin) throws IOException
	{
		FileReader fr = new FileReader(fin); 
		BufferedReader br = new BufferedReader(fr); 
		String s = br.readLine();
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		int num;
		while(s!=null)
		{
			num = Integer.valueOf(s).intValue();
			list.add(num);
			s = br.readLine();
		}
		int [] arr = new int[list.size()];
		int c = 0;
		for(int x:list)
		{
			arr[c++] = x;
		}
		ImmutableConciseSet set = new ImmutableConciseSet(IntBuffer.wrap(arr));
		fr.close();
		br.close();
		return set;
	}
	
	public static WAHSet in_WAH(String fin) throws IOException
	{
		FileReader fr = new FileReader(fin); 
		BufferedReader br = new BufferedReader(fr); 
		String s = br.readLine();
		ArrayList<Integer> list = new ArrayList<Integer>();
		int num;
		while(s!=null)
		{
			num = Integer.valueOf(s).intValue();
			list.add(num);
			s = br.readLine();
		}
		
		int [] arr = new int[list.size()];
		int c = 0;
		for(int x:list)
		{
			arr[c++] = x;
		}
		
		WAHSet set = new WAHSet(IntBuffer.wrap(arr));
		fr.close();
		br.close();
		
		return set;
	}

	public static void out_concise(ImmutableConciseSet set, String fout) throws IOException
	{
		int [] integer = set.getWords();
		File myFile=new File(fout);
        if( ! myFile. exists() )
        {     
        	myFile.createNewFile();
        }
		if(integer != null)
		{
		 try {
            FileOutputStream out=new FileOutputStream(fout);
            PrintStream p=new PrintStream(out);
            String s = new String ();
            for(int i = 0 ;i<set.getWords().length;i++)
    		 {
    			p.println(integer[i]);
    		 }

        } catch (FileNotFoundException e) 
        	{
            e.printStackTrace();
        	}
		}
	}
	
	public static void out_WAH(WAHSet set, String fout) throws IOException
	{
		int [] integer = set.getWords();
		File myFile=new File(fout);
        if( ! myFile. exists() )
        {     
        	myFile.createNewFile();
        }
		if(integer != null)
		{
		 try {
            FileOutputStream out=new FileOutputStream(fout);
            PrintStream p=new PrintStream(out);
            String s = new String ();
            for(int i = 0 ;i<set.getWords().length;i++)
    		 {
    			p.println(integer[i]);
    		 }

        } catch (FileNotFoundException e) 
        	{
            e.printStackTrace();
        	}
		}
	}

	public  static void out_compax(COMPAXSet set, String fout) throws IOException
	{
		int [] integer = set.getWords();
		File myFile=new File(fout);
        if( ! myFile. exists() )
        {     
        	myFile.createNewFile();
        }
		if(integer != null)
		{
		 try {
             FileOutputStream out=new FileOutputStream(fout);
             PrintStream p=new PrintStream(out);
             String s = new String ();
             for(int i = 0 ;i<set.getWords().length;i++)
     		 {
     			p.println(integer[i]);
     		 }

         } catch (FileNotFoundException e) 
         	{
             e.printStackTrace();
         	}
		}
	}
	
}
