package com.stefvic.springdata.test.cassandra;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;

/** Annotation to activate embedded shared Cassandra server. */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(EmbeddedCassandraExtension.class)
// Do not run test cases in parallel since may result in unpredictable result due to shared
// cassandra server
@ResourceLock("com.comcast.springdata.common.test.cassandra.EmbeddedCassandra")
public @interface EmbeddedCassandra {

  CleanupDataAction cleanupAction() default CleanupDataAction.NONE;

  enum CleanupDataAction {
    NONE,
    BEFORE_EACH_TEST_METHOD,
    AFTER_EACH_TEST_METHOD
  }
}
