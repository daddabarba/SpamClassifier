package src;
import java.util.*;

public class StringProcessor{
    ///String tokenizer for HTML tagged text

    public static ArrayList<String> splitTaggedText(String str){

        ///Identify HTML tags
        str = str.replace("<","#<#");
        str = str.replace(">","#>#");

        ///Split in HTML tags and sentences in between tags
        String[] parts = str.split("#");

        ///Empty token list
        ArrayList<String> splitted = new ArrayList<>();

        ///For each sub-string
        for(int i= 0; i<parts.length; i++){

            ///If it is a tag
            if(parts[i].equals("<") && i<parts.length-1){
                ///Add it as a single token
                splitted.add("<"+parts[i+1]+">");
                i+=2;

            ///If it is a sentence between tags
            }else{
                ///Remove invalid characters
                String line = parts[i].replaceAll("[^a-zA-Z0-9\\s]", "");

                ///Split the sentence into tokens (by space), and add each word as a single token
                splitted.addAll(Arrays.asList(line.split("\\s")));
            }
                
        }

        ///Return tokenized string
        return splitted;
    }
}