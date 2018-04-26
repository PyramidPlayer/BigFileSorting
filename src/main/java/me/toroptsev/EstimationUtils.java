package me.toroptsev;

public class EstimationUtils {

    private static long OBJ_OVERHEAD;
    static {
        String arch = System.getProperty("sun.arch.data.model");
        // it depends of OS which number of extra bytes per string is used
        OBJ_OVERHEAD = (arch != null && arch.contains("32")) ? 36 : 60;

    }

    public static long estimateAvailableMemory() {
        System.gc();
        // http://stackoverflow.com/questions/12807797/java-get-available-memory
        Runtime r = Runtime.getRuntime();
        long allocatedMemory = r.totalMemory() - r.freeMemory();
        return r.maxMemory() - allocatedMemory;
    }



    public static long estimatedSizeOf(String s) {
        return s.length() * 2 + OBJ_OVERHEAD;
    }
}
