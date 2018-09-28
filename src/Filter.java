package src;

public interface Filter<T>{
    public boolean accept(T obj);
}