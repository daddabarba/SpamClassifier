package src;
import java.util.*;

public class Reducer<T>{
    Collection<Filter<T>> filters; // make array
    public Reducer(Filter<T>... f){
        this.filters = Arrays.asList(f);
    }

    public Collection<T> reduce( Collection<T> coll){
        Collection<T> filtered = new ArrayList<>(coll);

        for(Filter<T> filter : filters)
            for(T o : coll)
                if(!filter.accept(o)) filtered.remove(o);
        
        return filtered;
    }
}