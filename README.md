# ディスク容量可視化ツール(scalafx on JavaFX8)

　scalafxもJavaFX8も全然知らないので練習で作ってみました。

    Scalaが入っている環境で起動
    $ sbt run

    ↓動かない
    Windows用のインストーラ(msi)作成
    $ sbt windows:packageBin
    
    ↓たぶん動かない
    Mac用のインストーラ(DMG)作成
    $ sbt universal:packageOsxDmg

![画像](/resources/diskreport.png)

![動画](/resources/diskreport.gif)
