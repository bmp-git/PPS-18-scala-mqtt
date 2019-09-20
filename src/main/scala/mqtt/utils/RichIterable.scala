package mqtt.utils

object IterableImplicits {
  
  /**
   * Add some methods to Iterable scala type.
   *
   * @tparam T the type of collection's elements
   */
  implicit class RichIterable[T](iterable: Iterable[T]) {
    /**
     * Inclusive Span.
     *
     * @param f the split function
     * @return 2 new Iterable, the left Iterable have all elements that satisfies f condition plus the first falsy one,
     *         the right Iterable has the remaining elements
     */
    def spanUntil(f: T => Boolean): (Iterable[T], Iterable[T]) = iterable.span(f) match {
      case (included, excluded) => (included ++ excluded.take(1), excluded.drop(1))
    }
  
    //TODO: doc
    def bendLeft[A](acc: A)(f: (A, T) => A): Unit = {
      def internalBendLeft[A](acc: A, it: Iterable[T])(f: (A, T) => A): Unit = {
        if (it.nonEmpty) {
          internalBendLeft(f(acc, it.head), it.tail)(f)
        }
      }
      internalBendLeft(acc, iterable)(f)
    }
  }
}
