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
import net.the_last_sword.defence.DefenceManager;

import java.util.Set;
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

        int totalCount = 0;

        //获取所有有防御记录的实体UUID
        Set<UUID> allEntityIds = DefenceManager.getAllDefenceEntityIds();

        if (allEntityIds.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No defence records found."), false);
            return 0;
        }

        //遍历所有记录并显示
        for (UUID uuid : allEntityIds) {
            //尝试在所有维度中查找实体
            LivingEntity entity = null;
            String entityName = "Unknown";
            String dimensionName = "Unknown";

            for (ServerLevel level : server.getAllLevels()) {
                Entity foundEntity = level.getEntity(uuid);
                if (foundEntity instanceof LivingEntity) {
                    entity = (LivingEntity) foundEntity;
                    entityName = entity.getName().getString();
                    dimensionName = level.dimension().location().toString();
                    break;
                }
            }

            //使用新的API获取数据
            float health = entity != null ? DefenceManager.getHealth(entity) : 0.0f;
            float maxHealth = entity != null ? DefenceManager.getMaxHealth(entity) : 0.0f;
            int defenceLevel = entity != null ? DefenceManager.getDefenceLevel(entity) : 0;

            final String displayInfo = String.format(
                "  §7UUID: §f%s\n  §7Name: §f%s\n  §7Dimension: §e%s\n  §7Health: §a%.1f§f/§a%.1f\n  §7Defence Level: §b%d",
                uuid.toString(),
                entityName,
                dimensionName,
                health,
                maxHealth,
                defenceLevel
            );

            source.sendSuccess(() -> Component.literal(displayInfo), false);
            source.sendSuccess(() -> Component.literal("  §8---"), false);

            totalCount++;
        }

        final int finalTotalCount = totalCount;
        source.sendSuccess(() -> Component.literal(
            String.format("§aTotal: %d records", finalTotalCount)
        ), false);
        source.sendSuccess(() -> Component.literal("§6======================================"), false);

        return totalCount;
    }
}
