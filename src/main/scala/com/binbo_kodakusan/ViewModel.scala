package com.binbo_kodakusan

import scalafx.beans.property.StringProperty

/**
  * Created by tada on 2017/01/04.
  */
class ViewModel {
  var root: DirectoryInfo = null
  var selected: DirectoryInfo = null

  def analyze(path: String, followLink: Boolean, statusText: StringProperty): Unit = {
    root = Model.analyze(path, followLink, statusText)
    selected = root
  }

  def select(id: String): Boolean = {
    if (selected != null) {
      selected.children.find(c => c.id == id) match {
        case Some(c) if c.selectable =>
          selected = c.asInstanceOf[DirectoryInfo]
          true
        case _ =>
          false
      }
    } else
      false
  }

  def moveToParent: Boolean = {
    if (selected != null && selected.parent != null) {
      selected = selected.parent
      true
    } else
      false
  }
}
