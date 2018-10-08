package src;

import java.util.*;
import src.Reducer;

public class Featurizer{
    ///Collection of static function to tokenize a string

    ///Filters used on the tokens (remove numbers and stop words)
    private static Reducer<String> reducer =  new Reducer<>(new WordFilter(), new StopWordFilter());

    ///Return Collection of tokens
    public static ArrayList<String> extractFeatures(String line){

        ///Set all characters to lower case
        line.toLowerCase();

        ///Split line in list of words (blank space separated)
        ArrayList<String> words = StringProcessor.splitTaggedText(line);
                
        ///Pass all filters on all words
        words = reducer.reduce(words);

        ///Return collection of filtered tokens
        return words;
    }

    public static ArrayList<String> extractBigrams(String line){
        ///Extract filtered unigrams
        ArrayList<String> tokens = extractFeatures(line);

        ///Initialize emopty bigram collection
        ArrayList<String> bigrams = new ArrayList<>();

        ///For each unigram
        for(int i = 0; i < tokens.size()-1; i++){
            ///If it is not an HTML tag
            if(!isTag(tokens.get(i)) && !isTag(tokens.get(i+1)))
                ///Builf bigram with successive token
                bigrams.add(tokens.get(i)+" "+tokens.get(i+1));
        }

        ///Return collection of bigrams
        return bigrams;
    }

    public static void changeMinSize(int minSize){
        ///Edit the minimum word size (for the stop word filter)
        reducer =new Reducer<>(new WordFilter(), new StopWordFilter(minSize));
    }

    private static boolean isTag(String token){
        ///Checks if a given string is an HTML tag
        return token.charAt(0) == '<' && token.charAt(token.length()-1) == '>';
    }

}