package src;
import java.util.*;

///This class apllies a each filter in a given list to each word of a collection
public class Reducer<T>{

    ///List of filters
    Collection<Filter<T>> filters;

    ///Initialize (give list of filters)
    public Reducer(Filter<T>... f){
        this.filters = Arrays.asList(f);
    }

    ///Filter collection
    public ArrayList<T> reduce( ArrayList<T> coll){

        ///Given a collection of type T, copy it
        ArrayList<T> filtered = new ArrayList<>(coll);

        ///For each filter
        for(Filter<T> filter : filters)
            ///For each object
            for(T o : coll)
                ///If object is not accepted by filter remove it
                if(!filter.accept(o)) filtered.remove(o);

        ///Return filtered collections
        return filtered;
    }
}