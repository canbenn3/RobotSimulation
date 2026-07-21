import observer.AbstractSubject
import observer.Observer
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserverTest {

    private class TestSubject<T> : AbstractSubject<T>() {
        fun emit(value: T) = notifyObservers(value)
    }

    private class RecordingObserver<T> : Observer<T> {
        val values = mutableListOf<T>()
        override fun onUpdate(value: T) {
            values += value
        }
    }

    @Test
    fun `subscribe receives notified values`() {
        val subject = TestSubject<Int>()
        val observer = RecordingObserver<Int>()

        subject.subscribe(observer)
        subject.emit(1)
        subject.emit(2)

        assertEquals(listOf(1, 2), observer.values)
    }

    @Test
    fun `unsubscribe stops further notifications`() {
        val subject = TestSubject<String>()
        val observer = RecordingObserver<String>()

        subject.subscribe(observer)
        subject.emit("a")
        subject.unsubscribe(observer)
        subject.emit("b")

        assertEquals(listOf("a"), observer.values)
    }

    @Test
    fun `notifyObservers fans out to every subscriber`() {
        val subject = TestSubject<Boolean>()
        val first = RecordingObserver<Boolean>()
        val second = RecordingObserver<Boolean>()

        subject.subscribe(first)
        subject.subscribe(second)
        subject.emit(true)

        assertEquals(listOf(true), first.values)
        assertEquals(listOf(true), second.values)
    }

    @Test
    fun `subscribing the same observer twice does not duplicate delivery`() {
        val subject = TestSubject<Int>()
        val observer = RecordingObserver<Int>()

        subject.subscribe(observer)
        subject.subscribe(observer)
        subject.emit(42)

        assertEquals(listOf(42), observer.values)
    }

    @Test
    fun `unsubscribe is a no-op for an observer that was never subscribed`() {
        val subject = TestSubject<Int>()
        val observer = RecordingObserver<Int>()

        subject.unsubscribe(observer)
        subject.subscribe(observer)
        subject.emit(7)
        subject.emit(8)

        assertEquals(listOf(7, 8), observer.values)
    }
}
