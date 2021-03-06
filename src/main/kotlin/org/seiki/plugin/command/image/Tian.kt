package org.seiki.plugin.command.image

import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.sendTo
import org.seiki.plugin.SeikiMain
import org.seiki.plugin.runCatching
import org.seiki.plugin.uploadAsImage

object Tian : SimpleCommand(
    SeikiMain, "tian", "舔",
    description = "舔"
) {
    @Handler
    suspend fun UserCommandSender.handle(user: User) {
        subject.runCatching {
            subject.uploadAsImage("http://ovooa.com/API/tian/?url=${user.avatarUrl}").sendTo(subject)
        }
    }
}