package net.jkcode.jkutil.lock

import net.jkcode.jkutil.common.JkApp
import net.jkcode.jkutil.redis.ShardedJedisFactory
import redis.clients.jedis.ShardedJedis

/**
 * 分布式锁实现: redis锁
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
class JedisKeyLock : IDistributedKeyLock() {

    companion object {

        /**
         * 键的前缀
         */
        public val KeyPrefix: String = "lock/"

        /**
         * redis连接
         */
        protected val jedis: ShardedJedis
            get(){
                return ShardedJedisFactory.getConnection()
            }
    }

    /**
     * 快速加锁, 锁不住不等待, 有过期时间
     *
     * @param key
     * @param expireSeconds 锁的过期时间, 单位秒
     * @return
     */
    public override fun doQuickLock(key: Any, expireSeconds: Int): Boolean{
        val path = "$KeyPrefix$key"
        if(isNotExpired(key)) {
            // 延长过期时间
            jedis.expire(path, expireSeconds)
            return true
        }

        // TODO: 优化: setnx+expire 合并为一行代码 jedis.set(key, data, "NX", "EX", expireSeconds)
        // 锁不住直接false
        if(jedis.setnx(path, JkApp.fullWorkerId) === 0L){
            // 处理没有过期时间(即上一次设置过期时间失败)的情况：直接删锁，下一个请求就正常了
            if(jedis.ttl(path) === -1L)
                jedis.del(path);

            return false;
        }

        // 锁n秒，注：此时可能线程中断，导致设置过期时间失败，则ttl = -1
        return jedis.expire(path, expireSeconds) != 0L // 为0则表示key被其他线程删掉了
    }

    /**
     * 解锁
     *
     * @param key
     */
    public override fun doUnlock(key: Any){
        if(isNotExpired(key)) { // 未过期, 则删除key
            val path = "$KeyPrefix$key"
            jedis.del(path)
        }
    }

}