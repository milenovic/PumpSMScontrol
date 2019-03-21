/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package PDU;

/**
 * Class that has some useful functions
 *
 * @author Nikita Kapitonov <nikita.kapitonov@gmail.com> / www.teplomonitor.ru
 */
public class Utilities {
 
    /**
     * Returns boolean array with required length from integer with necessary nulls in MSB, or cropped omitting MSB.
     * @param i The integer to be converted to boolean array
     * @param w The required length of the returned array
     * @return boolean[] <b>BinaryArray</b> of required length "w",<br> <b>NULL</b> if "w" is negative
     */
    static public boolean[] intToBinaryArrayFixedWidth(int i, int w) {
        if (w < 0) { //If the length is negative
            return null; //Error
        }
        boolean[] t = intToBinaryArray(i); //Make raw array
        if (t.length > w) { //If the resulting length is bigger than we need
            boolean[] result = new boolean[w];  //Make an array of the required length
            System.arraycopy(t, 0, result, 0, w); //And copy data there from raw array
            return result; //Done
        } else if (t.length < w) { //If the resulting length is less than we need
            boolean[] result = new boolean[w];  //Make an array of the required length
            System.arraycopy(t, 0, result, 0, t.length); //And copy data there from raw array
            return result; //Done
        } else { //If the resulting length is exactly that we need
            return t; //Done
        }
    }
 
    /**
     * Helps to know what bit is at some position in some number
     * @param number in which we search a bit
     * @param bitPosition at which we want to know a bit
     * @return bit that is on bitPosition at number
     * @throws IllegalArgumentException if bitPosition is negative
     */
    public static boolean bitAt(int number, int bitPosition) throws IllegalArgumentException {
        if (bitPosition >= 0) {
            return ((number >> bitPosition) & 1) == 1 ? true : false;
        } else {
            throw new IllegalArgumentException();
        }
    }
 
    /**
     * Creates a boolean array representation of the integer argument as an unsigned integer in base 2.<br>
     * <br>
     * The unsigned integer value is the argument plus 2^32 if the argument is negative; otherwise it is equal to the argument.
     * This value is converted to a binary array (base 2) with no extra leading 0s.
     * If the unsigned magnitude is zero, it is represented by a single zero character '0' (false);
     * otherwise, the last character of the representation of the unsigned magnitude will not be the zero character.
     * The characters '0' (false) and '1' (true) are used as binary digits.
     * @param n
     * @return the boolean array representation of the unsigned integer value represented by the argument in binary (base 2).
     */
    public static boolean[] intToBinaryArray(int n) {
        int l = 0;
        int nShifted = n;
 
        while (nShifted != 0) {
            l++;
            nShifted >>>= 1;
        }
        if (l == 0) {
            return new boolean[]{false};
        }
        boolean[] result = new boolean[l];
 
        for (int i = 0; i
                < l; i++) {
            result[i] = ((n >> i) & 1) == 1 ? true : false;
        }
        return result;
    }
 
    /**
     * Creates an integer from boolean array treating it as binary number
     * @param arr binary number
     */
    public static int binaryArrayToInt(boolean[] arr) {
        int result = 0;
        int i = 0;
 
        while (i < arr.length) {
            result += (arr[i] ? 1 << i : 0);
            i++;
        }
        return result;
    }
 
    /**
     * Creates an integer from subset of integer-to-boolean-array boolean array treating it as binary number
     * @param n the number from which we'll generate new number
     * @param from bit position from which we'll start to calculate
     * @param till bit position on which we'll end to calculate (included)
     * @return
     */
    public static int intFromIntegerSubset(int n, int from, int till) throws IllegalArgumentException {
        int result = 0;
        int i = 0;
 
        if (from <= till) {
            while (from + i <= till) {
                result += (bitAt(n, from + i) ? 1 << i : 0);
                i++;
            }
            return result;
        } else {
            throw new IllegalArgumentException();
        }
    }
 
 
    private static char[] intToHexBuffer;
    private static final char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    /**
     * Returns Hex-string with required width from integer with necessary nulls in front.
     * @param num The integer to be converted to hex string
     * @param w The required width of the returned string
     * @return String <b>Hex-string</b> of required width "w",<br> <b>NULL</b> if "w" is negative
     */
    public static synchronized String intToHexFixedWidth(int num, int w) {
        if (w < 0) {
            return null;
        }
        if (intToHexBuffer == null || intToHexBuffer.length < w) {
            intToHexBuffer = new char[w];
        }
        for (int i = 0; i < w; i++) {
            intToHexBuffer[w - i - 1] = hexChars[((num >>> (4 * i)) & (0x0F))];
        }
        return new String(intToHexBuffer, 0, w);
    }
 
}