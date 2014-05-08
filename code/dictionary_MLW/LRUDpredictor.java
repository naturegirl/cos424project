import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


public class LRUDpredictor {
    
    // the input sequence that we read is in this directory
    private static final String sequence_dir = "../../data/input-words/";
    
    HashSet<Character> LeftUp;
    HashSet<Character> LeftDown;
    HashSet<Character> RightUp;
    HashSet<Character> RightDown;

    Character[] left_up_set = new Character[] {'q','w','e','r','t'};
    Character[] left_down_set = new Character[] {'a','s','d','f','g','z','x','c','v','b'};
    Character[] right_up_set = new Character[] {'y','u','i','o','p'};
    Character[] right_down_set = new Character[] {'h','j','k','l','n','m'};
    
    Dictionary dictionary;
    
    public LRUDpredictor() {
	// TODO Auto-generated constructor stub
	LeftUp = new HashSet<Character>(Arrays.asList(left_up_set));
	LeftDown = new HashSet<Character>(Arrays.asList(left_down_set));
	RightUp = new HashSet<Character>(Arrays.asList(right_up_set));
	RightDown = new HashSet<Character>(Arrays.asList(right_down_set));
	
	dictionary = new Dictionary();
    }
    
    /**
     * return all the matching words, that match according to the LRUD sequence
     * @sequence: input sequence, i.e. "lrud"
     * @dict_num: which dictionary, between 1 ~ 72
     * @return: all words in the dictionary with the same LRUD sequence 
     */
    private ArrayList<String> getMatchingWords(String sequence, int dict_num) {
	if (dict_num < 1 || dict_num > 72)
	    throw new RuntimeException("Dictionary number is out of range!");
	if (!isValid(sequence))
	    throw new RuntimeException("invalid input sequence!");
	
	ArrayList<String> result = new ArrayList<String>();
	HashSet<String> dict = dictionary.getDictionary(dict_num);
	for (String entry : dict) {
	    String entry_seq = generateSequence(entry);
	    if (get_distance(sequence, entry_seq) == 0)
		result.add(entry);
	}
	return result;
    }
    
    /**
     * returns the top k closest words in the dictionary specified by dict_num
     * as measured by the distance of their LRUD sequences
     * @param input sequence: same as in getMatchingWords
     * @param dict_num: between 1 ~ 72
     * @param k: how many words we want to return, is upper bound
     * @return: top k closest words measured by LRUD sequence distance
     */
    private ArrayList<String> getClosestWords(String sequence, int dict_num, int k) {
	if (dict_num < 1 || dict_num > 72)
	    throw new RuntimeException("dict_num is out of range!");
	if (!isValid(sequence))
	    throw new RuntimeException("invalid input sequence!");	
	
	HashSet<String> dict = dictionary.getDictionary(dict_num);
	ArrayList<String> result = new ArrayList<String>(k);	// treat as circular array
	int distance = Integer.MAX_VALUE;
	int pos = 0;
	for (String entry : dict) {
	    String entry_seq = generateSequence(entry);
	    if (get_distance(sequence, entry_seq) < distance) {
		result.add(pos,entry);
		pos = (pos + 1) % k;
		distance = get_distance(sequence, entry_seq);
	    }
	}
	return result;	
    }
    
    /*
     * check if sequence is valid. Returns
     */
    private boolean isValid(String sequence) {
	if (sequence.length() % 2 == 1)
	    return false;
	for (int i = 0; i < sequence.length(); ++i) {
	    char c = sequence.charAt(i);
	    if (c != 'l' && c != 'r' && c != 'u' && c!= 'd')
		return false;
	}
	return true;
    }
    
    /* 
     * given a word generate the sequence of L R U D values
     */
    private String generateSequence(String word) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < word.length(); ++i) {
	    char c = word.charAt(i);
	    if (LeftUp.contains(c))
		sb.append("lu");
	    else if (LeftDown.contains(c))
		sb.append("ld");
	    else if (RightUp.contains(c))
		sb.append("ru");
	    else if (RightDown.contains(c))
		sb.append("rd");
	    else
		System.out.println("Error! Character not in any Set!");
	}
	return sb.toString();
    }

    private static int get_distance(String x, String y) {
	if (x.length() != y.length())
	    return Integer.MAX_VALUE;
	if (x.equals(y))
	    return 0;	
	int dist = 0;
	for (int i = 0; i < x.length(); ++i) {
	    if (x.charAt(i) != y.charAt(i))
		dist++;
	}
	return dist;
    }
    
    /*
     * reads the sequence from file
     */
    private String readSequenceFromFile(String filename) {
	String sequence = null;
	try {
	    //BufferedReader br = new BufferedReader(new FileReader(filename));
	    BufferedReader br = new BufferedReader(new FileReader(sequence_dir+filename));
	    sequence = br.readLine();
	    br.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return sequence;
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
	
	if (args.length == 0) {
	    System.out.println("Usage: LRUDpredictor <filename>  \n" +
	    		"Pass in the filename of the sequence file. Looks in the data/input-words/ directory.");
	    return;
	}
	
	LRUDpredictor pred = new LRUDpredictor();
	String seq = pred.readSequenceFromFile(args[0]);
	System.out.println("input sequence: "+seq);
	ArrayList<String> result = pred.getMatchingWords(seq, 1);
	System.out.println("Found "+result.size()+" exact match(es):");
	for (String s : result)
	    System.out.println(s);
	
	ArrayList<String> result2 = pred.getClosestWords(seq, 1, 5);
	System.out.println("Found "+result2.size()+" match(es):");
	for (String s : result2) {
	    int dist = get_distance(seq, pred.generateSequence(s));
	    System.out.println(s+" "+dist);
	}
    }

}
