package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;

// TODO: The "me" query and UserPayload resolver have been extracted to the User Service.
// The User Service handles all user-related GraphQL queries.
// Remove this placeholder once the monolith no longer needs to reference these queries.
@DgsComponent
public class MeDatafetcher {}
