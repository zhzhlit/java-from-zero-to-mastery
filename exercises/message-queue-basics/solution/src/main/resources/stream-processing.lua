local operation, group, consumer, id = ARGV[1], ARGV[2], ARGV[3], ARGV[4]
local limit, errorCode = tonumber(ARGV[5]), ARGV[6]
if operation ~= 'BEGIN' and operation ~= 'SUCCESS' and operation ~= 'FAILURE' then
    return redis.error_reply('INVALID_OPERATION')
end
if not limit or limit < 1 or limit > 100 or limit ~= math.floor(limit) then
    return redis.error_reply('INVALID_LIMIT')
end

local sourceType = redis.call('TYPE', KEYS[1]).ok
local stateType = redis.call('TYPE', KEYS[2]).ok
local deadType = redis.call('TYPE', KEYS[3]).ok
if sourceType ~= 'stream' then return redis.error_reply('SOURCE_NOT_STREAM') end
if stateType ~= 'none' and stateType ~= 'hash' then return redis.error_reply('STATE_WRONGTYPE') end
if deadType ~= 'none' and deadType ~= 'stream' then return redis.error_reply('DEAD_LETTER_WRONGTYPE') end

local attemptField, codeField, deadField = id .. ':attempts', id .. ':code', id .. ':dead'
local attempts = tonumber(redis.call('HGET', KEYS[2], attemptField) or '0')
if not attempts or attempts < 0 or attempts ~= math.floor(attempts) then
    return redis.error_reply('CORRUPT_RETRY_STATE')
end
local pending = redis.call('XPENDING', KEYS[1], group, id, id, 1)
if #pending > 0 and pending[1][2] ~= consumer then
    return redis.error_reply('OWNERSHIP_LOST')
end
local deadId = redis.call('HGET', KEYS[2], deadField)
if deadId then
    if #pending > 0 then redis.call('XACK', KEYS[1], group, id) end
    return {'ALREADY_DEAD_LETTERED', attempts, deadId}
end
if #pending == 0 then return redis.error_reply('NOT_PENDING') end
if operation == 'BEGIN' then return {'READY', attempts, ''} end

if operation == 'SUCCESS' then
    redis.call('XACK', KEYS[1], group, id)
    redis.call('HDEL', KEYS[2], attemptField, codeField)
    return {'ACKNOWLEDGED', 0, ''}
end

local validCodes = {INVALID_EVENT=true, EVENT_ID_CONFLICT=true, BUSINESS_CONSTRAINT=true, TRANSIENT_DATABASE=true}
if not validCodes[errorCode] then return redis.error_reply('INVALID_ERROR_CODE') end
attempts = attempts + 1
if attempts < limit then
    redis.call('HSET', KEYS[2], attemptField, attempts, codeField, errorCode)
    return {'RETRY', attempts, ''}
end

local original = redis.call('XRANGE', KEYS[1], id, id)
local fields = {'dlqVersion', '1', 'sourceStream', KEYS[1], 'sourceGroup', group,
    'sourceId', id, 'errorCode', errorCode, 'failures', tostring(attempts),
    'payloadMissing', #original == 0 and '1' or '0'}
if #original > 0 then
    local payload = original[1][2]
    for i = 1, #payload, 2 do
        fields[#fields + 1] = 'payload.' .. payload[i]
        fields[#fields + 1] = payload[i + 1]
    end
end

-- Append before acknowledging: an append failure must keep the original pending.
deadId = redis.call('XADD', KEYS[3], '*', unpack(fields))
redis.call('HSET', KEYS[2], attemptField, attempts, codeField, errorCode, deadField, deadId)
redis.call('XACK', KEYS[1], group, id)
return {'DEAD_LETTERED', attempts, deadId}
