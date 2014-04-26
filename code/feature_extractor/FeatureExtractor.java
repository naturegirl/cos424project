import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * @author aks0
 *
 */
public class FeatureExtractor {
    
    public static final double G = 9.81;
    public static final BigInteger FACTOR = new BigInteger("1000");
    private static ArrayList<Features> featuresList;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java FeatureExtractor " +
                    "<directory to traverse>");
            System.out.println("Example: java FeatureExtractor " +
                    "~/signal-plotting/data/c");
            System.exit(0);
        }
        String path = "./" + args[0];
        File directory = new File(path);

        FeatureExtractor ob = new FeatureExtractor();
        featuresList = new ArrayList<Features>();
        ob.readKeyPresses(directory);

        String featuresFile = directory.getAbsolutePath() + ".data.csv";
        ob.writeToFile(featuresList, featuresFile);
    }
    
    /**
     * Recursively traverse the directory of key presses to extract the
     * features for the letters' signals and label them correctly
     * 
     * @param directory
     * @throws FileNotFoundException
     */
    private void readKeyPresses(File directory)
            throws FileNotFoundException {
        File[] files = directory.listFiles();
        int label = this.getLabel(directory.getName());

        for (File file: files) {
            if (file.getAbsolutePath().endsWith(".csv")) {
                ArrayList<Signal> signals = this.processCSV(file);
                Features features = this.getFeatures(signals);
                features.setLabel(label);
                featuresList.add(features);
            } else if (file.isDirectory()) {
                this.readKeyPresses(file);
            }
        }
    }
    
    private int getLabel(String dirName) {
        if (dirName.length() != 1) {
            return -1;
        }
        char letter = dirName.toLowerCase().charAt(0);
        return (letter - 'a' + 1);
    }

    /**
     * Write the features for a letter to a features list file at the
     * same level as the directory.
     * @param featuresList
     * @param datafile
     * @throws IOException
     */
    private void writeToFile(ArrayList<Features> featuresList,
            String datafile) throws IOException {
        
        PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(datafile)));
        pw.println(Features.getFeaturesName());
        for(Iterator<Features> iter = featuresList.iterator(); iter.hasNext();){
            pw.println(iter.next().toString());
        }
        pw.close();
    }

    private Features getFeatures(ArrayList<Signal> signals) {
        Features features = new Features();
        features.setMin(this.feature_min(signals));
        features.setMax(this.feature_max(signals));
        features.setMean(this.feature_mean(signals));
        features.setVariance(this.feature_variance(signals));
        features.setRms(this.feature_rms(signals));
        features.setSkewness(this.feature_skewness(signals));
        features.setKurtosis(this.feature_kurtosis(signals));
        return features;
    }

    /**
     * Reads the csv file and outputs the Signal computation for
     * each row of data
     * 
     * @param file csv file to read the data from 
     * @param datafile name of the output .data.csv file
     * @throws IOException
     * @return ArrayList of Signals from the file
     * @throws FileNotFoundException 
     */
    private ArrayList<Signal> processCSV(File file)
            throws FileNotFoundException {
        Scanner ob = new Scanner(file);
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
        ob.close();
        return signals;
    }
    
    /**
     * Computes the minimum GForce recorded by the accelerometer for the
     * given key presses readings. 
     * @param signals
     * @return minimum g-force recorded
     */
    private double feature_min(ArrayList<Signal> signals) {
        double minGForce = Double.MAX_VALUE;
        for (Iterator<Signal> iter = signals.iterator(); iter.hasNext();) {
            Signal signal = iter.next();
            minGForce = Math.min(signal.getGForce(), minGForce);
        }
        return minGForce;
    }

    /**
     * Computes the maximum GForce recorded by the accelerometer for the
     * given key presses readings. 
     * @param signals
     * @return maximum g-force recorded
     */
    private double feature_max(ArrayList<Signal> signals) {
        double minGForce = Double.MIN_VALUE;
        for (Iterator<Signal> iter = signals.iterator(); iter.hasNext();) {
            Signal signal = iter.next();
            minGForce = Math.max(signal.getGForce(), minGForce);
        }
        return minGForce;
    }

    private double feature_mean(ArrayList<Signal> signals){
      double sum = 0;
      int size = signals.size();
      
      for(int i = 0; i < size; i++){
        sum = sum + (signals.get(i)).getGForce();
      }

      double mean = sum/size;
      return mean;
    }

    private double feature_variance(ArrayList<Signal> signals){
      double mean = feature_mean(signals);
      double sum = 0;
      int size = signals.size();

      for(int i = 0; i<size; i++){
        sum = sum + Math.pow(((signals.get(i)).getGForce() - mean), 2);
      }
      
      double variance = sum/size;
      return variance;
    }
    
    private double feature_rms(ArrayList<Signal> signals){
      double sum = 0;
      int size = signals.size();
      
      for(int i = 0; i<size; i++){
        sum = sum + Math.pow((signals.get(i)).getGForce(), 2);
      } 
      
      double square_mean = sum/size;
      double sqrt = Math.sqrt(square_mean);
      return sqrt;
    }

    /**
     * Skewness is a measure of the asymmetry of our signal.
     * A positive skew means that the right tail is longer
     * and a negative skew means that the left tail is longer.
     *
     * @param signals
     * @return
     */
    private double feature_skewness(ArrayList<Signal> signals) {
        double mean = feature_mean(signals);
        double variance = feature_variance(signals);
        double sum = 0;
        for (Signal s : signals) {
            sum += Math.pow(s.getGForce() - mean, 3); 
        }
        double skewness = sum 
                / (signals.size() - 1)
                / Math.pow(variance, 1.5);
        return skewness;
    }


    /**
     * Kurtosis is a measure of whether the data are peaked or flat relative
     * to a normal distribution. That is, data sets with high kurtosis tend to
     * have a distinct peak near the mean, decline rather rapidly, and have
     * heavy tails. Data sets with low kurtosis tend to have a flat top near
     * the mean rather than a sharp peak. A uniform distribution would be the
     * extreme case.
     * 
     * src: http://itl.nist.gov/div898/handbook/eda/section3/eda35b.htm
     * @param signals
     * @return
     */
    private double feature_kurtosis(ArrayList<Signal> signals){
        double mean = feature_mean(signals);
        double variance = feature_variance(signals);
        
        double quad_sum = 0;
        for (Iterator<Signal> iter = signals.iterator(); iter.hasNext();) {
            quad_sum = quad_sum + Math.pow(iter.next().getGForce() - mean, 4);
        }
        
        double kurtosis = quad_sum
                          / (signals.size() - 1)
                          / (variance * variance);
        return kurtosis;
      }

}