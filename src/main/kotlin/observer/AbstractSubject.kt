package observer

/**
 * Reusable base implementation of [Subject] — this is the heart of the Observer pattern.
 * Every sensor extends this class, so once it works, every sensor can be subscribed to.
 */
abstract class AbstractSubject<T> : Subject<T> {
    private val observers = mutableSetOf<Observer<T>>()

    override fun subscribe(observer: Observer<T>) {
        observers.add(observer)
    }

    override fun unsubscribe(observer: Observer<T>) {
        observers.remove(observer)
    }

    override fun notifyObservers(value: T) {
        for (observer in observers) {
            observer.onUpdate(value)
        }
    }
}
