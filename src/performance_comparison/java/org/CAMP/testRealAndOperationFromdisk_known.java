package org.CAMP;

//import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import junit.framework.Assert;

import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.roaringbitmap.RoaringBitmap;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah32.EWAHCompressedBitmap32;

import net.sourceforge.sizeof.SizeOf;
import it.uniroma3.mat.extendedset.intset.ConciseSet;

/**
 * O. Kaser's benchmark over real data
 * 
 */
public class testRealAndOperationFromdisk_known {
        static final String AND = "AND";
        static final String OR = "OR";
        static final String XOR = "XOR";
   //     static final String[] ops = { AND, OR, XOR };
        static final String[] ops = {AND};
        static final String EWAH32 = "EWAH32";
        static final String EWAH64 = "EWAH64";
        static final String CONCISE = "CONCISE";
      
        static final String WAH = "WAH";
        static final String BITSET = "BITSET";
        static final String COMPAX = "COMPAX";
        
        static final String CAMP = "CAMP";
        
        static final String CAMP_block = "CAMP_block";
        
        static final String ROARING = "ROARING";
        
        static final int NTRIALS=100;
        
    //    static final String[] formats = { EWAH32, EWAH64, CONCISE, WAH, BITSET, ROARING };
        
        static final String[] formats = {CAMP_block,CAMP,COMPAX,CONCISE, WAH, ROARING};
        
        static final int [] maxs = {13581811, 13581811, 13581811, 13581811, 13581811, 13581811,13581811,13581811};

        static final int max_value = 13581811;
        
        static int junk = 0; // to fight optimizer.

        static long LONG_ENOUGH_NS = 1000L * 1000L * 1000L;

	public static int [] id_list_src_ip;

	public static int [] id_list_dst_ip;

	public static int [] id_list_src_port;

	public static int [] id_list_dst_port;
        
