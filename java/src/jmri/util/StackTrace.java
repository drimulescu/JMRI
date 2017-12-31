package jmri.util;

/**
 * Get a stack trace
 */
public final class StackTrace {
    
    private StackTrace() {
    }
    
    
    /**
     * Return stack trace.
     * @param level the level in the stack trace
     * @return a string with the file name and line number
     */
    public static String getStackTrace(final int level) {
        try {
            throw new Exception();
        } catch (Exception ex) {
            StackTraceElement e = ex.getStackTrace()[level];
            ex.printStackTrace();
            return String.format("%s", e.getFileName());
        }
    }
}
