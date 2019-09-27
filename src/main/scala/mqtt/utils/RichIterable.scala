package mqtt.utils

import scala.annotation.tailrec

object IterableImplicits {
  
  /**
   * Add some methods to Iterable scala type.
   *
   * @tparam A the type of collection's elements
   */
  implicit class RichIterable[A](iterable: Iterable[A]) {
    /**
     * Inclusive Span.
     *
     * @param f the split function
     * @return 2 new Iterable, the left Iterable have all elements that satisfies f condition plus the first falsy one,
     *         the right Iterable has the remaining elements
     */
    def spanUntil(f: A => Boolean): (Iterable[A], Iterable[A]) = iterable.span(f) match {
      case (included, excluded) => (included ++ excluded.take(1), excluded.drop(1))
    }
  
    /**
     * Like foldLeft but as soon as first element is ready the operation is called.
     *
     * @param z  the default B value to give to the operation
     * @param op the binary operation
     * @tparam B the return type of the operation
     * @return the result of operation with all elements in this iterable
     */
    @tailrec
    final def bendLeft[B](z: B)(op: (B, A) => B): B = iterable.headOption match {
      case Some(head) =>
        val result = op(z, head)
        iterable.tail.bendLeft(result)(op)
      case None => z
    }
  }
}