        public static int iteration_count = 100;
		private static int omit_time = 50;
        
        
        public static void testDataset(String file_format, String[] seq, Boolean IPorPort, int max) throws IOException{
            boolean sizeof = false;
            try {
                    SizeOf.setMinSizeToLog(0);
                    SizeOf.skipStaticField(true);
                    // SizeOf.skipFinalField(true);
          
            } catch (IllegalStateException e) {
                    sizeof = false;
                    System.out
                            .println("# disabling sizeOf, run  -javaagent:lib/SizeOf.jar or equiv. to enable");

            }

//            RealDataRetriever dataSrc = new RealDataRetriever(path);
            HashMap<String, Double> totalTimes = new HashMap<String, Double>();
            HashMap<String, Double> totalSizes = new HashMap<String, Double>();
            for (String op : ops)
                    for (String format : formats) {
                            totalTimes.put(op + ";" + format, 0.0);
                            totalSizes.put(format, 0.0); // done more than
                                                         // necessary
                    }

            
            for (int i = 0; i < NTRIALS; ++i)
                    for (String op : ops){
                    	 //	genera
                        Random rand = new Random((new Date()).getTime());
                		int  a ;
             		
                			a = rand.nextInt(seq.length);
					switch(file_format)
					{
					case("src_ip"):id_list_src_ip[i] = a;break;
					case("dst_ip"):id_list_dst_ip[i] = a;break;
					case("src_port"):id_list_src_port[i] = a;break;
					case("dst_port"):id_list_dst_port[i] = a;break;
					default:break;
					}
//                			System.out.println(i + Arrays.toString(a));
                			if(IPorPort)
                			{
                				String[][] IP_seq = get_IP_sequence(seq);
                				String []retri_file_name = new String[4];
                				for(int r = 0; r<retri_file_name.length; r++)
                				{
                					retri_file_name[r] = String.valueOf(r) + "_" + IP_seq[a][r] + ".txt";
                				}
                				
                				for (String format : formats)
                            		test(op, format, totalTimes,
                                                    totalSizes, sizeof, file_format, retri_file_name);

                			}
                			else
                			{
                				String[][] port_seq = get_port_sequence(seq);
                				String []retri_file_name = new String[2];
                				for(int r = 0; r<retri_file_name.length; r++)
                				{
                					retri_file_name[r] = String.valueOf(r) + "_" + port_seq[a][r] + ".txt";
                				}
                				
                				for (String format : formats)
                            		test(op, format, totalTimes,
                                                    totalSizes, sizeof, file_format, retri_file_name);
                			}

            if (sizeof) {
                    System.out.println("Size ratios");
                    double baselineSize = totalSizes.get(CAMP);
                   
                    for (String format : formats) {
                            double thisSize = totalSizes.get(format);
                            System.out.printf("%s\t%5.2f\n", format,
                                    thisSize / baselineSize);
                    }
            }
            }
            
            
            

           System.out.println("Time ratios");

            for (String op : ops) {
                    double baseline = totalTimes.get(op + ";" + CAMP);

                    System.out.println("baseline is " + baseline);
                    System.out.println(op);
                    System.out.println();
                    for (String format : formats) {
                            double ttime = totalTimes
                                    .get(op + ";" + format);
                            if (ttime != 0.0)
                                    System.out.printf("%s\t%s\t%5.2f\t%5.2f\n",
                                            format, op, ttime/(NTRIALS * (iteration_count - omit_time)), ttime / baseline);
                    }
            }
            System.out.println("ignore this " + junk);

        	
        	
        }

        
		@SuppressWarnings("javadoc")
        public static void main(final String[] args) throws IOException {

	    id_list_src_ip = new int [NTRIALS];
	    id_list_dst_ip = new int [NTRIALS];
	    id_list_src_port = new int [NTRIALS];
	    id_list_dst_port = new int [NTRIALS];
	    String format = "src_ip";
            String file_name = format + "_list.txt";
            Boolean IPorPort = true;

            testDataset(format, get_known_sequence(file_name), IPorPort, max_value);
            FileOutputStream out=new FileOutputStream("src_ip_id_list");
            PrintStream p=new PrintStream(out);
	    for(int i = 0; i<NTRIALS; i++)
	    {
		p.println(id_list_src_ip[i]);		
	    }
	    p.close();
	    out.close();

       	    format = "dst_ip";
            file_name = format + "_list.txt";
            IPorPort = true;
            
            testDataset(format, get_known_sequence(file_name), IPorPort, max_value);
            format = "src_port";
        	file_name = format + "_list.txt";
            IPorPort = false;
            
            testDataset(format, get_known_sequence(file_name), IPorPort, max_value);
            format = "dst_port";
        	file_name = format + "_list.txt";
            IPorPort = false;
            
            testDataset(format, get_known_sequence(file_name), IPorPort, max_value);
	    
	    out=new FileOutputStream("dst_ip_id_list");
            p=new PrintStream(out);
            for(int i = 0; i<NTRIALS; i++)
            {
                p.println(id_list_dst_ip[i]);
            }
            p.close();
            out.close();
	    out=new FileOutputStream("src_port_id_list");
            p=new PrintStream(out);
            for(int i = 0; i<NTRIALS; i++)
            {
                p.println(id_list_src_port[i]);
            }
            p.close();
            out.close();
	    out=new FileOutputStream("dst_port_id_list");
            p=new PrintStream(out);
            for(int i = 0; i<NTRIALS; i++)
            {
                p.println(id_list_dst_port[i]);
            }
            p.close();
            out.close();

      }
        static BitSet toBitSet(int[] dat) {
                BitSet ans = new BitSet();
                for (int i : dat)
                        ans.set(i);
                return ans;
        }

        static ConciseSet toConcise(int[] dat) {
                ConciseSet ans = new ConciseSet();
                for (int i : dat)
                        ans.add(i);
                return ans;
        }

        static ConciseSet toConciseWAH(int[] dat) {
                ConciseSet ans = new ConciseSet(true);
                for (int i : dat)
                        ans.add(i);
                return ans;
        }

        /*
         * What follows is ugly and repetitive, but it has the virtue of being
         * straightforward.
         */

