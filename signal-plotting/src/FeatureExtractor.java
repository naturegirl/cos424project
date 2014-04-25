import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * @author aks0
 *
 */
public class FeatureExtractor {
    
    public static final double G = 9.81;
    public static final BigInteger FACTOR = new BigInteger("1000");

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java GForceComputation " +
                    "<directory to traverse>");
            System.exit(0);
        }
        String path = "./" + args[0];
        File f = new File(path);
        File[] files = f.listFiles();
        FeatureExtractor ob = new FeatureExtractor();
        for (File file : files) {
            if (file.getAbsolutePath().endsWith(".csv")) {
                String datafile = file.getName().replaceFirst("[.][^.]+$", "");
                datafile = path + "/" + datafile + ".data.csv";
                ArrayList<Signal> signals = ob.processCSV(file, datafile);
            }
        }
    }

    /**
     * Reads the csv file and outputs the Signal computation for
     * each row of data
     * 
     * @param file csv file to read the data from 
     * @param datafile name of the output .data.csv file
     * @throws IOException
     * @return ArrayList of Signals from the file
     */
    private ArrayList<Signal> processCSV(File file,
            String datafile) throws IOException {
        Scanner ob = new Scanner(file);
        PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(datafile)));
        BigInteger start_time = null;
        ArrayList<Signal> signals = new ArrayList<Signal>();
        
        while(ob.hasNextLine()) {
            StringTokenizer data = new StringTokenizer(ob.nextLine(), ",");
            BigInteger time = new BigInteger(data.nextToken());
            if (start_time == null) {
                start_time = time;
                time = BigInteger.ZERO;
            } else {
                // convert time passed to micro-second
                time = time.subtract(start_time).divide(FACTOR);
            }
            double x = Double.parseDouble(data.nextToken());
            double y = Double.parseDouble(data.nextToken());
            double z = Double.parseDouble(data.nextToken());
            signals.add(new Signal(time, x, y, z));
        }
        pw.close();
        ob.close();
        return signals;
    }

    private double feature_mean(ArrayList<Signal> signals){
      double sum = 0;
      int size = signals.size();
      
      for(int i = 0; i < size; i++){
        sum = sum + (signals.get(i)).getGforce();
      }

      double mean = sum/size;
      return mean;
    }

    private double feature_variance(ArrayList<Signal> signals){
      double mean = feature_mean(signals);
      double sum = 0;
      int size = signals.size();

      for(int i = 0; i<size; i++){
        sum = sum + Math.pow(((signals.get(i)).getGforce() - mean), 2);
      }
      
      double variance = sum/size;
      return variance;
    }
    
    private double feature_mean(ArrayList<Signal> signals){
      double sum = 0;
      int size = signals.size();
      
      for(int i = 0; i<size; i++){
        sum = sum + Math.pow((signals.get(i)).getGforce(), 2);
      } 
      
      double square_mean = sum/size;
      double sqrt = Math.sqrt(square_mean);
      return sqrt;
    }

}
