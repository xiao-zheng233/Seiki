package org.seiki.plugin

import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.execute
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.info
import org.jetbrains.skia.Paint
import org.seiki.SweetBoy
import org.seiki.SweetBoy.matchRegexOrFail
import org.seiki.SweetBoy.transToTime
import org.seiki.plugin.command.audio.Audio
import org.seiki.plugin.command.audio.Say
import org.seiki.plugin.command.card.BiliApp
import org.seiki.plugin.command.card.BiliLight
import org.seiki.plugin.command.card.bili
import org.seiki.plugin.command.image.*
import org.seiki.plugin.command.image.Moyu.moyu
import org.seiki.plugin.command.plain.*

object SeikiMain : KotlinPlugin(
    JvmPluginDescription(
        id = "org.seiki.main",
        version = "1.0-SNAPSHOT",
        name = "Seiki Main"
    ) {
        author("xiao-zheng233")
//        dependsOn("org.laolittle.plugin.SkikoMirai", ">=1.0.3", true)
    }
) {


    val audioFolder = dataFolder.resolve("audio")

    private const val useTimeTickEvent = true

    private lateinit var jobTimeTick: Job

    @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
    override fun onEnable() {
        logger.info { Paint::class.java.name }
        logger.info { "Seiki Main loaded" }
        val commandList: List<Command> = listOf(
            Ping,
            Gpbt,
            Form,
            Two,
            Dazs,
            Kfc,
            Diana,
            Yiyan,
            Nbnhhsh,
            Baike,
            Bottle,
            Fencing,
            Moyu,
            Yinglish,
            Diu,
            Tian,
            Bishi,
            Pa,
            Zan,
            Love,
            Qian,
            Draw,
            Osu,
            PronHub,
            FiveK,
            BlackWhite,
            Zero,
            Setu,
            Aotu,
            Cosplay,
            Audio,
            Say
        )
        commandList.forEach {
            CommandManager.registerCommand(it)
        }
        val eventChannel = globalEventChannel()
        eventChannel.subscribeAlways<MessageEvent> {
            message.forEach {
                if (it is LightApp) {
                    val gson = Gson()
                    runCatching {
                        val json = gson.fromJson(it.content, BiliLight::class.java)
                        if (json.meta.detail_1.appid == "1109937557") {
                            val url = SweetBoy.get(
                                json.meta.detail_1.qqdocurl.substringBefore('?')
                            ).request.url.toString()
                            val bv = url.matchRegexOrFail(biliUrlRegex)[1]
                            subject.bili(bv).sendTo(subject)
                        }
                    }.onFailure { logger.info { "不是B站小程序" } }
                    runCatching {
                        val json = gson.fromJson(it.content, BiliApp::class.java)
                        if (json.meta.news.appid == 1105517988) {
                            val url = SweetBoy.get(json.meta.news.jumpUrl).request.url.toString()
                            val bv = url.matchRegexOrFail(biliUrlRegex)[1]
                            subject.sendMessage(subject.bili(bv))
                        }
                    }.onFailure { logger.info { "不是B站类XML的JSON卡片" } }
                }
            }
        }
        eventChannel.subscribeAlways<TimeTickEvent> {
            if (this.timestamp.transToTime("HH:mm:ss") == "11:45:13") {
                bot.groups.forEach {
                    it.sendMessage(buildMessageChain {
                        +PlainText("Seiki报时!\n现在是11:45:14")
                        +it.moyu()
                    })
                }
            }
        }
        eventChannel.subscribeMessages {
            biliUrlRegex findingReply {
                val (id) = it.destructured
                subject.bili(id)
            }
            bili23tvRegex findingReply {
                val (url) = it.destructured
                subject.bili(SweetBoy.get(url).request.url.toString().matchRegexOrFail(biliUrlRegex)[1])
            }
            """#0%?(?:\s+(\d+))?""".toRegex() finding {
                val (num) = it.destructured
                if (message.findIsInstance<Image>() == null) Zero.execute(
                    sender.asCommandSender(isTemp = false),
                    getOrWaitImage()!!,PlainText(num)
                )
            }
            """#bw ([\s\S]+)""".toRegex() finding {
                val (text) = it.destructured
                if (message.findIsInstance<Image>() == null) BlackWhite.execute(
                    sender.asCommandSender(isTemp = false),
                    buildMessageChain {
                        +PlainText(text)
                        +getOrWaitImage()!!
                    }
                )
            }
            """#draw""".toRegex() finding {
                if (message.findIsInstance<Image>() == null) Draw.execute(
                    sender.asCommandSender(isTemp = false),
                    getOrWaitImage()!!
                )
            }
            """#5k<([\s\S]*)><([\s\S]*)>""".toRegex() finding {
                val (top, bottom) = it.destructured
                with(FiveK) {
                    sender.asCommandSender(isTemp = false).handle(top, bottom)
                }
            }
            """#osu<([\s\S]+)>""".toRegex() finding {
                val (text) = it.destructured
                with(Osu) {
                    sender.asCommandSender(isTemp = false).handle(text)
                }
            }
            """\s*击剑\s*""".toRegex() finding {
                kotlin.runCatching {
                    Fencing.execute(
                        sender.asCommandSender(isTemp = false),
                        messageChainOf(message.findIsInstance<At>()!!)
                    )
                }
            }
            """#consolas ([\s\S]+)""".toRegex() findingReply { it.groupValues[1].consolas }
        }
        eventChannel.subscribeAlways<BotOnlineEvent> {
            jobTimeTick = if (useTimeTickEvent) launch {
                while (true) {
                    TimeTickEvent(bot, System.currentTimeMillis()).broadcast()
                    delay(999L)
                }
            } else launch {}
        } // bot上线
        eventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            if (invitorId in ownerList) launch {
                delay(1000L)
                accept()
            }
        } // bot拉群
        eventChannel.subscribeAlways<BotJoinGroupEvent> {
            launch {
                delay(100L)
                group.sendMessage("这里是${"Seiki".consolas}.发送\"# help\"来康教程.")
            }
        } // bot进群
        eventChannel.subscribeAlways<NudgeEvent> {
            if (target.id == bot.id && from.id != bot.id) launch {
                delay(100L)
                from.nudge().sendTo(subject)
            }
        } // 戳一戳
        eventChannel.subscribeAlways<MemberMuteEvent> {
            launch {
                delay(100L)
                group.sendMessage(
                    if (operator == null) "已口球${member.nameCardOrNick}(${member.id})"
                    else "${member.nameCardOrNick}(${member.id})被${operatorOrBot.nameCardOrNick}(${operatorOrBot.id})塞了口球"
                )
            }
        } // 禁言
        eventChannel.subscribeAlways<MemberUnmuteEvent> {
            launch {
                delay(100L)
                group.sendMessage(
                    if (operator == null) "已解禁${member.nameCardOrNick}(${member.id})"
                    else "${member.nameCardOrNick}(${member.id})的口球被${operatorOrBot.nameCardOrNick}(${operatorOrBot.id})取了下来"
                )
            }
        } // 解禁
        eventChannel.subscribeAlways<MemberJoinEvent.Active> {
            launch {
                delay(100L)
                group.sendMessage("欢迎${member.nameCardOrNick}(${member.id})的加入")
            }
        } // 进群
        eventChannel.subscribeAlways<MemberJoinEvent.Invite> {
            launch {
                delay(100L)
                group.sendMessage("${invitor.nameCardOrNick}(${invitor.id})邀请${member.nameCardOrNick}(${member.id})加入了本群")
            }
        } // 拉群
        eventChannel.subscribeAlways<MemberLeaveEvent.Quit> {
            launch {
                delay(100L)
                group.sendMessage("${member.nameCardOrNick}(${member.id})逃跑了")
            }
        } // 退群
        eventChannel.subscribeAlways<MemberLeaveEvent.Kick> {
            launch {
                delay(100L)
                group.sendMessage(
                    if (operator == null) "已飞机${member.nameCardOrNick}(${member.id})"
                    else "${operatorOrBot.nameCardOrNick}(${operatorOrBot.id})飞机了${member.nameCardOrNick}(${member.id})"
                )
            }
        } // 踢群
        eventChannel.subscribeAlways<MemberPermissionChangeEvent> {
            launch {
                delay(100L)
                group.sendMessage("${member.nameCardOrNick}(${member.id})从${origin.getName()}便乘了${new.getName()}")
            }
        } // 管理权限改变
    }

    override fun onDisable() {
        jobTimeTick.cancel()
        super.onDisable()
    }
}
