package dev.lopyluna.slag.network;

import dev.lopyluna.slag.network.packets.SelectFluidIndexC2S;
import dev.lopyluna.slag.network.packets.SyncTemperaturesS2C;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

public class AllNetworks {
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);
        registrar = registrar.executesOn(HandlerThread.NETWORK);

        registrar.playToServer(SelectFluidIndexC2S.TYPE, SelectFluidIndexC2S.CODEC, SelectFluidIndexC2S::handle);
        registrar.playToClient(SyncTemperaturesS2C.TYPE, SyncTemperaturesS2C.CODEC, SyncTemperaturesS2C::handle);
    }
}
