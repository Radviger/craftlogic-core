package ru.craftlogic.api.world;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import ru.craftlogic.api.server.AdvancedServer;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.util.WrappedCommandSender;

public interface CommandSender extends Permissible {
    default Server getServer() {
        return Server.from(unwrap().getServer());
    }

    @Override
    default ITextComponent getDisplayName() {
        return unwrap().getDisplayName();
    }

    default int getOperatorLevel() {
        return 0;
    }

    String getName();

    default void sendMessage(ITextComponent message) {
        unwrap().sendMessage(message);
    }

    default void sendMessage(Text<?, ?> text) {
        unwrap().sendMessage(text.build());
    }

    default void sendMessage(String format, Object... args) {
        unwrap().sendMessage(new TextComponentTranslation(format, args));
    }

    ICommandSender unwrap();

    static CommandSender from(Server server, ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {
            return server.getPlayerManager().getOnline(((EntityPlayerMP) sender).getGameProfile());
        } else if (sender instanceof MinecraftServer) {
            return ((AdvancedServer)sender).wrapped();
        } else if (sender instanceof CommandBlockBaseLogic || sender instanceof RConConsoleSource) {
            return new WrappedCommandSender(sender) {
                @Override
                public boolean hasPermission(String permission, int opLevel) {
                    return true;
                }

                @Override
                public int getOperatorLevel() {
                    return 4;
                }
            };
        }
        return new WrappedCommandSender(sender);
    }
}
