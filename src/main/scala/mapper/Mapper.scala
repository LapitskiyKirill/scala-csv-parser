package mapper

trait Mapper {
  def map(iterator: Iterator[String]): Iterator[Any]
}
