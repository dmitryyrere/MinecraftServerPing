package ch.jamiete.mcping;

import com.google.gson.Gson;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MinecraftPing {
    private MinecraftPingOptions options;

    public MinecraftPingReply getPing(final MinecraftPingOptions options) throws IOException {
        this.options = options;
        validateOptions();
        String json = getPingRaw(options);
        return new Gson().fromJson(json, MinecraftPingReply.class);
    }

    public String getPingRaw(final MinecraftPingOptions options) throws IOException {
        this.options = options;
        validateOptions();
        String hostname = options.getHostname();
        int port = options.getPort();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, port), options.getTimeout());

            try (DataInputStream in = new DataInputStream(socket.getInputStream());
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                // Handshake packet
                ByteArrayOutputStream handshakeBytes = new ByteArrayOutputStream();
                DataOutputStream handshake = new DataOutputStream(handshakeBytes);
                handshake.writeByte(0x00); // Packet ID for handshake
                writeVarInt(handshake, 5); // Протокол 1.7.10 (версія 5)
                writeVarInt(handshake, hostname.length());
                handshake.writeBytes(hostname);
                handshake.writeShort(port);
                writeVarInt(handshake, 1); // Status state
                writeVarInt(out, handshakeBytes.size());
                out.write(handshakeBytes.toByteArray());

                // Status request packet
                out.writeByte(0x01); // Size of packet (1 byte)
                out.writeByte(0x00); // Packet ID for status request

                // Read response
                readVarInt(in); // Packet length
                int id = readVarInt(in);
                if (id != 0x00) {
                    throw new IOException("Invalid packet ID: " + id);
                }

                return readString(in);
            }
        }
    }

    private void validateOptions() {
        if (options.getHostname() == null || options.getHostname().isEmpty()) {
            throw new IllegalArgumentException("Hostname cannot be null or empty");
        }
        if (options.getPort() <= 0 || options.getPort() > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
    }

    private void writeVarInt(DataOutputStream out, int value) throws IOException {
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
                out.writeByte(value);
                return;
            }
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int length = 0;
        byte currentByte;
        while (true) {
            currentByte = in.readByte();
            value |= (currentByte & 0x7F) << (length * 7);
            length++;
            if ((currentByte & 0x80) != 0x80) {
                break;
            }
            if (length > 5) {
                throw new IOException("VarInt too big");
            }
        }
        return value;
    }

    private String readString(DataInputStream in) throws IOException {
        int length = readVarInt(in);
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, options.getCharset());
    }
}