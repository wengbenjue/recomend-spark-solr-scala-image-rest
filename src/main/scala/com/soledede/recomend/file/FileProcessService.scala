package com.soledede.recomend.file

import java.io.{InputStream, OutputStream}

import com.soledede.recomend.ui.GeneralRecommendUIImpl

/**
  * Created by soledede on 16/1/15.
  *
  * process file
  * eg:  upload image
  */
trait FileProcessService {

   def saveAttachment(fileName: String,content: Array[Byte],dir: String): Boolean

   def saveAttachment(fileName: String, content: InputStream,dir: String): Boolean

}

object FileProcessService{
  def apply(rType: String = "default"): FileProcessService = {
    rType match {
      case "default" => DefaultFileProcessServiceImpl()
    }
  }
}


class DefaultFileProcessServiceImpl extends  FileProcessService{


  override def saveAttachment(fileName: String, content: InputStream,dir: String): Boolean = {
    saveAttachment[InputStream](fileName, content,dir, { (is, os) =>
      val buffer = new Array[Byte](16384)
      Iterator
        .continually(is.read(buffer))
        .takeWhile(-1 !=)
        .foreach(read => os.write(buffer, 0, read))
    }
    )
  }

  override def saveAttachment(fileName: String, content: Array[Byte],dir: String): Boolean = {
    saveAttachment[Array[Byte]](fileName, content, dir,{ (is, os) => os.write(is) })
  }

  private def saveAttachment[T](fileName: String, content: T, dir: String,writeFile: (T, OutputStream) => Unit): Boolean = {
    try {
      val fos = new java.io.FileOutputStream(dir + fileName)
      writeFile(content, fos)
      fos.close()
      true
    } catch {
      case _ => false
    }
  }

}


object DefaultFileProcessServiceImpl {
  var defaultFileProcessServiceImpl: DefaultFileProcessServiceImpl = null

  def apply(): DefaultFileProcessServiceImpl = {
    if (defaultFileProcessServiceImpl == null) defaultFileProcessServiceImpl = new DefaultFileProcessServiceImpl()
    defaultFileProcessServiceImpl
  }
}