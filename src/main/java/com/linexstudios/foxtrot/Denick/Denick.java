package com.linexstudios.foxtrot.Denick;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class Denick extends CommandBase {
    private final String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "PIT" + EnumChatFormatting.GRAY + "] ";

    @Override
    public String getCommandName() {
        return "dn"; // main command
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> temp = new ArrayList<>();
        temp.add("denick"); // Alias command
        return temp;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Denicks a player";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            // Starts the logic in a separate thread to prevent game lag
            DenickRunnable newDenick = new DenickRunnable(args[0]);
            Thread newThread = new Thread(newDenick);
            newThread.start();
        } else {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText(prefix + EnumChatFormatting.RED + "Usage: /dn [ign]")
            );
        }
    }
}
