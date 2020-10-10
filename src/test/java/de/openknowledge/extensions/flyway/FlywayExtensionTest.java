/*
 * Copyright 2020 René Frerichs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.openknowledge.extensions.flyway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.openknowledge.extensions.Customer;

@Flyway
public class FlywayExtensionTest {

  private static EntityManager entityManager;

  @BeforeEach
  void setUp() {
    Map<String, String> properties = new HashMap<>();
    properties.put("javax.persistence.jdbc.url", System.getProperty("jdbc.url"));
    properties.put("javax.persistence.jdbc.user", System.getProperty("jdbc.username"));
    properties.put("javax.persistence.jdbc.password", System.getProperty("jdbc.password"));
    EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("test-unit", properties);
    entityManager = entityManagerFactory.createEntityManager();

    Customer peter = new Customer("Peter", "peter@mail.de");
    Customer hans = new Customer("Hans", "hans@mail.de");

    entityManager.getTransaction().begin();
    entityManager.persist(peter);
    entityManager.persist(hans);
    entityManager.getTransaction().commit();
    Persistence.generateSchema("test-unit", properties);

  }

  @Test
  void initialTest() {
    List<Customer> users = entityManager.createQuery("Select u from Customer u", Customer.class).getResultList();

    //assert
    assertThat(users).hasSize(2);
  }
}
