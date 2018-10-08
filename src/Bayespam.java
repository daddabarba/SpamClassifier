package src;

import java.io.*;
import java.util.*;

public class Bayespam {
    //Unigram Naive Bayes Classifier

    // This defines the two types of messages we have.
    public static enum MessageType
    {
        NORMAL(0), SPAM(1);

        private int val;//Int value of each type

        //Constructor
        private MessageType(int val){
            this.val = val;
        }

        //Int conversion of MessageType
        public int value(){
            return this.val;
        }
    }

    
    // Confusiton matrix, keeps track of error rate (w.r.t. each class)
    public static class ConfusionMatrix{
        int totPos; //Number of positive (spam) examples
        int totNeg; //Number of negative (regular) examples
        int FP; //Number of False Positive errors
        int FN; //Number of False Negative errors

        //Constructor, initialize number of positive (spam) and negative (non spam) examples
        public ConfusionMatrix(int totPos, int totNeg){
            this.totPos = totPos;
            this.totNeg = totNeg;
        }

        //Setters for false positive and false negative rate errors
        public void setFP(int fp){
            this.FP = fp;
        };
        public void setFN(int fn){
            this.FN = fn;
        };

        //Print full confusion matrix as a Json
        public void printJSONMatrix(){
            System.out.println("{ \"tp\":"+(totPos-FN)+", \"fn\": "+FN+","+
                                "\"fp\":"+FP+", \"tn\": "+(totNeg-FP)+"}");
        }

        //Print full confusion matrix
        public void printMatrix(){
            //True positives vs False Negatives
            System.out.println(" tp: "+(totPos-FN)+" fn: "+FN);
            //False positives vs True negatives
            System.out.println(" fp: "+FP+" tn: "+(totNeg-FP));
        }

    }

    //Initialize Bayes stats wrapper
    private static BayesClass bayesClass;

    // Listings of the two subdirectories (regular/ and spam/)
    private static File[] listing_regular = new File[0];
    private static File[] listing_spam = new File[0];

    // A hash table for the vocabulary (maps from word to absolute frequency)
    private static Hashtable <String, Multiple_Counter> vocab = new Hashtable <String, Multiple_Counter> ();
    
    // Add a word to the vocabulary
    private static void addWord(String word, MessageType type)
    {
        Multiple_Counter counter = new Multiple_Counter();

        // if word exists already in the vocabulary..
        if ( vocab.containsKey(word) )
            // get the counter from the hashtable
            counter = vocab.get(word);

        // increase the counter appropriately
        counter.incrementCounter(type);

        // put the word with its counter into the hashtable
        vocab.put(word, counter);
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

        //Check which folder contains regular messages, and which contains spam messages
        int regular_idx = dir_listing[0].getName().equals("regular") ? 0 : 1;

        //Get paths to regular and spam folder
        listing_regular = dir_listing[regular_idx].listFiles();
        listing_spam    = dir_listing[1-regular_idx].listFiles();
    }

    //Test the classifier and buil resulting confusion matrix (given test location)
    private static ConfusionMatrix buildConfusionMatrix(File test_location){

        // List all files in the directory passed
        File[] test_listing = test_location.listFiles();

        // Check that there are 2 subdirectories
        if ( test_listing.length != 2 )
        {
            System.out.println( "- Error: specified TEST directory does not contain two subdirectories.\n" );
            Runtime.getRuntime().exit(0);
        }

        //Check which folder contains regular messages, and which contains spam messages
        int regular_idx = test_listing[0].getName().equals("regular") ? 0 : 1;

        //Get paths to regular and spam folder
        File[] test_listing_regular = test_listing[regular_idx].listFiles();
        File[] test_listing_spam    = test_listing[1-regular_idx].listFiles();

        //Initialize confusion matrix with number of Positives(spam) and Negative(regular) examples in the testing data-set
        ConfusionMatrix cf = new ConfusionMatrix(test_listing_spam.length, test_listing_regular.length);

        //Compute number of false positives (Number of regular classified as spam)
        cf.setFP(getFalses(MessageType.NORMAL,test_listing_regular));
        //Compute number of false negatives (Number of spam classified as regular)
        cf.setFN(getFalses(MessageType.SPAM,test_listing_spam));

        //Return confusion matrix
        return cf;
    }

    //Returns number of errors in classifying messages testMsgs of known class type
    private static int getFalses(MessageType type, File[] testMsgs){

        //Initialize number of misclassifications to 0
        int falses=0;

        //For each message
        for(File msg : testMsgs){

            try{

                //Classify message. If classified class different from type
                if(type.value()!=bayesClass.classify(msg))
                    //Add one error to the count
                    falses+=1;

            }catch(FileNotFoundException e){
                System.out.println("Cannot find file");
            }catch(IOException e){
                System.out.println("Cannot read or close file");
            }
        }

        //Return number of errors
        return falses;
    }
    
    // Print the current content of the vocabulary
    private static void printVocab()
    {
        //Empty counter variable
        Multiple_Counter counter = new Multiple_Counter();

        //For each token in vocabulaty
        for (Enumeration<String> e = vocab.keys() ; e.hasMoreElements() ;)
        {   
            String word;

            //Get word
            word = e.nextElement();
            //Get word's counter
            counter  = vocab.get(word);

            //Print absolute frequencies
            System.out.println( word + " | in regular: " + counter.counter_regular + 
                                " in spam: "    + counter.counter_spam);
        }
    }


    // Read the words from messages and add them to your vocabulary. The boolean type determines whether the messages are regular or not  
    private static void readMessages(MessageType type)
    throws IOException
    {
        //Empty list of messages
        File[] messages = new File[0];

        //Depending on the class counted, select folder
        if (type == MessageType.NORMAL)
            messages = listing_regular;
        else
            messages = listing_spam;

        //For each message
        for (int i = 0; i < messages.length; ++i)
        {
            // create read stream
            FileInputStream i_s = new FileInputStream( messages[i] );
            BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
            String line;
            String word;

            // For each line
            while ((line = in.readLine()) != null)
            {

                //Parse line into words
                ArrayList<String> st = Featurizer.extractFeatures(line);

                //For every word
                for(String token : st)
                    //Add to vocabulary
                    addWord(token, type);
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

        //If third argument is given
        if(args.length>2){

            //Parse it as the used epsilon
            try{

                //Get epsilon
                double eps = Double.parseDouble(args[2]);
                //Initialize wrapper with given epsilon
                bayesClass = new BayesClass(eps);

            }catch(NullPointerException e){
                System.out.println("Third argument must be double (epsilon)");
                System.exit(1);
            }
        }else
            //Initialize wrapper with default parameters (epsilon)
            bayesClass = new BayesClass();

        //Initialize prior probabilities
        bayesClass.initializePriors(dir_location.listFiles());

        //Read the e-mail messages
        readMessages(MessageType.NORMAL);
        readMessages(MessageType.SPAM);

        //Convert absolute frequencies to log likelihoods
        bayesClass.initializeLikelihoods(vocab);

        //Get testing folder
        File test_location = new File( args[1] );

        // Check if the cmd line arg is a directory
        if ( !dir_location.isDirectory() )
        {
            System.out.println( "- Error: cmd line arg not a directory.\n" );
            Runtime.getRuntime().exit(0);
        }

        //Test Classigier on data-set in testing folder (build confusion matrix)
        ConfusionMatrix cf = buildConfusionMatrix(test_location);

        //Print confusion matrix
        cf.printMatrix();
    }
}