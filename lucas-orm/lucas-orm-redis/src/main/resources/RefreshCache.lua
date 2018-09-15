-- 缓存刷入脚本
--[[ 
KEY1: redis key (e.g. KEY_18600001234)
ARGV1: redis value
ARGV2: redis key ttl
ARGV3: [可选，默认SELECT] 缓存类型, 可选值 SELECT/UPDATE
ARGV4: [可选，ARGV3=UPDATE时必填] 操作时间时间戳，用于时间比较 (e.g. 1536063554041)
ARGV5: [可选，默认60s] 操作校验有效时间， (e.g. 60)
--]]

local result
-- 判断key是否存在
local exist = redis.call('exists', KEYS[1])

if exist == 0 then
    -- key不存在直接写入缓存
    result = redis.call('setex', KEYS[1], ARGV[2], ARGV[1])
    result = 'SUCCESS_SAVE'
    if ARGV[3] ~= nil and ARGV[3] == 'UPDATE' then
        -- 如果当前操作为UPDATE类型, 同时写入比较缓存
        redis.call('setex', KEYS[1] .. '_LUA_COMPARE_CACHE', ARGV[5], ARGV[4])
    end
else
    -- key存在时判断是否可更新缓存
    local compareCache = redis.call('get', KEYS[1] .. '_LUA_COMPARE_CACHE')
    if compareCache == false then
        -- 比较缓存不存在, 直接写入
        result = redis.call('setex', KEYS[1], ARGV[2], ARGV[1])
        result = 'SUCCESS_REFRESH'
        if ARGV[3] ~= nil and ARGV[3] == 'UPDATE' then
            -- 如果当前操作为UPDATE类型, 同时写入比较缓存
            redis.call('setex', KEYS[1] .. '_LUA_COMPARE_CACHE', ARGV[5], ARGV[4])
        end
    elseif ARGV[3] == 'UPDATE' and compareCache < ARGV[4] then
        if compareCache < ARGV[4] then
            -- 当前操作时间在比较缓存时间之后, 允许覆盖写入缓存
            result = redis.call('setex', KEYS[1], ARGV[2], ARGV[1])
            redis.call('setex', KEYS[1] .. '_LUA_COMPARE_CACHE', ARGV[5], ARGV[4])
            result = 'SUCCESS_NEWER_REFRESH'
        else
            result = 'UNSUCCESS_OLDER'
        end
    else
        result = 'UNSUCCESS_CAN_NOT'
    end
end
return result