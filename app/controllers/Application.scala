package controllers


import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import play.api._
import play.api.mvc._


import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.Play.current

import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent._
import scala.concurrent.duration._

import models.GameState

import actors.TickActor
import actors.ReceiveTick
import actors.SocketDisconnect
import actors.SocketConnect

object Application extends Controller {

  val tickActor = Akka.system.actorOf(Props[TickActor])

  def index = WebSocket.async[JsValue] { request =>

    implicit val timeout = Timeout(3 seconds)

      val id = GameState.generateUid

      (tickActor ? SocketConnect(id)).map {
        enumerator =>
          (Iteratee.foreach[JsValue] { event =>
              tickActor ! ReceiveTick(id, event)
          }.map { _ => tickActor ! SocketDisconnect(id) }, enumerator.asInstanceOf[Enumerator[JsValue]])
      }

  }

}
