# SpringDistributedCache
POC for distributed cache with Redis and Spring Boot

## Concecpts
* JSR 107 is the specification that rules how cache abstraction layer works
* It provides a common cache interface, for creating, requesting, updating and removing information from a cache
* This common interface makes easier for implementing many cache providers
* By default Spring Boot uses ConcurrentHashMap for cache storage (in this case it's a cache per application instance, instead of a centered cache provider for many application instances)
* For using Redis as a distributed cache server / provider Spring Boot requires some configuration

* Cache hit - when requested data exists in cache, avoiding additional calls to service / data repositories (which is faster)
* Cache miss - when requested data doesn't exists in cache, where data repository call is needed to providing response (which increase response time)

* Enable cache in app with @EnableCaching annotation
    * Annotation to ensure that post processor will check all beans trying to find demarcated methods and will create proxy to intercept all invocations.
    * Apply in configuration class or main class

* Check spring documentation for details about annotation parameters (link in the end of this document)

* @Cacheable
    * Set / add cache entry after method execution
    * In subsequent invocations cache data will be used instead of repository request (faster)
    * Global or conditional
    * Apply in methods where data is setted or retrieved (GET, POST)
        ```
        @Cacheable(cacheNames = "post-single", key = "#id", unless = "#result.shares < 500")
        @GetMapping("/{id}")
        public Post getPostByID(@PathVariable String id) throws PostNotFoundException {
            
        @Cacheable(value = "post-top")
        @GetMapping("/top")
        public List<Post> getTopPosts() {
        ```
    * cacheNames = cache identifier
    * key = expression for setting cache key (#parameter, #root.methodName etc)
    * unless = conditional caching expression

* @CachePut
    * Update cache entry after method execution
        ```
        @CachePut(cacheNames = "post-single", key = "#post.id")
        @PutMapping("/update")
        public Post updatePostByID(@RequestBody Post post) throws PostNotFoundException {
        ```

* @CacheEvict
    * Remove entry from cache
    * Conditional or global
    * Apply to delete methods or specific methods for cache cleaning	
        ```	
        @CacheEvict(cacheNames = "post-single", key = "#id")
        @DeleteMapping("/delete/{id}")
        public void deletePostByID(@PathVariable String id) throws PostNotFoundException {
        
        @CacheEvict(cacheNames = "post-top", allEntries = true)
        @GetMapping("/top/evict")
        public void evictTopPosts() {
        ```
	
* @Caching
    * Aggregate multiple annotations of the same type when e.g. you need to use different conditions and caches.
	
* @CacheConfig
    * Class level annotation allows to specify global values for annotations like cache name or key generator.

* Redis configuration
    * Create a class with @Configuration annotation for setting cache manager settings
    * Set TTL for each cache key
    * Set JSON serializer for storing data in redis
    * Refer to [this article](https://medium.com/dev-cave/redis-i-cache-distribu%C3%ADdo-34190dce037a)

## Redis
* In 64 bit system redis have no memory limit (more memory = more cache hits)
* Set configs in redis.conf file
    ```
    #memory limit up to 128MB (up to you)
    maxmemory 128mb
    #remove the last recently used (LRU) keys first
    maxmemory-policy allkeys-lru
    #eviction precision (up to you)
    maxmemory-samples 10
    ```

* Evictions algorithms
    * Last Recently Used (LRU) track when key was used last time. So probably it will be still used in future, but what if it was only ‘one shot’ before long idle time? Key will be stored to next eviction cycle.
    * Least Frequently Used (LFU) - Available from Redis 4.0 - will count how many times key was used. The most popular keys will survive eviction cycle.
* Durability — For some reasons you may want to persist your cache. After startup, cache is initially empty, it will be useful to fulfill it with snapshot data in case of recovery after outage. Redis support three types of persistence:
    * RDB point-in-time snapshots after specific interval of time or amount of writes. Rare snapshots should not harm performance but it will be a good task trying to find balance between snapshot interval and to avoid serving obsolete data after outage.
    * AOF create persistence logs with every write operation. If you consider this level of durability, you should read about different fsync policies under appendfsync configuration parameter.
    * Both RDB and AOF.    

* Metrics
    * Hit/miss ratio — Describes cache efficiency and give us relevant information about rightness of our approach. Low hit ratio is a signal to reflect on nature of stored data. It’s easy to fall into the trap of premature optimization at the early
     stages of project when you can only guess relying on experience what data should be cached.
    * Latency — Maximum delay between request and respond. First place where we can find if something bad happen with your cache. Many factors can impact on latency like VM overhead, slow commands or I/O operations.
    * Fragmentation Ratio — Redis will always use more memory than you declared in maxmemory for himself and e.g. to keeps keys metadata but high ratio can be first signal of memory fragmentation.
        * ratio < 1.0 —Memory allocator need more than you can give him. Old data will be swapped to disk what occurs resources consumption and increase latency. (In case of cache usage, swapping should be disabled)
        * ratio > ~1.5 — Operation system slice memory segment into pieces, Redis use more physical memory than he requested for.
    * Evicted keys — When cache size exceeds maxmemory limit Redis removes data using chosen eviction policy. Constantly fast growing value can be signal of insufficient cache size.

* Redis CLI:
    ```
    docker exec -it redis redis-cli
  
    SET "my key" "value" EX 20 #set a value with 20 sec expiration
    GET "my key"         #get value
    KEYS *               #get all stored keys
    TTL "key"            #show remaining key time
  
    MONITOR              #shows executed commands (by connected apps)
    ```

## Cache setup in a Spring boot project
* Setting cache type and host in application.properties file:

```
spring.cache.type=redis
#spring.cache.redis.time-to-live: 20_000
spring.redis.host=127.0.0.1
spring.redis.port=6379
```

* uncomment time-to-live for setting a global ttl for cache entries

## Project environment setup
* Setup & run Redis on Docker:
    * First run
    ```
    docker run -p 6379:6379 -d --name redis redis:latest
    ```
    * Start redis server
    ```
    docker start redis
    ```
    * Run CLI and monitor (for monitoring app commands)
    ```
    docker exec -it redis redis-cli
    MONITOR
    ```

## Resources / links
* https://medium.com/@MatthewFTech/spring-boot-cache-with-redis-56026f7da83a
* https://emmanuelneri.com.br/2019/04/30/cache-distribuido-com-redis-no-spring-boot/amp/
* https://redis.io/topics/lru-cache
* https://docs.spring.io/spring/docs/current/spring-framework-reference/integration.html#cache
* https://medium.com/dev-cave/redis-i-cache-distribu%C3%ADdo-34190dce037a
