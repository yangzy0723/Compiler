import java.io.*;

public class Main
{    
    public static void main(String[] args) throws IOException {
        InputStream is = System.in;

        String file;
        if(args.length > 0){
            file = args[0];
            is = new FileInputStream(file);
        }

        int data;
        while ((data = is.read()) != -1)
            System.out.print((char) data);

        is.close();
    }
}