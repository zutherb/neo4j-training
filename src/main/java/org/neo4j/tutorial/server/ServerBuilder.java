package org.neo4j.tutorial.server;

import static org.neo4j.server.ServerTestUtils.asOneLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.configuration.PropertyFileConfigurator;
import org.neo4j.server.configuration.validation.DatabaseLocationMustBeSpecifiedRule;
import org.neo4j.server.configuration.validation.Validator;
import org.neo4j.server.database.CommunityDatabase;
import org.neo4j.server.database.Database;
import org.neo4j.server.rest.paging.Clock;
import org.neo4j.server.rest.paging.FakeClock;
import org.neo4j.server.rest.paging.LeaseManagerProvider;
import org.neo4j.server.startup.healthcheck.StartupHealthCheck;
import org.neo4j.server.startup.healthcheck.StartupHealthCheckRule;

public class ServerBuilder
{
    private String portNo = "7474";
    private String maxThreads = null;
    protected String dbDir = null;
    private String webAdminUri = "/db/manage/";
    private String webAdminDataUri = "/db/data/";
    protected StartupHealthCheck startupHealthCheck;
    private final HashMap<String, String> thirdPartyPackages = new HashMap<String, String>();
    private final Properties arbitraryProperties = new Properties();

    private static enum WhatToDo
    {
        CREATE_GOOD_TUNING_FILE,
        CREATE_DANGLING_TUNING_FILE_PROPERTY,
        CREATE_CORRUPT_TUNING_FILE
    }

    private WhatToDo action;
    protected Clock clock = null;
    private String[] autoIndexedNodeKeys = null;
    private String[] autoIndexedRelationshipKeys = null;
    private String host = null;
    private String[] securityRuleClassNames;
    public boolean persistent;
    private Boolean httpsEnabled = false;

    public static ServerBuilder server()
    {
        return new ServerBuilder();
    }

    public CommunityNeoServer build() throws IOException
    {
        if ( dbDir == null )
        {
            this.dbDir = org.neo4j.server.ServerTestUtils.createTempDir().getAbsolutePath();
        }
        File configFile = createPropertiesFiles();

        if ( startupHealthCheck == null )
        {
            startupHealthCheck = new StartupHealthCheck()
            {
                @Override
                public boolean run()
                {
                    return true;
                }
            };
        }

        if ( clock != null )
        {
            LeaseManagerProvider.setClock( clock );
        }

        return new CommunityNeoServer(
                new PropertyFileConfigurator( new Validator( new DatabaseLocationMustBeSpecifiedRule() ), configFile ) )
        {
            @Override
            protected StartupHealthCheck createHealthCheck()
            {
                return startupHealthCheck;
            }

            @Override
            protected Database createDatabase()
            {
                return new CommunityDatabase( configurator.configuration() );
            }
        };
    }

    public File createPropertiesFiles() throws IOException
    {
        File temporaryConfigFile = org.neo4j.server.ServerTestUtils.createTempPropertyFile();

        createPropertiesFile( temporaryConfigFile );
        createTuningFile( temporaryConfigFile );

        return temporaryConfigFile;
    }

