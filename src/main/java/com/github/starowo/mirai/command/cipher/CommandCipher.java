package com.github.starowo.mirai.command.cipher;

import com.github.starowo.mirai.command.CommandBase;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CommandCipher extends CommandBase {


    public CommandCipher() {
        super("cipher", 2);
    }

    public String getUsage() {
        return "密码相关指令";
    }

    public Message process(User sender, String[] args, MessageChain messages) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                MessageChainBuilder builder = new MessageChainBuilder()
                        .append("/cipher a1z26 <密文>\n")
                        .append("/cipher caesar <密文>\n")
                        .append("/cipher virginia <密钥> <密文>\n")
                        .append("/cipher base64 <密文>\n")
                        .append("/cipher tobase64 <密文>\n")
                        .append("/cipher morse <密文>\n")
                        .append("/cipher flag <密文>\n")
                        .append("/cipher adfgvx <密文>\n")
                        .append("/cipher ascii <密文>\n")
                        .append("/cipher binary <密文>\n")
                        .append("/cipher ternary <密文>\n");
            }
        }
        if (args.length == 2) {
            if (args[0].equals("mw")) {
                BufferedImage[] imgs = MoonlightWindow.generateImage(args[1], 300);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] baImage = null;
                try {
                    ImageIO.write(imgs[0], "png", os);
                    baImage = os.toByteArray();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ExternalResource resource = ExternalResource.create(baImage).toAutoCloseable();
                ByteArrayOutputStream os2 = new ByteArrayOutputStream();
                byte[] baImage2 = null;
                try {
                    ImageIO.write(imgs[1], "png", os2);
                    baImage2 = os2.toByteArray();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ExternalResource resource2 = ExternalResource.create(baImage2).toAutoCloseable();
                return new MessageChainBuilder().append(sender.uploadImage(resource)).append(sender.uploadImage(resource2)).build();
            }
        }
        return null;
    }
}
