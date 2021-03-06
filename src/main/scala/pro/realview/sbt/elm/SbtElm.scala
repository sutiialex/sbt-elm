package pro.realview.sbt.elm

import sbt._
import sbt.Keys._
import com.typesafe.sbt.web._
import com.typesafe.sbt.jse.SbtJsTask
import spray.json._

object Import {

  object ElmKeys {
    val elm = TaskKey[Seq[File]]("elm", "Invoke the elm compiler.")
  }

}

object SbtElm extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport.ElmKeys._

  val elmUnscopedSettings = Seq(
    includeFilter :=
      new SimpleFileFilter(file =>
          ((file.getParent contains "/assets/elm") &&
          !(file.getParent contains "/elm-stuff")))
      && GlobFilter("Main.elm")
  )

  override def projectSettings = inTask(elm)(
    SbtJsTask.jsTaskSpecificUnscopedSettings ++
      inConfig(Assets)(elmUnscopedSettings) ++
      inConfig(TestAssets)(elmUnscopedSettings) ++
      Seq(
        moduleName := "elm",
        shellFile := getClass.getClassLoader.getResource("elm-shell.js"),

        taskMessage in Assets := "Elm compiling",
        taskMessage in TestAssets := "Elm test compiling"
      )
  ) ++ SbtJsTask.addJsSourceFileTasks(elm) ++ Seq(
    elm in Assets := (elm in Assets).dependsOn(webModules in Assets).value,
    elm in TestAssets := (elm in TestAssets).dependsOn(webModules in TestAssets).value
  )

}
