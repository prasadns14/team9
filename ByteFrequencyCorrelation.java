import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ByteFrequencyCorrelation
{
	public static String mimetypes[] = {"text/html"};//,"application/octet-stream","image/jpeg","image/png"};
	public static void main(String args[]) throws IOException
	{
		for(String type:mimetypes)
		{
			String curdir = new java.io.File( "." ).getCanonicalPath();
			String filetypename = type.replace("/","-");
			File fingerprint = new File(curdir+"/"+type+"/"+filetypename+".txt");
			ArrayList<double[]> a = readFingerprint(fingerprint);
			double []fingerprintCounts = a.get(0);
	        double []correlationCounts = a.get(1);
	        int numFiles = (int)a.get(2)[0];
	        
			File testFolder = new File(curdir+"/"+type+"/testfiles");
			File[] testFiles = testFolder.listFiles();
			for(File file:testFiles)
			{
				double inputCounts[] = getInputCounts(file);
				correlationCounts = getCorrelationFactors(inputCounts,fingerprintCounts,correlationCounts,numFiles++);
			}
			double[][] correlationMatrix = getCorrelationMatrix(fingerprintCounts, correlationCounts,numFiles);
			writeFingerprint(type+"/"+filetypename+".txt",fingerprintCounts,correlationCounts,numFiles);
			JSONObject json = new JSONObject();
            for(int j = 0; j < 256; j++){
                json.put(j, correlationCounts[j]);
            }
            FileWriter jsonFile = new FileWriter(curdir+"/"+type+"/"+filetypename+"_correlation.json");
            jsonFile.write(json.toJSONString());
            jsonFile.close();
            
            JSONArray rows = new JSONArray();
            
            for(int i =0;i<256;++i)
            {
	            for(int j = 0; j < 256; j++)
	            {
	            	json = new JSONObject();
	            	json.put("i", i);
	                json.put("j", j);
	                json.put("value", correlationMatrix[i][j]);
	                rows.add(json);
	            }
            }
            JSONObject jsonobj = new JSONObject();
            jsonFile = new FileWriter(curdir+"/"+type+"/"+filetypename+"_correlationMatrix.json");
            jsonobj.put("data", rows);
            //System.out.println(jsonobj);
            jsonFile.write(jsonobj.toJSONString());
            jsonFile.close();
		}
		System.out.println("Done :)");
	}
	
	public static ArrayList<double[]> readFingerprint(File fingerprint) throws FileNotFoundException
	{
		Scanner sc = new Scanner(new FileReader(fingerprint));
        int numFiles = sc.nextInt();
        String s = sc.next();
        ArrayList<double[]> a = new ArrayList<double[]>();
        double []fingerprintCounts = new double[256];
        double []correlationCounts = new double[256];
        double []d = {(double)numFiles};
        // parse and get 2 arrays: fingerprint and correlation
		String line = sc.nextLine();
		String[] freq_corr = line.split("\\t");
		freq_corr[0] = s;
		for(int i=0;i<freq_corr.length;++i)
		{
			String[] sarr = freq_corr[i].split(",");
			fingerprintCounts[i] = Double.parseDouble(sarr[0]);
			correlationCounts[i] = Double.parseDouble(sarr[1]);
		}
		a.add(fingerprintCounts);
		a.add(correlationCounts);
		a.add(d);
        return a;
	}
	
	public static double[] getInputCounts(File file) throws IOException
	{
		FileInputStream fIS = new FileInputStream(file);
		byte[] byteFile = new byte[(int) file.length()];
        fIS.read(byteFile);
        fIS.close();
        int[] byteCount = new int[256];
        double[] normalizedCount = new double[256];
        for(int i = 0; i < byteFile.length; i++){
            byteCount[0xFF & byteFile[i]]++;
        }
        int max = 0, count=0;
        for(int i = 0; i < byteCount.length; i++){
            if(byteCount[i] > max){
                max = byteCount[i];
            }
            count += byteCount[i];
        }
        for(int i = 0; i < byteCount.length; i++){
            normalizedCount[i] = (byteCount[i]/(double)max);
        }
        
        if (max > (0.7 * count)) {
            for (int i = 0; i < 256; ++i) {
               normalizedCount[i] = Math.pow(normalizedCount[i], 0.5);
            }
        }
        return normalizedCount;
	}
	
	/*
		Every byte in the file gets a correlation factor with relation to the mime type.
		Correlation factor of byte = |frequency(byte,inputfile) - frequency(byte,fingerprint)| 
	*/
	public static double[] getCorrelationFactors(double[] inputFileCounts,double[] fingerprintCounts,double[] correlationCounts, int nfiles)
	{
		double[] new_correlationCounts = new double[correlationCounts.length];
		for(int i=0;i<256;++i)
		{
			double corrfactor = getCorrelationStrength(Math.abs(inputFileCounts[i] - fingerprintCounts[i]));
			double new_corrfactor = (correlationCounts[i]*nfiles+corrfactor)/(nfiles+1);
			new_correlationCounts[i] = new_corrfactor;
		}
		return new_correlationCounts;
	}

	/*
		Every byte in the file gets a correlation factor with relation to the mime type.
		Correlation factor of byte = |frequency(byte,inputfile) - frequency(byte,fingerprint)| 
	*/
	public static double getCorrelationStrength(double x)
	{
		double sigma = 0.125;
		return Math.exp(-x/(2*Math.pow(sigma,2)));
	}

	public static void writeFingerprint(String filename,double[] fingerprintCounts,double[] correlationCounts,int numFiles) throws IOException
	{
		FileWriter fw = new FileWriter(filename);
        fw.write(Integer.toString(numFiles));

        fw.write("\n");
        for (int j = 0; j < 256; j++) {
            String temp = fingerprintCounts[j] + "," + correlationCounts[j] + "\t";
            fw.write(temp);
        }
        fw.write("\n");
        fw.close();
	}

	public static double[][] getCorrelationMatrix(double[] fingerprintCounts, double[] correlationCounts,int numFiles)
	{
		double [][] correlationMatrix = new double[256][256];
		correlationMatrix[0][0] = (double)numFiles;
		for(int i=0;i<256;++i)
		{
			for(int j=0;j<256;++j)
			{
				correlationMatrix[i][j] = Math.abs(fingerprintCounts[i] - fingerprintCounts[j]);
				double countDiff = Math.abs(correlationCounts[i]-correlationCounts[j]);
				correlationMatrix[j][i] = getCorrelationStrength(countDiff);
			}
		}
		/**
		for(int i=0;i<256;++i)
		{
			for(int j=0;j<256;++j)
			{
				System.out.print(" |"+correlationMatrix[i][j]+"| ");
			}
			System.out.println();
		}
		**/
		return correlationMatrix;
	}
	
}
