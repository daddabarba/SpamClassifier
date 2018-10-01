package src;

import java.util.*;
import src.Reducer;

public class Featurizer{

    private static Reducer<String> reducer =  new Reducer<>(new WordFilter(), new StopWordFilter());
    
    public static Collection<String> extractFeatures(String line){

        //Set all characters to lower case
        line.toLowerCase();

        //Split line in list of words (blank space separated)
        Collection<String> words = StringProcessor.splitTaggedText(line);
                
        //Pass all filters on words
        words = reducer.reduce(words);

        return words;
    }

}