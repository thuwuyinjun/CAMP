package org.CAMP;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.roaringbitmap.RoaringBitmap;


import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah32.EWAHCompressedBitmap32;


/**
 * O. Kaser's benchmark over real data
 * 
 */
public class testRealAndOperationfromdisk_CAIDA {
        static final String AND = "AND";
        static final String OR = "OR";
        static final String XOR = "XOR";
   //     static final String[] ops = { AND, OR, XOR };
        static final String[] ops = {" "};
        static final String EWAH32 = "EWAH32";
        static final String EWAH64 = "EWAH64";
        static final String CONCISE = "CONCISE";
      
        static final String WAH = "WAH";
        static final String BITSET = "BITSET";
        static final String ROARING = "ROARING";
        static final String CAMP = "CAMP";
        static final String CAMP_block = "CAMP_block";
        static final String COMPAX = "COMPAX";
        
        static final int NTRIALS=100;
        
        static final int num = 100;
        
        static final int max = 13581810;
        
        static final int range = 256;
        
    //    static final String[] formats = { EWAH32, EWAH64, CONCISE, WAH, BITSET, ROARING };
        
        static final String[] formats = {CAMP_block,CAMP, CONCISE, COMPAX, WAH, ROARING};

//        static final String[] formats = {COMPAX};

        static int junk = 0; // to fight optimizer.

        static long LONG_ENOUGH_NS = 1000L * 1000L * 1000L;
        private static UniformDataGenerator uniformDataGeneror=new UniformDataGenerator();
    	private static ClusteredDataGenerator clusteredDataGenerator=new ClusteredDataGenerator();
    	public static int max_num = 100000;
    	public static double []densities={0.0001,0.001,0.005,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.1};
//    	public static double []densities={0.06,0.07,0.08,0.1};

    	public static int iteration_count=60;
		private static int omit_time = 50;
    	
        public static void testDataset(String path,String type, int k) throws IOException{
    		
            boolean sizeof = true;
//            try {
//                    SizeOf.setMinSizeToLog(0);
//                    SizeOf.skipStaticField(true);
//                    // SizeOf.skipFinalField(true);
//          
//            } catch (IllegalStateException e) {
//                    sizeof = false;
//                    System.out
//                            .println("# disabling sizeOf, run  -javaagent:lib/SizeOf.jar or equiv. to enable");
//
//            }
            RealDataRetriever2 dataSrc = new RealDataRetriever2(path + type);
            HashMap<String, Double> totalTimes = new HashMap<String, Double>();
            HashMap<String, Double> totalSizes = new HashMap<String, Double>();
            for (String op : ops)
                    for (String format : formats) {
                            totalTimes.put(op + ";" + format, 0.0);
                            totalSizes.put(format, 0.0); // done more than
                                                         // necessary
                    }

            //for (int i = 0; i < NTRIALS; ++i)
            for (int i = 0; i < num; ++i)
    		{
            		Random r = new Random();
            		
            	    int index1 = r.nextInt(range);
            	    int index2 = r.nextInt(range);
                    for (String op : ops)
                            for (String format : formats)
                            {
                            	String str = path + k;
                            	test(op, format, type, totalTimes, index1, index2, sizeof,
                                        dataSrc.fetchBitPositions(
                                                k, index1),dataSrc.fetchBitPositions(
                                                        k, index2), max, k);
                            }
    		}
            
            if (sizeof) {
                    System.out.println("Size ratios");
//                    double baselineSize = totalSizes.get(CAMP);
//                   
//                    for (String format : formats) {
//                            double thisSize = totalSizes.get(format);
//                            System.out.printf("%s\t%5.2f\n", format,
//                                    thisSize / baselineSize);
//                    }
                    System.out.println();
            }
            
            
            

             System.out.println("Time ratios");

            for (String op : ops) {
                    double baseline = totalTimes.get(op + ";" + ROARING);

                    System.out.println("baseline is " + baseline);
                    System.out.println(op);
                    System.out.println();
                    for (String format : formats) {
                            double ttime = totalTimes
                                    .get(op + ";" + format);
                            if (ttime != 0.0)
                            {
                            	System.out.printf("%s\t%s\t%5.2f\t%5.2f\n",format, op, ttime, ttime / baseline);
                            }
                                            
                    }
            }
            System.out.println("ignore this " + junk);

        	
        	
        }

