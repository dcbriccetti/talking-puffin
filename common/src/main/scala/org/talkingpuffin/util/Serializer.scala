package org.talkingpuffin.util
import java.io._

object Serializer {
  def serialize(obj: AnyRef): Array[Byte] = {
    val os = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(os)
    oos.writeObject(obj)
    oos.close()
    os.close()
    os.toByteArray
  }

  def deSerialize[T](stream: Array[Byte]): T = {
    val is = new ByteArrayInputStream(stream)
    val ois = new ObjectInputStream(is)
    ois.readObject.asInstanceOf[T]
  }
}
