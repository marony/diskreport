package com.binbo_kodakusan

import java.io.File

import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.chart.PieChart
import scalafx.scene.control.{Button, CheckBox, Label, ProgressBar}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.stage.{DirectoryChooser, Screen}
import scalafx.beans.property._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.forkjoin.ForkJoinTask

object Application extends JFXApp {
  private var vm = new ViewModel
  private val followLink = BooleanProperty(false)
  private val controllEnable = BooleanProperty(true)
  private val upButtonEnable = BooleanProperty(false)
  private val statusText = StringProperty("")

  private val chart = initialize

  def initialize: PieChart = {
    var c: PieChart = null

    val button1 = new Button {
      // ディレクトリを解析
      text = "解析…"
      disable <== !controllEnable
      onMouseClicked = (e: MouseEvent) => openDir
    }

    val button2 = new Button {
      text = "親ディレクトリ"
      disable <== !controllEnable || !upButtonEnable
      onMouseClicked = (e: MouseEvent) => moveToParent
    }

    val checkBox = new CheckBox {
      text = "シンボリックリンクを追跡する"
      disable <== !controllEnable
      followLink <== selected
    }

    val hbox = new HBox {
      spacing = 12.0
      padding = Insets(6.0)
      children = Seq(
        button1, button2, checkBox
      )
    }

    c = new PieChart {
      val screenSize = Screen.primary.getVisualBounds
      prefWidth = screenSize.getWidth
      prefHeight = screenSize.getHeight
      title = "ディレクトリを選択してください。"
      legendVisible = false
    }

    val label = new Label {
      text <== statusText
    }

    val vbox = new VBox {
      padding = Insets(6.0)
      children = Seq(hbox, c, label)
    }

    AnchorPane.setAnchors(vbox, 12.0, 12.0, 12.0, 12.0)
    val pane = new AnchorPane {
      children = Seq(vbox)
    }

    stage = new JFXApp.PrimaryStage {
      title = "diskreport"
      width = 1024
      height = 768
      scene = new Scene {
        stylesheets += this.getClass.getResource("/css/diskreport.css").toExternalForm
        root = pane
      }
    }
    c
  }

  private def openDir: Unit = {
    val dc = new DirectoryChooser
    dc.title = "ディレクトリを選択してください"
    dc.setInitialDirectory(new File(System.getProperty("user.home")))
    val f = dc.showDialog(stage)
    if (f != null) {
      // ディレクトリを解析
      statusText.value = "START"
      controllEnable.value = false
      Future {
        val vm_ = new ViewModel
        vm_.analyze(f.getAbsolutePath, followLink(), statusText)
        setupData(vm_)
        Platform.runLater {
          vm = vm_
          statusText.value = "END"
          controllEnable.value = true
        }
      }
    }
  }

  private def moveToParent: Unit = {
    if (vm.moveToParent)
      setupData(vm)
  }

  private def setupData(vm_ : ViewModel): Unit = {
    Platform.runLater {
      // チャートのデータ設定
      chart.title = "[" + Model.convertSize(vm_.selected.size) + "] " + vm_.selected.fullName
      upButtonEnable.value = (vm_.selected != null && vm_.selected.parent != null)
      chart.data = vm_.selected.children.map(c => PieChart.Data("[" + Model.convertSize(c.size) + "] " + c.name, c.size))
      // チャートの設定
      chart.data.getValue.zip(vm_.selected.children).foreach { data =>
        val node = data._1.node()
        val info = data._2
        // ID
        node.id = info.id
        // 選択できなければdisable
        node.disable = !info.selectable
        // イベントハンドラ
        node.onMouseClicked = (e: MouseEvent) => {
          if (vm_.select(info.id))
            setupData(vm_)
        }
      }
    }
  }
}
