package ru.craftlogic.common.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.event.player.PlayerTeleportHomeEvent;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.PhantomPlayer;
import ru.craftlogic.api.world.Player;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class CommandHome extends CommandBase {
    CommandHome() {
        super("home", 0, "", "<target:Player>");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player sender = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.has("target") ? ctx.get("target").asOfflinePlayer() : sender;
        if (target.isOnline()) {
            Location bedLocation = adjustBedLocation(target.asOnline().getBedLocation(sender.getWorld()));
            teleportHome(ctx, sender, target, bedLocation, false);
        } else {
            PhantomPlayer fake = target.asPhantom(sender.getWorld());
            Location bedLocation = adjustBedLocation(fake.getBedLocation(sender.getWorld()));
            teleportHome(ctx, sender, fake, bedLocation, true);
        }
    }

    @Nullable
    private Location adjustBedLocation(Location l) {
        if (l != null) {
            BlockPos p = l.getPos();
            net.minecraft.world.World world = l.getWorld();
            IBlockState state = l.getBlockState();
            Block block = state.getBlock();
            if (block.isBed(state, world, p, null)) {
                p = block.getBedSpawnPosition(state, world, p, null);
                if (p != null) {
                    return new Location(world, p);
                }
            }
        }
        return null;
    }

    private void teleportHome(CommandContext ctx, Player sender, OfflinePlayer target, Location bedLocation, boolean offline) throws CommandException {
        GameProfile targetProfile = target.getProfile();
        if (bedLocation != null) {
            if (!MinecraftForge.EVENT_BUS.post(new PlayerTeleportHomeEvent(sender, target, bedLocation, ctx, offline))) {
                Consumer<Server> callback = server -> {
                    if (sender.getId().equals(targetProfile.getId())) {
                        ctx.sendMessage(Text.translation("commands.home.teleport.you").green());
                    } else {
                        ctx.sendMessage(Text.translation("commands.home.teleport.other").green().arg(targetProfile.getName(), Text::darkGreen));
                    }
                };
                Text<?, ?> message = sender.getId().equals(targetProfile.getId()) ?
                    Text.translation("tooltip.home_teleport") :
                    Text.translation("tooltip.home_teleport.other");
                sender.teleportDelayed(callback, "home", message, bedLocation, 5, true);
            }
        } else {
            if (sender.getId().equals(targetProfile.getId())) {
                throw new CommandException("commands.home.missing.you");
            } else {
                throw new CommandException("commands.home.missing.other", targetProfile.getName());
            }
        }
    }
}
