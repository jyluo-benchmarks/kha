package com.kalixia.ha.dao.cassandra

import com.kalixia.ha.dao.DevicesDao
import com.kalixia.ha.dao.SensorsDao
import com.kalixia.ha.dao.UsersDao
import com.netflix.astyanax.Keyspace
import groovy.util.logging.Slf4j
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import spock.lang.Shared
import spock.lang.Specification

/**
 * Abstract class easing tests for Cassandra DAOs.
 */
@Slf4j("LOGGER")
abstract class AbstractCassandraDaoTest extends Specification {
    @Shared UsersDao usersDao
    @Shared DevicesDao devicesDao
    @Shared SensorsDao sensorsDao

    def setupSpec() {
        LOGGER.info("Starting Embedded Cassandra Server...")
        EmbeddedCassandraServerHelper.startEmbeddedCassandra()

        CassandraModule cassandraModule = new CassandraModule()
        def cassandraPool = cassandraModule.provideConnectionPool()
        def cassandraContext = cassandraModule.provideContext(cassandraPool)
        def keyspace = cassandraModule.provideKeyspace(cassandraContext)
        def schema = new SchemaDefinition(keyspace)

        usersDao = new CassandraUsersDao(schema)
        devicesDao = new CassandraDevicesDao(schema, usersDao)
        sensorsDao = new CassandraSensorsDao(schema)
    }

    def cleanupSpec() {
        LOGGER.info("Stopping Embedded Cassandra Server...")
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
    }

}
