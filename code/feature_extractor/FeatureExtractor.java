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
    public static boolean WANT_GFORCE_DATA = true;
    public static final boolean WANT_LR_LABEL = false;
    public static final boolean WANT_UP_LABEL = false;
    public static final boolean WANT_PAIRED_LABEL = false;
    public static final boolean WANT_TRIAD_LABEL = false;
    public static final boolean WANT_SEPTET_LABEL = false;

    private static ArrayList<Features> featuresList;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java FeatureExtractor " +
                    "<data-folder or 'all'> <<gforce>>");
            System.out.println("'all' defaults to ./../data/" +
                    "original-recordings/");
            System.out.println("Example: java FeatureExtractor a");
            System.exit(0);
        }
        if (args.length == 2) {
            if (args[1].equals("gforce")) {
                WANT_GFORCE_DATA = true;
            }
        }
        String path = "../../data/" + args[0];
        if (args[0].equals("all"))
            path = "../../data/original_recordings/";
        File directory = new File(path);

        FeatureExtractor ob = new FeatureExtractor();
        featuresList = new ArrayList<Features>();
        ob.processKeyPresses(directory);
        
        String featuresFile = "../../data/features/" + args[0] + ".csv";
        ob.writeToFile(featuresList, featuresFile);
    }
    
    /**
     * Recursively traverse the directory of key presses to extract the
     * features for the letters' signals and label them correctly
     * 
     * @param directory
     * @throws IOException 
     */
    private void processKeyPresses(File directory) throws IOException {
        File[] files = directory.listFiles();
        int label = this.getLabel(directory.getName());

        for (File file: files) {
            if (file.getAbsolutePath().endsWith(".csv")) {
                ArrayList<Signal> signals = this.readCSV(file);
                //this.smoothGForce(signals);
                //this.stripSignalHead(signals);
                //this.stripSignalTail(signals);
                // write g-force's to file
                if (WANT_GFORCE_DATA) {
                    String filepath = file.getAbsolutePath();
                    this.writeGForceToFile(
                            signals,
                            filepath.substring(0, filepath.length()-4) +
                            ".gforce.csv"
                            );
                }
                Features features = this.getFeatures(signals);
                features.setLabel(label);
                featuresList.add(features);
            } else if (file.isDirectory()) {
                this.processKeyPresses(file);
            }
        }
    }
    
    private void stripSignalHead(ArrayList<Signal> signals) {
        int max_index = 0;
        int min_index = 0;
        for(int i = 0; i < signals.size(); i++){
            Signal signal = signals.get(i);
            if (signal.getGForce() > signals.get(max_index).getGForce()) {
                max_index = i;
            }
            if (signal.getGForce() < signals.get(min_index).getGForce()) {
                min_index = i;
            }
        }
        int peak_index = Math.min(max_index, min_index);
        BigInteger peak_timestamp = signals.get(peak_index).getTimeStamp();
        BigInteger threshold = new BigInteger("500000"); // 500ms
        int cut_off_index = 0;
        for (int i = peak_index; i >= 0; i--) {
            BigInteger diff = signals.get(i).getTimeStamp().subtract(
                    peak_timestamp).abs(); 
            if (diff.compareTo(threshold) > 0) {
                cut_off_index = i;
                break;
            }
        }
        for (int i = cut_off_index; i >= 0; i--) {
            signals.remove(i);
        }
    }
    
    private void stripSignalTail(ArrayList<Signal> signals) {
        double sum = 0;
        int n = 100;
        for (int i = signals.size()-n; i < signals.size(); i++) {
            sum += signals.get(i).getGForce();
        }
        double mean = sum / n;
        double sq_diff_sum = 0;
        for (int i = signals.size()-n; i < signals.size(); i++) {
            sq_diff_sum += Math.pow(signals.get(i).getGForce()-mean,2);
        }
        double std_dev = Math.sqrt(sq_diff_sum/n);
        System.out.println("Mean = " + mean + "\nStd-dev = " + std_dev);
    }
    
    private int getLabel(String dirName) {
        if (WANT_LR_LABEL) {
            if (dirName.equals("enter") || dirName.equals("space")) {
                return 0;
            }
            return this.getLRLabel(dirName.charAt(0));
        }
        if (WANT_UP_LABEL) {
            if (dirName.equals("enter") || dirName.equals("space")) {
                return 0;
            }
            return this.getUDLabel(dirName.charAt(0));
        }
        if (WANT_PAIRED_LABEL) {
            if (dirName.equals("enter") || dirName.equals("space")) {
                return 0;
            }
            return this.getPairedLabel(dirName.charAt(0));
        }
        if (WANT_TRIAD_LABEL) {
            if (dirName.equals("enter") || dirName.equals("space")) {
                return 0;
            }
            return this.getTriadLabel(dirName.charAt(0));
        }
        if (WANT_SEPTET_LABEL) {
            if (dirName.equals("enter") || dirName.equals("space")) {
                return 0;
            }
            return this.getSeptetLabel(dirName.charAt(0));
        }
        if (dirName.equals("enter")) {
            return 27;
        } else if (dirName.equals("space")) {
            return 28;
        }
        char letter = dirName.toLowerCase().charAt(0);
        return (letter - 'a' + 1);
    }
    
    private int getSeptetLabel(char ch) {
        switch(ch) {
        case 'q':
        case 'w':
        case 'e':
        case 'a':
        case 's':
        case 'z':
        case 'x': return 1;
        case 'r':
        case 't':
        case 'd':
        case 'f':
        case 'g':
        case 'c':
        case 'v': return 2;
        case 'y':
        case 'u':
        case 'h':
        case 'j':
        case 'b':
        case 'n': return 3;
        case 'i':
        case 'o':
        case 'p':
        case 'k':
        case 'l':
        case 'm': return 4;
        }
        return 0;
    }

    private int getTriadLabel(char ch) {
        switch(ch) {
        case 'q':
        case 'a':
        case 'w': return 1;
        case 'z':
        case 's':
        case 'x': return 2;
        case 'e':
        case 'd':
        case 'r': return 3;
        case 'f':
        case 'c':
        case 'v': return 4;
        case 't':
        case 'g':
        case 'y': return 5;
        case 'h':
        case 'b':
        case 'n': return 6;
        case 'u':
        case 'j':
        case 'i': return 7;
        case 'o':
        case 'l':
        case 'p': return 8;
        case 'k':
        case 'm': return 9;
        }
        return 0;
    }

    private int getPairedLabel(char ch) {
        switch(ch) {
        case 'q':
        case 'w': return 1;
        case 'e':
        case 'r': return 2;
        case 't':
        case 'y': return 3;
        case 'u':
        case 'i': return 4;
        case 'o':
        case 'p': return 5;
        case 'a':
        case 's': return 6;
        case 'd':
        case 'f': return 7;
        case 'g':
        case 'b': return 8;
        case 'z':
        case 'x': return 9;
        case 'c':
        case 'v': return 10;
        case 'h':
        case 'j': return 11;
        case 'k':
        case 'l': return 12;
        case 'n':
        case 'm': return 13;
        }
        return 0;
    }

    private int getLRLabel(char ch) {
        char[] left = {'a','b','c','d','e','f','g','q',
                        'r','s','t','v','w','x','z'};
        char[] right = {'h','i','j','k','l','m','n','o','p','u','y'};
        for (int i = 0; i < left.length; i++) {
            if (left[i] == ch) {
                return 1;   // L
            }
        }
        for (int i = 0; i < right.length; i++) {
            if (right[i] == ch) {
                return 2;   // R
            }
        }
        return 0;
    }

    private int getUDLabel(char ch) {
        char[] up = {'q','w','e','r','t','y','u','i','o','p'};
        char[] down = {'a','s','d','f','g','h','j','k','l','z','x','c','v',
                        'b','n','m'};
        for (int i = 0; i < up.length; i++) {
            if (up[i] == ch) {
                return 1;   // U
            }
        }
        for (int i = 0; i < down.length; i++) {
            if (down[i] == ch) {
                return 2;   // D
            }
        }
        return 0;
    }

    /**
     * Writes the time vs g-force values for a key press
     * @param rows of signal for a keypress
     * @param filename
     * @throws IOException
     */
    public void writeGForceToFile(ArrayList<Signal> signals,
            String filename) throws IOException {
        
        PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(filename)));
        pw.println("timestamp, gforce");
        for(Iterator<Signal> iter = signals.iterator(); iter.hasNext();){
            Signal row = iter.next();
            pw.println(row.getTimeStamp().toString() + "," + row.getGForce());
        }
        pw.close();
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
        System.out.println("writing "+datafile);
    }

    public Features getFeatures(ArrayList<Signal> signals) {
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
    public ArrayList<Signal> readCSV(File file)
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