    private void createPropertiesFile( File temporaryConfigFile )
    {
        Map<String, String> properties = MapUtil.stringMap(
                Configurator.DATABASE_LOCATION_PROPERTY_KEY, dbDir,
                Configurator.MANAGEMENT_PATH_PROPERTY_KEY, webAdminUri,
                Configurator.REST_API_PATH_PROPERTY_KEY, webAdminDataUri );
        if ( portNo != null )
        {
            properties.put( Configurator.WEBSERVER_PORT_PROPERTY_KEY, portNo );
        }
        if ( host != null )
        {
            properties.put( Configurator.WEBSERVER_ADDRESS_PROPERTY_KEY, host );
        }
        if ( maxThreads != null )
        {
            properties.put( Configurator.WEBSERVER_MAX_THREADS_PROPERTY_KEY, maxThreads );
        }

        if ( thirdPartyPackages.keySet().size() > 0 )
        {
            properties.put( Configurator.THIRD_PARTY_PACKAGES_KEY, asOneLine( thirdPartyPackages ) );
        }

        if ( autoIndexedNodeKeys != null && autoIndexedNodeKeys.length > 0 )
        {
            properties.put( "node_auto_indexing", "true" );
            String propertyKeys = org.apache.commons.lang.StringUtils.join( autoIndexedNodeKeys, "," );
            properties.put( "node_keys_indexable", propertyKeys );
        }

        if ( autoIndexedRelationshipKeys != null && autoIndexedRelationshipKeys.length > 0 )
        {
            properties.put( "relationship_auto_indexing", "true" );
            String propertyKeys = org.apache.commons.lang.StringUtils.join( autoIndexedRelationshipKeys, "," );
            properties.put( "relationship_keys_indexable", propertyKeys );
        }

        if ( securityRuleClassNames != null && securityRuleClassNames.length > 0 )
        {
            String propertyKeys = org.apache.commons.lang.StringUtils.join( securityRuleClassNames, "," );
            properties.put( Configurator.SECURITY_RULES_KEY, propertyKeys );
        }

        if ( httpsEnabled != null )
        {
            if ( httpsEnabled )
            {
                properties.put( Configurator.WEBSERVER_HTTPS_ENABLED_PROPERTY_KEY, "true" );
            }
            else
            {
                properties.put( Configurator.WEBSERVER_HTTPS_ENABLED_PROPERTY_KEY, "false" );
            }
        }

        for ( Object key : arbitraryProperties.keySet() )
        {
            properties.put( String.valueOf( key ), String.valueOf( arbitraryProperties.get( key ) ) );
        }

        org.neo4j.server.ServerTestUtils.writePropertiesToFile( properties, temporaryConfigFile );
    }

    private void createTuningFile( File temporaryConfigFile ) throws IOException
    {
        if ( action == WhatToDo.CREATE_GOOD_TUNING_FILE )
        {
            File databaseTuningPropertyFile = org.neo4j.server.ServerTestUtils.createTempPropertyFile();
            Map<String, String> properties = MapUtil.stringMap(
                    "neostore.nodestore.db.mapped_memory", "25M",
                    "neostore.relationshipstore.db.mapped_memory", "50M",
                    "neostore.propertystore.db.mapped_memory", "90M",
                    "neostore.propertystore.db.strings.mapped_memory", "130M",
                    "neostore.propertystore.db.arrays.mapped_memory", "130M" );
            org.neo4j.server.ServerTestUtils.writePropertiesToFile( properties, databaseTuningPropertyFile );
            org.neo4j.server.ServerTestUtils.writePropertyToFile( Configurator.DB_TUNING_PROPERTY_FILE_KEY,
                    databaseTuningPropertyFile.getAbsolutePath(), temporaryConfigFile );
        }
        else if ( action == WhatToDo.CREATE_DANGLING_TUNING_FILE_PROPERTY )
        {
            org.neo4j.server.ServerTestUtils.writePropertyToFile( Configurator.DB_TUNING_PROPERTY_FILE_KEY,
                    org.neo4j.server.ServerTestUtils.createTempPropertyFile().getAbsolutePath(),
                    temporaryConfigFile );
        }
        else if ( action == WhatToDo.CREATE_CORRUPT_TUNING_FILE )
        {
            File corruptTuningFile = trashFile();
            org.neo4j.server.ServerTestUtils.writePropertyToFile( Configurator.DB_TUNING_PROPERTY_FILE_KEY,
                    corruptTuningFile.getAbsolutePath(),
                    temporaryConfigFile );
        }
    }

    private File trashFile() throws IOException
    {
        File f = org.neo4j.server.ServerTestUtils.createTempPropertyFile();

        FileWriter fstream = new FileWriter( f, true );
        BufferedWriter out = new BufferedWriter( fstream );

        for ( int i = 0; i < 100; i++ )
        {
            out.write( (int) System.currentTimeMillis() );
        }

        out.close();
        return f;
    }

