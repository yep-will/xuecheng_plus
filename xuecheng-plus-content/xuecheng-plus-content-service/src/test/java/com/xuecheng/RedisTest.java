package com.xuecheng;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

/**
 * @author will
 * @version 1.0
 * @description redis测试
 * @date 2023/3/24 18:55
 */
public class RedisTest {

    @Test
    public void testRedis(){
        Jedis jedis = new Jedis("localhost", 6379);

        jedis.set("username", "will");

        String value = jedis.get("username");
        System.out.println(value);

        jedis.del("username");

        jedis.close();
    }

}
