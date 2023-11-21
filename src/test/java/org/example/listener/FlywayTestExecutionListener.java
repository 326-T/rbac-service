package org.example.listener;

import org.flywaydb.core.Flyway;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class FlywayTestExecutionListener extends AbstractTestExecutionListener {

  @Override
  public void afterTestClass(org.springframework.test.context.TestContext testContext)
      throws Exception {
    Flyway flyway = testContext.getApplicationContext().getBean(Flyway.class);
    flyway.clean();
    flyway.migrate();
  }
}
