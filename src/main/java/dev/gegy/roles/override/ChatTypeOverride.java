package dev.gegy.roles.override;

import com.mojang.serialization.Codec;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

// We'd rather be name consistent with Vanilla registries than Yarn, especially since it's exposed to datapacks.
public record ChatTypeOverride(RegistryKey<MessageType> chatType) {
	public static final Codec<ChatTypeOverride> CODEC = RegistryKey.createCodec(RegistryKeys.MESSAGE_TYPE).xmap(ChatTypeOverride::new, ChatTypeOverride::chatType);
}
