package mqtt.parser

trait Monad[F[_]] {
  def unit[A](a: => A): F[A]
  
  def flatMap[A, B](ma: F[A])(f: A => F[B]): F[B]
}

object Monad {
  
  def m[F[_ ]: Monad]: Monad[F] = implicitly[Monad[F]]
  
  def map2[F[_]: Monad, A, B, C](ma: F[A])(mb: => F[B])(f: (A, B) => C): F[C] =
    ma.flatMap(a => mb.map(b => f(a, b)))
  
  def sequence[F[_]: Monad, A](lma : List[F[A]]): F[List[A]] =
    lma.foldRight(m[F].unit(List[A]()))((ma, mla) => map2(ma)(mla)(_ :: _))
  
  implicit class RichMonad[F[_] : Monad, A](ma: F[A]) {
    val monad = implicitly[Monad[F]]
    
    def map[B](f: A => B): F[B] = ma.flatMap(a => monad.unit(f(a)))
    
    def flatMap[B](f: A => F[B]): F[B] = monad.flatMap[A, B](ma)(f)
  }
  
}

