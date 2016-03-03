import java.io.*;
import java.util.Scanner;

import org.json.simple.JSONObject;

public class ByteFrequencyAnalysis {
    public static final String[] fileTypes = {"application\\octet-stream"};

    public static void main(String[] args) throws Exception{

        if(args.length < 1){
            System.out.println("Usage: ByteFrequencyAnalysis.java <files_location>");
        }else {

            double[] byteFreq = new double[256];
            double[] byteCorrelation = new double[256];

            for (int i = 0; i < fileTypes.length; i++) {
                String sourceDir = new java.io.File(args[0]).getCanonicalPath();
                //Selecting the file type and setting the path to the folder which has the data and fingerprint file corresponding to that file type
                File fileType = new File(sourceDir + "\\" + fileTypes[i] + "\\data");
                String fingerprintFile = fileTypes[i].replace("\\", "-");
                String tarDir = new java.io.File(".").getCanonicalPath();
                System.out.println(tarDir);
                File fingerprint = new File(tarDir + "\\src\\main\\resources\\" + "fingerprint_files" + "\\" + fingerprintFile + ".txt");
                File[] files = fileType.listFiles();
                System.out.println(fileType);
                System.out.println(fingerprint);
                if (!fingerprint.exists()) {
                    fingerprint.createNewFile();
                    FileWriter fw = new FileWriter(fingerprint);
                    fw.write("0");// no of files processed = 0
                    fw.write("\n");
                    for (int k = 0; k < 256; k++) {
                        String temp = Double.toString(0.0) + "," + Double.toString(0.0) + "\t";
                        fw.write(temp);
                    }
                    fw.close();
                }


                //Reading over each file of the selected type
                for (File file : files) {
                    FileInputStream fIS = new FileInputStream(file);
                    Scanner sc = new Scanner(new FileReader(fingerprint));

                    int numFiles = sc.nextInt();
                    System.out.println(numFiles);
                    sc.nextLine();
                    String[] terms = sc.nextLine().split("\\t");

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
                JSONObject json = new JSONObject();
                for (int j = 0; j < 256; j++) {
                    json.put(j, byteFreq[j]);
                }
                FileWriter jsonFile = new FileWriter(tarDir + "\\src\\main\\resources\\visualization_files" + "\\" + fingerprintFile + ".json");
                jsonFile.write(json.toJSONString());
                jsonFile.close();
                System.out.println(json);
            }
        }
    }
}
