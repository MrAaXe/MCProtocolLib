package org.spacehq.mc.protocol.packet.ingame.server.world;

import org.spacehq.mc.protocol.data.game.chunk.Chunk;
import org.spacehq.mc.protocol.data.game.chunk.Column;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.io.NetOutput;
import org.spacehq.packetlib.io.stream.StreamNetOutput;
import org.spacehq.packetlib.packet.Packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ServerChunkDataPacket implements Packet {

    private Column column;

    @SuppressWarnings("unused")
    private ServerChunkDataPacket() {
    }

    /**
     * Convenience constructor for creating a packet to unload chunks.
     *
     * @param x X of the chunk column.
     * @param z Z of the chunk column.
     */
    public ServerChunkDataPacket(int x, int z) {
        this(new Column(x, z, new Chunk[16], new byte[256]));
    }

    /**
     * Constructs a ServerChunkDataPacket for updating chunks.
     *
     * @param column Column to send.
     */
    public ServerChunkDataPacket(Column column) {
        this.column = column;
    }

    public Column getColumn() {
        return this.column;
    }

    @Override
    public void read(NetInput in) throws IOException {
        int x = in.readInt();
        int z = in.readInt();
        boolean fullChunk = in.readBoolean();
        int chunkMask = in.readInt();
        byte data[] = in.readBytes(in.readVarInt());

        this.column = NetUtil.readColumn(data, x, z, fullChunk, false, chunkMask);
    }

    @Override
    public void write(NetOutput out) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        NetOutput netOut = new StreamNetOutput(byteOut);
        int mask = NetUtil.writeColumn(netOut, this.column, this.column.hasBiomeData(), this.column.hasSkylight());

        out.writeInt(this.column.getX());
        out.writeInt(this.column.getZ());
        out.writeBoolean(this.column.hasBiomeData());
        out.writeShort(mask);
        out.writeVarInt(byteOut.size());
        out.writeBytes(byteOut.toByteArray(), byteOut.size());
    }

    @Override
    public boolean isPriority() {
        return false;
    }
}
