package com.linexstudios.foxtrot.Util.discord;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class DiscordIPC {

    private static final Gson GSON = new Gson();
    private final String clientId;
    private RandomAccessFile pipe;
    private boolean connected = false;
    private Thread readThread;

    public DiscordIPC(String clientId) {
        this.clientId = clientId;
    }

    public void connect() throws IOException {
        if (connected) return;

        for (int i = 0; i < 10; i++) {
            try {
                File pipeFile = new File("\\\\.\\pipe\\discord-ipc-" + i);
                this.pipe = new RandomAccessFile(pipeFile, "rw");
                break;
            } catch (Exception ignored) {}
        }

        if (this.pipe == null) {
            throw new IOException("Discord IPC Pipe not found. Is Discord running?");
        }

        // 1. Send Handshake
        JsonObject handshake = new JsonObject();
        handshake.addProperty("v", 1);
        handshake.addProperty("client_id", clientId);
        send(0, handshake.toString());

        connected = true;

        // 2. Start a simple read thread to keep the pipe clear and handle potential errors
        readThread = new Thread(() -> {
            try {
                while (connected) {
                    // Read header (8 bytes)
                    byte[] header = new byte[8];
                    pipe.readFully(header);
                    ByteBuffer buf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
                    int op = buf.getInt();
                    int len = buf.getInt();

                    // Read payload
                    byte[] payload = new byte[len];
                    pipe.readFully(payload);
                    
                    // Log response for debugging (optional)
                    // System.out.println("Discord IPC Received Op " + op + ": " + new String(payload));
                    
                    if (op == 2) { // Close
                        connected = false;
                        break;
                    }
                }
            } catch (IOException e) {
                connected = false;
            }
        }, "Foxtrot-Discord-IPC-Reader");
        readThread.setDaemon(true);
        readThread.start();
    }

    public void setPresence(String details, String state, long startTime, String largeImageKey, String largeImageText) {
        if (!connected) {
            try {
                connect();
            } catch (IOException e) {
                return;
            }
        }

        JsonObject activity = new JsonObject();
        activity.addProperty("details", details);
        activity.addProperty("state", state);
        
        if (startTime > 0) {
            JsonObject timestamps = new JsonObject();
            timestamps.addProperty("start", startTime);
            activity.add("timestamps", timestamps);
        }

        if (largeImageKey != null) {
            JsonObject assets = new JsonObject();
            assets.addProperty("large_image", largeImageKey);
            if (largeImageText != null) assets.addProperty("large_text", largeImageText);
            activity.add("assets", assets);
        }

        JsonObject args = new JsonObject();
        args.addProperty("pid", getPid());
        args.add("activity", activity);

        JsonObject payload = new JsonObject();
        payload.addProperty("cmd", "SET_ACTIVITY");
        payload.add("args", args);
        payload.addProperty("nonce", UUID.randomUUID().toString());

        try {
            send(1, payload.toString());
        } catch (IOException e) {
            connected = false;
            close();
        }
    }

    private synchronized void send(int op, String json) throws IOException {
        if (pipe == null) return;
        byte[] d = json.getBytes("UTF-8");
        ByteBuffer buf = ByteBuffer.allocate(8 + d.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(op);
        buf.putInt(d.length);
        buf.put(d);
        pipe.write(buf.array());
    }

    public void close() {
        connected = false;
        try {
            if (pipe != null) pipe.close();
        } catch (IOException ignored) {}
        pipe = null;
    }

    private int getPid() {
        try {
            String name = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            return Integer.parseInt(name.split("@")[0]);
        } catch (Exception e) {
            return 0;
        }
    }
}
