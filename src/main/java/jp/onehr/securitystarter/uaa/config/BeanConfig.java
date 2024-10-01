package jp.onehr.securitystarter.uaa.config;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class BeanConfig {

    @Bean
    LoadingCache<Object, Object> caffeineCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                // 通常 refreshAfterWrite 的时间会短于 expireAfterWrite，这样可以确保数据在缓存有效期内得以刷新，同时也有一个硬性过期时间。
                // 控制缓存条目从写入或最后更新后经过一段时间后过期，并从缓存中移除。设置的时间较长，数据在这段时间内保持有效，但一旦过了这个时间，数据就会完全失效并被移除。
                .expireAfterWrite(Duration.ofMinutes(5))
                // 控制缓存条目在达到指定时间后下一次访问时进行刷新，重新加载数据，但条目在刷新之前不会被删除。这个时间一般比 expireAfterWrite 短，用于确保缓存中的数据在有效期内被定期更新。
                .refreshAfterWrite(Duration.ofMinutes(1))
                // 初始的缓存空间大小
                .initialCapacity(500)
                .recordStats()
                .removalListener((key, value, cause) ->  System.out.println("key: "+key+" removed, removalCause: "+cause+ "."))
                // 缓存未命中时的加载逻辑
                .build(key -> {
                    System.out.println("----++++");
                    System.out.println(key);
                    return key;
                });
    }

    @Bean
    AsyncCache<Object,Object> asyncCaffeineCache(){
        return Caffeine.newBuilder()
                .maximumSize(10000)
                // 通常 refreshAfterWrite 的时间会短于 expireAfterWrite，这样可以确保数据在缓存有效期内得以刷新，同时也有一个硬性过期时间。
                // 控制缓存条目从写入或最后更新后经过一段时间后过期，并从缓存中移除。设置的时间较长，数据在这段时间内保持有效，但一旦过了这个时间，数据就会完全失效并被移除。
                .expireAfterWrite(Duration.ofMinutes(5))
                // 控制缓存条目在达到指定时间后下一次访问时进行刷新，重新加载数据，但条目在刷新之前不会被删除。这个时间一般比 expireAfterWrite 短，用于确保缓存中的数据在有效期内被定期更新。
                .refreshAfterWrite(Duration.ofMinutes(1))
                // 初始的缓存空间大小
                .initialCapacity(500)
                .recordStats()
                .removalListener(((key, value, cause) -> System.out.println("key: "+key+" removed, removalCause: "+cause.name()+ ".")))
                // 缓存未命中时的加载逻辑
                .buildAsync(key -> {
                    System.out.println("----++++");
                    System.out.println(key);
                    return key;
                });
    }

}
