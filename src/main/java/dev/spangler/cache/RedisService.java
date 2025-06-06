package dev.spangler.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class RedisService {

    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;

    @Inject
    public RedisService(RedisDataSource redisDataSource) {
        this.valueCommands = redisDataSource.value(String.class);
        this.keyCommands = redisDataSource.key();
    }

    public void setValue(String key, String value) {
        valueCommands.set(key, value);
    }

    public void setValueWithTTL(String key, String value, long seconds) {
        valueCommands.setex(key, seconds, value);
    }

    public String getValue(String key) {
        return valueCommands.get(key);
    }

    public void deleteKey(String key) {
        keyCommands.del(key);
    }

    public void setListValueWithTTL(String key, List<?> value, long seconds) {
        String json = Json.encode(value);
        setValueWithTTL(key, json, seconds);
    }

    public <T> List<T> getListValue(String key, Class<T> type) {
        String json = getValue(key);

        if (json == null) {
            return null;
        }

        return Json.decodeValue(json, JsonArray.class)
                .stream()
                .map(o -> Json.decodeValue(o.toString(), type))
                .toList();
    }

    public void invalidatePaginatedStudentCache() {
        var keys = keyCommands.keys("students:page:*");
        for (String key : keys) {
            keyCommands.del(key);
        }
    }

}
