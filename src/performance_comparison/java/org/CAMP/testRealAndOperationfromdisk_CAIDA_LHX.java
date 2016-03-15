package org.CAMP;
//import java.util.*;
import java.io.File;
import java.io.IOException;
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
public class testRealAndOperationfromdisk_CAIDA_LHX {
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
        
        static final int NTRIALS=6;
        
    //    static final String[] formats = { EWAH32, EWAH64, CONCISE, WAH, BITSET, ROARING };
        
        static final String[] formats = {CAMP,COMPAX,CONCISE, WAH};
        
        static final int [] maxs = {13581811, 13581811, 13581811, 13581811, 13581811, 13581811,13581811,13581811};

        static final int max_value = 13581811;
        
        static int junk = 0; // to fight optimizer.

        static long LONG_ENOUGH_NS = 1000L * 1000L * 1000L;
        
        public static int iteration_count = 100;
		private static int omit_time = 5;
        
        
        public static void testDataset(String path,String[] dataset, int max) throws IOException{
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
                		int[] a = new int[12];
                		String[] target = new String[a.length];
                		System.out.println("length of a = " + a.length);
                  		for(int k = 0; k < a.length; k++)
                		{
                			a[k] = rand.nextInt(255);
                			target[k] = String.valueOf(a[k]);
//                			System.out.println(i + Arrays.toString(a));
                		}

                            for (String format : formats)
                        	
                                                               
                    		test(op, format, totalTimes,
                                            totalSizes, sizeof,
                                            null,
                                            null, max, dataset, target, i);
            System.out.println(dataset);
            if (sizeof) {
                    System.out.println("Size ratios");
                    double baselineSize = totalSizes.get(CAMP);
                   
                    for (String format : formats) {
                            double thisSize = totalSizes.get(format);
                            System.out.printf("%s\t%5.2f\n", format,
                                    thisSize / baselineSize);
                    }
            }
                    System.out.println();
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
                                            format, op, ttime, ttime / baseline);
                    }
            }
            System.out.println("ignore this " + junk);

        	
        	
        }

        @SuppressWarnings("javadoc")
        public static void main(final String[] args) throws IOException {

        	String path="Netflow/";
//            String[] dataset = {"src_ip/0_", "src_ip/1_", "src_ip/2_", "src_ip/3_", "dst_ip/0_", "dst_ip/1_", "dst_ip/2_", "dst_ip/3_", "src_port/0_", "src_port/1_", "dst_port/0_", "dst_port/1_"};
            String[] dataset = {"dst_port/0_", "dst_port/1_"};

            testDataset(path, dataset, max_value);
        	
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
                boolean sizeof, int[] data1, int[] data2, int max, String[] dataset, String[] target, int i) throws IOException {
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

                	for(int iteratecnt = 0; iteratecnt < 19; iteratecnt++)
                	{

                		start = System.nanoTime();

                		String stroriginal = format + "/" + dataset[0] + "/" + target[0] + ".txt";
                		index result_index = CAMP_IO.input(stroriginal);
                		
                		for(int cnt = 1; cnt < dataset.length; cnt++)
                		{
                			String str1 = format + "/" + dataset[cnt] + "/" + target[cnt] + ".txt";
                	   
                			index id1 = CAMP_IO.input(str1);

                			result_index = operation.intersection(result_index, id1);
                		}
                     
                		stop = System.nanoTime();
                		
                		if(iteratecnt > omit_time )
                		{
                			thisTime += stop - start;
                		}
                		
                		totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));

                		//                    result_indices[iteratecnt] = result_index;

                	}
                }
                
                else  if (format.equals(COMPAX)) 
                {
                	
                	double thisTime = 0.0;
                  	double start = 0.0;
                  	double stop = 0.0;

                	for(int iteratecnt = 0; iteratecnt < 19; iteratecnt++)
                	{
                		start = System.nanoTime();

                  	    String stroriginal = format + "/" + dataset[0] + "/" + target[0] + ".txt";
                  	    COMPAXSet result_compax = CAMP_IO.in_compax(stroriginal);
                  	   
                		for(int cnt = 1; cnt < dataset.length; cnt++)
                        {
                			String str1 = format + "/" + dataset[cnt] + "/" + target[cnt] + ".txt";
                			
                			COMPAXSet bm1 = CAMP_IO.in_compax(str1);
                			
                			result_compax = COMPAXSet.intersection(bm1,result_compax);
                		}
                		
                		stop = System.nanoTime();
                		
                    	if(iteratecnt > omit_time)
               		   	{
                    		thisTime += stop - start;
               		   	}
             	                	   
                    	totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
                	}
                }
                /***************************************************************************/
                /***************************************************************************/
                else if (format.equals(WAH)) 
                {
                	
                	double thisTime = 0.0;
                  	double start = 0.0;
                  	double stop = 0.0;
                  	
                	for(int iteratecnt = 0; iteratecnt < 19; iteratecnt++)
                	{
                		start = System.nanoTime();
                		
                		String stroriginal = format + "/" + dataset[0] + "/" + target[0] + ".txt";
                		WAHSet result_wah = CAMP_IO.in_WAH(stroriginal);
                   	   
                		for(int cnt = 1; cnt < dataset.length; cnt++)
                		{
                			String str1 = format + "/" + dataset[cnt] + "/" + target[cnt] + ".txt";
                			
                			WAHSet bm1 = CAMP_IO.in_WAH(str1);
                			
                			result_wah = WAHSet.intersection(bm1, result_wah);
                		}
                		
                		stop = System.nanoTime();
                		
                		if(iteratecnt > omit_time)
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
                  	
                	for(int iteratecnt = 0; iteratecnt < 19; iteratecnt++)
                	{
                  		start = System.nanoTime();

                		String stroriginal = format + "/" + dataset[0] + "/" + target[0] + ".txt";
                 	    ImmutableConciseSet result_concise = CAMP_IO.in_concise(stroriginal);
                       
                 	    for(int cnt = 0; cnt < dataset.length; cnt++)
                 	    {
                 	    	String str1 = format + "/" + dataset[cnt] + "/" + target[cnt] + ".txt";
                 	    	
                 	    	ImmutableConciseSet  bm1 = CAMP_IO.in_concise(str1);

                 	    	result_concise = ImmutableConciseSet.intersection(bm1, result_concise);
                 	    }
                 	    
                		stop = System.nanoTime();

                 	    if(iteratecnt > omit_time)
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

}