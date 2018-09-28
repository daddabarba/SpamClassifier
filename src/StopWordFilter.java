package src;

public class StopWordFilter implements Filter<String>{

    @Override
    public boolean accept(String word){
        return word.length()>4;
    }    
}