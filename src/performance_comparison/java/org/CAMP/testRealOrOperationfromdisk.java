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

import net.sourceforge.sizeof.SizeOf;
import it.uniroma3.mat.extendedset.intset.ConciseSet;

/**
 * O. Kaser's benchmark over real data
 * 
 */
public class testRealOrOperationfromdisk {
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
        
        static final int NTRIALS=100;
        
    //    static final String[] formats = { EWAH32, EWAH64, CONCISE, WAH, BITSET, ROARING };
        
        static final String[] formats = {CAMP,COMPAX,CONCISE, WAH};
        
        static final int [] maxs = {4277807, 199523, 1178559, 1015367};

        static int junk = 0; // to fight optimizer.

        static long LONG_ENOUGH_NS = 1000L * 1000L * 1000L;
        
        public static int iteration_count = 100;
        
        
        public static void testDataset(String path,String dataset, int max) throws IOException{
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

            RealDataRetriever dataSrc = new RealDataRetriever(path);
            HashMap<String, Double> totalTimes = new HashMap<String, Double>();
            HashMap<String, Double> totalSizes = new HashMap<String, Double>();
            for (String op : ops)
                    for (String format : formats) {
                            totalTimes.put(op + ";" + format, 0.0);
                            totalSizes.put(format, 0.0); // done more than
                                                         // necessary
                    }

            for (int i = 0; i < NTRIALS; ++i)
                    for (String op : ops)
                            for (String format : formats)
                                    test(op, format, totalTimes,
                                            totalSizes, sizeof,
                                            dataSrc.fetchBitPositions(
                                                    dataset + ".csv", 2 * i),
                                            dataSrc.fetchBitPositions(
                                                    dataset + ".csv", 2 * i + 1), max,dataset, i);
            System.out.println(dataset);
            if (sizeof) {
                    System.out.println("Size ratios");
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
                                            format, op,ttime, ttime / baseline);
                    }
            }
            System.out.println("ignore this " + junk);

        	
        	
        }

        @SuppressWarnings("javadoc")
        public static void main(final String[] args) throws IOException {
        	ArrayList<String> datasets= new ArrayList<String>();
        	String path="/home/wuyinjun/workspace/zenvisagedb/real-roaring-datasets";
        	datasets.add(("census1881"));   
        	datasets.add(("census-income"));   
           	datasets.add("wikileaks-noquotes");
        	//datasets.add(("uscensus2000.csv"));
         	datasets.add("weather_sept_85");
         	for(int i = 0; i<datasets.size(); i++)
         	{
         		testDataset(path,datasets.get(i), maxs[i]);
         	}
//        	for(String dataset:datasets){
//        		testDataset(path,dataset);
//        	}
              	
        	
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
                boolean sizeof, int[] data1, int[] data2, int max, String dataset, int i) throws IOException {
                String timeKey = op + ";" + format;
                String spaceKey = format;
              
                /***************************************************************************/
                if (format.equals(CAMP)) {
                
                	   File f1 = new File (format + "/" + dataset);
                	   if(!f1.exists())
                	   {
                		   f1.mkdirs();
                	   }
                	   
                	   String str1 = format + "/" + dataset + "/" + (2 * i) + ".txt";
                	   String str2 = format + "/" + dataset + "/" + (2 * i + 1) + ".txt";
                	   
                	   index_set id1 = CAMP_IO_set.input( str1);
                	   index_set id2 = CAMP_IO_set.input(str2);
                	   
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
                    		   result_index = operation_set.union(id1, id2);
                    		   stop = System.nanoTime();
                    		   if(i > 5)
                    		   {
                    			   thisTime += stop - start;
                    		   }
                    	   }
                    	   
//                               thisTime = avgSeconds(new Computation() {
//                                       @Override
//                                       public void compute() {
//                                    	   
//                                       }
//                               });
                               totalTimes.put(timeKey,
                                       thisTime + totalTimes.get(timeKey));
                       }
                      if (sizeof) {
//                               long theseSizesInBits = 8 * (SizeOf
//                                       .deepSizeOf(bm1) + SizeOf
//                                       .deepSizeOf(bm2));
//                               totalSizes.put(spaceKey, (double) theseSizesInBits
//                            		   + totalSizes.get(spaceKey));
                               
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
                else  if (format.equals(COMPAX)) {
                        
                        File f1 = new File (format + "/" + dataset);
                 	   if(!f1.exists())
                 	   {
                 		   f1.mkdirs();
                 	   }
                 	   
                 	   String str1 = format + "/" + dataset + "/" + (2 * i) + ".txt";
                 	   String str2 = format + "/" + dataset + "/" + (2 * i + 1) + ".txt";
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
                    		 result_compax = COMPAXSet.union(bm1,bm2);
                           stop = System.nanoTime();
	                  		   if(i > 5)
	                  		   {
	                  			   thisTime += stop - start;
	                  		   }
                    	   }
                    	   
//                              thisTime = avgSeconds(new Computation() {
//                                      @Override
//                                      public void compute() {
//                                              BitSet result;
//                                              result = (BitSet) bm1.clone();
//                                              result.and(bm2);
//                                              testSyntheticAndOperation2.junk += result
//                                                      .size(); // cheap
//                                      }
//                              });
                              totalTimes.put(timeKey,
                                      thisTime + totalTimes.get(timeKey));
                      }
                }
                /***************************************************************************/
                else if (format.equals(BITSET)) {
                        final BitSet bm1 = toBitSet(data1);
                        final BitSet bm2 = toBitSet(data2);
                        if (sizeof) {
                                long theseSizesInBits = 8 * (SizeOf
                                        .deepSizeOf(bm1) + SizeOf
                                        .deepSizeOf(bm2));
                                totalSizes.put(spaceKey, (double) theseSizesInBits
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
                                                testRealOrOperationfromdisk.junk += result
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
                                                testRealOrOperationfromdisk.junk += result
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
                                                testRealOrOperationfromdisk.junk += result
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
                        
                        File f1 = new File (format + "/" + dataset);
                  	   if(!f1.exists())
                  	   {
                  		   f1.mkdirs();
                  	   }
                  	   
                  	   String str1 = format + "/" + dataset + "/" + (2 * i) + ".txt";
                  	   String str2 = format + "/" + dataset + "/" + (2 * i + 1) + ".txt";
                         
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
                      		result_wah = WAHSet.union(bm1, bm2);
                      		stop = System.nanoTime();
                      		if(i > 5)
                      			thisTime += stop - start;
                      		
                      	   }
                      	   
                      	   
//                               thisTime = avgSeconds(new Computation() {
//                                       @Override
//                                       public void compute() {
//                                               ConciseSet result = bm1.intersection(bm2);
//                                               testSyntheticAndOperation2.junk += result
//                                                       .isEmpty() ? 1 : 0; // cheap???
//                                       }
//                               });
                               totalTimes.put(timeKey,
                                       thisTime + totalTimes.get(timeKey));
                       }
//                       if (op.equals(OR)) {
//                               thisTime = avgSeconds(new Computation() {
//                                       @Override
//                                       public void compute() {
//                                               ConciseSet result = bm1.union(bm2);
//
////                                               testSyntheticAndOperation4.junk += result
////                                                       .isEmpty() ? 1 : 0; // dunno
//                                                                           // if
//                                                                           // cheap
//                                                                           // enough
//                                       }
//                               });
//                               totalTimes.put(timeKey,
//                                       thisTime + totalTimes.get(timeKey));
//                       }
//                       if (op.equals(XOR)) {
//                               thisTime = avgSeconds(new Computation() {
//                                       @Override
//                                       public void compute() {
//                                               ConciseSet result = bm1.symmetricDifference(bm2);
//
////                                               testSyntheticAndOperation4.junk += result
////                                                       .isEmpty() ? 1 : 0; // cheap???
//                                       }
//                               });
//                               totalTimes.put(timeKey,
//                                       thisTime + totalTimes.get(timeKey));
//                       }
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
                                                testRealOrOperationfromdisk.junk += result
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
                                                testRealOrOperationfromdisk.junk += result
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
                                                testRealOrOperationfromdisk.junk += result
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
                                                testRealOrOperationfromdisk.junk += result
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
                                                testRealOrOperationfromdisk.junk += result
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
                                                testRealOrOperationfromdisk.junk += result
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
                        File f1 = new File (format + "/" + dataset);
                   	   if(!f1.exists())
                   	   {
                   		   f1.mkdirs();
                   	   }
                   	   
                   	   String str1 = format + "/" + dataset + "/" + (2 * i) + ".txt";
                   	   String str2 = format + "/" + dataset + "/" + (2 * i + 1) + ".txt";
                          
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
                      		result_concise = ImmutableConciseSet.union(bm1, bm2);
                      		stop = System.nanoTime();
                      		if(i > 5)
                      			thisTime += stop - start;
                      		
                      	   }
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