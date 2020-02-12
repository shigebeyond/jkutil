package net.jkcode.jkutil.cache

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.redis.ShardedJedisFactory
import net.jkcode.jkutil.serialize.ISerializer
import redis.clients.jedis.ShardedJedis
import java.lang.UnsupportedOperationException

/**
 * redis做的缓存
 * @author shijianhang
 * @create 2018-02-27 下午7:24
 **/
class JedisCache(protected val configName: String = "default") : BaseCache(){

    /**
     * redis配置
     */
    public val config = Config.instance("redis.${configName}", "yaml")

    /**
     * 序列器
     */
    public val serializer: ISerializer = ISerializer.instance(config["serializer"]!!)

    /**
     * redis连接
     */
    protected val jedis: ShardedJedis
        get(){
            return ShardedJedisFactory.getConnection(configName)
        }


    /**
     * 根据键获得值
     *
     * @param key 键
     * @return
     */
    public override fun doGet(key: Any): Any? {
        val value = jedis.get(serializer.serialize(key))
        if(value == null)
            return null
        return serializer.unserialize(value)
    }

    /**
     * 设置键值
     *
     * @param key 键
     * @param value 值
     * @param expireSencond 过期秒数
     */
    public override fun doPut(key: Any, value: Any, expireSencond:Long) {
        //jedis.set(key.toString(), value.toString(), "NX", "EX", expires)
        jedis.set(serializer.serialize(key), serializer.serialize(value), "NX".toByteArray(), "EX".toByteArray(), expireSencond)
    }

    /**
     * 删除指定的键的值
     * @param key 要删除的键
     */
    public override fun remove(key: Any) {
        jedis.del(serializer.serialize(key))
    }

    /**
     * 删除指定正则的值
     * @param pattern 要删除的键的正则
     */
    public override fun removeByPattern(pattern: String){
        /*val keys = jedis.keys(pattern)
        for (key in keys) {
            jedis.del(key)
        }*/
        // 由于 ShardedJedis 是分块管理 Jedis, 因此遍历每块来扫描key -- 性能较差
        for(s in jedis.allShards){
            val keys = s.keys(pattern)
            for (key in keys) {
                jedis.del(key)
            }
        }
    }

    /**
     * 清空缓存
     */
    public override fun clear() {
        //jedis.flushAll()
        throw UnsupportedOperationException("not implemented")
    }

}