package com.samosbor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.samosbor.dimension.ModDimensions;
import com.samosbor.event.SamosborManager;
import com.samosbor.event.SamosborPhase;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class SamosborCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("samosbor")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("status")
                        .executes(ctx -> {
                            SamosborPhase phase = SamosborManager.getCurrentPhase();
                            ctx.getSource().sendFeedback(() -> Text.literal("§7[Самосбор]: Текущая фаза: §e" + phase.name()), false);
                            return 1;
                        }))
                .then(CommandManager.literal("trigger")
                        .then(CommandManager.argument("phase", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("warning");
                                    builder.suggest("escalation");
                                    builder.suggest("peak");
                                    builder.suggest("recession");
                                    builder.suggest("calm");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String phaseStr = StringArgumentType.getString(ctx, "phase").toLowerCase();
                                    switch (phaseStr) {
                                        case "warning" -> SamosborManager.setPhase(SamosborPhase.WARNING, SamosborManager.DURATION_WARNING);
                                        case "escalation" -> SamosborManager.setPhase(SamosborPhase.ESCALATION, SamosborManager.DURATION_ESCALATION);
                                        case "peak" -> SamosborManager.setPhase(SamosborPhase.PEAK, SamosborManager.DURATION_PEAK);
                                        case "recession" -> SamosborManager.setPhase(SamosborPhase.RECESSION, SamosborManager.DURATION_RECESSION);
                                        default -> SamosborManager.setPhase(SamosborPhase.CALM, SamosborManager.DURATION_CALM);
                                    }
                                    ctx.getSource().sendFeedback(() -> Text.literal("§a[Самосбор]: Фаза переключена на " + phaseStr.toUpperCase()), true);
                                    return 1;
                                })))
                .then(CommandManager.literal("stop")
                        .executes(ctx -> {
                            SamosborManager.setPhase(SamosborPhase.CALM, SamosborManager.DURATION_CALM);
                            ctx.getSource().sendFeedback(() -> Text.literal("§a[Самосбор]: Аномалия принудительно остановлена."), true);
                            return 1;
                        }))
                .then(CommandManager.literal("travel")
                        .executes(ctx -> {
                            ServerCommandSource source = ctx.getSource();
                            ServerPlayerEntity player = source.getPlayer();
                            if (player == null) {
                                source.sendError(Text.literal("Команду может вызывать только игрок."));
                                return 0;
                            }

                            ServerWorld currentWorld = player.getServerWorld();
                            boolean isGigakhrushch = currentWorld.getRegistryKey().getValue().equals(ModDimensions.GIGAKHRUSHCHYOVKA_KEY.getValue());

                            if (isGigakhrushch) {
                                ServerWorld overworld = source.getServer().getWorld(World.OVERWORLD);
                                if (overworld != null) {
                                    BlockPos topPos = overworld.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, player.getBlockPos());
                                    player.teleport(overworld, topPos.getX() + 0.5, topPos.getY() + 1.0, topPos.getZ() + 0.5, player.getYaw(), player.getPitch());
                                    source.sendFeedback(() -> Text.literal("§aВы вернулись на поверхность в Обычный мир."), false);
                                }
                            } else {
                                ServerWorld gigaWorld = source.getServer().getWorld(ModDimensions.GIGAKHRUSHCHYOVKA_KEY);
                                if (gigaWorld != null) {
                                    player.teleport(gigaWorld, player.getX(), 67.0, player.getZ(), player.getYaw(), player.getPitch());
                                    source.sendFeedback(() -> Text.literal("§eДобро пожаловать в Гигахрущёвку. Проверьте гермодвери."), false);
                                }
                            }
                            return 1;
                        }))
        );

        // Also alias /gigakhrushch
        dispatcher.register(CommandManager.literal("gigakhrushch")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player != null) {
                        ServerWorld gigaWorld = ctx.getSource().getServer().getWorld(ModDimensions.GIGAKHRUSHCHYOVKA_KEY);
                        if (gigaWorld != null) {
                            player.teleport(gigaWorld, player.getX(), 67.0, player.getZ(), player.getYaw(), player.getPitch());
                            ctx.getSource().sendFeedback(() -> Text.literal("§eПеремещение в Гигахрущёвку выполнено."), false);
                            return 1;
                        }
                    }
                    return 0;
                }));
    }
}
