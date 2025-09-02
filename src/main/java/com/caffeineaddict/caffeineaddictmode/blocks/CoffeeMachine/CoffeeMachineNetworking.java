package com.caffeineaddict.caffeineaddictmode.blocks.CoffeeMachine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class CoffeeMachineNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("caffeineaddictmode", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, BrewRequestPacket.class,
                BrewRequestPacket::encode,
                BrewRequestPacket::decode,
                BrewRequestPacket::handle);
    }

    public static void sendBrewRequest(int idx) {
        CHANNEL.sendToServer(new BrewRequestPacket(idx));
    }
}

