package src;

import java.io.*;
import java.util.*;

public class BayesClass{
    ///Wrapper for Bayes formulas and data (posterior, likelihood, and evidence computation)
    ///In this class the null hypothesis class (H0, or class 0) is the regular class,
    ///while the alternative hypothesis class (H1, or class 1) is the spam class.

    ///Defines the used feature space
    public enum FeatureMode{
        UNIGRAM, BIGRAM
    }

    ///Parameters
    private double eps;///Constance epsilon (for likelihood of new tokens)
    public static final double alpha = 1.0;///Normalizing constant for posteriors
    private double minF;///threshold (minimum) evidence for Bigram classifier

    ///Statistical data of dataset
    private double prior_H0, prior_H1;///Priors of each class
    private double log_prior_H0, log_prior_H1;///Log priors of each class
    private int size_H0, size_H1;///Number of tokens in each class


    private HashMap<String, ProbabilityPair> likelihoods;///Dictionary of likelihoods (maps from token to likelihood)
    private ArrayList<String> blackList;///Ignored word (with evidence smaller than minF)

    ///Inner class storying statistical data of a single token
    private class ProbabilityPair implements  Comparable<ProbabilityPair>{

        private double P_H0, P_H1;///Conditional probability of token w.r.t. each class
        private double evidence;///evidence (p(token))

        ///Initialize
        public ProbabilityPair(double lH0, double lH1){
            P_H0 = lH0;
            P_H1 = lH1;
            evidence = 0.0;
        }

        ///Update or initialize evidence value
        public void setEvidence(double evidence){
            this.evidence = evidence;
        }

        ///Returns condition probability w.r.t. class cls
        public double getP(int cls){
            if(cls==0)
                return P_H0;

            return P_H1;
        }

        ///Returns evidence (p(token))
        public double getEvidence(){
            return this.evidence;
        }

        ///Addition operation between ProbabilityPair objects
        public void add(ProbabilityPair p){
            P_H0 += p.getP(0);
            P_H1 += p.getP(1);
        }

        ///Function defining how to compare two probability pairs
        public int compareTo(ProbabilityPair o){
            ///Evidence determines which probability pair is higher
            if(evidence==o.getEvidence())
                return 0;

            ///If evidence of this token is higher than o's, then this ProabilityPair is bigger than o
            return evidence<o.getEvidence() ? -1 : 1;
        }
    }

    ///CONSTRUCTORS

    ///Initialize with epsilon and evidence threshold set
    public BayesClass(double eps, double minF){
        this.eps = eps;
        this.minF = minF;

        size_H0 = 0;
        size_H1 = 0;

        likelihoods = new HashMap<>();
        blackList = new ArrayList<>();
    }

    ///Initialize with epsilon set (and default minF)
    public BayesClass(double eps){
        this(eps, 0.0);
    }

    ///Initialize with default epsilon and minF
    public BayesClass(){
        this(1.0, 0.0);
    }

    ///METHODS

    ///Returns the class of a message, that maximizes the posterior probability, given the message's features(tokens)
    public int classify(File message) throws FileNotFoundException, IOException {
        return classify(message, FeatureMode.UNIGRAM);
    }

    ///Classifies messa (see above), with a specified feature extraction method ("see above");
    public int classify(File message, FeatureMode mode) throws FileNotFoundException, IOException{

        ///Compute log posterior probabilities for each class (store in a ProbabilityPair for convenience)
        ProbabilityPair posterior = getPosteriors(message, mode);

        ///Return class that maximizes the posterior probability p(class|features), given the message('s features)
        if(posterior.getP(0)>posterior.getP(1))
            return 0;
        
        return 1;
    }

    ///Compute posteriors for each class (default mode UNIGRAM)
    public ProbabilityPair getPosteriors(File message) throws FileNotFoundException, IOException{
        return getPosteriors(message, FeatureMode.UNIGRAM);
    }

    ///Compute log posteriors for each class, given a specic feature extraction method (UNIGRAM or BIGRAM)
    public ProbabilityPair getPosteriors(File message, FeatureMode mode) throws FileNotFoundException, IOException{

        ///Open file
        FileInputStream i_s = new FileInputStream( message );
        BufferedReader in = new BufferedReader(new InputStreamReader(i_s));

        ///Current line
        String line;

        ///Initialize log posterior to log(alpha) + log(priors), then sum the log likelihoods of tokens
        ProbabilityPair posterior = new ProbabilityPair(getPrior(0)+alpha,getPrior(1)+alpha); 

        ///For each line in the file
        while ((line = in.readLine()) != null)
            ///Add the log likelihoods (in parallel w.r.t. each class) of each token
            posterior.add(getLineLikelihood(line, mode));

        in.close();

        ///Return log posteriors
        return posterior;
    }