        @SuppressWarnings("javadoc")
        public static void main(final String[] args) throws IOException {
        	System.out.println("uniform data:");
        	
        	
        	String path="Netflow/";
        	String[] data = {"src_ip", "dst_ip","src_port","dst_port"};
        	
        	for(int i = 0; i<data.length; i++)
        	{
        		if(i < 2)
        		for(int k = 0; k<4; k++)
	        	{
			        testDataset(path,data[i],k);
	        	}
        		else
        		{
        			for(int k = 0; k<2; k++)
    	        	{
    			        testDataset(path, data[i],k);
    	        	}
        		}
        	}
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

        static void test(String op, String format,String type,
                Map<String, Double> totalTimes, int index1, int index2,
                boolean sizeof, int[] data1, int[] data2, int max, int k) throws IOException {
        	String timeKey = op + ";" + format;
                /***************************************************************************/
            if (format.equals(ROARING)) {
                
                RoaringBitmap id1 = CAMP_IO.input_roaring("ROARING/" + type + "/" + k + "_" + index1 + ".txt");
                RoaringBitmap id2 = CAMP_IO.input_roaring("ROARING/" + type + "/" + k + "_" + index2 + ".txt");
                  if (sizeof) {
           	  		 	    RoaringBitmap id;
           	  		 	    double start;
           	  		 	    double end;
           	  		 	    double thisTime = 0;
           	  		 	    for(int i = 0; i<NTRIALS;i++)
           	  		 	    {
           	  		 	    	start = System.nanoTime();
						id1 = CAMP_IO.input_roaring("ROARING/" + type + "/" + k + "_" + index1 + ".txt");
						id2 = CAMP_IO.input_roaring("ROARING/" + type + "/" + k + "_" + index2 + ".txt");
           	  		 	    	id = RoaringBitmap.and(id1, id2);
           	  		 	    	end = System.nanoTime();
           	  		 	    	if(i > omit_time)
           	  		 	    	{
           	  		 	    		thisTime += end - start;
           	  		 	    	}
           	  		 	    }
           	  		 	totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
                  }
            
            }
            else
            if (format.equals(CAMP_block)) {
            	index_set id1 = CAMP_IO_set.input("CAMP_block/" + type + "/" + k + "_" + index1 + ".txt");
                index_set id2 = CAMP_IO_set.input("CAMP_block/" + type + "/" + k + "_" + index2 + ".txt");
                  if (sizeof) {
                	  		index_set id;
           	  		 	    double start;
           	  		 	    double end;
           	  		 	    double thisTime = 0;
           	  		 	    for(int i = 0; i<NTRIALS;i++)
           	  		 	    {
           	  		 	    	start = System.nanoTime();
						id1 = CAMP_IO_set.input("CAMP_block/" + type + "/" + k + "_" + index1 + ".txt");
                                                id2 = CAMP_IO_set.input("CAMP_block/" + type + "/" + k + "_" + index2 + ".txt");
           	  		 	    	id = operation_set.intersection(id1, id2);
           	  		 	    	end = System.nanoTime();
           	  		 	    	if(i > omit_time)
           	  		 	    	{
           	  		 	    		thisTime += end - start;
           	  		 	    	}
           	  		 	    }
           	  		 	totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
                  }
            }
            else
        	if (format.equals(CAMP)) {
                
        	index id1 = CAMP_IO.input("CAMP/" + type + "/" + k + "_" + index1 + ".txt");
                index id2 = CAMP_IO.input("CAMP/" + type + "/" + k + "_" + index2 + ".txt");
                  if (sizeof) {
                	  		index id;
           	  		 	    double start;
           	  		 	    double end;
           	  		 	    double thisTime = 0;
           	  		 	    for(int i = 0; i<NTRIALS;i++)
           	  		 	    {
           	  		 	    	start = System.nanoTime();
						id1 = CAMP_IO.input("CAMP/" + type + "/" + k + "_" + index1 + ".txt");
						id2 = CAMP_IO.input("CAMP/" + type + "/" + k + "_" + index2 + ".txt");
           	  		 	    	id = operation.intersection(id1, id2);
           	  		 	    	end = System.nanoTime();
           	  		 	    	if(i > omit_time)
           	  		 	    	{
           	  		 	    		thisTime += end - start;
           	  		 	    	}
           	  		 	    }
           	  		 	totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
                  }
                
                }
                /***************************************************************************/
                else if (format.equals(COMPAX)) {
                	COMPAXSet id1 = CAMP_IO.in_compax("COMPAX/" + type + "/" + k + "_" + index1 + ".txt");
                       COMPAXSet id2 = CAMP_IO.in_compax("COMPAX/" + type + "/" + k + "_" + index2 + ".txt");
                      if (sizeof) {
                    	  		COMPAXSet id;
               	  		 	    double start;
               	  		 	    double end;
               	  		 	    double thisTime = 0;
               	  		 	    for(int i = 0; i<NTRIALS;i++)
               	  		 	    {
               	  		 	    	start = System.nanoTime();
						id1 = CAMP_IO.in_compax("COMPAX/" + type + "/" + k + "_" + index1 + ".txt");
						id2 = CAMP_IO.in_compax("COMPAX/" + type + "/" + k + "_" + index2 + ".txt");
               	  		 	    	id = COMPAXSet.intersection(id1, id2);
               	  		 	    	end = System.nanoTime();
               	  		 	    	if(i > omit_time)
               	  		 	    	{
               	  		 	    		thisTime += end - start;
               	  		 	    	}
               	  		 	    }
               	  		 	totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
                      }
                }
                /***************************************************************************/
                else if (format.equals(WAH)) {
                	WAHSet id1 = CAMP_IO.in_WAH("WAH/" + type + "/" + k + "_" + index1 + ".txt");
                        WAHSet id2 = CAMP_IO.in_WAH("WAH/" + type + "/" + k + "_" + index2 + ".txt");
                      if (sizeof) {
                    	  		WAHSet id;
               	  		 	    double start;
               	  		 	    double end;
               	  		 	    double thisTime = 0;
               	  		 	    for(int i = 0; i<NTRIALS;i++)
               	  		 	    {
               	  		 	    	start = System.nanoTime();
						id1 = CAMP_IO.in_WAH("WAH/" + type + "/" + k + "_" + index1 + ".txt");
						id2 = CAMP_IO.in_WAH("WAH/" + type + "/" + k + "_" + index2 + ".txt");
               	  		 	    	id = WAHSet.intersection(id1, id2);
               	  		 	    	end = System.nanoTime();
               	  		 	    	if(i > omit_time)
               	  		 	    	{
               	  		 	    		thisTime += end - start;
               	  		 	    	}
               	  		 	    }
               	  		 	totalTimes.put(timeKey, thisTime + totalTimes.get(timeKey));
                      }
                  }
                /***************************************************************************/
                else if (format.equals(CONCISE)) {
                 	ImmutableConciseSet id1 = CAMP_IO.in_concise("WAH/" + type + "/" + k + "_" + index1 + ".txt");
                    ImmutableConciseSet id2 = CAMP_IO.in_concise("WAH/" + type + "/" + k + "_" + index2 + ".txt");
                      if (sizeof) {
                    	  		ImmutableConciseSet id;
               	  		 	    double start;
               	  		 	    double end;
               	  		 	    double thisTime = 0;
               	  		 	    for(int i = 0; i<NTRIALS;i++)
               	  		 	    {
               	  		 	    	start = System.nanoTime();
						id1 = CAMP_IO.in_concise("WAH/" + type + "/" + k + "_" + index1 + ".txt");
						id2 = CAMP_IO.in_concise("WAH/" + type + "/" + k + "_" + index2 + ".txt");
               	  		 	    	id = ImmutableConciseSet.intersection(id1, id2);
               	  		 	    	end = System.nanoTime();
               	  		 	    	if(i > omit_time)
               	  		 	    	{
               	  		 	    		thisTime += end - start;
               	  		 	    	}
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
