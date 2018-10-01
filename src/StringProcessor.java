package src;
import java.util.*;

public class StringProcessor{
    public static ArrayList<String> splitTaggedText(String str){
        str = str.replace("<","#<#");
        str = str.replace(">","#>#");
        
        String[] parts = str.split("#");
        ArrayList<String> splitted = new ArrayList<>();
        
        for(int i= 0; i<parts.length; i++){
            if(parts[i].equals("<") && i<parts.length-1){
                splitted.add("<"+parts[i+1]+">");
                i+=2;
            }else{
                String line = parts[i].replaceAll("[^a-zA-Z0-9\\s]", "");
                splitted.addAll(Arrays.asList(line.split("\\s")));
            }
                
        }
        
        return splitted;
    }
}