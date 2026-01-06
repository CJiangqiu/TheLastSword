package net.the_last_sword.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AttackCommand {

    //注册攻击子命令
    public static LiteralArgumentBuilder<CommandSourceStack> registerSubCommand() {
        return Commands.literal("attack")
            .then(Commands.literal("show_all")
                .executes(AttackCommand::showAll)
            );
    }

    //显示所有攻击效果记录
    private static int showAll(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        source.sendSuccess(() -> Component.literal("§6========== Attack Effect Records =========="), false);

        //显示禁疗记录
        int healNegationCount = showHealNegationRecords(source, server);

        source.sendSuccess(() -> Component.literal("§8"), false);

        //显示禁复活记录
        int reviveBanCount = showReviveBanRecords(source);

        int totalCount = healNegationCount + reviveBanCount;
        final int finalTotalCount = totalCount;
        source.sendSuccess(() -> Component.literal(
            String.format("§aTotal: %d heal negation + %d revive ban = %d records",
                healNegationCount, reviveBanCount, finalTotalCount)
        ), false);
        source.sendSuccess(() -> Component.literal("§6============================================"), false);

        return totalCount;
    }

    //显示禁疗记录
    private static int showHealNegationRecords(CommandSourceStack source, MinecraftServer server) {
        source.sendSuccess(() -> Component.literal("§e--- Heal Negation Records ---"), false);

        //收集所有有禁疗效果的实体
        List<LivingEntity> healNegatedEntities = new ArrayList<>();

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity living && EntityUtil.isHealBanned(living)) {
                    healNegatedEntities.add(living);
                }
            }
        }

        if (healNegatedEntities.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No heal negation records found."), false);
            return 0;
        }

        //显示每个禁疗记录
        for (LivingEntity entity : healNegatedEntities) {
            UUID uuid = entity.getUUID();
            String entityName = entity.getName().getString();
            String dimensionName = entity.level().dimension().location().toString();

            int remainingTime = EntityUtil.getHealBanTime(entity);

            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            String timeDisplay = String.format("§c%dm %ds", minutes, seconds);

            final String displayInfo = String.format(
                "  §7UUID: §f%s\n  §7Name: §f%s\n  §7Dimension: §e%s\n  §7Remaining Time: %s",
                uuid.toString(),
                entityName,
                dimensionName,
                timeDisplay
            );

            source.sendSuccess(() -> Component.literal(displayInfo), false);
            source.sendSuccess(() -> Component.literal("  §8---"), false);
        }

        return healNegatedEntities.size();
    }

    //显示禁复活记录
    private static int showReviveBanRecords(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§e--- Revive Ban Records ---"), false);

        ServerLevel level = source.getLevel();
        Map<EntityType<?>, Integer> reviveBanTypes = EntityUtil.getAllReviveBans(level);

        if (reviveBanTypes.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No revive ban records found."), false);
            return 0;
        }

        for (var entry : reviveBanTypes.entrySet()) {
            EntityType<?> entityType = entry.getKey();
            int remainingTime = entry.getValue();

            String entityTypeName = entityType.getDescriptionId();

            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            String timeDisplay = String.format("§c%dm %ds", minutes, seconds);

            final String displayInfo = String.format(
                "  §7EntityType: §f%s\n  §7Remaining Time: %s",
                entityTypeName,
                timeDisplay
            );

            source.sendSuccess(() -> Component.literal(displayInfo), false);
            source.sendSuccess(() -> Component.literal("  §8---"), false);
        }

        return reviveBanTypes.size();
    }
}
