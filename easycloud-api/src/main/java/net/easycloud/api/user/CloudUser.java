package net.easycloud.api.user;

import dev.httpmarco.evelon.PrimaryKey;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.easycloud.api.CloudDriver;
import net.easycloud.api.network.packet.PermissionUpdatePacket;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public final class CloudUser {
    @PrimaryKey
    private final UUID uniqueId;

    @Getter(AccessLevel.NONE)
    private String permissions;

    public void addPermission(String permission) {
        if(permissions.isEmpty()) {
            permissions = permission.toLowerCase();
            update();
            return;
        }

        if(permissions.contains(", " + permission.toLowerCase()) || (permissions.equals(permission.toLowerCase()) || permissions.startsWith(permission.toLowerCase() + ","))) {
            return;
        }

        permissions = permissions + ", " + permission.toLowerCase();
        update();
    }

    public void removePermission(String permission) {
        permissions = permissions.replace(permission.toLowerCase() + ", ", "").replace(permission.toLowerCase(), "");
        update();
    }

    private void update() {
        CloudDriver.instance().userProvider().getRepository().query().update(this);
        CloudDriver.instance().nettyClient().sendPacket(new PermissionUpdatePacket(uniqueId));
    }

    public List<String> getPermissions() {
        return Arrays.stream(permissions.split(", ")).toList();
    }
}
