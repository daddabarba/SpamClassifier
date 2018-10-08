package src;

public interface Filter<T>{
    //Filter for object of type T

    public boolean accept(T obj); //Return true if the object doesn't have to be filtered (is accepted)
}