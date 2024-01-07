package ru.littleligr.creatopico.create.utils.redis;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RedisManager {
    private final List<JedisPooled> jedisPools = new ArrayList<>(Type.values().length);

    private JedisClientConfig configBuilder(int db) {
        return DefaultJedisClientConfig.builder()
                .database(db)
                .build();
    }

    public RedisManager(String ip, int port) {
        for (Type type : Type.values())
            jedisPools.add(new JedisPooled(new HostAndPort(ip, port), configBuilder(type.redisDatabaseID)));
    }

    public JedisPooled getPool(Type type) {
        return jedisPools.get(type.redisDatabaseID);
    }

    public NbtCompound read(Type type, UUID key) {
        JedisPooled pool = getPool(type);

        try {
            String nbt = pool.get(key.toString());
            if (nbt != null)
                return StringNbtReader.parse(nbt);
            return null;
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(Type type, UUID key, NbtCompound nbtCompound) {
        getPool(type).set(key.toString(), nbtCompound.asString());
    }

    public List<NbtCompound> getAll(Type type) {
        JedisPooled pool = getPool(type);

        List<NbtCompound> values;

        values = pool.scan("*").getResult().stream().map(pool::get).map(val -> {
            try {
                return StringNbtReader.parse(val);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        return values;
    }

    public enum Type {
        player(0),
        train(1),
        track(2),
        signal(3),
        dimension(4);

        public final int redisDatabaseID;

        Type(int redisDatabaseID) {
            this.redisDatabaseID = redisDatabaseID;
        }
    }
}
