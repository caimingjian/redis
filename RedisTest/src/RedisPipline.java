import com.alibaba.fastjson.JSON;
import org.junit.Test;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: cmj
 * @Description:
 * @Date: 2019/12/27
 */
public class RedisPipline {

    private static ShardedJedisPool pool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(50);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        // 集群
        JedisShardInfo jedisShardInfo = new JedisShardInfo("r-bp1v8dka8y7vd1rybr.redis.rds.aliyuncs.com", 6379);
        jedisShardInfo.setPassword("Sye32OhBKymVEQcT");
        List<JedisShardInfo> list = new LinkedList<JedisShardInfo>();
        list.add(jedisShardInfo);
        pool = new ShardedJedisPool(config, list);
    }

    @Test
    public void demoTest() {
        //String keys = "myname";
        //String vaule = jedis.set(keys, "lxr");
        //System.out.println(vaule);

        ShardedJedis one = pool.getResource();
        ShardedJedisPipeline pipeline = one.pipelined();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            pipeline.set("sppn" + i, "n" + i);
        }
        List<Object> results = pipeline.syncAndReturnAll();
        long end = System.currentTimeMillis();
        System.out.println(JSON.toJSONString(results));
        System.out.println("Pipelined@Pool SET: " + ((end - start) / 1000.0) + " seconds");
    }

    @Test
    public void getData() {
        piplineSetValue();
        piplineGet();
    }

    private void piplineGet() {
        ShardedJedis resource = pool.getResource();
        ShardedJedisPipeline pipeline = resource.pipelined();
        pipeline.get("key1");
        pipeline.hmget("key2", "hash1");
        List<Object> results = pipeline.syncAndReturnAll();
        System.out.println(JSON.toJSONString(results));
    }

    private void piplineSetValue() {
        ShardedJedis resource = pool.getResource();
        ShardedJedisPipeline pipeline = resource.pipelined();
        long start = System.currentTimeMillis();
        pipeline.set("key1", "value");
        pipeline.hsetnx("key2", "hash1", "hashValue");
        List<Object> results = pipeline.syncAndReturnAll();
        long end = System.currentTimeMillis();
        System.out.println(JSON.toJSONString(results));
        System.out.println(end-start);
    }
}
