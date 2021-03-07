package me.aleksilassila.islands.utils;

import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.Random;

public class Utils {
    public static int getHighestYAt(World world, int x, int z) {
        for (int y = 256; y > 0; y--) {
            if (!world.getBlockAt(x, y, z).isEmpty()) return y;
        }

        return -1;
    }

    @Nullable
    static public OfflinePlayer getOfflinePlayer(String name) {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (name.equalsIgnoreCase(player.getName())) return player;
        }

        return null;
    }

    @Nullable
    public static Biome getTargetBiome(String biome) {
         Biome targetBiome = null;

         for (Biome b : Biome.values()) {
             if (b.name().equalsIgnoreCase(biome)) {
                 targetBiome = b;
             }
         }

         return targetBiome;
    }

    public static int[][] randomStalactitePositions(int size, int density) {
        float spacing = size / (float) (density + 1);
        ArrayList<FastNoiseLite.Vector2> positions = new ArrayList<>();

        FastNoiseLite noise = new FastNoiseLite();
        noise.SetSeed(new Random().nextInt());
        noise.SetFrequency(1);
        noise.SetDomainWarpType(FastNoiseLite.DomainWarpType.BasicGrid);
        noise.SetDomainWarpAmp(spacing);


        for (int i = 0; i < density * density; i++) {
            float x = ((i % density) + 1) * spacing;
            float z = ((i / density) + 1) * spacing;

            FastNoiseLite.Vector2 v = new FastNoiseLite.Vector2(x, z);
            noise.DomainWarp(v);
            positions.add(v);
        }

        int[][] arr = new int[positions.size()][2];
        for (int d = 0; d < positions.size(); d++) {
            int[] pos = new int[] {Math.round(positions.get(d).x), Math.round(positions.get(d).y)};

            arr[d] = pos;
        }

        return arr;
    }
}
