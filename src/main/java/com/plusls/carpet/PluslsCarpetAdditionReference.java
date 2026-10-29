/*
 * Copyright (c) Copyright 2020 - 2022 The Cat Town Craft and contributors.
 * This source code is subject to the terms of the GNU Lesser General Public
 * License, version 3. If a copy of the LGPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/lgpl-3.0.txt
 */
package com.plusls.carpet;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.hendrixshen.magiclib.util.FabricUtil;

public class PluslsCarpetAdditionReference {
    private static final String MOD_IDENTIFIER = "pca";
    private static final String MOD_NAME = "Plusls Carpet Addition";
    private static final Logger LOGGER = LogManager.getLogger(MOD_IDENTIFIER);
    public static final boolean tisCarpetLoaded = FabricUtil.isModLoaded("carpet-tis-addition", ">=1.27.0");

    public static String getModIdentifier() {
        return MOD_IDENTIFIER;
    }

    public static String getModName() {
        return MOD_NAME;
    }

    public static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_IDENTIFIER)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("dev");
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_IDENTIFIER, path);
    }
}
