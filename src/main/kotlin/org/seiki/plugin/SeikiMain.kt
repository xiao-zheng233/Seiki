package org.seiki.plugin

import cn.hutool.cron.CronUtil
import cn.hutool.cron.task.Task
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.info
import okhttp3.internal.wait
import org.seiki.SweetBoy
import org.seiki.SweetBoy.matchRegex
import org.seiki.SweetBoy.matchRegexOrFail
import org.seiki.plugin.command.audio.Audio
import org.seiki.plugin.command.audio.Say
import org.seiki.plugin.command.card.*
import org.seiki.plugin.command.file.File
import org.seiki.plugin.command.image.*
import org.seiki.plugin.command.plain.*


object SeikiMain : KotlinPlugin(
    JvmPluginDescription(
        id = "org.seiki.main",
        version = "1.0-SNAPSHOT",
        name = "Seiki Main"
    ) {
        author("xiao-zheng233")
        dependsOn(
            "org.laolittle.plugin.SkikoMirai",
            versionRequirement = ">=1.0.3",
            isOptional = true
        )
        dependsOn(
            "com.huaban.analysis.jieba.JiebaSegmenter",
            isOptional = true
        )
    }
) {
    val audioFolder = dataFolder.resolve("audio")

    @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
    override fun onEnable() {
        logger.info { "Seiki Main Loaded!" }
        if (!System.getProperties().getProperty("os.name").startsWith("Windows")) {
            System.setProperty("java.awt.headless", "true")
        }
        val commandList: List<Command> = listOf(
            Ping,
            Gpbt,
            Form,
            Two,
            Unvcode,
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
            PatPat,
            PronHub,
            FiveK,
            Flash,
            BlackWhite,
            Zero,
            Setu,
            Aotu,
            Cosplay,
            Audio,
            Say,
            BuildForward,
            File
        )
        commandList.forEach {
            CommandManager.registerCommand(it)
        }
        val eventChannel = globalEventChannel()
        eventChannel.subscribeAlways<MessageEvent> {
            message.forEach {
                if (it is LightApp) {
                    logger.info { it.toString() }
                    val gson = Gson()
                    // 1105517988 ????????????HD ???XML???JSON?????? MiraiApp
                    // 100951776 ???????????? ???XML???JSON?????? MiraiApp
                    // 1109937557 ????????????????????? JSON?????? MiraiLightApp
                    runCatching {
                        val json = gson.fromJson(it.content, BiliLight::class.java)
                        if (json.meta.detail_1.appid == "1109937557") {
                            val url = SweetBoy.get(json.meta.detail_1.qqdocurl).use { r -> r.request.url.toString() }
                            val bv = url.matchRegexOrFail(biliVideoRegex)[1]
                            subject.biliVideo(bv)?.sendTo(subject)
                        } else logger.info { "BiliLight????????????" }
                    }.onFailure { logger.info { "??????B????????????" } }
                    runCatching {
                        val json = gson.fromJson(it.content, BiliApp::class.java)
                        if (json.meta.news.appid == 1105517988 || json.meta.news.appid == 100951776) {
                            val url = SweetBoy.get(json.meta.news.jumpUrl).use { r -> r.request.url.toString() }
                            val bv = url.matchRegexOrFail(biliVideoRegex)[1]
                            subject.biliVideo(bv)?.sendTo(subject)
                        } else logger.info { "BiliApp AppID???1105517988 ??? 100951776" }
                    }.onFailure { logger.info { "??????B??????XML???JSON??????" } }
                }
            }
        }
        eventChannel.subscribeMessages {
            biliVideoRegex findingReply {
                logger.info { it.groupValues[1] }
                subject.biliVideo(it.groupValues[1])
            }
            biliUserRegex findingReply { subject.biliUser(it.groupValues[1].toLong()) }
            bili23tvRegex finding {
                val url = SweetBoy.get(it.groupValues[1]).use { r -> r.request.url }.toString()
                when {
                    biliVideoRegex.matches(url) -> subject.biliVideo(url.matchRegex(biliVideoRegex)!![1])
                    biliUserRegex.matches(url) -> subject.biliUser(url.matchRegex(biliUserRegex)!![1].toLong())
                    else -> null
                }?.sendTo(subject)
            }

            """^#?(help|??????|??????)$""".toRegex() findingReply { "http://seiki.fun/wiki/seiki-bot/#%E4%BD%BF%E7%94%A8" }
            """^#throw$""".toRegex() finding {
                subject.runCatching { throw Throwable("www") }
            }
            """^#exception$""".toRegex() finding {
                subject.runCatching { throw Exception("www") }
            }
            """^#error$""".toRegex() finding {
                subject.runCatching { throw Error("www") }
            }
            """^#?????? ([\s\S]+)$""".toRegex() findingReply {
                subject.runCatching { Image(it.groupValues[1]) }.getOrNull()
            }
            """^#??????$""".toRegex() findingReply {
                subject.runCatching { getOrWaitImage()?.imageId }.getOrNull()
            }
            """^#mirai??? ([\s\S]+)$""".toRegex() findingReply {
                subject.runCatching { it.groupValues[1].deserializeMiraiCode() }.getOrNull()
            }
            """^#get ([\s\S]+)$""".toRegex() findingReply {
                subject.runCatching {
                    SweetBoy.get(it.groupValues[1]).use { r -> r.body!!.string() }
                }.getOrNull()
            }
            """^#post ([\s\S]+)$""".toRegex() findingReply {
                subject.runCatching {
                    SweetBoy.post(it.groupValues[1]).use { r -> r.body!!.string() }
                }.getOrNull()
            }
            """^????????????$""".toRegex() finding {
                subject.uploadAsAudio(java.io.File("$audioFolder/vocaloid1.mp3")).sendTo(subject)
                Image("{209274B1-3E09-6D32-C0EF-7FC70A6D06C6}.gif").sendTo(subject)
            }
            """^????????????$""".toRegex() finding {
                subject.uploadAsAudio(java.io.File("$audioFolder/vocaloid2.mp3")).sendTo(subject)
                Image("{1AF4FC8F-299A-F22F-A348-83F29D190117}.gif").sendTo(subject)
            }
            "error" reply "error?????????"
        }
        eventChannel.subscribeAlways<BotOnlineEvent> {
            CronUtil.schedule("14 45 11,23 * * ?", Task {
                logger.info("666")
                this@SeikiMain.launch {
                    bot.groups.forEach {
                        it.sendMessage("Seiki??????!\n?????????11:45:14")
                    }
                }
            })
            CronUtil.setMatchSecond(true)
            CronUtil.start()
        } // bot??????
        eventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            if (invitorId in ownerList) launch {
                delay(1000L)
                accept()
            }
        } // bot??????
        eventChannel.subscribeAlways<BotJoinGroupEvent> {
            launch {
                delay(100L)
                group.sendMessage("?????????Seiki Bot.????????????:\nhttp://seiki.fun/wiki/seiki-bot/#%E4%BD%BF%E7%94%A8")
            }
        } // bot??????
        eventChannel.subscribeAlways<NudgeEvent> {
            if (target.id == bot.id && from.id != bot.id) launch {
                delay(100L)
                from.nudge().sendTo(subject)
            }
        } // ?????????
        eventChannel.subscribeAlways<MemberMuteEvent> {
            launch {
                delay(100L)
                group.sendMessage(
                    if (operator == null) "?????????${member.name}"
                    else "${member.name}???${operatorOrBot.name}????????????"
                )
            }
        } // ??????
        eventChannel.subscribeAlways<MemberUnmuteEvent> {
            launch {
                delay(100L)
                group.sendMessage(
                    if (operator == null) "?????????${member.name}"
                    else "${member.name}????????????${operatorOrBot.name}????????????"
                )
            }
        } // ??????
        eventChannel.subscribeAlways<MemberJoinEvent.Active> {
            launch {
                delay(100L)
                group.sendMessage("??????${member.name}?????????")
            }
        } // ??????
        eventChannel.subscribeAlways<MemberJoinEvent.Invite> {
            launch {
                delay(100L)
                group.sendMessage("${invitor.name}??????${member.name}???????????????")
            }
        } // ??????
        eventChannel.subscribeAlways<MemberLeaveEvent.Quit> {
            launch {
                delay(100L)
                group.sendMessage("${member.name}?????????")
            }
        } // ??????
        eventChannel.subscribeAlways<MemberLeaveEvent.Kick> {
            launch {
                delay(100L)
                group.sendMessage(
                    if (operator == null) "?????????${member.name}"
                    else "${operatorOrBot.name}?????????${member.name}"
                )
            }
        } // ??????
        eventChannel.subscribeAlways<MemberPermissionChangeEvent> {
            launch {
                delay(100L)
                group.sendMessage("${member.name}???${origin.levelName}?????????${new.levelName}")
            }
        } // ??????????????????
    }

    override fun onDisable() {
        CronUtil.stop()
        super.onDisable()
    }
}
