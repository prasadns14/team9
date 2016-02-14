import java.io.*;
import java.util.Scanner;

public class ByteFrequencyAnalysis {

    public static void main(String[] args) throws Exception{
        
		public static final String[] fileTypes = {"text\\plain", "text\\pdf", "text\\rdf+xml", "text\\rsf+xml", "text\\xhtml+xml", "text\\html", "image\\png", "image\\jpeg", "audio\\mpeg", "video\\mp4", "video\\quicktime", "applicaiton\\x-sh", "application\\gzip", "application\\msword", "application\\octet-stream"};

    public static void main(String[] args) throws Exception{

        for(int i = 0; i < fileTypes.length; i++) {

            //Selecting the file type and setting the path to the folder which has the data and fingerprint file corresponding to that file type
            File fileType = new File("D:\\PolarDump\\"+fileTypes[i]+"\\data");
            String fingerprintFile = fileTypes[i].replace("\\","-");
            File fingerprint = new File("D:\\PolarDump\\"+fileTypes[i]+"\\"+fingerprintFile+".txt");
            File[] files = fileType.listFiles();

            //Reading over each file of the selected type
            for(File file : files) {
                FileInputStream fIS = new FileInputStream(file);
                Scanner sc = new Scanner(new FileReader(fingerprint));
                int numFiles = sc.nextInt();
                System.out.println(numFiles);
                sc.nextLine();
                String[] terms = sc.nextLine().split("\\t");
                double[] byteFreq = new double[256];
                double[] byteCorrelation = new double[256];
                for (int j = 0; j < 256; j++) {
                    String[] values = terms[j].split(",");
                    byteFreq[j] = Double.parseDouble(values[0]);
                    byteCorrelation[j] = Double.parseDouble(values[1]);
                }
                byte[] byteFile = new byte[(int) file.length()];
                fIS.read(byteFile);
                fIS.close();
                double[] byteCount = new double[256];

                //calculating its byte frequency distribution
                for (int j = 0; j < byteFile.length; j++) {
                    byteCount[0xFF & byteFile[j]]++;
                }

                //Finding the max occurring byte
                double max = byteCount[0];
                int count = 0;
                for (int j = 1; j < byteCount.length; j++) {
                    if (byteCount[j] > max) {
                        max = byteCount[j];
                    }
                    count += byteCount[j];
                }

                //Normalizing the byte frequency
                for (int j = 0; j < byteCount.length; j++) {
                    byteCount[j] = byteCount[j] / max;
                }

                //Compounding the byte frequency if needed
                if (max > (0.7 * count)) {
                    for (int j = 0; j < 256; j++) {
                        byteCount[j] = Math.pow(byteCount[j], 0.5);
                    }
                }

                //Updating the fingerprint file
                for (int j = 0; j < 256; j++) {
                    byteFreq[j] = ((byteFreq[j] * numFiles) + byteCount[j]) / (numFiles + 1);
                }

                sc.close();
                FileWriter fw = new FileWriter(fingerprint);
                fw.write(Integer.toString(numFiles + 1));

                fw.write("\n");
                for (int j = 0; j < 256; j++) {
                    String temp = byteFreq[j] + "," + byteCorrelation[j] + "\t";
                    fw.write(temp);
                }
                fw.write("\n");
                fw.close();
            }
        }
    }

    }
}
