package com.woodcutter.mod;

import com.mojang.brigadier.CommandDispatcher;
import com.woodcutter.mod.entity.WoodcutterBotEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WoodcutterCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        // /spawnbot - spawns a bot near the player
        dispatcher.register(
            Commands.literal("spawnbot")
                .requires(source -> source.hasPermission(0))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerLevel level = source.getLevel();
                    Vec3 pos = source.getPosition();

                    WoodcutterBotEntity bot = WoodcutterMod.WOODCUTTER_BOT.get().create(level);

                    if (bot != null) {
                        bot.moveTo(pos.x + 1, pos.y, pos.z + 1, 0, 0);
                        bot.finalizeSpawn(
                            level,
                            level.getCurrentDifficultyAt(bot.blockPosition()),
                            MobSpawnType.COMMAND,
                            null,
                            null
                        );
                        level.addFreshEntity(bot);
                        source.sendSuccess(() ->
                            Component.literal("§a✔ Woodcutter Bot spawned! It will chop wood and grass nearby."),
                            false
                        );
                    }
                    return 1;
                })
        );

        // /removebots - removes all nearby bots (op only)
        dispatcher.register(
            Commands.literal("removebots")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerLevel level = source.getLevel();

                    List<WoodcutterBotEntity> bots = level.getEntitiesOfClass(
                        WoodcutterBotEntity.class,
                        AABB.ofSize(source.getPosition(), 200, 200, 200)
                    );

                    bots.forEach(net.minecraft.world.entity.Entity::discard);
                    int count = bots.size();

                    source.sendSuccess(() ->
                        Component.literal("§c✖ Removed " + count + " Woodcutter Bot(s)."),
                        false
                    );
                    return count;
                })
        );
    }
}
