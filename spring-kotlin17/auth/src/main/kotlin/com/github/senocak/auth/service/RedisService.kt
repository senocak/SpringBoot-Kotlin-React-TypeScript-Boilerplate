package com.github.senocak.auth.service

import com.github.senocak.auth.config.RedisConfig
import com.github.senocak.auth.exception.ConfigException
import com.github.senocak.auth.util.logger
import org.slf4j.Logger
import org.springframework.stereotype.Service
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub

@Service
class RedisService(
    private val redisConfig: RedisConfig
){
    private val log: Logger by logger()

    @Throws(ConfigException::class)
    fun getValue(key: String?): String {
        var jedis: Jedis? = null
        return try {
            jedis = this.jedis
            jedis[key]
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun setValue(key: String?, value: String?) {
        var jedis: Jedis? = null
        try {
            jedis = this.jedis
            jedis[key] = value
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun getAttribute(key: String?, attributeKey: String?): String {
        var jedis: Jedis? = null
        return try {
            jedis = this.jedis
            jedis.hget(key, attributeKey)
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun getAttributes(key: String, attributeKeys: List<String>): List<String> {
        var jedis: Jedis? = null
        return try {
            jedis = this.jedis
            val attributeKeyArray = arrayOfNulls<String>(attributeKeys.size)
//            attributeKeys.toArray<String>(attributeKeyArray)
            jedis.hmget(key, *attributeKeyArray)
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun setAttribute(key: String?, attributeKey: String?, value: String?) {
        var jedis: Jedis? = null
        try {
            jedis = this.jedis
            jedis.hset(key, attributeKey, value)
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun getAttributes(key: String?): Map<String, String> {
        var jedis: Jedis? = null
        return try {
            jedis = this.jedis
            jedis.hgetAll(key)
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun setAttributes(key: String?, attributeMap: Map<String?, String?>?) {
        var jedis: Jedis? = null
        try {
            jedis = this.jedis
            jedis.hmset(key, attributeMap)
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun removeAttributes(key: String?, vararg attributeKeys: String?) {
        var jedis: Jedis? = null
        try {
            jedis = this.jedis
            jedis.hdel(key, *attributeKeys)
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun removeKey(key: String?) {
        var jedis: Jedis? = null
        try {
            jedis = this.jedis
            jedis.del(key)
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun getKeys(pattern: String?): Set<String> {
        var jedis: Jedis? = null
        val keys: Set<String>
        try {
            jedis = this.jedis
            keys = jedis.keys(pattern)
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
        return keys
    }

    @Throws(ConfigException::class)
    fun psubscribe(subscriber: JedisPubSub?, vararg channels: String?) {
        try {
            Thread {
                val jedis = jedis
                jedis.psubscribe(subscriber, *channels)
                releaseJedis(jedis)
            }.start()
        } catch (e: Exception) {
            throw ConfigException(e)
        }
    }

    @Throws(ConfigException::class)
    fun addToList(key: String?, value: String?) {
        var jedis: Jedis? = null
        try {
            jedis = this.jedis
            jedis.lpush(key, value)
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun getFromList(key: String?, start: Int, end: Int): List<String> {
        var jedis: Jedis? = null
        return try {
            jedis = this.jedis
            jedis.lrange(key, start.toLong(), end.toLong())
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    @Throws(ConfigException::class)
    fun removeFromList(key: String?, count: Long, value: String?): Long {
        var jedis: Jedis? = null
        return try {
            jedis = this.jedis
            jedis.lrem(key, count, value)
        } catch (e: Exception) {
            throw ConfigException(e)
        } finally {
            releaseJedis(jedis)
        }
    }

    /**
     * Get a jedis instance from the pool.
     * @return a jedis instance
     */
    private val jedis: Jedis
        get() {
            val jedisPool = redisConfig.jedisPool
            log.debug(
                "Get new jedis resource from pool. Current pool stats: Active= {} Idle= {} Waiters= {} ",
                jedisPool!!.numActive, jedisPool.numIdle, jedisPool.numWaiters
            )
            return jedisPool.resource
        }

    /**
     * release jedis resource
     * @param jedis jedis
     */
    private fun releaseJedis(jedis: Jedis?) {
        jedis?.close()
    }
}