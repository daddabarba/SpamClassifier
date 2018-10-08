package src;

public class WordFilter implements Filter<String>{
    //Filters out numerals

    @Override
    public boolean accept(String word){
        return !isNumeric(word);
    }    

    //returns true if a string is a numeral
    private static boolean isNumeric(String str)  
    {  
        try {
            //Parse a double
            double d = Double.parseDouble(str);  
        } catch(NumberFormatException nfe){
            //If fails to do so, the string is not a double
            return false;  
        }

        //If it doesn't fail the string is a doble
        return true;  
    }
}