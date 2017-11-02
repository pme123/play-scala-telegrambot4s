package bots.control

import akka.actor.{Actor, FSM}
import bots.botToken
import bots.entity.LogLevel._
import bots.entity._
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.{GetFile, ParseMode, SendDocument, SendMessage}
import info.mukel.telegrambot4s.models._

import scala.concurrent.Future

trait ChatFeature extends TelegramBot
  with Polling
  with Commands
  with Callbacks
  with Logger {

  def token: String = botToken

  protected def sendMessage(msg: Message, text: String): Future[Message] =
    request(SendMessage(msg.source, text, parseMode = Some(ParseMode.HTML)))

  protected def fileName(fileId: String, path: String): String =
    fileId.substring(10) + path.substring(path.lastIndexOf("/") + 1)

  protected def getFilePath(msg: Message, maxSize: Option[Int] = None): Future[Option[(String, String)]] = {
    val optFileId: Option[String] =
      msg.document.map(_.fileId)
        .orElse(msg.video.map(_.fileId))
        .orElse(extractPhoto(msg, maxSize))

    optFileId match {
      case Some(fileId: String) =>
        request(GetFile(fileId)).map { (file: File) =>
          file.filePath match {
            case Some(path) =>
              Some((file.fileId, fileUrl(path)))
            case _ =>
              sendMessage(msg, s"I could not retrieve the File from the fileId: $fileId")
              None
          }
        }
      case other =>
        sendMessage(msg, "Sorry but you have to add a file to the chat. (Use button <i>send file</i>)\n" +
          s"Not expected: $other / $msg")
        Future(None)
    }
  }

  private def extractPhoto(msg: Message, maxSize: Option[Int]): Option[String] = {
    maxSize match {
      case None => msg.photo.map(_.last.fileId)
      case Some(size) => msg.photo.map(ps =>
        ps.tail.foldLeft[String](ps.head.fileId)((acc, ps: PhotoSize) =>
          if (ps.fileSize.isDefined && ps.fileSize.get <= size) ps.fileId else acc))
    }

  }

  private def fileUrl(filePath: String) =
    s"https://api.telegram.org/file/bot$token/$filePath"

}

trait ChatService
  extends ChatFeature
    with Actor

trait ChatConversation
  extends ChatFeature
    with FSM[FSMState, FSMData] {

  import bots.entity.RunAspect._
  // starts every conversation
  startWith(Idle, NoData)

  // handle async execution,
  // see http://stackoverflow.com/questions/29489564/akka-fsm-goto-within-future
  when(WaitingForExecution) {
    case Event(ExecutionResult(state, data), _) =>
      goto(state) using data
    case Event(Stay, _) =>
      stay()
  }

  whenUnhandled {
    case Event(RestartCommand, _) => goto(Idle)
    case Event(RunAspect(`logStateCommand`, msg), data) =>
      sendMessage(msg, "Logged state:\n" + data)
      stay()
    case Event(RunAspect(other, msg), _) =>
      sendMessage(msg, s"Sorry this Aspect '$other' is not supported by this conversation.")
      stay()
    case event@Event(Command(msg, _), other) =>
      sendMessage(msg, s"Sorry I could not handle your message. You need to start over with a command. - $other")
      notExpectedData(event)
    case event@Event(_, _) =>
      notExpectedData(event)
  }

  def newConversation(): State = goto(Idle)

  val callback = "callback"
  protected val showReport = "Show Report"

  protected def sendMessage(msg: Message, text: String, replyMarkup: Option[ReplyMarkup] = None): Future[Message] =
    request(SendMessage(msg.source, text, parseMode = Some(ParseMode.HTML), replyMarkup = replyMarkup))

  protected def sendDocument(msg: Message, inputFile: InputFile): Future[Message] =
    request(SendDocument(msg.source, inputFile))

  protected def notExpectedData(other: Event): State = {
    log.warning(s"received unhandled request ${other.event} in state $stateName/${other.stateData}")
    stay()
  }

  def reportText(logReport: LogReport): String = logReport.maxLevel() match {
    case ERROR => "There were some Exceptions!\n"
    case WARN => "There were some Warning!\n"
    case _ => "Everything went just fine!\n"
  }

  // start state of any conversation
  case object Idle extends FSMState

  // to handle async execution you can use this state (goto(WaitingForExecution))
  case object WaitingForExecution extends FSMState

  // if there is no data use this (e.g. when Idle)
  case object NoData extends FSMData

  // used for sending the next state and data after an async execution
  case class ExecutionResult(fSMState: FSMState, fSMData: FSMData)

  // used to indicate that there is no state change after an async exection
  case object Stay

}
