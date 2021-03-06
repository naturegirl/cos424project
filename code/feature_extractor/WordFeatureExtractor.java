import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class WordFeatureExtractor {

    public static final double G = 9.81;
    public static final BigInteger FACTOR = new BigInteger("1000");
    public static BigInteger BEFORE_THRESH = new BigInteger("250000"); // 250ms
    public static BigInteger AFTER_THRESH = new BigInteger("1000000"); // 1000ms
    public static double G_FORCE_THRESH = 0.15;
    public static boolean TIME_X_Y_Z_ONLY = false;
    
    private HashMap<String, ArrayList<Features>> featuresMap;
    private File gForceFile;
    private String indivLettersFile;
    
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java WordFeatureExtractor " +
                    "<data-folder> <<timexyz>>");
            System.out.println("Example: java WordFeatureExtractor akshay");
            System.exit(0);
        }
        if (args.length == 2) {
            if (args[1].equals("timexyz")) {
                TIME_X_Y_Z_ONLY = true;
            }
        }
        WordFeatureExtractor ob = new WordFeatureExtractor();
        
        String path = "../../data/" + args[0];
        ob.indivLettersFile = "../../data/" + args[0] + ".indivletters";
        File directory = new File(path);

        ob.featuresMap = new HashMap<String, ArrayList<Features>>();
        ob.gForceFile = new File(path + ".letters.gforces");
        if (!ob.gForceFile.exists()) {
            ob.gForceFile.mkdir();
        }
        ob.processWordCSV(directory);
        
        if (!TIME_X_Y_Z_ONLY) {
            String features_dir = "../../data/" + args[0] + ".feature";
            ob.writeToFile(features_dir);
        }
    }

    private void writeToFile(String dir_name) throws IOException {
        File file = new File(dir_name);
        if (!file.exists()) {
            file.mkdir();
        }
        for (String name : featuresMap.keySet()) {
            String filepath = dir_name + "/" + name + ".csv";
            PrintWriter pw = new PrintWriter(
                    new BufferedWriter(new FileWriter(filepath)));
            String heading = Features.getFeaturesName();
            // remove the ", label" from the features heading
            heading = heading.substring(0, heading.lastIndexOf(','));
            pw.println(heading);
            
            for (Features feature : featuresMap.get(name)) {
                String fstr = feature.toString();
                fstr = fstr.substring(0, fstr.lastIndexOf(','));
                pw.println(fstr);
            }
            pw.close();
            //System.out.println("writing " + filepath);
        }
    }

    /**
     * Reads the csv file and breaks the word signal into multiple
     * files for each letter
     * 
     * @param file csv file to read the data from 
     * @param datafile name of the output .data.csv file
     * @throws IOException
     * @return ArrayList of Signals from the file
     */
    private ArrayList<Signal> processWordCSV(File directory)
            throws IOException {
        File[] files = directory.listFiles();

        FeatureExtractor fe = new FeatureExtractor();
        for (File file: files) {
            String filepath = file.getAbsolutePath();
            if (filepath.endsWith(".csv")) {
                ArrayList<Signal> signals = fe.readCSV(file);
                ArrayList<ArrayList<Signal>> letter_signals =
                        this.breakSignal(signals);
                this.orderLetterSignals(letter_signals);
                letter_signals = this.shiftRelativeToOrigin(letter_signals);
                System.out.println(file.getName() +
                        ": # letters = " + letter_signals.size());
                this.writeWordLettersGForce(letter_signals, file);
                if (TIME_X_Y_Z_ONLY) {
                    this.writeTimeXYZ(letter_signals, file);
                } else {
                    this.addFeatures(file, letter_signals);
                }
            } else if (file.isDirectory()) {
                this.processWordCSV(file);
            }
        }
        return null;
    }

    private void writeTimeXYZ(ArrayList<ArrayList<Signal>> letter_signals,
            File csv_file) throws IOException {
        File indiv = new File(this.indivLettersFile);
        if (!indiv.exists()) {
            indiv.mkdir();
        }
        String output_dir = this.indivLettersFile + "/" +
                csv_file.getName().split("\\.")[0];
        File file = new File(output_dir);
        if (!file.exists()) {
            file.mkdir();
        }
        String filepath = file.getAbsolutePath() + "/";
        for (int i = 0; i < letter_signals.size(); i++) {
            String path = filepath + i + ".letter.csv";
            ArrayList<Signal> signals = letter_signals.get(i);
            PrintWriter pw = new PrintWriter(
                    new BufferedWriter(new FileWriter(path)));
            for (Signal signal : signals) {
                String time = "";
                if (TIME_X_Y_Z_ONLY) {
                    time = signal.getTimeStamp().multiply(FACTOR).toString();
                } else {
                    time = signal.getTimeStamp().toString();
                }
                String data = time + "," +
                              String.valueOf(signal.getX()) + "," +
                              String.valueOf(signal.getY()) + "," +
                              String.valueOf(signal.getZ());
                pw.println(data);
            }
            pw.close();
            //System.out.println("writing " + path);
        }
    }

    private ArrayList<ArrayList<Signal>> shiftRelativeToOrigin(
            ArrayList<ArrayList<Signal>> letter_signals) {
        ArrayList<ArrayList<Signal>> new_letter_signals =
                new ArrayList<ArrayList<Signal>>();
        for(ArrayList<Signal> signals : letter_signals) {
            BigInteger start_time = signals.get(0).getTimeStamp();
            ArrayList<Signal> new_signals = new ArrayList<Signal>();
            for(Signal signal : signals) {
                BigInteger new_timestamp =
                        signal.getTimeStamp().subtract(start_time);
                Signal new_signal = new Signal(new_timestamp,
                        signal.getX(), signal.getY(), signal.getZ());
                new_signals.add(new_signal);
            }
            new_letter_signals.add(new_signals);
        }
        return new_letter_signals;
    }

    private void addFeatures(File file,
            ArrayList<ArrayList<Signal>> letter_signals) {
        ArrayList<Features> features = new ArrayList<Features>();
        FeatureExtractor fe = new FeatureExtractor();
        
        for(ArrayList<Signal> signals : letter_signals) {
            Features feat = fe.getFeatures(signals);
            features.add(feat);
        }
        String key = file.getName().split("\\.")[0];
        this.featuresMap.put(key, features);
    }

    private void writeWordLettersGForce(
            ArrayList<ArrayList<Signal>> letter_signals, File csv_file)
                    throws IOException {
        String output_dir = this.gForceFile.getAbsolutePath() + "/" +
                    csv_file.getName().split("\\.")[0];
        File file = new File(output_dir);
        if (!file.exists()) {
            file.mkdir();
        }
        String filepath = file.getAbsolutePath() + "/letter_";
        FeatureExtractor fe = new FeatureExtractor();
        for (int i = 0; i < letter_signals.size(); i++) {
            String path = filepath + i + ".gforce.csv";
            fe.writeGForceToFile(letter_signals.get(i), path);
            //System.out.println("writing " + path);
        }
    }

    /**
     * Orders the letter signals according as they appear in the word typed
     * @param letter_signals
     */
    private void orderLetterSignals(
            ArrayList<ArrayList<Signal>> letter_signals) {
        Collections.sort(letter_signals, new Comparator<ArrayList<Signal>>(){
            public int compare(ArrayList<Signal> letter1,
                    ArrayList<Signal> letter2) {
                BigInteger diff = letter1.get(0).getTimeStamp().subtract(
                        letter2.get(0).getTimeStamp());
                return diff.compareTo(BigInteger.ZERO);
            }
        });
    }

    private ArrayList<ArrayList<Signal>> breakSignal(ArrayList<Signal> signals){
        FeatureExtractor.moveToBaseReference(signals);

        @SuppressWarnings("unchecked")
        ArrayList<Signal> sorted_signals = (ArrayList<Signal>) signals.clone();
        // reverse sorted according to the absolute values of the g-force values
        Collections.sort(sorted_signals, new Comparator<Signal>() {
            public int compare(Signal signal1, Signal signal2) {
                double diff = Math.abs(signal1.getGForce()) -
                        Math.abs(signal2.getGForce());
                return -(int)Math.signum(diff);
            }
        });
        ArrayList<ArrayList<Signal>> letter_signals =
                new ArrayList<ArrayList<Signal>>();
        while (sorted_signals.size() > 0) {
            if (Math.abs(sorted_signals.get(0).getGForce()) < G_FORCE_THRESH) {
                break;
            }
            int max_index = signals.indexOf(sorted_signals.get(0));
            ArrayList<Signal> letter =
                    this.removeAroundMax(signals, max_index);
            letter_signals.add(letter);
            sorted_signals.removeAll(letter);
        }
        return letter_signals;
    }

    private ArrayList<Signal> removeAroundMax(ArrayList<Signal> signals,
            int max_index){
        BigInteger peak_time_stamp = signals.get(max_index).getTimeStamp();
        
        // letter start index
        int start_cut_off_index = 0;
        for (int i = max_index; i >= 0; i--) {
            BigInteger diff = signals.get(i).getTimeStamp().subtract(
                    peak_time_stamp).abs();
            start_cut_off_index = i;
            if (diff.compareTo(BEFORE_THRESH) > 0) {
                break;
            }
        }
        
        // letter end index
        int end_cut_off_index = 0;
        for (int i = max_index; i < signals.size(); i++) {
            BigInteger diff = signals.get(i).getTimeStamp().subtract(
                    peak_time_stamp).abs(); 
            end_cut_off_index = i;
            if (diff.compareTo(AFTER_THRESH) > 0) {
                break;
            }
        }
        
        ArrayList<Signal> letter_signal = new ArrayList<Signal>();
        for (int i = start_cut_off_index; i <= end_cut_off_index; i++) {
            letter_signal.add(signals.get(i));
        }

        return letter_signal;
    }
}
