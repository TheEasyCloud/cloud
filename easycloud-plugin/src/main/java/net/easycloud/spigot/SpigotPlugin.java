package net.easycloud.spigot;

import lombok.Getter;
import net.easycloud.api.CloudDriver;
import net.easycloud.api.network.packet.PermissionUpdatePacket;
import net.easycloud.api.network.packet.ServiceStatePacket;
import net.easycloud.api.service.state.ServiceState;
import net.easycloud.spigot.listener.AsyncPlayerPreLoginListener;
import net.easycloud.spigot.listener.PlayerLoginListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@SuppressWarnings("all")
public final class SpigotPlugin extends JavaPlugin {
    @Getter
    private static SpigotPlugin instance;

    private Map<UUID, PermissionAttachment> permissions;

    @Override
    public void onEnable() {
        instance = this;

        this.permissions = new HashMap<UUID, PermissionAttachment>();
        Bukkit.getConsoleSender().sendMessage("§aSuccessfully §7injected the §b@EasyCloudService");
        Bukkit.getConsoleSender().sendMessage("§bPlugin §7was §asuccessfully §7connected to the §bWrapper§7!");

        getServer().getPluginManager().registerEvents(new AsyncPlayerPreLoginListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(), this);

        CloudDriver.instance().nettyClient().listen(PermissionUpdatePacket.class, (channel, packet) -> {
            System.out.println("UPDATE PLAYER");
            var player = Bukkit.getPlayer(packet.getUniqueId());
            if(!player.isOnline()) {
                System.out.println("UPDATE PLAYER OFFLINE");
                return;
            }
            System.out.println("UPDATE PLAYER UPDATE");
            updatePlayer(player);
        });

        var current = CloudDriver.instance().serviceProvider().getCurrentService().getId();
        CloudDriver.instance().nettyClient().sendPacket(new ServiceStatePacket(current, ServiceState.RUNNING));
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(it -> {
            it.kick(Component.text("§cService is offline§8!"));
        });
        CloudDriver.instance().serviceProvider().stop(CloudDriver.instance().serviceProvider().getCurrentService().getId());

        var current = CloudDriver.instance().serviceProvider().getCurrentService().getId();
        CloudDriver.instance().nettyClient().sendPacket(new ServiceStatePacket(current, ServiceState.STOPPED));
    }

    public void updatePlayer(Player player) {
        getPermissions().get(player.getUniqueId()).getPermissions().forEach((permission, unused) -> {
            getPermissions().get(player.getUniqueId()).unsetPermission(permission);
        });

        if(CloudDriver.instance().userProvider().getUser(player.getUniqueId()).getPermissions().stream().anyMatch(it -> it.equals("*"))) {
            player.setOp(true);
        } else {
            player.setOp(false);
            CloudDriver.instance().userProvider().getUser(player.getUniqueId()).getPermissions().forEach(permission -> getPermissions().get(player.getUniqueId()).setPermission(permission, true));
        }
    }
}
