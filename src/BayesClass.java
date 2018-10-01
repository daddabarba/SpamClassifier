package src;

import java.io.*;
import java.util.*;

public class BayesClass{

    public static final double eps = 1.0;
    public static final double alpha = 1.0;

    private double prior_H0, prior_H1;
    private int size_H0=0, size_H1=0;

    private Hashtable<String, ProbabilityPair> likelihoods = new Hashtable<>();

    private class ProbabilityPair{

        private double P_H0, P_H1;

        public ProbabilityPair(double lH0, double lH1){
            P_H0 = lH0;
            P_H1 = lH1;
        }

        public double getP(int cls){
            if(cls==0)
                return P_H0;

            return P_H1;
        }

        public void add(ProbabilityPair p){
            P_H0 += p.getP(0);
            P_H1 += p.getP(1);
        }
    }

    public int classify(File message) throws FileNotFoundException, IOException{
        ProbabilityPair posterior = getPosteriors(message);
        if(posterior.getP(0)>posterior.getP(1))
            return 0;
        
        return 1;
    }

    public ProbabilityPair getPosteriors(File message) throws FileNotFoundException, IOException{
        FileInputStream i_s = new FileInputStream( message );
        BufferedReader in = new BufferedReader(new InputStreamReader(i_s));

        String line;

        ProbabilityPair posterior = new ProbabilityPair(prior_H0+alpha,prior_H1+alpha); 

        while ((line = in.readLine()) != null)
            posterior.add(getLineLikelihood(line));

        in.close();

        return posterior;
    }

    private ProbabilityPair getLineLikelihood(String line){

        ProbabilityPair lineLikelihood = new ProbabilityPair(0.0, 0.0);

        Collection<String> words  = Featurizer.extractFeatures(line);

        for(String token : words)
            if(likelihoods.keySet().contains(token))
                lineLikelihood.add(likelihoods.get(token));
        
        return lineLikelihood;
    }

    public void initializePriors(File[] directories){ 

        int num_regular = directories[0].listFiles().length;
        int num_spam = directories[1].listFiles().length;

        int total = num_regular + num_spam;

        prior_H0 = (double)num_regular/total;
        prior_H1 = (double)num_spam/total;

        prior_H0 = Math.log(prior_H0);
        prior_H1 = Math.log(prior_H1);
    }

    public void initializeLikelihoods(Hashtable<String, Bayespam.Multiple_Counter> absFrequencies){

        for(String word : absFrequencies.keySet()){
            size_H0 += absFrequencies.get(word).counter_regular;
            size_H1 += absFrequencies.get(word).counter_spam;
        }

        for(String word : absFrequencies.keySet()){

            double likelihood_H0 = (double)absFrequencies.get(word).counter_regular/size_H0;
            double likelihood_H1 = (double)absFrequencies.get(word).counter_spam/size_H1;

            if(!(likelihood_H0>0.0))
                likelihood_H0 = eps/(size_H0+size_H1);

            if(!(likelihood_H1>0.0))
                likelihood_H1 = eps/(size_H0+size_H1);


            likelihood_H0 = Math.log(likelihood_H0);
            likelihood_H1 = Math.log(likelihood_H1);

            likelihoods.put(word, new ProbabilityPair(likelihood_H0, likelihood_H1));
        }

    }

    public void printTable(){
        for(String key : likelihoods.keySet()){
            System.out.print("Word="+key+" p_reg="+likelihoods.get(key).getP(0));
            System.out.println("p_spam="+likelihoods.get(key).getP(1));
        }
    }

    public double getPrior(int cls){
        if(cls==0)
            return prior_H0;

        return prior_H1;
    }
}