        static void test(String op, String format,
                Map<String, Double> totalTimes, Map<String, Double> totalSizes,
                boolean sizeof, String file_format, String [] retri_file_name) throws IOException {
                String timeKey = op + ";" + format;
                String spaceKey = format;
        
                
                /***************************************************************************/
                if (format.equals(CAMP)) 
                {
                	//iterate 5 times and calculate the average time
//                	index[] result_indices = new index[20];
              	   	double thisTime = 0.0;
            		double start = 0;
            		double stop = 0;
			String stroriginal;
			index result_index;
			index id1;
			String str1;
            		for(int count = 0; count< iteration_count; count++)
            		{
            			start = System.nanoTime();
            			stroriginal = format + "/" + file_format + "/" + retri_file_name[0];
                		result_index = CAMP_IO.input(stroriginal);

	        		for(int cnt = 1; cnt < retri_file_name.length; cnt++)
                		{
                			str1 = format + "/" + file_format + "/" + retri_file_name[cnt];
                			id1 = CAMP_IO.input(str1);


                			result_index = operation.intersection(result_index, id1);
				}
                			
                			stop = System.nanoTime();
                    		
                    		if(count > omit_time )
                    		{
                    			thisTime += stop - start;
                    		}
                    		
                    		totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
            		}
                }
                
                else if (format.equals(CAMP_block)) 
                {
                	//iterate 5 times and calculate the average time
//                	index[] result_indices = new index[20];
              	   	double thisTime = 0.0;
            		double start = 0;
            		double stop = 0;
			String stroriginal;
                        index_set result_index;
                        index_set id1;
                        String str1;
	
                		for(int count = 0; count< iteration_count; count++)
                		{
                			start = System.nanoTime();
                			stroriginal = format + "/" + file_format + "/" + retri_file_name[0];
                    		result_index = CAMP_IO_set.input(stroriginal);

    	        		for(int cnt = 1; cnt < retri_file_name.length; cnt++)
                    		{
                    			str1 = format + "/" + file_format + "/" + retri_file_name[cnt];
                    			id1 = CAMP_IO_set.input(str1);


                    			result_index = operation_set.intersection(result_index, id1);
    				}
                    			
                    			stop = System.nanoTime();
                        		
                        		if(count > omit_time )
                        		{
                        			thisTime += stop - start;
                        		}
                        		
                        		totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
                		}
                }
                else  if (format.equals(ROARING)) 
                {
                	
                	double thisTime = 0.0;
                  	double start = 0.0;
                  	double stop = 0.0;
			String stroriginal;
			String str1;
			RoaringBitmap id1;
			RoaringBitmap result_index;

    		for(int count = 0; count< iteration_count; count++)
    		{
    			start = System.nanoTime();
    			stroriginal = format + "/" + file_format + "/" + retri_file_name[0];
        		result_index = CAMP_IO.input_roaring(stroriginal);

    		for(int cnt = 1; cnt < retri_file_name.length; cnt++)
        		{
        			str1 = format + "/" + file_format + "/" + retri_file_name[cnt];
        			id1 = CAMP_IO.input_roaring(str1);


        			result_index = RoaringBitmap.and(result_index, id1);
		}
        			
        			stop = System.nanoTime();
            		
            		if(count > omit_time )
            		{
            			thisTime += stop - start;
            		}
            		
            		totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
    		}
                }
                
                else  if (format.equals(COMPAX)) 
                {
                	
                	double thisTime = 0.0;
                  	double start = 0.0;
                  	double stop = 0.0;
			String stroriginal;
			COMPAXSet result_index;
			String str1;
			COMPAXSet id1;

    		for(int count = 0; count< iteration_count; count++)
    		{
    			start = System.nanoTime();
    			stroriginal = format + "/" + file_format + "/" + retri_file_name[0];
        		result_index = CAMP_IO.in_compax(stroriginal);

    		for(int cnt = 1; cnt < retri_file_name.length; cnt++)
        		{
        			str1 = format + "/" + file_format + "/" + retri_file_name[cnt];
        			id1 = CAMP_IO.in_compax(str1);


        			result_index = COMPAXSet.intersection(result_index, id1);
		}
        			
        			stop = System.nanoTime();
            		
            		if(count > omit_time )
            		{
            			thisTime += stop - start;
            		}
            		
            		totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
    		}
                }
                /***************************************************************************/
                else if (format.equals(WAH)) 
                {
                	
                	double thisTime = 0.0;
                  	double start = 0.0;
                  	double stop = 0.0;
                 	String stroriginal;
			WAHSet result_index;
			String str1;
			WAHSet id1; 	
			for(int count = 0; count< iteration_count; count++)
    		{
    			start = System.nanoTime();
    			stroriginal = format + "/" + file_format + "/" + retri_file_name[0];
        		result_index = CAMP_IO.in_WAH(stroriginal);

    		for(int cnt = 1; cnt < retri_file_name.length; cnt++)
        		{
        			str1 = format + "/" + file_format + "/" + retri_file_name[cnt];
        			id1 = CAMP_IO.in_WAH(str1);


        			result_index = WAHSet.intersection(result_index, id1);
		}
        			
        			stop = System.nanoTime();
            		
            		if(count > omit_time )
            		{
            			thisTime += stop - start;
            		}
            		
            		totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
    		}
                }
                /***************************************************************************/
                /***************************************************************************/
                else if (format.equals(CONCISE)) 
                {         
                	
                	double thisTime = 0.0;
                  	double start = 0.0;
                  	double stop = 0.0;
			String stroriginal;
			ImmutableConciseSet result_index;
			String str1;
			ImmutableConciseSet id1;
			for(int count = 0; count< iteration_count; count++)
    		{
    			start = System.nanoTime();
    			stroriginal = format + "/" + file_format + "/" + retri_file_name[0];
        		result_index = CAMP_IO.in_concise(stroriginal);

    		for(int cnt = 1; cnt < retri_file_name.length; cnt++)
        		{
        			str1 = format + "/" + file_format + "/" + retri_file_name[cnt];
        			id1 = CAMP_IO.in_concise(str1);


        			result_index = ImmutableConciseSet.intersection(result_index, id1);
		}
        			
        			stop = System.nanoTime();
            		
            		if(count > omit_time )
            		{
            			thisTime += stop - start;
            		}
            		
            		totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
    		}
                }
                
        }

