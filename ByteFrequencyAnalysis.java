import java.io.*;
import java.util.Scanner;

public class ByteFrequencyAnalysis {

    public static void main(String[] args) throws Exception{
        File file = new File("C:\\Users\\prasa\\Desktop\\ResumePrasadNS.pdf");
        File fingerprint = new File("C:\\Users\\prasa\\Desktop\\TextPlain.txt");
        FileInputStream fIS = new FileInputStream(file);
        Scanner sc = new Scanner(new FileReader(fingerprint));
        int numFiles = sc.nextInt();


        byte[] byteFile = new byte[(int) file.length()];
        fIS.read(byteFile);
        fIS.close();
        int[] byteCount = new int[256];
        for(int i = 0; i < byteFile.length; i++){
            byteCount[0xFF & byteFile[i]]++;
        }
        int max = 0;
        for(int i = 0; i < byteCount.length; i++){
            if(byteCount[i] > max){
                max = byteCount[i];
            }
        }
        System.out.println(max);
        for(int i = 0; i < byteCount.length; i++){
            System.out.println(i+" " + (byteCount[i]*1.0/max));
        }

    }
}
