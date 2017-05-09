package net.glowstone.entity.ai;

import net.glowstone.entity.GlowLivingEntity;
import net.glowstone.entity.GlowPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.List;

public class FollowPlayerTask extends EntityTask {

    private GlowPlayer target;
    private int delay = 1;
    private static final double RANGE = 10;

    public FollowPlayerTask() {
        super("follow_player");
    }

    @Override
    public boolean isInstant() {
        return false;
    }

    @Override
    public int getDurationMin() {
        return 4 * 20;
    }

    @Override
    public int getDurationMax() {
        return 6 * 20;
    }

    @Override
    public boolean shouldStart(GlowLivingEntity entity) {
        return entity.getState() == HostileMobState.TARGETING;
    }

    @Override
    public void start(GlowLivingEntity entity) {
        target = null;
        List<Entity> nearbyEntities = entity.getNearbyEntities(RANGE, RANGE / 2, RANGE);
        double nearestSquared = Double.MAX_VALUE;
        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity.getType() != EntityType.PLAYER) {
                continue;
            }
            double dist = nearbyEntity.getLocation().distanceSquared(entity.getLocation());
            if (dist < nearestSquared) {
                target = (GlowPlayer) nearbyEntity;
                nearestSquared = dist;
            }
        }
    }

    @Override
    public void end(GlowLivingEntity entity) {
        Location location = entity.getLocation();
        location.setPitch(0);
        location.setYaw(entity.getHeadYaw());
        entity.teleport(location);
        target = null;
    }

    @Override
    public void execute(GlowLivingEntity entity) {
        if (delay == 1) {
            delay = 0;
            return;
        }
        if (target == null || !target.isOnline() || entity.getLocation().distanceSquared(target.getLocation()) > (RANGE * RANGE)) {
            reset(entity);
            return;
        }
        Location other = target.getEyeLocation();
        Location location = entity.getLocation();
        double x = other.getX() - location.getX();
        double z = other.getZ() - location.getZ();
        float yaw = (float) (Math.atan2(z, x) * (180 / Math.PI)) - 90;
        entity.setHeadYaw(yaw); // todo: smooth head rotation (delta)
        // todo: pitch rotation (head up/down)
        delay = 1;
        TransportHelper.moveTowards(entity, target.getLocation());
    }
}