    protected ServerBuilder()
    {
    }

    public ServerBuilder persistent()
    {
        this.persistent = true;
        return this;
    }

    public ServerBuilder onPort( int portNo )
    {
        this.portNo = String.valueOf( portNo );
        return this;
    }

    public ServerBuilder withMaxJettyThreads( int maxThreads )
    {
        this.maxThreads = String.valueOf( maxThreads );
        return this;
    }

    public ServerBuilder usingDatabaseDir( String dbDir )
    {
        this.dbDir = dbDir;
        return this;
    }

    public ServerBuilder withRelativeWebAdminUriPath( String webAdminUri )
    {
        try
        {
            URI theUri = new URI( webAdminUri );
            if ( theUri.isAbsolute() )
            {
                this.webAdminUri = theUri.getPath();
            }
            else
            {
                this.webAdminUri = theUri.toString();
            }
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( e );
        }
        return this;
    }

    public ServerBuilder withRelativeWebDataAdminUriPath( String webAdminDataUri )
    {
        try
        {
            URI theUri = new URI( webAdminDataUri );
            if ( theUri.isAbsolute() )
            {
                this.webAdminDataUri = theUri.getPath();
            }
            else
            {
                this.webAdminDataUri = theUri.toString();
            }
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( e );
        }
        return this;
    }

    public ServerBuilder withoutWebServerPort()
    {
        portNo = null;
        return this;
    }

    public ServerBuilder withFailingStartupHealthcheck()
    {
        startupHealthCheck = new StartupHealthCheck()
        {
            @Override
            public boolean run()
            {
                return false;
            }

            @Override
            public StartupHealthCheckRule failedRule()
            {
                return new StartupHealthCheckRule()
                {

                    @Override
                    public String getFailureMessage()
                    {
                        return "mockFailure";
                    }

                    @Override
                    public boolean execute( Properties properties )
                    {
                        return false;
                    }
                };
            }
        };
        return this;
    }

    public ServerBuilder withDefaultDatabaseTuning() throws IOException
    {
        action = WhatToDo.CREATE_GOOD_TUNING_FILE;
        return this;
    }

    public ServerBuilder withNonResolvableTuningFile() throws IOException
    {
        action = WhatToDo.CREATE_DANGLING_TUNING_FILE_PROPERTY;
        return this;
    }

    public ServerBuilder withCorruptTuningFile() throws IOException
    {
        action = WhatToDo.CREATE_CORRUPT_TUNING_FILE;
        return this;
    }

    public ServerBuilder withThirdPartyJaxRsPackage( String packageName, String mountPoint )
    {
        thirdPartyPackages.put( packageName, mountPoint );
        return this;
    }

    public ServerBuilder withFakeClock()
    {
        clock = new FakeClock();
        return this;
    }

    public ServerBuilder withAutoIndexingEnabledForNodes( String... keys )
    {
        autoIndexedNodeKeys = keys;
        return this;
    }

    public ServerBuilder withAutoIndexingEnabledForRelationships( String... keys )
    {
        autoIndexedRelationshipKeys = keys;
        return this;
    }

    public ServerBuilder onHost( String host )
    {
        this.host = host;
        return this;
    }

    public ServerBuilder withSecurityRules( Class... securityRuleClasses )
    {
        ArrayList<String> classNames = new ArrayList<String>();
        for ( Class c : securityRuleClasses )
        {
            classNames.add( c.getCanonicalName() );
        }

        this.securityRuleClassNames = classNames.toArray( new String[securityRuleClasses.length] );

        return this;
    }

    public ServerBuilder withHttpsEnabled()
    {
        httpsEnabled = true;
        return this;
    }

    public ServerBuilder withProperty( String key, String value )
    {
        arbitraryProperties.put( key, value );
        return this;
    }

    public ServerBuilder withStartupHealthCheckRules( StartupHealthCheckRule... rules )
    {
        this.startupHealthCheck = new StartupHealthCheck( arbitraryProperties, rules );
        return this;
    }
}