        static double avgSeconds(Computation toDo) {
                int ntrials = 1;
                long elapsedNS = 0L;
                long start, stop;
                do {
                        ntrials *= 2;
                        start = System.nanoTime();
                        for (int i = 0; i < ntrials; ++i) {
                                // might have to do something to stop hoisting
                                // here
                                toDo.compute();
                        }
                        stop = System.nanoTime();
                        elapsedNS = stop - start;
                } while (elapsedNS < LONG_ENOUGH_NS);
                /* now things are very hot, so do an actual timing */
                start = System.nanoTime();
                for (int i = 0; i < ntrials; ++i) {
                        // danger, optimizer??
                        toDo.compute();
                }
                stop = System.nanoTime();
                return (stop - start) / (ntrials * 1e+9); // ns to s
        }

        abstract static class Computation {
                public abstract void compute(); // must mess with "junk"
        }
        
        public static String[] get_known_sequence(String file_name) throws IOException
        {
        	ArrayList<String> temp_list = new ArrayList<String>();
        	FileReader fr = new FileReader(file_name); 
    		BufferedReader br = new BufferedReader(fr); 
    		String s = br.readLine();
    		while(s != null)
    		{
    			temp_list.add(s);
    			s = br.readLine();
    		}
    		
    		String[] known_seq = new String[temp_list.size()];
    		int k = 0;
    		for(String c:temp_list)
    		{
    			known_seq[k++] = c;
    		}
    		return known_seq;
        }
        
        public static String[][] get_IP_sequence(String []seq) throws IOException
        {
        	String [][] IP_seq = new String[seq.length][4];
        	for(int i = 0; i<seq.length; i++)
        	{
        		String []temp = seq[i].split("\\.");
        		for(int j = 0; j<4; j++)
        		{
        			IP_seq[i][j] = temp[j];
        		}
        	}
        	return IP_seq;
        }
        public static String[][] get_port_sequence(String[] seq) {
			// TODO Auto-generated method stub
        	
        	String [][] port_seq = new String[seq.length][2];
        	for(int i = 0; i<seq.length; i++)
        	{
        		port_seq[i][0] = String.valueOf(Integer.valueOf(seq[i])%256);
        		port_seq[i][1] = String.valueOf(Integer.valueOf(seq[i])/256);        		
        	}
        	return port_seq;
		}


}
