interface Summable<A> {
    infix operator fun plus(other: A): A
}

fun <T : Summable<T>> Iterable<T>.sum(): T = this.reduce { sum, el -> sum + el }