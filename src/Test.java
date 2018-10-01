package src;

import java.io.*;
import src.BayesClass;

public class Test{

    public static void main(String[] args){
        BayesClass classif = new BayesClass();
        
        classif.initializePriors(new File("../data/spam-filter/test").listFiles());
        System.out.println("P_0="+classif.getPrior(0)+" P_1="+classif.getPrior(1));
    }

}