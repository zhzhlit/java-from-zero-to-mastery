package io.github.javamastery.exercises.rediscache;


import java.util.Objects;

public class AfterCommitInvalidator {
    private final RedisCourseCatalog catalog;

    public AfterCommitInvalidator(RedisCourseCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
    }

    public void invalidateAfterCommit(long id) {
        throw new UnsupportedOperationException("练习：校验实际事务，注册 afterCommit 后才删除缓存");
    }
}
