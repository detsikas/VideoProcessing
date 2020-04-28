package net.peeknpoke.apps.frameprocessor;

public interface ObserverSubject<T> {
    void registerObserver(T observer);
    void removeObserver(T observer);
    void notifyObservers();
}
