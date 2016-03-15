package org.CAMP;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
public class testSyntheticMemoryUsage {
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

        
        static final int NTRIALS=10;
        
    //    static final String[] formats = { EWAH32, EWAH64, CONCISE, WAH, BITSET, ROARING };
        
        static final String[] formats = {CAMP_block,CAMP,ROARING,WAH,BITSET};

        static int junk = 0; // to fight optimizer.

        static long LONG_ENOUGH_NS = 1000L * 1000L * 1000L;
        private static UniformDataGenerator uniformDataGeneror=new UniformDataGenerator();
    	private static ClusteredDataGenerator clusteredDataGenerator=new ClusteredDataGenerator();
    	public static int max_num = 100000;
    	public static double []densities={0.0001,0.001,0.005,0.01,0.02,0.04,0.05,0.08,0.1,0.25,0.5};
    	public static int iteration_count=60;
    	
        public static void testDataset(double density, String type){
    		
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
                                            totalSizes, sizeof,
                                            dataset,
                                            dataset);
        		}
        		else
        		{
        			dataset= clusteredDataGenerator.generateClustered(integer_count,max_num);
                    for (String op : ops)
                            for (String format : formats)
                                    test(op, format, totalTimes,
                                            totalSizes, sizeof,
                                            dataset,
                                            dataset);
        		}
    		}
            
            if (sizeof) {
                    System.out.println("Size ratios");
                    System.out.println("density:"+density);
                    double baselineSize = totalSizes.get(CAMP_block);
                   
                    for (String format : formats) {
                            double thisSize = totalSizes.get(format);
                            System.out.printf("%s\t%5.2f\n", format,
                                    thisSize / baselineSize);
                    }
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
        public static void main(final String[] args) {
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
                boolean sizeof, int[] data1, int[] data2) {
                String timeKey = op + ";" + format;
                String spaceKey = format;
              
                /***************************************************************************/
                if (format.equals(CAMP_block)) {
                
                	final index_set id1 = create_index_set.bitmapof(data1, max_num);
                	final index_set id2 = create_index_set.bitmapof(data2, max_num);
                      if (sizeof) {
                               long theseSizesInBits = 8 * (SizeOf
                                       .deepSizeOf(id1) + SizeOf
                                       .deepSizeOf(id2));
                               totalSizes.put(spaceKey, theseSizesInBits
                                       + totalSizes.get(spaceKey));
                               
  //                             System.out.println("ZVBitmap size:"+theseSizesInBits);
                               /*for(int i=0;i<bm1.zvArray.size;i++){
                       			//		System.out.println(zvb1.zvArray.elementArray[i].getContainerLevel()+": "+zvb1.zvArray.elementArray[i].getCardinality());
                       				//	if(zvb1.zvArray.elementArray[i].getContainerLevel()==2)
                      
                       					
                       					if(bm1.zvArray.elementArray[i].getContainerLevel()==3){
                       						ZvArrayContainer3 container = (ZvArrayContainer3)(bm1.zvArray.elementArray[i]);
                       						System.out.println("container3:" + container.size + "::" + container.lastBytesContent.length);
                       					}
                       					
                       					
                       				}*/
                      }
                
                }
                else
                	if (format.equals(CAMP)) {
                        
                    	final index id1 = create_index.bitmapof(data1, max_num);
                    	final index id2 = create_index.bitmapof(data2, max_num);
                          if (sizeof) {
                                   long theseSizesInBits = 8 * (SizeOf
                                           .deepSizeOf(id1) + SizeOf
                                           .deepSizeOf(id2));
                                   totalSizes.put(spaceKey, theseSizesInBits
                                           + totalSizes.get(spaceKey));
                                   
      //                             System.out.println("ZVBitmap size:"+theseSizesInBits);
                                   /*for(int i=0;i<bm1.zvArray.size;i++){
                           			//		System.out.println(zvb1.zvArray.elementArray[i].getContainerLevel()+": "+zvb1.zvArray.elementArray[i].getCardinality());
                           				//	if(zvb1.zvArray.elementArray[i].getContainerLevel()==2)
                          
                           					
                           					if(bm1.zvArray.elementArray[i].getContainerLevel()==3){
                           						ZvArrayContainer3 container = (ZvArrayContainer3)(bm1.zvArray.elementArray[i]);
                           						System.out.println("container3:" + container.size + "::" + container.lastBytesContent.length);
                           					}
                           					
                           					
                           				}*/
                          }
                    
                    }
                else  if (format.equals(ROARING)) {
                        final RoaringBitmap bm1 = RoaringBitmap.bitmapOf(data1);
                        final RoaringBitmap bm2 = RoaringBitmap.bitmapOf(data2);
                        bm1.trim();
                        bm2.trim();
                        if (sizeof) {
                                long theseSizesInBits = 8 * (SizeOf
                                        .deepSizeOf(bm1) + SizeOf
                                        .deepSizeOf(bm2));
                                totalSizes.put(spaceKey, theseSizesInBits
                                        + totalSizes.get(spaceKey));
                                
 //                               System.out.println("Roaring size:"+theseSizesInBits);
                        }
                       
                        
                        double thisTime = 0.0;
                        if (op.equals(AND)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                RoaringBitmap result = RoaringBitmap
                                                        .and(bm1, bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .getCardinality(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } else if (op.equals(OR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                RoaringBitmap result = RoaringBitmap
                                                        .or(bm1, bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .getCardinality(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } else if (op.equals(XOR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                RoaringBitmap result = RoaringBitmap
                                                        .xor(bm1, bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .getCardinality(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } /*else
                                throw new RuntimeException("unknown op " + op);*/
                }
                /***************************************************************************/
                else if (format.equals(BITSET)) {
                        final BitSet bm1 = toBitSet(data1);
                        final BitSet bm2 = toBitSet(data2);
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
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                BitSet result;
                                                result = (BitSet) bm1.clone();
                                                result.and(bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .size(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } else if (op.equals(OR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                BitSet result;
                                                result = (BitSet) bm1.clone();
                                                result.or(bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .size(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } else if (op.equals(XOR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                BitSet result;
                                                result = (BitSet) bm1.clone();
                                                result.xor(bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .size(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } /*else
                                throw new RuntimeException("unknown op " + op);*/
                }
                /***************************************************************************/
                else if (format.equals(WAH)) {
                        final ConciseSet bm1 = toConciseWAH(data1);
                        final ConciseSet bm2 = toConciseWAH(data2);
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
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                ConciseSet result = bm1.intersection(bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .isEmpty() ? 1 : 0; // cheap???
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        }
                        if (op.equals(OR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                ConciseSet result = bm1.union(bm2);

                                                testSyntheticMemoryUsage.junk += result
                                                        .isEmpty() ? 1 : 0; // dunno
                                                                            // if
                                                                            // cheap
                                                                            // enough
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        }
                        if (op.equals(XOR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                ConciseSet result = bm1.symmetricDifference(bm2);

                                                testSyntheticMemoryUsage.junk += result
                                                        .isEmpty() ? 1 : 0; // cheap???
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        }
                }
                /***************************************************************************/
                else if (format.equals(EWAH32)) {
                        final EWAHCompressedBitmap32 bm1 = EWAHCompressedBitmap32
                                .bitmapOf(data1);
                        final EWAHCompressedBitmap32 bm2 = EWAHCompressedBitmap32
                                .bitmapOf(data2);
                        bm1.trim();
                        bm2.trim();
                        if (sizeof) {
                                long theseSizesInBits = 8 * (SizeOf
                                        .deepSizeOf(bm1) + SizeOf
                                        .deepSizeOf(bm2));
                                totalSizes.put(spaceKey, theseSizesInBits
                                        + totalSizes.get(spaceKey));
                        }
                        double thisTime = 0.0;
                        if (op.equals(AND)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                EWAHCompressedBitmap32 result = EWAHCompressedBitmap32
                                                        .and(bm1, bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .sizeInBits(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } else if (op.equals(OR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                EWAHCompressedBitmap32 result = EWAHCompressedBitmap32
                                                        .or(bm1, bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .sizeInBits(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } else if (op.equals(XOR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                EWAHCompressedBitmap32 result = EWAHCompressedBitmap32
                                                        .xor(bm1, bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .sizeInBits(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } /*else
                                throw new RuntimeException("unknown op " + op);*/
                }
                /***************************************************************************/
                else if (format.equals(EWAH64)) {
                        final EWAHCompressedBitmap bm1 = EWAHCompressedBitmap
                                .bitmapOf(data1);
                        final EWAHCompressedBitmap bm2 = EWAHCompressedBitmap
                                .bitmapOf(data2);
                        bm1.trim();
                        bm2.trim();
                        if (sizeof) {
                                long theseSizesInBits = 8 * (SizeOf
                                        .deepSizeOf(bm1) + SizeOf
                                        .deepSizeOf(bm2));
                                totalSizes.put(spaceKey, theseSizesInBits
                                        + totalSizes.get(spaceKey));
                        }
                        double thisTime = 0.0;
                        if (op.equals(AND)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                EWAHCompressedBitmap result = EWAHCompressedBitmap
                                                        .and(bm1, bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .sizeInBits(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } else if (op.equals(OR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                EWAHCompressedBitmap result = EWAHCompressedBitmap
                                                        .or(bm1, bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .sizeInBits(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } else if (op.equals(XOR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                EWAHCompressedBitmap result = EWAHCompressedBitmap
                                                        .xor(bm1, bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .sizeInBits(); // cheap
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        } /*else
                                throw new RuntimeException("unknown op " + op);*/
                }

                /***************************************************************************/
                else if (format.equals(CONCISE)) {
                        final ConciseSet bm1 = toConcise(data1);
                        final ConciseSet bm2 = toConcise(data2);
                        if (sizeof) {
                                long theseSizesInBits = 8 * (SizeOf
                                        .deepSizeOf(bm1) + SizeOf
                                        .deepSizeOf(bm2));
                                totalSizes.put(spaceKey, theseSizesInBits
                                        + totalSizes.get(spaceKey));
                        }
                        double thisTime = 0.0;
                        if (op.equals(AND)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                ConciseSet result = bm1.intersection(bm2);
                                                testSyntheticMemoryUsage.junk += result
                                                        .isEmpty() ? 1 : 0; // cheap???
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        }
                        if (op.equals(OR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                ConciseSet result = bm1.union(bm2);

                                                testSyntheticMemoryUsage.junk += result
                                                        .isEmpty() ? 1 : 0; // dunno
                                                                            // if
                                                                            // cheap
                                                                            // enough
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
                        }
                        if (op.equals(XOR)) {
                                thisTime = avgSeconds(new Computation() {
                                        @Override
                                        public void compute() {
                                                ConciseSet result = bm1.symmetricDifference(bm2);

                                                testSyntheticMemoryUsage.junk += result
                                                        .isEmpty() ? 1 : 0; // cheap???
                                        }
                                });
                                totalTimes.put(timeKey,
                                        thisTime + totalTimes.get(timeKey));
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