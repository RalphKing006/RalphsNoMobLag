package com.zenya.nomoblag.util;

import com.zenya.nomoblag.file.ConfigManager;
import com.zenya.nomoblag.scheduler.TrackTPSTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    private static String translateColor(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        for (Matcher matcher = pattern.matcher(message); matcher.find(); matcher = pattern.matcher(message)) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, net.md_5.bungee.api.ChatColor.of(color) + "");
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    private static String parseMessage(String message) {
        message = translateColor(message);
        message = message.replaceAll("%tps%", Float.toString(TrackTPSTask.getInstance().getAverageTps()));
        message = message.replaceAll("%min_spawner_distance%", String.valueOf(ConfigManager.getInstance().getInt("spawners.minimum-spawner-distance")));
        return message;
    }

    public static void sendMessage(Player player, String message) {
        if (message.isEmpty()) {
            return;
        }

        message = parseMessage(message);
        player.sendMessage(message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (message.isEmpty()) {
            return;
        }

        message = parseMessage(message);
        sender.sendMessage(message);
    }

    public static void sendBroadcast(String message) {
        if (message.isEmpty()) {
            return;
        }

        message = parseMessage(message);

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public static void sendProtectedBroadcast(List<String> permissions, String message) {
        if (message.isEmpty()) {
            return;
        }

        message = parseMessage(message);

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            for (String permission : permissions) {
                if (player.hasPermission(permission)) {
                    player.sendMessage(message);
                }
            }
        }
    }

    public static void sendTitle(Player player, String title) {
        if (title.isEmpty()) {
            return;
        }

        title = parseMessage(title);
        player.resetTitle();
        player.sendTitle(title, null, 10, 40, 20);
    }

    public static void sendSubtitle(Player player, String subtitle) {
        if (subtitle.isEmpty()) {
            return;
        }

        subtitle = parseMessage(subtitle);
        player.resetTitle();
        player.sendTitle(null, subtitle, 0, 10, 0);
    }
}
