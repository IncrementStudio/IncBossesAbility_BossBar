package ru.incrementstudio.incbosses.abilities.incbossesability_bossbar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.incrementstudio.incapi.utils.ColorUtil;
import ru.incrementstudio.incapi.utils.MathUtil;
import ru.incrementstudio.incbosses.api.AbilityBase;
import ru.incrementstudio.incbosses.api.StartReason;
import ru.incrementstudio.incbosses.api.StopReason;
import ru.incrementstudio.incbosses.api.bosses.Boss;
import ru.incrementstudio.incbosses.api.bosses.phases.Phase;
import ru.incrementstudio.incapi.configs.templates.BarTemplate;

import java.util.List;
import java.util.stream.Collectors;

public class Ability extends AbilityBase {
    private final double radius;
    private final BossBar bossBar;
    private BukkitTask task;

    public Ability(Boss boss, Phase phase, FileConfiguration bossConfig, ConfigurationSection abilityConfig) {
        super(boss, phase, bossConfig, abilityConfig);
        radius = abilityConfig.contains("radius") ? abilityConfig.getDouble("radius") : 10;
        String title = abilityConfig.contains("title") ? abilityConfig.getString("title")
                .replace("%boss-name%", boss.getData().getBossName())
                .replace("%phase-name%", phase.getData().getPhaseName())
                : boss.getData().getBossName();
        BarTemplate barTemplate = new BarTemplate(abilityConfig);
        bossBar = Bukkit.createBossBar(ColorUtil.toColor(title), barTemplate.getColor(), barTemplate.getStyle(), barTemplate.getFlags().toArray(new BarFlag[0]));
    }

    @Override
    public void start(StartReason reason) {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                bossBar.setProgress(MathUtil.inverseLerp(0, boss.getEntity().getMaxHealth(), boss.getEntity().getHealth()));
                List<Player> players = boss.getEntity().getNearbyEntities(radius, radius, radius).stream()
                        .filter(x -> x instanceof Player)
                        .filter(x -> x.getLocation().distance(boss.getEntity().getLocation()) <= radius)
                        .map(x -> (Player) x)
                        .collect(Collectors.toList());
                for (Player player : bossBar.getPlayers()) {
                    if (!players.contains(player))
                        bossBar.removePlayer(player);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 5L);
    }

    @Override
    public void stop(StopReason reason) {
        bossBar.removeAll();
        task.cancel();
    }
}
