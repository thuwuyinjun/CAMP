package org.CAMP;
import java.util.ArrayList;


public class static_combination {
	public BitSet alpha;
	public ArrayList<Integer> alphanum;
	public ArrayList<Integer> betanum;
	static_combination()
	{
		alpha = new BitSet();
		alphanum = new ArrayList<Integer>();
		betanum = new ArrayList<Integer>();
	}
	public  static  static_combination convert(BitSet a, BitSet b, int a_num, int max_num)
	{
		static_combination st_com = new static_combination();
		BitSet alpha = new BitSet();
		ArrayList<Integer> alphanum= new ArrayList<Integer>();
		ArrayList<Integer> betanum = new ArrayList<Integer>();
		boolean crt0 = false;
        boolean crt1 = false;
        boolean start = false;
        boolean first = false;
        int num0 = 0;
        int num1 = 0;
        int i = 0;
   	 for(int j=0;j<a_num;j++)
	 {
		 if(a.get(j)== true)
		 {
			 if(start == false)
			 {
				 crt1 = true;
				 num1 ++;
				 start = true;
				 crt0 = false;
			 }
			 else
			 {
				 if(crt1 == true)
				 {
					 num1++;
				 }
				 else
				 {
					 if(!first)
					 {
						 alpha.set(i,0);
						 alphanum.add(num0);
						 i++;
    					 first = true;
					 }
					 else
					 {
						 alpha.set(i,0);
						 alphanum.add(num0);
						 i++;
    					 first = true;
					 }
					 num0 = 0;
					 num1 ++;
				 }
			 }
		 }
		 else
		 {
			 if(start == false)
			 {
				 crt0 = true;
				 num0 ++;
				 start = true;
				 crt1 = false;
			 }
			 else
			 {
				 if(crt0 == true)
				 {
					 num0++;
				 }
				 else
				 {
					 if(!first)
					 {
						 alpha.set(i,1);
						 alphanum.add(num1);
						 i++;
    					 first = true;
					 }
					 else
					 {
						 alpha.set(i,1);
						 alphanum.add(num1);
						 i++;
    					 first = true;
					 }
					 num1 = 0;
					 num0 ++;
				 }
			 }
		 }
	 }
   	if(crt0 == true)
	 {
		 if(!first)
		 {
			 alpha.set(i,0);
			 alphanum.add(num0);
			 first = true;
		 }
		 else
		 {
			 alpha.set(i,0);
			 alphanum.add(num0);
			 first = true;
		 }
	 }
	 else
	 {	 
		 if(crt1 == true)
   	 {
			 if(!first)
			 {
				 alpha.set(i,1);
				 alphanum.add(num1);
				 first = true;
			 }
			 else
			 {
				 alpha.set(i,1);
				 alphanum.add(num1);
				 first = true;
			 }
   	 }
	 }
	 for(int j=0;j<max_num;j++)
	 {
		 if(b != null && b.get(j)== true)
		 {
			 betanum.add(j);
		 }
	 }
	 st_com.alpha = alpha;
	 st_com.alphanum = alphanum;
	 st_com.betanum = betanum;
	 return st_com;
	}

	
}
