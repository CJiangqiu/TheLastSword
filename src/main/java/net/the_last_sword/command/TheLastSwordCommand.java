package net.the_last_sword.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class TheLastSwordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("the_last_sword")
                .requires(source -> source.hasPermission(2))
                //防御系统子命令
                .then(DefenceCommand.registerSubCommand())
                //攻击系统子命令
                .then(AttackCommand.registerSubCommand())
        );
    }
}
