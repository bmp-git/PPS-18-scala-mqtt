package mqtt.parser

trait Monad[F[_]] {
  def unit[A](a: => A): F[A]
  
  def flatMap[A, B](ma: F[A])(f: A => F[B]): F[B]
}

object Monad {
  
  def map2[F[_] : Monad, A, B, C](ma: F[A])(mb: => F[B])(f: (A, B) => C): F[C] = ma.flatMap(a => mb.map(b => f(a, b)))
  
  implicit class RichMonad[F[_] : Monad, A](ma: F[A]) {
    val monad = implicitly[Monad[F]]
    
    def flatMap[B](f: A => F[B]): F[B] = monad.flatMap[A, B](ma)(f)
    
    def map[B](f: A => B): F[B] = ma.flatMap(a => monad.unit(f(a)))
  }
  
}

