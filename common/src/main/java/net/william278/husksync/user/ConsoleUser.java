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

package net.william278.husksync.user;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import org.jetbrains.annotations.NotNull;

public final class ConsoleUser implements CommandUser {

    @NotNull
    private final Audience audience;

    private final java.util.function.Consumer<net.kyori.adventure.text.Component> messages;

    public ConsoleUser(@NotNull AudienceProvider audiences) {
        this(audiences, audiences.console()::sendMessage);
    }

    public ConsoleUser(@NotNull AudienceProvider audiences,
                       java.util.function.Consumer<net.kyori.adventure.text.Component> messages) {
        this.audience = audiences.console();
        this.messages = messages;
    }

    @Override
    public void sendMessage(@NotNull net.kyori.adventure.text.Component component) {
        messages.accept(component);
    }

    @Override
    @NotNull
    public Audience getAudience() {
        return audience;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }


}
