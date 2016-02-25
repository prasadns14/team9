import java.io.*;
import java.util.Scanner;

public class FHTAnalysis {

	public static final int maxByteValue = 256;

	public static final String[] fileTypes = { "text\\plain", "text\\pdf", "text\\rdf+xml", "text\\rsf+xml",
			"text\\xhtml+xml", "text\\html", "image\\png", "image\\jpeg", "audio\\mpeg", "video\\mp4",
			"video\\quicktime", "applicaiton\\x-sh", "application\\gzip", "application\\msword",
			"application\\octet-stream" };

	public double[][] get_byte_frequency_matrix(int numOfBytes) {
		double[][] matrix = new double[numOfBytes][maxByteValue];
		return matrix;
	}

	public void writeFingerPrint(File fingerPrintFile, double[][] fingerPrint, int numOfHeaderTrailerBytes,
			int numOfFilesProcessed) throws Exception {
		FileWriter fw = new FileWriter(fingerPrintFile);
		fw.write(Integer.toString(numOfFilesProcessed + 1));

		for (int i = 0; i < numOfHeaderTrailerBytes; i++) {
			fw.write("\n");
			for (int j = 0; j < maxByteValue; j++) {
				String temp = Double.toString(fingerPrint[i][j]) + "\t";
				fw.write(temp);
			}
		}

		fw.close();
	}

	// calculating byte frequency
	public double[][] updateByteFrequency(double[][] byteFrequency, int numOfFilesProcessed, double[][] byteCount,
			int numOfHeaderTrailerBytes) {
		for (int i = 0; i < numOfHeaderTrailerBytes; i++) {
			for (int j = 0; j < maxByteValue; j++) {
				byteFrequency[i][j] = (byteFrequency[i][j] * numOfFilesProcessed + byteCount[i][j])
						/ (numOfFilesProcessed + 1);
			}
		}

		return byteFrequency;
	}

	public static void main(String[] args) throws Exception {

		FHTAnalysis fht = new FHTAnalysis();

		// num of header or trailer bytes considered.
		int numOfHeaderTrailerBytes = 4;

		for (int i = 0; i < fileTypes.length; i++) {

			// Selecting the file type and setting the path to the folder which
			// has the data and fingerprint file corresponding to that file type
			// File fileType = new File("C:\\DataDump\\" + fileTypes[i] +
			// "\\data");
			File fileType = new File("C:\\DataDump\\" + fileTypes[i]);
			File[] files = fileType.listFiles();

			String headerFingerPrintFile = "Header_" + fileTypes[i].replace("\\", "-");
			String trailerFingerPrintFile = "Trailer_" + fileTypes[i].replace("\\", "-");
			File headerFingerPrint = new File("C:\\DataDump\\" + fileTypes[i] + "\\" + headerFingerPrintFile + ".txt");
			System.out.println("C:\\DataDump\\" + fileTypes[i] + "\\" + headerFingerPrintFile + ".txt");

			File trailerFingerPrint = new File(
					"C:\\DataDump\\" + fileTypes[i] + "\\" + trailerFingerPrintFile + ".txt");

			int num = 0;
			
			if (!trailerFingerPrint.exists()) {
				trailerFingerPrint.createNewFile();

				FileWriter fw = new FileWriter(trailerFingerPrint);

				fw.write("0");

				for (int k = 0; k < numOfHeaderTrailerBytes; k++) {
					fw.write("\n");
					for (int j = 0; j < maxByteValue; j++) {
						String temp = Double.toString(0.0) + "\t";
						fw.write(temp);
					}
				}

				fw.close();
			}

			if (!headerFingerPrint.exists()) {
				headerFingerPrint.createNewFile();
				FileWriter fw = new FileWriter(headerFingerPrint);

				fw.write("0");// no of files processed = 0

				for (int k = 0; k < numOfHeaderTrailerBytes; k++) {
					fw.write("\n");
					for (int j = 0; j < maxByteValue; j++) {
						String temp = Double.toString(0.0) + "\t";
						fw.write(temp);
					}
				}

				fw.close();
			}

			for (File file : files) {
				// input file
				FileInputStream fIS = new FileInputStream(file);

				// headerByteFrequency
				double[][] headerByteFrequency = fht.get_byte_frequency_matrix(numOfHeaderTrailerBytes);

				System.out.println("headerByteFrequency allocated ");
				// trailerByteFrequency
				double[][] trailerByteFrequency = fht.get_byte_frequency_matrix(numOfHeaderTrailerBytes);

				Scanner scHFP = new Scanner(new FileReader(headerFingerPrint));
				Scanner scTFP = new Scanner(new FileReader(trailerFingerPrint));

				int numOfFilesProcessed = scHFP.nextInt();

				System.out.println(numOfFilesProcessed);

				scHFP.nextLine();
				// load the header figerprint
				for (int k = 0; k < numOfHeaderTrailerBytes; k++) {
					String[] terms = scHFP.nextLine().split("\\t");
					for (int j = 0; j < maxByteValue; j++) {
						headerByteFrequency[i][j] = Double.parseDouble(terms[j]);
					}
				}

				scHFP.close();

				// load the header figerprint
				numOfFilesProcessed = scTFP.nextInt();

				scTFP.nextLine();

				for (int k = 0; k < numOfHeaderTrailerBytes; k++) {
					String[] terms = scTFP.nextLine().split("\\t");
					for (int j = 0; j < maxByteValue; j++) {
						trailerByteFrequency[i][j] = Double.parseDouble(terms[j]);
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
					for (int j = 0; j < numOfHeaderTrailerBytes; j++) {
						headerByteCount[j][0xFF & byteFile[j]]++;
						trailerByteCount[j][0xFF & byteFile[byteFile.length - numOfHeaderTrailerBytes + j]]++; //
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
		}
	}
}
