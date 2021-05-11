package com.stefvic.springdata.test.cassandra;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

/** JUnit 5 extension to ensure a running Embedded Cassandra server. */
class EmbeddedCassandraExtension implements BeforeAllCallback {

  static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(EmbeddedCassandraExtension.class);

  private static void checkTestClassAnnotation(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    AnnotationUtils.findAnnotation(testClass, EmbeddedCassandra.class)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Test class not annotated with @" + EmbeddedCassandra.class.getSimpleName()));
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    // check required annotation on test class
    checkTestClassAnnotation(context);
    context
        .getStore(NAMESPACE)
        .getOrComputeIfAbsent(
            EmbeddedCassandraServer.class,
            t -> EmbeddedCassandraLifecycle.start(),
            EmbeddedCassandraServer.class);
  }
}
