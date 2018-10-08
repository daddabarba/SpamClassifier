package src;

public class StopWordFilter implements Filter<String>{
    ///Filters out stop words (words with length less than 4)

    private int minSize; ///Minimum word size

    public StopWordFilter(int minSize){
        this.minSize = minSize;
    }

    public StopWordFilter(){
        this(4);
    }

    @Override
    public boolean accept(String word){
        return word.length()>this.minSize;
    }    
}