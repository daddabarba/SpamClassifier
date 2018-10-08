package src;

public class EmptyTagFilter implements Filter<String>{
    ///Filters token if it is an empty HTML tag

    @Override
    public boolean accept(String word){
        ///If by removing all "<" and ">", nothing is left
        word = new String(word).replaceAll("[<>]", "");
        ///The filter out the word
        return word.length() != 0;
    }  
}