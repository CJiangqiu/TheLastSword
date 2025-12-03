package net.the_last_sword.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.the_last_sword.attack.AttackManager;

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

    //显示禁疗记录（使用新的API测试）
    private static int showHealNegationRecords(CommandSourceStack source, MinecraftServer server) {
        source.sendSuccess(() -> Component.literal("§e--- Heal Negation Records ---"), false);

        //收集所有有禁疗效果的实体
        List<Entity> healNegatedEntities = new ArrayList<>();

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                //使用新的API检查
                if (AttackManager.isHealNegated(entity)) {
                    healNegatedEntities.add(entity);
                }
            }
        }

        if (healNegatedEntities.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No heal negation records found."), false);
            return 0;
        }

        //显示每个禁疗记录
        for (Entity entity : healNegatedEntities) {
            UUID uuid = entity.getUUID();
            String entityName = entity.getName().getString();
            String dimensionName = entity.level().dimension().location().toString();

            //使用新的API获取数据
            float lockedHealth = AttackManager.getLockedHealth(entity);
            int remainingTime = AttackManager.getRemainingTime(entity);

            String timeDisplay;
            if (remainingTime == -1) {
                timeDisplay = "§dPermanent";
            } else {
                int minutes = remainingTime / 60;
                int seconds = remainingTime % 60;
                timeDisplay = String.format("§c%dm %ds", minutes, seconds);
            }

            final String displayInfo = String.format(
                "  §7UUID: §f%s\n  §7Name: §f%s\n  §7Dimension: §e%s\n  §7Locked Health: §c%.1f\n  §7Remaining Time: %s",
                uuid.toString(),
                entityName,
                dimensionName,
                lockedHealth,
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

        Map<Class<?>, Integer> reviveBanTypes = AttackManager.getAllReviveBanTypes();

        if (reviveBanTypes.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No revive ban records found."), false);
            return 0;
        }

        for (var entry : reviveBanTypes.entrySet()) {
            Class<?> entityClass = entry.getKey();
            int remainingTime = entry.getValue();

            String simpleClassName = entityClass.getSimpleName();
            String fullClassName = entityClass.getName();

            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            String timeDisplay = String.format("§c%dm %ds", minutes, seconds);

            final String displayInfo = String.format(
                "  §7Class: §f%s\n  §7Full Name: §8%s\n  §7Remaining Time: %s",
                simpleClassName,
                fullClassName,
                timeDisplay
            );

            source.sendSuccess(() -> Component.literal(displayInfo), false);
            source.sendSuccess(() -> Component.literal("  §8---"), false);
        }

        return reviveBanTypes.size();
    }
}
