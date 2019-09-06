package mqtt.parser

trait Monad[F[_]] {
  def unit[A](a: => A): F[A]
  
  def flatMap[A, B](ma: F[A])(f: A => F[B]): F[B]
}

object Monad {
  
  implicit class RichMonad[F[_] : Monad, A](ma: F[A]) {
    val monad = implicitly[Monad[F]]
    
    def map[B](f: A => B): F[B] = ma.flatMap(a => monad.unit(f(a)))
    
    def flatMap[B](f: A => F[B]): F[B] = monad.flatMap[A, B](ma)(f)
  }
  
}

