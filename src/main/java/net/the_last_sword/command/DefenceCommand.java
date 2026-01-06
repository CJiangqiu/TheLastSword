package net.the_last_sword.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DefenceCommand {

    //注册防御子命令
    public static LiteralArgumentBuilder<CommandSourceStack> registerSubCommand() {
        return Commands.literal("defence")
            .then(Commands.literal("show_all")
                .executes(DefenceCommand::showAll)
            );
    }

    //显示所有防御记录
    private static int showAll(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        source.sendSuccess(() -> Component.literal("§6========== Defence Records =========="), false);

        //收集所有受保护的实体
        List<LivingEntity> trackedEntities = new ArrayList<>();

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity livingEntity) {
                    //检查是否受保护
                    if (EntityUtil.hasProtection(livingEntity)) {
                        trackedEntities.add(livingEntity);
                    }
                }
            }
        }

        if (trackedEntities.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No defence records found."), false);
            source.sendSuccess(() -> Component.literal("§6======================================"), false);
            return 0;
        }

        //显示每个防御记录
        for (LivingEntity entity : trackedEntities) {
            UUID uuid = entity.getUUID();
            String entityName = entity.getName().getString();
            String dimensionName = entity.level().dimension().location().toString();

            //获取真实生命值
            float realHealth = EntityUtil.getTrueHealth(entity);
            float realMaxHealth = EntityUtil.getTrueMaxHealth(entity);

            final String displayInfo = String.format(
                "  §7Entity: §f%s\n  §7UUID: §8%s\n  §7Dimension: §e%s\n  §7Real Health: §a%.1f§7/§a%.1f",
                entityName,
                uuid.toString(),
                dimensionName,
                realHealth,
                realMaxHealth
            );

            source.sendSuccess(() -> Component.literal(displayInfo), false);
            source.sendSuccess(() -> Component.literal("  §8---"), false);
        }

        final int count = trackedEntities.size();
        source.sendSuccess(() -> Component.literal(
            String.format("§aTotal: %d entities with defence tracking", count)
        ), false);
        source.sendSuccess(() -> Component.literal("§6======================================"), false);

        return count;
    }
}
