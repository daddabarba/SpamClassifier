package src;

public class StopWordFilter implements Filter<String>{
    ///Filters out stop words (words with length less than 4)

    @Override
    public boolean accept(String word){
        return word.length()>4;
    }    
}