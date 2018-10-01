package src;

import java.io.*;
import java.util.*;

public class Bayespam
{
    // This defines the two types of messages we have.
    public static enum MessageType
    {
        NORMAL(0), SPAM(1);

        private int val;

        private MessageType(int val){
            this.val = val;
        }

        public int value(){
            return this.val;
        }
    }

    
    // This a class with two counters (for regular and for spam)
    public static class ConfusionMatrix{
        int totPos;
        int totNeg;
        int FP;
        int FN;

        public ConfusionMatrix(int totPos, int totNeg){
            this.totPos = totPos;
            this.totNeg = totNeg;
        }

        public void setFP(int fp){
            this.FP = fp;
        };
        public void setFN(int fn){
            this.FN = fn;
        };

        public void printMatrix(){
            System.out.println(" tp: "+(totPos-FN)+" fn: "+FN);
            System.out.println(" fp: "+FP+" tn: "+(totNeg-FP));
        }

    }

    //Initialize Bayes stats wrapper
    private static BayesClass bayesClass;
    // Listings of the two subdirectories (regular/ and spam/)
    private static File[] listing_regular = new File[0];
    private static File[] listing_spam = new File[0];

    // A hash table for the vocabulary (word searching is very fast in a hash table)
    private static Hashtable <String, Multiple_Counter> vocab = new Hashtable <String, Multiple_Counter> ();

    
    // Add a word to the vocabulary
    private static void addWord(String word, MessageType type)
    {
        Multiple_Counter counter = new Multiple_Counter();

        if ( vocab.containsKey(word) ){                  // if word exists already in the vocabulary..
            counter = vocab.get(word);                  // get the counter from the hashtable
        }
        counter.incrementCounter(type);                 // increase the counter appropriately

        vocab.put(word, counter);                       // put the word with its counter into the hashtable
    }


    // List the regular and spam messages
    private static void listDirs(File dir_location)
    {
        // List all files in the directory passed
        File[] dir_listing = dir_location.listFiles();

        // Check that there are 2 subdirectories
        if ( dir_listing.length != 2 )
        {
            System.out.println( "- Error: specified directory does not contain two subdirectories.\n" );
            Runtime.getRuntime().exit(0);
        }

        int regular_idx = dir_listing[0].getName().equals("regular") ? 0 : 1;

        listing_regular = dir_listing[regular_idx].listFiles();
        listing_spam    = dir_listing[1-regular_idx].listFiles();
    }

    private static ConfusionMatrix buildConfusionMatrix(File test_location){
        File[] test_listing = test_location.listFiles();
        // Check that there are 2 subdirectories
        if ( test_listing.length != 2 )
        {
            System.out.println( "- Error: specified TEST directory does not contain two subdirectories.\n" );
            Runtime.getRuntime().exit(0);
        }
        int regular_idx = test_listing[0].getName().equals("regular") ? 0 : 1;

        File[] test_listing_regular = test_listing[regular_idx].listFiles();
        File[] test_listing_spam    = test_listing[1-regular_idx].listFiles();
        
        ConfusionMatrix cf = new ConfusionMatrix(test_listing_spam.length, test_listing_regular.length);
        cf.setFP(getFalses(MessageType.NORMAL,test_listing_regular));
        cf.setFN(getFalses(MessageType.SPAM,test_listing_spam));
        return cf;
    }

    private static int getFalses(MessageType type, File[] testMsgs){
        int falses=0;
        for(File msg : testMsgs){

            try{
                if(type.value()!=bayesClass.classify(msg)) falses+=1;
            }catch(FileNotFoundException e){
                System.out.println("Cannot find file");
            }catch(IOException e){
                System.out.println("Cannot read or close file");
            }
        }
        return falses;
    }

    
    // Print the current content of the vocabulary
    private static void printVocab()
    {
        Multiple_Counter counter = new Multiple_Counter();

        for (Enumeration<String> e = vocab.keys() ; e.hasMoreElements() ;)
        {   
            String word;
            
            word = e.nextElement();
            counter  = vocab.get(word);
            
            System.out.println( word + " | in regular: " + counter.counter_regular + 
                                " in spam: "    + counter.counter_spam);
        }
    }


    // Read the words from messages and add them to your vocabulary. The boolean type determines whether the messages are regular or not  
    private static void readMessages(MessageType type)
    throws IOException
    {
        Reducer<String> reducer = new Reducer<>(new WordFilter(), new StopWordFilter());
        File[] messages = new File[0];

        if (type == MessageType.NORMAL){
            messages = listing_regular;
        } else {
            messages = listing_spam;
        }
        
        for (int i = 0; i < messages.length; ++i) // for each file
        {
            // create read stream
            FileInputStream i_s = new FileInputStream( messages[i] );
            BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
            String line;
            String word;
            
            while ((line = in.readLine()) != null)                      // read a line
            {
                if(line.contains("0rgasm")){
                    System.out.println("asdas");
                }
                ArrayList<String> st = Featurizer.extractFeatures(line);         // parse it into words
                
                for(String token : st)                  // while there are stille words left..
                {
                    addWord(token, type);                  // add them to the vocabulary
                }
            }

            in.close();
        }
    }
   
    public static void main(String[] args)
    throws IOException
    {
        // Location of the directory (the path) taken from the cmd line (first arg)
        File dir_location = new File( args[0] ); // this is for train, the second arg will look for test data
        
        // Check if the cmd line arg is a directory
        if ( !dir_location.isDirectory() )
        {
            System.out.println( "- Error: cmd line arg not a directory.\n" );
            Runtime.getRuntime().exit(0);
        }

        // Initialize the regular and spam lists
        listDirs(dir_location);

        //Initialize Bayes stats wrapper
        bayesClass = new BayesClass();

        //Initialize prior probabilities
        bayesClass.initializePriors(dir_location.listFiles());

        // Read the e-mail messages
        readMessages(MessageType.NORMAL);
        readMessages(MessageType.SPAM);

        // Print out the hash table
        //printVocab();
        bayesClass.initializeLikelihoods(vocab);
        //bayesClass.printTable();
        
        File test_location = new File( args[1] );
        // Check if the cmd line arg is a directory
        if ( !dir_location.isDirectory() )
        {
            System.out.println( "- Error: cmd line arg not a directory.\n" );
            Runtime.getRuntime().exit(0);
        }
        ConfusionMatrix cf = buildConfusionMatrix(test_location);
        cf.printMatrix();

        // Now all students must continue from here:
        //
        // 1) A priori class probabilities must be computed from the number of regular and spam messages
        // 2) The vocabulary must be clean: punctuation and digits must be removed, case insensitive
        // 3) Conditional probabilities must be computed for every word
        // 4) A priori probabilities must be computed for every word
        // 5) Zero probabilities must be replaced by a small estimated value
        // 6) Bayes rule must be applied on new messages, followed by argmax classification
        // 7) Errors must be computed on the test set (FAR = false accept rate (misses), FRR = false reject rate (false alarms))
        // 8) Improve the code and the performance (speed, accuracy)
        //
        // Use the same steps to create a class BigramBayespam which implements a classifier using a vocabulary consisting of bigrams
    }
}