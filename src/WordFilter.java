package src;

public class WordFilter implements Filter<String>{

    @Override
    public boolean accept(String word){
        return !isNumeric(word);
    }    

    private static boolean isNumeric(String str)  
    {  
        try {  
            double d = Double.parseDouble(str);  
        }  
        catch(NumberFormatException nfe){  
            return false;  
        }  
        return true;  
    }
}