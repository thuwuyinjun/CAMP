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

public class CAMP_IO_set {

	public static double THRESHOLD = 0.04;
	public  static void output(index_set set, String str) throws IOException
	{
		
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
             int size  = set.size;
             s = new String();
             s += String.valueOf(size);
             p.println(s);
             
             for(int k = 0; k<size; k++)
             {
             s = new String ();
             s += set.id[k].size;
             p.println(s);
             
             int length_alpha = 0;
             int length_beta = 0;
             int bit = 0;
             int num = set.id[k].size;
             for(int i=0;i<num;i++)
             {
            	 static_combination st_com = static_combination.convert(set.id[k].set[2 * i], set.id[k].set[2 * i + 1], set.id[k].count[i] ,set.num);
            	 
            	 length_alpha = st_com.alphanum.size();
            	 
            	 boolean special = false;
            	 for(int j = 0;j<length_alpha; j++)
            	 {
            		 if(set.id[k].set[2 * i].get(j))
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
            	 
            	 BitSet temp = set.id[k].set[2 * i + 1];
            	 
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
		 }
         } catch (FileNotFoundException e) 
         	{
             e.printStackTrace();
         	}
	}
	
	
	public static index_set  input(String string) throws IOException
	{
		FileReader fr = new FileReader(string); 
		BufferedReader br = new BufferedReader(fr); 
        BitSet bitset;
        int block = 0;
        String s = br.readLine();
        block = Integer.valueOf(s);
        index_set result = new index_set();
        result.num = block;
        s = br.readLine();
        int block_num = Integer.valueOf(s);
            
        
        result.id = new index1[block_num];
        result.size = block_num;
        s = br.readLine();
        int h = 0;
        while(s!=null) 
        {
        	
        int size = Integer.valueOf(s);
        
        BitSet[] bitseq = new BitSet[2 * size];
        int [] count = new int[size];
        
        int oldnum = 0;
        int num = 0;
        int seq = 0;
        String[] str;
    	String[] beta_str;
    	String[] alpha_str;
    	 index1 id ;
    	 int len;
    	 int r = 0;
    	 int k = 0;
    	 long [] array;
        for(int l = 0; l<size;l++)
        {
        	    s = br.readLine();
	        	bitset = new BitSet();
	        	str = s.toString().split(",");
	        	num = 0;
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
        }
        id = new index1();
        id.set = bitseq;
        id.count = count;
        id.size = size;
        result.id[h++] = id ;
        s = br.readLine();
        }
        fr.close();
        br.close();
        return result;
	}

	public  static COMPAXSet in_compax(String fin) throws IOException
	{
		
		FileReader fr = new FileReader(fin); 
		BufferedReader br = new BufferedReader(fr); 
		String s = br.readLine();
		
		int size = Integer.valueOf(s).intValue();
		int [] seq = new int[size];
		s = br.readLine();
		int k = 0;
		while(s!=null)
		{
			seq[k++] = (Integer.valueOf(s).intValue());

			s = br.readLine();
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
		int size = Integer.valueOf(s);
		int [] arr = new int[size];
		s = br.readLine();
		int num = 0;
		while(s!=null)
		{
			arr[num++] = Integer.valueOf(s).intValue();
			s = br.readLine();
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
		int size = Integer.valueOf(s);
		int [] arr = new int[size];
		s = br.readLine();
		int num = 0;
		while(s!=null)
		{
			arr[num++] = Integer.valueOf(s).intValue();
			s = br.readLine();
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
            p.println(set.getWords().length);
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
            p.println(set.getWords().length);
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
             p.println(set.getWords().length);
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
