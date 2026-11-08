/*
 * This file is part of HuskSync, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husksync.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.william278.husksync.HuskSync;
import net.william278.husksync.data.DataSnapshot;
import net.william278.husksync.database.Database;
import net.william278.husksync.user.CommandUser;
import net.william278.husksync.util.LegacyConverter;
import net.william278.husksync.util.StatusLine;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.CommandProvider;
import net.william278.uniform.Permission;
import net.william278.uniform.element.ArgumentElement;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class HuskSyncCommand extends PluginCommand {

    private final Component aboutMenu;

    public HuskSyncCommand(@NotNull HuskSync plugin) {
        super("동기화", List.of(), Permission.Default.TRUE, ExecutionScope.ALL, plugin);
        this.aboutMenu = buildAboutMenu(plugin);
    }

    @NotNull
    private static Component buildAboutMenu(@NotNull HuskSync plugin) {
        return Component.text("Kokonut_Sync " + plugin.getPluginVersion() + " | 에고버스 플러그인 | pingpong")
            .clickEvent(ClickEvent.openUrl("https://wiki.egoverse.kr"));
    }

    @Override
    public void provide(@NotNull BaseCommand<?> command) {
        command.setDefaultExecutor((ctx) -> about(command, ctx));
        command.addSubCommand("정보", (sub) -> sub.setDefaultExecutor((ctx) -> about(command, ctx)));
        command.addSubCommand("상태", needsOp("status"), status());
        command.addSubCommand("추출", needsOp("dump"), dump());
        command.addSubCommand("다시불러오기", needsOp("reload"), reload());
        command.addSubCommand("구형자료변환", forceUpgrade());
    }

    private void about(@NotNull BaseCommand<?> c, @NotNull CommandContext<?> ctx) {
        user(c, ctx).sendMessage(aboutMenu);
    }

    @NotNull
    private CommandProvider status() {
        return (sub) -> sub.setDefaultExecutor((ctx) -> {
            final CommandUser user = user(sub, ctx);
            plugin.getLocales().getLocale("system_status_header").ifPresent(user::sendMessage);
            user.sendMessage(Component.join(
                    JoinConfiguration.newlines(),
                    Arrays.stream(StatusLine.values()).map(s -> s.get(plugin)).toList()
            ));
        });
    }

    @NotNull
    private CommandProvider dump() {
        return (sub) -> {
            sub.setDefaultExecutor((ctx) -> {
                final CommandUser user = user(sub, ctx);
                plugin.getLocales().getLocale("system_dump_confirm").ifPresent(user::sendMessage);
            });
            sub.addSubCommand("확인", (con) -> con.setDefaultExecutor((ctx) -> {
                final CommandUser user = user(sub, ctx);
                plugin.getLocales().getLocale("system_dump_started").ifPresent(user::sendMessage);
                plugin.runAsync(() -> {
                    final String url = plugin.createDump(user);
                    plugin.getLocales().getLocale("system_dump_ready").ifPresent(user::sendMessage);
                    user.sendMessage(Component.text(url).clickEvent(ClickEvent.openUrl(url))
                            .decorate(TextDecoration.UNDERLINED).color(NamedTextColor.GRAY));
                });
            }));
        };
    }

    @NotNull
    private CommandProvider reload() {
        return (sub) -> sub.setDefaultExecutor((ctx) -> {
            final CommandUser user = user(sub, ctx);
            try {
                plugin.loadSettings();
                plugin.loadLocales();
                plugin.loadServer();
                plugin.getLocales().getLocale("reload_complete").ifPresent(user::sendMessage);
            } catch (Throwable e) {
                user.sendMessage(plugin.getLocales().format(
                        "<red>내장 설정을 다시 불러오지 못했습니다. 콘솔을 확인해 주세요.</red>"
                ));
                plugin.log(Level.SEVERE, "내장 설정 다시 불러오기 실패", e);
            }
        });
    }





    @NotNull
    private CommandProvider forceUpgrade() {
        return (sub) -> {
            sub.setCondition((ctx) -> sub.getUser(ctx).isConsole());
            sub.setDefaultExecutor((ctx) -> {
                final LegacyConverter converter = plugin.getLegacyConverter().orElse(null);
                if (converter == null) {
                    return;
                }

                plugin.runAsync(() -> {
                    final Database database = plugin.getDatabase();
                    plugin.log(Level.INFO, "모든 플레이어의 구형 데이터 변환을 시작합니다.");
                    database.getAllUsers().forEach(user -> database.getLatestSnapshot(user).ifPresent(snapshot -> {
                        final DataSnapshot.Packed upgraded = converter.convert(
                                snapshot.asBytes(plugin),
                                UUID.randomUUID(),
                                OffsetDateTime.now()
                        );
                        upgraded.setSaveCause(DataSnapshot.SaveCause.CONVERTED_FROM_V2);
                        plugin.getDatabase().addSnapshot(user, upgraded);
                        plugin.getRedisManager().clearUserData(user);
                    }));
                    plugin.log(Level.INFO, "구형 데이터 변환을 완료했습니다.");
                });
            });
        };
    }



}
