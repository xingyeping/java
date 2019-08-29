import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.out;

public class HelloWorld {
    public static void main(String[] args) {
        try {
            out.println(args[0]);
            InputStream is = new FileInputStream(args[0]);
            byte[] b = null;
            try {
                b = new byte[is.available()];
                is.read(b);
                is.close();
            } catch (IOException e) {
                out.println("io err");
            }


            out.println(JavaClassExecuter.execute(b));
        } catch ( FileNotFoundException e) {
            out.println("not found");
        } catch (SecurityException e) {
            out.println("security");
        }
    }
}