    ///Returns the sum of all log lokeilihoods, of each token in a line (w.r.t. to each class)
    private ProbabilityPair getLineLikelihood(String line, FeatureMode mode){

        ///Initialize log likelihoods (with respect to each class)
        ProbabilityPair lineLikelihood = new ProbabilityPair(0.0, 0.0);

        ///Tokens in line
        ArrayList<String> words;

        ///Segment line in tokens according to specified mode
        if(mode==FeatureMode.UNIGRAM)
            ///Segment in unigrams
            words  = Featurizer.extractFeatures(line);
        else
            ///Segment in bigrams
            words = Featurizer.extractBigrams(line);

        ///For each token in the sentence
        for(String token : words)
            ///If word is in dictionary
            if(likelihoods.keySet().contains(token))
                ///If BIGRAM mode ignore bigrams with evidence < minF
                if(mode!=FeatureMode.BIGRAM || !blackList.contains(token))

                    ///Add token log likelihoods (w.r.t each class)
                    lineLikelihood.add(likelihoods.get(token));

        ///Return summed log likelihoods
        return lineLikelihood;
    }

    ///Given the directory containing spam and regular messages, compute priors and log priors of each class
    public void initializePriors(File[] directories){ 

        ///Number of messages for each class
        int num_regular = directories[0].listFiles().length;
        int num_spam = directories[1].listFiles().length;

        ///Total number of messages
        int total = num_regular + num_spam;

        ///Prior probability (p(class))
        prior_H0 = (double)num_regular/total;
        prior_H1 = (double)num_spam/total;

        ///Log priors (log(p(class)))
        log_prior_H0 = Math.log(prior_H0);
        log_prior_H1 = Math.log(prior_H1);
    }

    ///Given a dictionary of absolute frequencies, compute log likelihoods for each token
    public void initializeLikelihoods(Hashtable<String, Multiple_Counter> absFrequencies){

        ///Get number of tokens for each class
        for(String word : absFrequencies.keySet()){
            size_H0 += absFrequencies.get(word).counter_regular;
            size_H1 += absFrequencies.get(word).counter_spam;
        }

        ///For each token in the dictionary
        for(String word : absFrequencies.keySet()){

            ///Compute likelihood w.r.t. each class
            double likelihood_H0 = (double)absFrequencies.get(word).counter_regular/size_H0;
            double likelihood_H1 = (double)absFrequencies.get(word).counter_spam/size_H1;

            ///If likelihood_H0=0, then initialize to small constant value
            if(absFrequencies.get(word).counter_regular==0)
                likelihood_H0 = eps/(size_H0+size_H1);

            ///If likelihood_H1=0, then initialize to small constant value
            if(absFrequencies.get(word).counter_spam==0)
                likelihood_H1 = eps/(size_H0+size_H1);

            ///Convert to log likelihoods, compute evidence, then add token with log likelihoods to dictionary,
            likelihoods.put(word,computeEvidence(likelihood_H0, likelihood_H1));
        }

        ///COnvert dictionary of likelihoods in list of <Key,Value> pair
        List<Map.Entry<String, ProbabilityPair>> listDict = new LinkedList<Map.Entry<String, ProbabilityPair>>(likelihoods.entrySet());

        ///Sort list, by according to value
        Collections.sort(listDict, new Comparator<Map.Entry<String, ProbabilityPair>>() {

            ///To compare two <Key,Value> entries, compare the Values (ProbabilityPairs)
            public int compare(Map.Entry<String, ProbabilityPair> o1, Map.Entry<String, ProbabilityPair> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        ///For the first minF*100 percentage tokens (minF*100% tokens with the smalles evidence)
        for(int i=0; i<(int)(minF*listDict.size()); i++)
            ///Add to blacklist (ignore list)
            blackList.add(listDict.get(i).getKey());

    }

    ///Compute evidence given likelihoods w.r.t to each class, and convert likelihoods to log likellihoods
    public ProbabilityPair computeEvidence(double l_H0, double l_H1){

        ///Wrap results in ProbabilityPair object (converting to log likelihoods)
        ProbabilityPair ret = new ProbabilityPair(Math.log(l_H0), Math.log(l_H1));
        ///Compute evidence of token (not log, as it is used only for thresholding)
        ret.setEvidence(l_H0*prior_H0 + l_H1*prior_H1);

        ///Return probability pair
        return ret;
    }

    ///Returns log priors of class cls
    public double getPrior(int cls){
        if(cls==0)
            return log_prior_H0;

        return log_prior_H1;
    }

    ///Prints contents of likelihoods table
    public void printTable(){

        ///For each token
        for(String key : likelihoods.keySet()){
            ///Print log likelihoods
            System.out.print("Word="+key+"\t");
            System.out.print("log p(" + key + "|reg)=" +likelihoods.get(key).getP(0));
            System.out.println("log p(" + key + "|spam)=" +likelihoods.get(key).getP(1));
        }
    }
}