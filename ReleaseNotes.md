# Release Notes 22.0.6-BETA

## Upgrading to this Version
### Convert connection URLs to use query parameters
Force connection URLs now use standard query parameters.  For example:

    force://host;user=username;password=password

Becomes

    force://host?user=username&password=password


Note that if a new connection URL is added directly to XML, any ampersand ('&') will need to be escaped as `&amp;`

### Use ${} URL injection
A new JPA URL injection method is added that allows for better naming flexibility.  Past versions performed URL injection based on naming convention.  For example:

    $ export FORCE_FORCEDATABASE_URL=force://host;user=username;password=password

Coupled with this persistence-unit:

    <persistence-unit name="forceDatabase">
    ...

This can now be replaced with:

    $ export FORCE_FORCEDATABASE_URL=force://host?user=username&password=password

And

    <persistence-unit name="myPersistenceUnit">
      <property name="datanucleus.ConnectionURL" value="${FORCE_FORCEDATABASE_URL}" />
      ...

## Bug Fixes
- A problem with local development container detection causes infinite OAuth redirect loop.
