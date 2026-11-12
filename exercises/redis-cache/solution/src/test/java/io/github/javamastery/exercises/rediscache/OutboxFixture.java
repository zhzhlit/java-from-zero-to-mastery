package io.github.javamastery.exercises.rediscache;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import redis.clients.jedis.JedisPooled;

class OutboxFixture implements AutoCloseable {
    final TransactionFixture db;
    final InvalidationOutbox outbox;
    final OutboxCourseService service;
    final OutboxDispatcher dispatcher;

    OutboxFixture(JedisPooled redis) {
        db = new TransactionFixture(redis);
        new ResourceDatabasePopulator(new ClassPathResource("outbox-schema.sql")).execute(db.jdbc.getDataSource());
        outbox = new InvalidationOutbox(db.jdbc);
        service = new OutboxCourseService(db.source, outbox, db.catalog, db.transactions);
        dispatcher = new OutboxDispatcher(redis, outbox);
    }

    @Override
    public void close() {
        db.close();
    }
}
