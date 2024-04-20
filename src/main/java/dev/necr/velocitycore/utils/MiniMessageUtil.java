package dev.necr.velocitycore.utils;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.minimessage.MiniMessage;

@UtilityClass
public class MiniMessageUtil {
    @Getter
    private static final MiniMessage miniMessage = MiniMessage.builder().build();
}
