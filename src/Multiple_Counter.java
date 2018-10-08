package src;

///Counter of absolute frequencies (for one token), w.r.t. spam and refular
public class Multiple_Counter{
    int counter_spam    = 0;
    int counter_regular = 0;

    /// Increase one of the counters by one
    public void incrementCounter(Bayespam.MessageType type)
    {
        if ( type == Bayespam.MessageType.NORMAL ){
            ++counter_regular;
        } else {
            ++counter_spam;
        }
    }
}