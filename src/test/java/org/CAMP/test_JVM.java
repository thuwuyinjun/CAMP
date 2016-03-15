package org.CAMP;
public class test_JVM {
 
    /**
      * @param args
      */
    public static void main(String[] args) {
       System.out.println( " .... :" + toMemoryInfo ());
    }
 
    /**
      * .... jvm .....
      *
      * @return
      */
    public static String toMemoryInfo() {
 
       Runtime currRuntime = Runtime.getRuntime ();
       int nFreeMemory = ( int ) (currRuntime.freeMemory() / 1024 / 1024);
       int nTotalMemory = ( int ) (currRuntime.totalMemory() / 1024 / 1024);
       return nFreeMemory + "M/" + nTotalMemory + "M(free/total)" ;
    }
}
