package com.binbo_kodakusan

import java.io.File
import java.nio.file.{Files, NoSuchFileException, Paths}
import java.nio.file.attribute.BasicFileAttributes

import scala.annotation.tailrec
import scala.collection.mutable
import scala.math.BigDecimal.RoundingMode
import scala.reflect.io.Path
import scalafx.beans.property.StringProperty
import scala.concurrent.ExecutionContext.Implicits.global
import scalafx.application.Platform

trait Info {
  var parent: DirectoryInfo
  val id: String
  val fullName: String
  val name: String
  val size: Long
  val selectable: Boolean
}

class FileInfo private (_id: String, _fullName: String, _name: String, _size: Long) extends Info {
  override var parent: DirectoryInfo = _
  override val id = _id
  override val fullName = _fullName
  override val name = _name
  override val size: Long = _size
  override val selectable: Boolean = false
}

object FileInfo {
  def apply(f: File) = new FileInfo(f.getName, f.getAbsolutePath, Model.truncateName(f.getName), f.length())
}

class DirectoryInfo(_id: String, _fullName: String, _name: String, val _selectable: Boolean, _children: List[Info]) extends Info {
  val children = _children
  children.foreach(c => c.parent = this)

  override var parent: DirectoryInfo = _
  override val id = _id
  override val fullName = _fullName
  override val name = _name
  override val size: Long = children.foldLeft(0L)((acc, c) => acc + c.size)
  override val selectable = _selectable
}

object DirectoryInfo {
  def apply(f: File, selectable: Boolean, children: List[Info]) = new DirectoryInfo(f.getName, f.getAbsolutePath, Model.truncateName(f.getName), selectable, children)
}

case class DummyInfo(override val name: String, override val size: Long) extends Info {
  override var parent: DirectoryInfo = _
  override val fullName = name
  override val id = name
  override val selectable: Boolean = false
}

object Model {
  // ディレクトリ・ファイルの最大表示数
  val MaxCount = 30
  // ファイル名の最大長さ
  val MaxLength = 20

  def analyze(path: String, followSymlink: Boolean, statusText: StringProperty): DirectoryInfo = {
    val setFiles = new mutable.HashMap[Object, Info]

    def loop(path: String): Info = {
      Platform.runLater(statusText.value = path)
      val f = new File(path)
      val p = Files.readAttributes(Paths.get(path), classOf[BasicFileAttributes])
      val isSymbolicLink1 = p.isSymbolicLink
      val isSymbolicLink2 = Files.isSymbolicLink(Paths.get(path))
      val isSymbolicLink3 = (path != f.getAbsolutePath)
      try {
        if (p.isDirectory) {
          val k = p.fileKey
          if (k != null && setFiles.contains(k)) {
            setFiles(k)
          } else {
            // ディレクトリ
            val fs = f.listFiles
            // シンボリックリンクを追うかどうか
            if ((followSymlink || !isSymbolicLink2) && fs != null) {
              val c1 = fs.map(f => loop(f.getAbsolutePath)).toList.sortBy(_.size).reverse
              val c2 = c1.take(MaxCount)
              // 大きい順にMaxCount個
              val c3 = c1.drop(MaxCount)
              // その他のファイルたち
              val size = c3.foldLeft(0L)((acc, c) => acc + c.size)
              val children = c2 :+ DummyInfo("others…", size)
              val r = DirectoryInfo(f, true, children)
              if (k != null)
                setFiles(k) = r
              r
            } else {
              // シンボリックリンク
              val r = DummyInfo(f.getName, 0)
              if (k != null)
                setFiles(k) = r
              r
            }
          }
        } else if (!p.isOther) {
          // 通常のファイル
          FileInfo(f)
        } else {
          // その他
          DummyInfo(f.getName, 0)
        }
      }
      catch {
        case e: Throwable =>
          println(s"$path: $e")
          DummyInfo(f.getName, 0)
      }
    }
    loop(path).asInstanceOf[DirectoryInfo]
  }

  def truncateName(name: String): String = {
    if (name.length < MaxLength)
      name
    else {
      name.substring(0, MaxLength / 2 - 1) + "…" + name.substring(name.length - MaxLength / 2)
    }
  }

  def convertSize(size: Long): String = {
    val (v, u) = if (size < math.pow(1024, 1))
      (size.asInstanceOf[Double], "B")
    else if (size < math.pow(1024, 2))
      (size / math.pow(1024, 1), "KB")
    else if (size < math.pow(1024, 3))
      (size / math.pow(1024, 2), "MB")
    else if (size < math.pow(1024, 4))
      (size / math.pow(1024, 3), "GB")
    else
      (size / math.pow(1024, 4), "TB")
    BigDecimal(v).setScale(2, RoundingMode.HALF_UP) + " " + u
  }
}
