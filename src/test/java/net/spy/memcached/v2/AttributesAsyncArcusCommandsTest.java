package net.spy.memcached.v2;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.collection.CollectionOverflowAction;
import net.spy.memcached.collection.CreateAttributes;
import net.spy.memcached.collection.ElementValueType;
import net.spy.memcached.v2.attribute.UpdateAttributes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AttributesAsyncArcusCommandsTest extends AsyncArcusCommandsTest {

  @Test
  void setAttributesSuccess() throws ExecutionException, InterruptedException, TimeoutException {
    // given
    String key = keys.get(0);

    async.set(key, 0, VALUE)
        .thenAccept(Assertions::assertTrue)
        .toCompletableFuture()
        .get(300L, TimeUnit.MILLISECONDS);

    // when
    UpdateAttributes attributes = UpdateAttributes.builder()
        .expireTime(100L)
        .build();

    async.setAttributes(key, attributes)
        // then
        .thenAccept(Assertions::assertTrue)
        .toCompletableFuture()
        .get(300L, TimeUnit.MILLISECONDS);
  }

  @Test
  void setAttributesFailureNotExistsKey()
      throws ExecutionException, InterruptedException, TimeoutException {
    // given
    String key = keys.get(0);

    UpdateAttributes attributes = UpdateAttributes.builder()
        .expireTime(100L)
        .build();

    // when
    async.setAttributes(key, attributes)
        // then
        .thenAccept(Assertions::assertFalse)
        .toCompletableFuture()
        .get(300L, TimeUnit.MILLISECONDS);
  }

  @Test
  void setAttributesFailureInvalidOption() {
    // given
    String key = keys.get(0);

    CreateAttributes attributes = CreateAttributes.builder()
        .expireTime(100L)
        .overflowAction(CollectionOverflowAction.smallest_trim)
        .build();

    // expected
    // map/set only allow the "error" overflow action.
    assertThrows(
        IllegalArgumentException.class,
        () -> async.mopCreate(key, ElementValueType.STRING, attributes)
    );

    assertThrows(
        IllegalArgumentException.class,
        () -> async.sopCreate(key, ElementValueType.STRING, attributes)
    );
  }

  @Test
  void getAttributesSuccess() throws ExecutionException, InterruptedException, TimeoutException {
    // given
    String key = keys.get(0);

    CreateAttributes attributes = CreateAttributes.builder()
        .expireTime(100L)
        .maxCount(5000L)
        .overflowAction(CollectionOverflowAction.smallest_trim)
        .build();

    async.bopCreate(key, ElementValueType.STRING, attributes)
        .thenAccept(Assertions::assertTrue)
        .toCompletableFuture()
        .get(300L, TimeUnit.MILLISECONDS);

    // when
    async.getAttributes(key)
        // then
        .thenAccept(result -> {
          assertNotNull(result);
          assertEquals(100L, result.getExpireTime());
          assertEquals(5_000L, result.getMaxCount());
          assertEquals(CollectionOverflowAction.smallest_trim, result.getOverflowAction());
        })
        .toCompletableFuture()
        .get(300L, TimeUnit.MILLISECONDS);
  }

  @Test
  void getAttributesFailureNotExistsKey()
      throws ExecutionException, InterruptedException, TimeoutException {
    // given
    String key = keys.get(0);

    // when
    async.getAttributes(key)
        // then
        .thenAccept(Assertions::assertNull)
        .toCompletableFuture()
        .get(300L, TimeUnit.MILLISECONDS);
  }

}
