package com.sosnoski.concur.article6scala

import scala.concurrent.duration._
import scala.util.Random
import akka.actor._
import akka.event.LoggingReceive
import akka.util._

object Stars extends App {

  import Star._

  val starBaseLifetime = 6000 millis
  val starVariableLifetime = 2000 millis
  val starBaseSpawntime = 2000 millis
  val starVariableSpawntime = 1000 millis

  object Namer {
    case object GetName
    def props(names: Array[String]): Props = Props(new Namer(names))
  }

  class Namer(names: Array[String]) extends Actor {
    import context.dispatcher
    import Namer._
    var nextNameIndex = 0
    val nameIndexLimit = names.length * (names.length + 1)

    context.setReceiveTimeout(starBaseSpawntime + starVariableSpawntime)

    def receive = {
      case GetName => {
        val name =
          if (nextNameIndex < names.length) names(nextNameIndex)
          else {
            val first = nextNameIndex / names.length - 1
            val second = nextNameIndex % names.length
            names(first) + "-" + names(second)
          }
        sender ! Star.SetName(name)
        nextNameIndex = (nextNameIndex + 1) % nameIndexLimit
      }
      case ReceiveTimeout => system shutdown
    }

    override def postStop = println("Namer over and out")
  }

  object Star {
    case class SetName(name: String)
    case class Greet(peer: ActorRef)
    case object AskName
    case class TellName(name: String)
    case object Spawn
    case object IntroduceMe
    case object Die

    def props(greeting: String, gennum: Int, parent: String) = Props(new Star(greeting, gennum, parent))
  }

  class Star(greeting: String, gennum: Int, parent: String) extends Actor {
    import context.dispatcher

    var myName: String = ""
    var starsKnown = Map[String, ActorRef]()
    val random = Random
    val namer = context actorSelection namerPath
    namer ! Namer.GetName

    def scaledDuration(base: FiniteDuration, variable: FiniteDuration) =
      base + variable * random.nextInt(1000) / 1000

    val killtime = scaledDuration(starBaseLifetime, starVariableLifetime)
    val killer = scheduler.scheduleOnce(killtime, self, Die)
    val spawntime = scaledDuration(starBaseSpawntime, starVariableSpawntime)
    val spawner = scheduler.schedule(spawntime, 1 second, self, Spawn)
    if (gennum > 1) scheduler.scheduleOnce(1 second, context.parent, IntroduceMe)

    def receive = LoggingReceive {
      case SetName(name) => {
        myName = name
        println(s"$name is the ${gennum}th generation child of $parent")
        context become named
      }
    }

    def named: Receive = {
      case Greet(peer) => peer ! AskName
      case AskName => sender ! TellName(myName)
      case TellName(name) => {
        println(s"$myName says: '$greeting, $name'")
        starsKnown += name -> sender
      }
      case Spawn => {
        println(s"$myName says: A star is born!")
        context.actorOf(props(greeting, gennum + 1, myName))
      }
      case IntroduceMe => starsKnown.foreach {
        case (name, ref) =>
          ref ! Greet(sender)
      }
      case Die => {
        println(s"$myName says: 'I’d like to thank the Academy...'")
        context stop self
      }
    }

    override def postStop = {
      spawner.cancel
      killer.cancel
      println(s"$myName says: 'Urk'")
    }
  }

  val namerPath = "/user/namer"
  val system = ActorSystem("actor-demo-scala")
  val scheduler = system.scheduler
  system.actorOf(Namer.props(Array("Bob", "Alice", "Rock", "Paper", "Scissors", "North", "South", "East",
    "West", "Up", "Down")), "namer")
  val star1 = system.actorOf(props("Howya doing", 1, "Nobody"))
  val star2 = system.actorOf(props("Happy to meet you", 1, "Nobody"))
  Thread sleep 500
  star1 ! Greet(star2)
  star2 ! Greet(star1)
}