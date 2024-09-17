/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.authentication;

import io.camunda.service.UserServices;
import io.camunda.service.search.query.SearchQueryBuilders;
import java.util.Objects;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CamundaUserDetailsService implements UserDetailsService {
  private final UserServices userServices;

  public CamundaUserDetailsService(final UserServices userServices) {
    this.userServices = userServices;
  }

  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    final var userQuery =
        SearchQueryBuilders.userSearchQuery(
            fn -> fn.filter(f -> f.username(username)).page(p -> p.size(1)));
    return userServices.search(userQuery).items().stream()
        .filter(Objects::nonNull)
        .findFirst()
        .map(
            candidate ->
                User.builder()
                    .username(candidate.username())
                    .password(candidate.password())
                    .build())
        .orElseThrow(() -> new UsernameNotFoundException(username));
  }
}
