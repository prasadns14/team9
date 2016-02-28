import java.io.*;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class FHTAnalysis {

	public static final int maxByteValue = 256;

	/*public static final String[] fileTypes = { "text\\plain", "text\\pdf", "text\\rdf+xml", "text\\rsf+xml",
			"text\\xhtml+xml", "text\\html", "image\\png", "image\\jpeg", "audio\\mpeg", "video\\mp4",
			"video\\quicktime", "applicaiton\\x-sh", "application\\gzip", "application\\msword",
			"application\\octet-stream" };
	*/		

	public static final String[] fileTypes = { "application\\x-elc"
			 };
	public double[][] get_byte_frequency_matrix(int numOfBytes) {
		double[][] matrix = new double[numOfBytes][maxByteValue];
		return matrix;
	}

	public void writeFingerPrint(File fingerPrintFile, double[][] fingerPrint, int numOfHeaderTrailerBytes,
			int numOfFilesProcessed) throws Exception {
		FileWriter fw = new FileWriter(fingerPrintFile);
		fw.write(Integer.toString(numOfFilesProcessed + 1));

		for (int headerTrailerByteIndex = 0; headerTrailerByteIndex < numOfHeaderTrailerBytes; headerTrailerByteIndex++) {
			fw.write("\n");
			for (int byteIndex = 0; byteIndex < maxByteValue; byteIndex++) {
				String temp = Double.toString(fingerPrint[headerTrailerByteIndex][byteIndex]) + "\t";
				fw.write(temp);
			}
		}

		fw.close();
	}

	// calculating byte frequency 
	public double[][] updateByteFrequency(double[][] byteFrequency, int numOfFilesProcessed, double[][] byteCount,
			int numOfHeaderTrailerBytes) {
		for (int headerTrailerByteIndex = 0; headerTrailerByteIndex < numOfHeaderTrailerBytes; headerTrailerByteIndex++) {
			for (int byteIndex = 0; byteIndex < maxByteValue; byteIndex++) {
				byteFrequency[headerTrailerByteIndex][byteIndex] = (byteFrequency[headerTrailerByteIndex][byteIndex] * numOfFilesProcessed + byteCount[headerTrailerByteIndex][byteIndex])
						/ (numOfFilesProcessed + 1);
			}
		}

		return byteFrequency;
	}
	
public JSONArray getJsonObject(int numOfHeaderTrailerBytes,double[][] byteFrequency){
	 
	 	JSONArray jarray = new JSONArray();
		//JSONObject json = new JSONObject();
		
		for (int rowIdx = 0 ; rowIdx < numOfHeaderTrailerBytes; rowIdx++)
		{
			for(int columnIdx = 0; columnIdx < maxByteValue; columnIdx++){
		
				JSONObject json = new JSONObject();
		
				json.put("i",rowIdx);
				json.put("j",columnIdx);
				json.put("value", byteFrequency[rowIdx][columnIdx]);

				System.out.println(json);
				jarray.add(json);
			}
		}
		
		return jarray;
 }

 public void writeJsonFile(String fileType, String fileName, JSONArray json)throws IOException {
	 FileWriter jsonFile = new FileWriter("C:\\PolarDump\\" + fileType + "\\" +fileName +".json");
	 jsonFile.write(json.toJSONString());
	 jsonFile.close();
 }
 
	public static void main(String[] args) throws Exception {

		FHTAnalysis fht = new FHTAnalysis();

		// num of header or trailer bytes considered.
		int numOfHeaderTrailerBytes = 4;

		for (int fileTypesIndex = 0; fileTypesIndex < fileTypes.length; fileTypesIndex++) {

			// Selecting the file type and setting the path to the folder which
			// has the data and fingerprint file corresponding to that file type
		
			File fileType = new File("C:\\PolarDump\\" + fileTypes[fileTypesIndex] + "\\data\\");
			File[] files = fileType.listFiles();

			String headerFingerPrintFile = "Header_" + fileTypes[fileTypesIndex].replace("\\", "-");
			String trailerFingerPrintFile = "Trailer_" + fileTypes[fileTypesIndex].replace("\\", "-");
	        
			//create header finger print file 
			File headerFingerPrint = new File("C:\\PolarDump\\" + fileTypes[fileTypesIndex] + "\\" + headerFingerPrintFile +Integer.toString(numOfHeaderTrailerBytes) + ".txt");

			System.out.println("C:\\PolarDump\\" + fileTypes[fileTypesIndex] + "\\" + headerFingerPrintFile + Integer.toString(numOfHeaderTrailerBytes) + ".txt");
			
			//create trailer finger print file 
			File trailerFingerPrint = new File(
					"C:\\PolarDump\\" + fileTypes[fileTypesIndex] + "\\" + trailerFingerPrintFile +Integer.toString(numOfHeaderTrailerBytes)+ ".txt");

			int num = 0;
			
			//if trailer finger print file does not  exists , then create one . 
			//initialize first count to files processed to 0 
			//initialize maxbyte number of rows to 0 
			if (!trailerFingerPrint.exists()) {
				
				trailerFingerPrint.createNewFile();

				FileWriter fw = new FileWriter(trailerFingerPrint);

				fw.write("0");

				for (int trailerByteIndex = 0; trailerByteIndex < numOfHeaderTrailerBytes; trailerByteIndex++) {
					fw.write("\n");
					for (int byteIndex = 0; byteIndex < maxByteValue; byteIndex++) {
						String temp = Double.toString(0.0) + "\t";
						fw.write(temp);
					}
				}

				fw.close();
			}
			
			//if header finger print file does not  exists , then create one . 
			//initialize first count to files processed to 0 
			//initialize maxbyte number of rows to 0 

			if (!headerFingerPrint.exists()) {
				headerFingerPrint.createNewFile();
				FileWriter fw = new FileWriter(headerFingerPrint);

				fw.write("0");// no of files processed = 0

				for (int headerByteIndex = 0; headerByteIndex < numOfHeaderTrailerBytes; headerByteIndex++) {
					fw.write("\n");
					for (int byteIndex = 0; byteIndex < maxByteValue; byteIndex++) {
						String temp = Double.toString(0.0) + "\t";
						fw.write(temp);
					}
				}

				fw.close();
			}
			// get the headerByteFrequency
			double[][] headerByteFrequency = fht.get_byte_frequency_matrix(numOfHeaderTrailerBytes);

			System.out.println("headerByteFrequency allocated ");
			
			// get the trailerByteFrequency
			double[][] trailerByteFrequency = fht.get_byte_frequency_matrix(numOfHeaderTrailerBytes);

			
			for (File file : files) {
				// input file
				FileInputStream fIS = new FileInputStream(file);



				
				Scanner scHFP = new Scanner(new FileReader(headerFingerPrint));
				Scanner scTFP = new Scanner(new FileReader(trailerFingerPrint));

				int numOfFilesProcessed = scHFP.nextInt();

				System.out.println("Number of files processed : " + numOfFilesProcessed);

				scHFP.nextLine();
			
				// load the header figerprint
				for (int headerTrailerByteIndex = 0; headerTrailerByteIndex < numOfHeaderTrailerBytes; headerTrailerByteIndex++) {
					String[] terms = scHFP.nextLine().split("\\t");
					for (int byteIndex = 0; byteIndex < maxByteValue; byteIndex++) {
						headerByteFrequency[headerTrailerByteIndex][byteIndex] = Double.parseDouble(terms[byteIndex]);
					}
				}

				scHFP.close();

				// load the header figerprint
				numOfFilesProcessed = scTFP.nextInt();

				scTFP.nextLine();

				for (int headerTrailerByteIndex = 0; headerTrailerByteIndex < numOfHeaderTrailerBytes; headerTrailerByteIndex++) {
					String[] terms = scTFP.nextLine().split("\\t");
					for (int byteIndex = 0; byteIndex < maxByteValue; byteIndex++) {
						trailerByteFrequency[headerTrailerByteIndex][byteIndex] = Double.parseDouble(terms[byteIndex]);
					}
				}

				scTFP.close();

				// read the data from the file in consideration to a byte file
				byte[] byteFile = new byte[(int) file.length()];
				fIS.read(byteFile);
				fIS.close();

				// header and trailer byte frequency matrix for file in
				// consideration
				double[][] headerByteCount = fht.get_byte_frequency_matrix(numOfHeaderTrailerBytes);
				double[][] trailerByteCount = fht.get_byte_frequency_matrix(numOfHeaderTrailerBytes);

				// header frequency & trailer frequency
				// Calculate Header and Trailer byte frequency

				if (byteFile.length > numOfHeaderTrailerBytes) {
					for (int headerTrailerByteIndex = 0; headerTrailerByteIndex < numOfHeaderTrailerBytes; headerTrailerByteIndex++) {
						headerByteCount[headerTrailerByteIndex][0xFF & byteFile[headerTrailerByteIndex]]++;
						trailerByteCount[headerTrailerByteIndex][0xFF & byteFile[byteFile.length - numOfHeaderTrailerBytes + headerTrailerByteIndex]]++; //
					}
				} else {

					System.out.println(byteFile.length);

					int byteIndexJ = 0;
					int byteIndexI = 0;
					
					//headerFingerPrint
					for (byteIndexJ = 0; byteIndexJ < byteFile.length; byteIndexJ++) {
						headerByteCount[byteIndexJ][0xFF & byteFile[byteIndexJ]]++;
					}
					
					for (byteIndexI = byteIndexJ; byteIndexI < numOfHeaderTrailerBytes; byteIndexI++) {
						for (int byteIndexK = 0; byteIndexK < maxByteValue; byteIndexK++) {
							headerByteCount[byteIndexI][byteIndexK]--;
						}
					}
					
					/* commenting trailer analysis section  since it won't be considered for analysis when file lengthn is less than numOfHeaderTrailerBytes considered. 
					//trailerFingerPrint 
					for (byteIndexJ = 0, byteIndexI = byteFile.length; byteIndexI > 0; byteIndexI--, byteIndexJ++) {
						// headerByteCount[i][0xFF & byteFile[i]]++;
						trailerByteCount[byteIndexJ][0xFF & byteFile[byteFile.length - byteIndexI]]++;
					}
                     
					for (byteIndexI = byteIndexJ; byteIndexI < numOfHeaderTrailerBytes; byteIndexI++) {
						for (byteIndexJ = 0; byteIndexJ < maxByteValue; byteIndexJ++) {
							trailerByteCount[byteIndexI][byteIndexJ]--;
						}
					}
					*/
				}

				// update header byte frequency

				headerByteFrequency = fht.updateByteFrequency(headerByteFrequency, numOfFilesProcessed, headerByteCount,
						numOfHeaderTrailerBytes);

				// update trailer byte frequency

				trailerByteFrequency = fht.updateByteFrequency(trailerByteFrequency, numOfFilesProcessed,
						trailerByteCount, numOfHeaderTrailerBytes);

				// write header fingerprint

				fht.writeFingerPrint(headerFingerPrint, headerByteFrequency, numOfHeaderTrailerBytes,
						numOfFilesProcessed);

				// write trailer fingerprint
				fht.writeFingerPrint(trailerFingerPrint, trailerByteFrequency, numOfHeaderTrailerBytes,
						numOfFilesProcessed);
			}
			
			
			
			
			JSONArray headerJson = fht.getJsonObject(numOfHeaderTrailerBytes,headerByteFrequency);
			JSONArray trailerJson = fht.getJsonObject(numOfHeaderTrailerBytes,trailerByteFrequency);		
			
			fht.writeJsonFile(fileTypes[fileTypesIndex], headerFingerPrintFile, headerJson);
			fht.writeJsonFile(fileTypes[fileTypesIndex], trailerFingerPrintFile, trailerJson);
            
            
		}
	}
}
