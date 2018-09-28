package src;

public class EmptyTagFilter implements Filter<String>{

    @Override
    public boolean accept(String word){
        word = new String(word).replaceAll("[<>]", "");
        return word.length() != 0;
    }  
}