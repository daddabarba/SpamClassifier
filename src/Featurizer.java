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

    private static boolean isTag(String token){
        return token.charAt(0) == '<' && token.charAt(token.length()) == '>';
    }
    public static Collection<String> extractBigrams(String line){
        ArrayList<String> tokens = extractFeatures(line);
        Collection<String> bigrams = new ArrayList<>();
        
        for(int i = 0; i < tokens.size()-1; i++){
            if(!isTag(tokens.get(i)) && !isTag(tokens.get(i+1))){
                bigrams.add(tokens.get(i)+" "+tokens.get(i));
            }
        }

        return bigrams;
    }

}