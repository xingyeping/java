import java.io.*;

/**
 * 为JavaClass劫持java.lang.System提供支持
 * 除了out和err外，其余的都直接转发给System处理
 */
public class HackSystem {
    public final static InputStream in = System.in;
    private static ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    public final static PrintStream out = new PrintStream(buffer);
    public final static PrintStream err = out;
    static OutputStream file = null;

    public HackSystem () {
        if (file != null) {
            return;
        }
        try {
            file = new FileOutputStream("test.txt");
        } catch (FileNotFoundException e) {
            System.out.println("no file");
        } catch (SecurityException e) {
            System.out.println("security");
        }
    }

    public static String getBufferString() {
        try {
            buffer.writeTo(file);
        } catch (IOException e) {
            System.out.println("file error");
        }
        return buffer.toString();

    }

    public static void clearBuffer() {
        buffer.reset();
    }

    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }


}
