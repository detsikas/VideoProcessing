package net.peeknpoke.apps.videoprocessing;

public interface ObserverSubject<T> {
    void registerObserver(T observer);
    void removeObserver(T observer);
    void notifyObservers();
}
