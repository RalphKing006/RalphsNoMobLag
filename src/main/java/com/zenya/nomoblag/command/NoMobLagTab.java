package com.zenya.nomoblag.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class NoMobLagTab implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> matches = new ArrayList<>();

        switch(args.length) {
            case 0:
                matches.add("nomoblag");
                return StringUtil.copyPartialMatches(cmd.getName(), matches, new ArrayList<>());
            case 1:
                matches.add("help");
                matches.add("stats");
                matches.add("reload");
                matches.add("freeze");
                matches.add("unfreeze");
                matches.add("setcollisions");
                matches.add("loadspawners");
                return StringUtil.copyPartialMatches(args[0], matches, new ArrayList<>());

            case 2:
                switch(args[0].toLowerCase()) {
                    case "freeze":
                    case "unfreeze":
                        matches.add("chunk");
                        matches.add("world");
                        matches.add("all");
                        return StringUtil.copyPartialMatches(args[1], matches, new ArrayList<>());
                    case "setcollisions":
                        matches.add("true");
                        matches.add("false");
                        return StringUtil.copyPartialMatches(args[1], matches, new ArrayList<>());
                }
        }
        return null;
    }
}

