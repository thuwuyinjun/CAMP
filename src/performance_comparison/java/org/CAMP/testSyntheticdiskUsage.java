package org.CAMP;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.roaringbitmap.RoaringBitmap;


import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah32.EWAHCompressedBitmap32;


/**
 * O. Kaser's benchmark over real data
 * 
 */
public class testSyntheticdiskUsage {
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
        
        static final int NTRIALS=40;
        
    //    static final String[] formats = { EWAH32, EWAH64, CONCISE, WAH, BITSET, ROARING };
        
        static final String[] formats = {CAMP_block,CAMP, CONCISE, COMPAX, WAH, ROARING};

//        static final String[] formats = {COMPAX};

        static int junk = 0; // to fight optimizer.

        static long LONG_ENOUGH_NS = 1000L * 1000L * 1000L;
        private static UniformDataGenerator uniformDataGeneror=new UniformDataGenerator();
    	private static ClusteredDataGenerator clusteredDataGenerator=new ClusteredDataGenerator();
    	public static int max_num = 10000000;
    	public static double []densities={0.000001,0.00001,0.00003162,0.0001,0.0003162,0.001,0.003162,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.1};
//    	public static double []densities={0.06,0.07,0.08,0.1};

    	public static int iteration_count=60;
    	
        public static void testDataset(double density, String type) throws IOException{
    		
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

            HashMap<String, Double> totalTimes = new HashMap<String, Double>();
            HashMap<String, Double> totalSizes = new HashMap<String, Double>();
            for (String op : ops)
                    for (String format : formats) {
                            totalTimes.put(op + ";" + format, 0.0);
                            totalSizes.put(format, 0.0); // done more than
                                                         // necessary
                    }

            //for (int i = 0; i < NTRIALS; ++i)
            for (int i = 0; i < NTRIALS; ++i)
    		{
        	int integer_count=(int) (max_num*density);
        	int [] dataset;
        		if(type == "uniform")
        		{
        			dataset= uniformDataGeneror.generateUniform(integer_count,max_num);
                    for (String op : ops)
                            for (String format : formats)
                                    test(op, format, totalTimes,
                                            i, sizeof,
                                            dataset,
                                            dataset, density, type);
        		}
        		else
        		{
        			dataset= clusteredDataGenerator.generateClustered(integer_count,max_num);
                    for (String op : ops)
                            for (String format : formats)
                                    test(op, format, totalTimes,
                                            i, sizeof,
                                            dataset,
                                            dataset,density, type);
        		}
    		}
            
            if (sizeof) {
                    System.out.println("Size ratios");
                    System.out.println("density:"+density);
//                    double baselineSize = totalSizes.get(CAMP);
//                   
//                    for (String format : formats) {
//                            double thisSize = totalSizes.get(format);
//                            System.out.printf("%s\t%5.2f\n", format,
//                                    thisSize / baselineSize);
//                    }
                    System.out.println();
            }
            
            
            

 /*           System.out.println("Time ratios");

            for (String op : ops) {
                    double baseline = totalTimes.get(op + ";" + ROARING);

                    System.out.println("baseline is " + baseline);
                    System.out.println(op);
                    System.out.println();
                    for (String format : formats) {
                            double ttime = totalTimes
                                    .get(op + ";" + format);
                            if (ttime != 0.0)
                                    System.out.printf("%s\t%s\t%5.2f\n",
                                            format, op, ttime / baseline);
                    }
            }
            System.out.println("ignore this " + junk);
*/ 
        	
        	
        }

        @SuppressWarnings("javadoc")
        public static void main(final String[] args) throws IOException {
        	System.out.println("uniform data:");
        	
        	for(int k = 0; k<densities.length; k++)
        	{
		        testDataset(densities[k],"uniform");
        	}
        	
        	System.out.println("clustered data:");
        	for(int k = 0; k<densities.length; k++)
        	{
	        	testDataset(densities[k], "clustered");
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

        static void test(String op, String format,
                Map<String, Double> totalTimes, int index,
                boolean sizeof, int[] data1, int[] data2, double density, String type) throws IOException {
              
                /***************************************************************************/
            if (format.equals(ROARING)) {
                
                final RoaringBitmap id1 = RoaringBitmap.bitmapOf(data1);
                final RoaringBitmap id2 = RoaringBitmap.bitmapOf(data2);
                  if (sizeof) {
                           
                	  		File file = new File ("ROARING/" + type + "_" + density);
                	  		if(!file.exists())
                	  		{
                	  			file.mkdirs();
                	  		}
                	  	
                           CAMP_IO.output_roaring(id1, "ROARING/" + type + "_" + density + "/"+ index + ".txt");
                        
                  }
            
            }
            else
            if (format.equals(CAMP_block)) {
                
            	final index_set id1 = create_index_set.bitmapof(data1, max_num);
            	final index_set id2 = create_index_set.bitmapof(data2, max_num);
                  if (sizeof) {
                           
                	  		File file = new File ("CAMP_block/" + type + "_" + density);
                	  		if(!file.exists())
                	  		{
                	  			file.mkdirs();
                	  		}
                	  	
                           CAMP_IO_set.output(id1, "CAMP_block/" + type + "_" + density + "/"+ index + ".txt");
                        
                  }
            
            }
            else
        	if (format.equals(CAMP)) {
                
                	final index id1 = create_index.bitmapof(data1, max_num);
                	final index id2 = create_index.bitmapof(data2, max_num);
                      if (sizeof) {
                               
                    	  		File file = new File ("CAMP/" + type + "_" + density);
                    	  		if(!file.exists())
                    	  		{
                    	  			file.mkdirs();
                    	  		}
                    	  	
                               CAMP_IO.output(id1, "CAMP/" + type + "_" + density + "/"+ index + ".txt");
                            
                      }
                
                }
                /***************************************************************************/
                else if (format.equals(COMPAX)) {
                        final COMPAXSet bm1 = COMPAXSet.bitmapof(data1);
                        final COMPAXSet bm2 = COMPAXSet.bitmapof(data2);
                        File file = new File ("COMPAX/" + type + "_" + density);
            	  		if(!file.exists())
            	  		{
            	  			file.mkdirs();
            	  		}
            	  	
                       CAMP_IO_set.out_compax(bm1, "COMPAX/" + type + "_" + density + "/"+ index + ".txt");
                }
                /***************************************************************************/
                else if (format.equals(WAH)) {
                        final WAHSet bm1 = WAHSet.bitmapof(data1);
                        final WAHSet bm2 = WAHSet.bitmapof(data2);
                        File file = new File ("WAH/" + type + "_" + density);
            	  		if(!file.exists())
            	  		{
            	  			file.mkdirs();
            	  		}
                       CAMP_IO_set.out_WAH(bm1, "WAH/" + type + "_" + density + "/"+ index + ".txt");
                }
                /***************************************************************************/
                else if (format.equals(CONCISE)) {
                        final ImmutableConciseSet bm1 = ImmutableConciseSet.bitmapof(data1);
                        final ImmutableConciseSet bm2 = ImmutableConciseSet.bitmapof(data2);
                        File file = new File ("CONCISE/" + type + "_" + density);
            	  		if(!file.exists())
            	  		{
            	  			file.mkdirs();
            	  		}
                       CAMP_IO_set.out_concise(bm1, "CONCISE/" + type + "_" + density + "/"+ index + ".txt");
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
