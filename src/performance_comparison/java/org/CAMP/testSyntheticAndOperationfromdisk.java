package org.CAMP;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.roaringbitmap.RoaringBitmap;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah32.EWAHCompressedBitmap32;

import net.sourceforge.sizeof.SizeOf;

/**
 * O. Kaser's benchmark over real data
 * 
 */
public class testSyntheticAndOperationfromdisk {
        static final String AND = "AND";
        static final String OR = "OR";
        static final String XOR = "XOR";
        static final String[] ops = { AND};
        static final String EWAH32 = "EWAH32";
        static final String EWAH64 = "EWAH64";
        static final String CONCISE = "CONCISE";
      
        static final String WAH = "WAH";
        static final String BITSET = "BITSET";
        static final String ROARING = "ROARING";
        static final String CAMP = "CAMP";
        static final String COMPAX = "COMPAX";
        static final String CAMP_block = "CAMP_block";

        
        static final int NTRIALS=20;
        
    //    static final String[] formats = { EWAH32, EWAH64, CONCISE, WAH, BITSET, ROARING };
        
        static final String[] formats = {CAMP_block,CAMP, CONCISE, COMPAX, WAH, ROARING};

        static int junk = 0; // to fight optimizer.

        static long LONG_ENOUGH_NS = 1000L * 1000L * 1000L;
        private static UniformDataGenerator uniformDataGeneror=new UniformDataGenerator();
    	private static ClusteredDataGenerator clusteredDataGenerator=new ClusteredDataGenerator();
    	public static int max_num = 1000000;
    	public static double []densities={0.0001,0.001,0.005,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.1};
//    	public static double []densities={0.03,0.04,0.05,0.06,0.07,0.08,0.1};
    	public static int iteration_count=100;
		private static int omit_time = 50;
    	
