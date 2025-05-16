package com.bazaarflip.commands;

import com.bazaarflip.BazaarConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class BazaarFlipCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "bzflip";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bzflip <gui|set|help>";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            showHelp(sender);
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "gui":
                // Toggle GUI visibility
                // TODO: Implement GUI toggle
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "GUI toggled!"));
                break;

            case "set":
                if (args.length < 3) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: /bzflip set <setting> <value>"));
                    return;
                }
                handleSetCommand(sender, args[1], args[2]);
                break;

            case "help":
            default:
                showHelp(sender);
                break;
        }
    }

    private void handleSetCommand(ICommandSender sender, String setting, String value) {
        try {
            switch (setting.toLowerCase()) {
                case "x":
                    BazaarConfig.setGuiX(Integer.parseInt(value));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "GUI X position set to " + value));
                    break;
                case "y":
                    BazaarConfig.setGuiY(Integer.parseInt(value));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "GUI Y position set to " + value));
                    break;
                case "maxitems":
                    BazaarConfig.setMaxDisplayItems(Integer.parseInt(value));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Max display items set to " + value));
                    break;
                case "bgcolor":
                    BazaarConfig.setBackgroundColor(Integer.parseInt(value, 16));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Background color set to " + value));
                    break;
                case "interval":
                    BazaarConfig.setRefreshInterval(Integer.parseInt(value));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Refresh interval set to " + value));
                    break;
                default:
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown setting: " + setting));
            }
            BazaarConfig.saveConfig();
        } catch (NumberFormatException e) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid value: " + value));
        }
    }

    private void showHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "=== BazaarFlip Commands ==="));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "/bzflip gui" + EnumChatFormatting.WHITE + " - Toggle GUI visibility"));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "/bzflip set <setting> <value>" + EnumChatFormatting.WHITE + " - Update settings"));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Available settings: x, y, maxitems, bgcolor, interval"));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "/bzflip help" + EnumChatFormatting.WHITE + " - Show this help message"));
    }
}