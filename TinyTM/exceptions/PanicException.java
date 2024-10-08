package TinyTM.exceptions;
/*
 *
 * Initial code taken from:
 * From "The Art of Multiprocessor Programming",
 * by Maurice Herlihy and Nir Shavit.
 *
 * + Fixed Bugs
 * + Merged abstractions
 * + Added validation through a read set
 * + Added Distributed STM
 *
 * Universidade Federal de Pelotas 2022
 * 
 * This work is licensed under a Creative Commons Attribution-Share Alike 3.0 United States License.
 * http://i.creativecommons.org/l/by-sa/3.0/us/88x31.png
 */

/**
 * Thrown to indicate an error in the use of the transactional memory;
 * that is, a violation of the assumptions of use.
 */
public class PanicException extends java.lang.RuntimeException {

    /**
     * Creates new <code>PanicException</code> with no detail message.
     */
    public PanicException() {
    }

    public PanicException(String format, Object... args) {
        super(String.format(format, args));
    }

    /**
     * Creates a new <code>PanicException</code> with the specified detail message.
     * 
     * @param msg the detail message.
     */
    public PanicException(String msg) {
        super(msg);
    }

    /**
     * Creates an <code>PanicException</code> with the specified cause.
     * 
     * @param cause Throwable that caused PanicException to be thrown
     */
    public PanicException(Throwable cause) {
        super(cause);
    }
}
