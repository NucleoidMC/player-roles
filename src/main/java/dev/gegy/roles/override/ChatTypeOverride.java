package dev.gegy.roles.override;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;

// We'd rather be name consistent with Vanilla registries than Yarn, especially since it's exposed to datapacks.
public record ChatTypeOverride(ResourceKey<ChatType> chatType) {
	public static final Codec<ChatTypeOverride> CODEC = ResourceKey.codec(Registries.CHAT_TYPE).xmap(ChatTypeOverride::new, ChatTypeOverride::chatType);
}
