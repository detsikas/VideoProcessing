package net.peeknpoke.apps.videoprocessing;

public interface ObserverSubject {
    void registerObserver(RendererObserver observer);
    void removeObserver(RendererObserver observer);
    void notifyObservers();
}
