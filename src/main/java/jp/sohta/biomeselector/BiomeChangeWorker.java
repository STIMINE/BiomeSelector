package jp.sohta.biomeselector;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

/** Applies large biome edits gradually, keeping the server tick responsive. */
public class BiomeChangeWorker {
    private final Queue<ChangeJob> jobs = new ArrayDeque<ChangeJob>();

    public void enqueue(EntityPlayerMP player, int biomeId) {
        BiomeGenBase[] biomes = BiomeGenBase.getBiomeGenArray();
        if (biomeId < 0 || biomeId >= biomes.length || biomes[biomeId] == null) {
            player.addChatMessage(new ChatComponentText("[Biome Selector] 無効なバイオームです。"));
            return;
        }
        SelectionManager.Selection selection = SelectionManager.getComplete(player);
        if (selection == null) {
            player.addChatMessage(new ChatComponentText("[Biome Selector] 先にブロックを2点選択してください。"));
            return;
        }
        long area = (long) selection.getWidth() * selection.getDepth();
        if (area > BiomeSelectorMod.maxAreaBlocks) {
            player.addChatMessage(new ChatComponentText("[Biome Selector] 範囲が大きすぎます（最大 "
                    + BiomeSelectorMod.maxAreaBlocks + " ブロック）。"));
            return;
        }
        ChangeJob job = new ChangeJob(player, selection, biomeId, biomes[biomeId].biomeName, area);
        jobs.add(job);
        player.addChatMessage(new ChatComponentText("[Biome Selector] " + job.getChunkCount()
                + " チャンクを順次変更します（毎ティック最大 " + BiomeSelectorMod.chunksPerTick + " チャンク）。"));
    }

    /** Cancels all waiting or in-progress jobs created by one player. */
    public void cancel(EntityPlayerMP player) {
        UUID playerId = player.getUniqueID();
        int cancelled = 0;
        for (Iterator<ChangeJob> iterator = jobs.iterator(); iterator.hasNext();) {
            if (iterator.next().belongsTo(playerId)) {
                iterator.remove();
                cancelled++;
            }
        }
        if (cancelled == 0) {
            player.addChatMessage(new ChatComponentText("[Biome Selector] 中止できる変更処理はありません。"));
        } else {
            player.addChatMessage(new ChatComponentText("[Biome Selector] 変更処理を中止しました。"));
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        int budget = BiomeSelectorMod.chunksPerTick;
        while (budget > 0 && !jobs.isEmpty()) {
            ChangeJob job = jobs.peek();
            job.applyNextChunk();
            budget--;
            if (job.isFinished()) {
                jobs.remove();
                job.notifyFinished();
            }
        }
    }

    private static class ChangeJob {
        private final EntityPlayerMP source;
        private final WorldServer world;
        private final int minX, maxX, minZ, maxZ, biomeId;
        private final int maxChunkX, maxChunkZ;
        private final String biomeName;
        private final long area;
        private int chunkX, chunkZ;

        ChangeJob(EntityPlayerMP source, SelectionManager.Selection selection, int biomeId,
                String biomeName, long area) {
            this.source = source;
            this.world = (WorldServer) source.worldObj;
            this.minX = selection.minX();
            this.maxX = selection.maxX();
            this.minZ = selection.minZ();
            this.maxZ = selection.maxZ();
            this.biomeId = biomeId;
            this.biomeName = biomeName;
            this.area = area;
            this.chunkX = minX >> 4;
            this.chunkZ = minZ >> 4;
            this.maxChunkX = maxX >> 4;
            this.maxChunkZ = maxZ >> 4;
        }

        int getChunkCount() {
            return (maxChunkX - chunkX + 1) * (maxChunkZ - chunkZ + 1);
        }

        void applyNextChunk() {
            Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
            byte[] values = chunk.getBiomeArray();
            int startX = Math.max(minX, chunkX << 4);
            int endX = Math.min(maxX, (chunkX << 4) + 15);
            int startZ = Math.max(minZ, chunkZ << 4);
            int endZ = Math.min(maxZ, (chunkZ << 4) + 15);
            for (int x = startX; x <= endX; x++) {
                for (int z = startZ; z <= endZ; z++) {
                    values[((z & 15) << 4) | (x & 15)] = (byte) biomeId;
                }
            }
            chunk.setChunkModified();
            syncChunk(chunk);
            if (chunkX < maxChunkX) {
                chunkX++;
            } else {
                chunkX = minX >> 4;
                chunkZ++;
            }
        }

        boolean isFinished() {
            return chunkZ > maxChunkZ;
        }

        boolean belongsTo(UUID playerId) {
            return source.getUniqueID().equals(playerId);
        }

        @SuppressWarnings("unchecked")
        private void syncChunk(Chunk chunk) {
            for (Object entry : world.playerEntities) {
                EntityPlayerMP recipient = (EntityPlayerMP) entry;
                recipient.playerNetServerHandler.sendPacket(new S21PacketChunkData(chunk, true, 0xFFFF));
            }
        }

        void notifyFinished() {
            source.addChatMessage(new ChatComponentText("[Biome Selector] " + biomeName
                    + " への変更が完了しました（" + area + " ブロック）。"));
            SelectionManager.clearClientSelection(source);
        }
    }
}