        public static void testDataset(double density, String type) throws IOException{
            boolean sizeof = true;
            try {
                    SizeOf.setMinSizeToLog(0);
                    SizeOf.skipStaticField(true);
                    // SizeOf.skipFinalField(true);
          
            } catch (IllegalStateException e) {
                    sizeof = false;
                    System.out
                            .println("# disabling sizeOf, run  -javaagent:lib/SizeOf.jar or equiv. to enable");

            }

            HashMap<String, Double> totalTimes = new HashMap<String, Double>();
            HashMap<String, Double> totalSizes = new HashMap<String, Double>();
            for (String op : ops)
                    for (String format : formats) {
                            totalTimes.put(op + ";" + format, 0.0);
                            totalSizes.put(format, 0.0); // done more than
                                                         // necessary
                    }

            //for (int i = 0; i < NTRIALS; ++i)
            sizeof = false;
            for (int i = 0; i < NTRIALS; ++i)
    		{
        	int integer_count=(int) (max_num*density);
        	int [] dataset1;
        	int [] dataset2;
        		if(type == "uniform")
        		{
                    for (String op : ops)
                            for (String format : formats)
                                    test(op, format, totalTimes,
                                            totalSizes, sizeof,
                                            density, i, type);
        		}
        		else
        		{
        			dataset1= clusteredDataGenerator.generateClustered(integer_count,max_num);
        			dataset2= clusteredDataGenerator.generateClustered(integer_count,max_num);
                    for (String op : ops)
                            for (String format : formats)
                                    test(op, format, totalTimes,
                                            totalSizes, sizeof,
                                            density,i, type);
        		}
    		}
            
    		System.out.println("density:" + density);
            if (sizeof) {
                    System.out.println("Size ratios");
                    System.out.println("density:"+density);
                    double baselineSize = totalSizes.get(CAMP);
                   
                    for (String format : formats) {
                            double thisSize = totalSizes.get(format);
                            System.out.printf("%s\t%5.2f\n", format,
                                    thisSize / baselineSize);
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
                                            format, op, ttime/(NTRIALS * (iteration_count  - omit_time)),ttime/baseline);
                    }
            }
            System.out.println("ignore this " + junk);
 
        	
        	
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
                Map<String, Double> totalTimes, Map<String, Double> totalSizes,
                boolean sizeof, double density, int i, String type) throws IOException {
                String timeKey = op + ";" + format;
                String spaceKey = format;
              
                /***************************************************************************/
                if (format.equals(ROARING)) {
               	 
                	String str1 = "ROARING/" + type + "_" + density + "/" + (2 * i) + ".txt";
                	String str2 = "ROARING/" + type + "_" + density + "/" + (2 * i + 1) + ".txt";
                	RoaringBitmap id1;
                	RoaringBitmap id2;
                	
                	RoaringBitmap  result_index;
                               double thisTime = 0.0;
                               int card = 0;
                               if (op.equals(AND)) {
                            	   double start = 0;
                            	   double stop = 0;
                            	   for(int j = 0; j<iteration_count;j++)
                            	   {
                            		   start = System.nanoTime();
                            		   id1 = CAMP_IO.input_roaring(str1);
                            		   id2 = CAMP_IO.input_roaring(str2);
                            		   result_index = RoaringBitmap.and(id1, id2);
                            		   stop = System.nanoTime();
                            		   if(j > omit_time )
                            		   {
                            			   thisTime += stop - start;
                            		   }
                            	   }
                                       totalTimes.put(timeKey,
                                               thisTime + totalTimes.get(timeKey));
                               }
                }
                else
                
                if (format.equals(CAMP_block)) {
                	 
                	String str1 = "CAMP_block/" + type + "_" + density + "/" + (2 * i) + ".txt";
                	String str2 = "CAMP_block/" + type + "_" + density + "/" + (2 * i + 1) + ".txt";
                	index_set id1;
                	index_set id2;
                	
                	index_set  result_index;
                               double thisTime = 0.0;
                               int card = 0;
                               if (op.equals(AND)) {
                            	   double start = 0;
                            	   double stop = 0;
                            	   for(int j = 0; j<iteration_count;j++)
                            	   {
                            		   start = System.nanoTime();
                            		   id1 = CAMP_IO_set.input(str1);
                            		   id2 = CAMP_IO_set.input(str2);
                            		   result_index = operation_set.intersection(id1, id2);
                            		   stop = System.nanoTime();
                            		   if(j > omit_time)
                            		   {
                            			   thisTime += stop - start;
                            		   }
                            	   }
                                       totalTimes.put(timeKey,
                                               thisTime + totalTimes.get(timeKey));
                               }
                }
                else
                
                if (format.equals(CAMP)) {
 
                	String str1 = "CAMP/" + type + "_" + density + "/" + (2 * i) + ".txt";
                	String str2 = "CAMP/" + type + "_" + density + "/" + (2 * i + 1) + ".txt";
                	index id1;
                	index id2;
                	
                	index  result_index;
//                      if (sizeof) {
//                               long theseSizesInBits = 8 * (SizeOf
//                                       .deepSizeOf(id1) + SizeOf
//                                       .deepSizeOf(id2));
//                               totalSizes.put(spaceKey, theseSizesInBits
//                                       + totalSizes.get(spaceKey));
//                      }
                               
  //                             System.out.println("ZVBitmap size:"+theseSizesInBits);
                               /*for(int i=0;i<bm1.zvArray.size;i++){
                       			//		System.out.println(zvb1.zvArray.elementArray[i].getContainerLevel()+": "+zvb1.zvArray.elementArray[i].getCardinality());
                       				//	if(zvb1.zvArray.elementArray[i].getContainerLevel()==2)
                      
                       					
                       					if(bm1.zvArray.elementArray[i].getContainerLevel()==3){
                       						ZvArrayContainer3 container = (ZvArrayContainer3)(bm1.zvArray.elementArray[i]);
                       						System.out.println("container3:" + container.size + "::" + container.lastBytesContent.length);
                       					}
                       					
                       					
                       				}*/
                               double thisTime = 0.0;
                               int card = 0;
                               if (op.equals(AND)) {
                            	   double start = 0;
                            	   double stop = 0;
                            	   for(int j = 0; j<iteration_count;j++)
                            	   {
                            		   start = System.nanoTime();
                            		   id1 = CAMP_IO.input(str1);
                            		   id2 = CAMP_IO.input(str2);
                            		   result_index = operation.intersection(id1, id2);
                            		   stop = System.nanoTime();
                            		   if(j > omit_time)
                            		   {
                            			   thisTime += stop - start;
                            		   }
                            	   }
                            	   
//                                       thisTime = avgSeconds(new Computation() {
//                                               @Override
//                                               public void compute() {
//                                            	   
//                                               }
//                                       });
                                       totalTimes.put(timeKey,
                                               thisTime + totalTimes.get(timeKey));
                               }
//                                else if (op.equals(OR)) {
//                                       thisTime = avgSeconds(new Computation() {
//                                               @Override
//                                               public void compute() {
//                                                       RoaringBitmap result = RoaringBitmap
//                                                               .or(bm1, bm2);
//                                                       testSyntheticAndOperation2.junk += result
//                                                               .getCardinality(); // cheap
//                                               }
//                                       });
//                                       totalTimes.put(timeKey,
//                                               thisTime + totalTimes.get(timeKey));
//                               } else if (op.equals(XOR)) {
//                                       thisTime = avgSeconds(new Computation() {
//                                               @Override
//                                               public void compute() {
//                                                       RoaringBitmap result = RoaringBitmap
//                                                               .xor(bm1, bm2);
//                                                       testSyntheticAndOperation2.junk += result
//                                                               .getCardinality(); // cheap
//                                               }
//                                       });
//                                       totalTimes.put(timeKey,
//                                               thisTime + totalTimes.get(timeKey));
//                               }
                      
                
                }
                /***************************************************************************/
                else if (format.equals(COMPAX)) {
                	
                		String str1 = "COMPAX/" + type + "_" + density + "/" + (2 * i) + ".txt";
                		String str2 = "COMPAX/" + type + "_" + density + "/" + (2 * i + 1) + ".txt";
                        COMPAXSet bm1 = CAMP_IO_set.in_compax(str1);
                        COMPAXSet bm2 = CAMP_IO_set.in_compax(str2);
                        COMPAXSet result_compax;
                        if (sizeof) {
                                long theseSizesInBits = 8 * (SizeOf
                                        .deepSizeOf(bm1) + SizeOf
                                        .deepSizeOf(bm2));
                                totalSizes.put(spaceKey, theseSizesInBits
                                        + totalSizes.get(spaceKey));
 //                               System.out.println("Bitset size:"+theseSizesInBits);
                        }
                        double thisTime = 0.0;
                        if (op.equals(AND)) {
                        	double start = System.nanoTime();
                        	double stop = System.nanoTime();
                      	   for(int j = 0; j<iteration_count;j++)
                      	   {
                      		 start = System.nanoTime();
                      		 bm1 = CAMP_IO_set.in_compax(str1);
                      		 bm2 = CAMP_IO_set.in_compax(str2);
                      		 result_compax = COMPAXSet.intersection(bm1,bm2);
                             stop = System.nanoTime();
	                  		   if(j > omit_time)
	                  		   {
	                  			   thisTime += stop - start;
	                  		   }
                      	   }
                      	   
//                                thisTime = avgSeconds(new Computation() {
//                                        @Override
//                                        public void compute() {
//                                                BitSet result;
//                                                result = (BitSet) bm1.clone();
//                                                result.and(bm2);
//                                                testSyntheticAndOperation2.junk += result
//                                                        .size(); // cheap
//                                        }
//                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        }
                }
                /***************************************************************************/
                else if (format.equals(WAH)) {
                	
	                	String str1 = "WAH/" + type + "_" + density + "/" + (2 * i) + ".txt";
	            		String str2 = "WAH/" + type + "_" + density + "/" + (2 * i + 1) + ".txt";
                        WAHSet bm1 = CAMP_IO_set.in_WAH(str1);
                        WAHSet bm2 = CAMP_IO_set.in_WAH(str2);
                        WAHSet result_wah;
                        
                        if (sizeof) {
                                long theseSizesInBits = 8 * (SizeOf
                                        .deepSizeOf(bm1) + SizeOf
                                        .deepSizeOf(bm2));
                                totalSizes.put(spaceKey, theseSizesInBits
                                        + totalSizes.get(spaceKey));
 //                               System.out.println("WAH size:"+theseSizesInBits);
                        }
                        double thisTime = 0.0;
                        if (op.equals(AND)) {
                        	double start = System.nanoTime();
                        	double stop = System.nanoTime();
                       	   for(int j = 0; j<iteration_count;j++)
                       	   {
                       		start = System.nanoTime();
                       		bm1 = CAMP_IO_set.in_WAH(str1);
                       		bm2 = CAMP_IO_set.in_WAH(str2);
                       		result_wah = WAHSet.intersection(bm1, bm2);
                       		stop = System.nanoTime();
                       		if(j > omit_time)
                       			thisTime += stop - start;
                       		
                       	   }
                       	   
                       	   
//                                thisTime = avgSeconds(new Computation() {
//                                        @Override
//                                        public void compute() {
//                                                ConciseSet result = bm1.intersection(bm2);
//                                                testSyntheticAndOperation2.junk += result
//                                                        .isEmpty() ? 1 : 0; // cheap???
//                                        }
//                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        }
//                        if (op.equals(OR)) {
//                                thisTime = avgSeconds(new Computation() {
//                                        @Override
//                                        public void compute() {
//                                                ConciseSet result = bm1.union(bm2);
//
////                                                testSyntheticAndOperation4.junk += result
////                                                        .isEmpty() ? 1 : 0; // dunno
//                                                                            // if
//                                                                            // cheap
//                                                                            // enough
//                                        }
//                                });
//                                totalTimes.put(timeKey,
//                                        thisTime + totalTimes.get(timeKey));
//                        }
//                        if (op.equals(XOR)) {
//                                thisTime = avgSeconds(new Computation() {
//                                        @Override
//                                        public void compute() {
//                                                ConciseSet result = bm1.symmetricDifference(bm2);
//
////                                                testSyntheticAndOperation4.junk += result
////                                                        .isEmpty() ? 1 : 0; // cheap???
//                                        }
//                                });
//                                totalTimes.put(timeKey,
//                                        thisTime + totalTimes.get(timeKey));
//                        }
                }
                /***************************************************************************/
                else if (format.equals(CONCISE)) {
	                	String str1 = "CONCISE/" + type + "_" + density + "/" + (2 * i) + ".txt";
	            		String str2 = "CONCISE/" + type + "_" + density + "/" + (2 * i + 1) + ".txt";
                        ImmutableConciseSet  bm1 = CAMP_IO_set.in_concise(str1);
                        ImmutableConciseSet  bm2 = CAMP_IO_set.in_concise(str2);
                        ImmutableConciseSet result_concise;
                        if (sizeof) {
                                long theseSizesInBits = 8 * (SizeOf
                                        .deepSizeOf(bm1) + SizeOf
                                        .deepSizeOf(bm2));
                                totalSizes.put(spaceKey, theseSizesInBits
                                        + totalSizes.get(spaceKey));
                        }
                        double thisTime = 0.0;
                        if (op.equals(AND)) {
                        	double start = System.nanoTime();
                        	double stop = System.nanoTime();
                       	   for(int j = 0; j<iteration_count;j++)
                       	   {
                       		start = System.nanoTime();
                       		bm1 =  CAMP_IO_set.in_concise(str1);
                       		bm2 = CAMP_IO_set.in_concise(str2);
                       		result_concise = ImmutableConciseSet.intersection(bm1, bm2);
                       		stop = System.nanoTime();
                       		if(j > omit_time)
                       			thisTime += stop - start;
                       		
                       	   }
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } 
                }
        }
                

  
        static double avgSeconds(Computation toDo) {
                int ntrials = 10;
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
