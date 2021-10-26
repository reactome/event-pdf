package org.reactome.server.tools.event.exporter;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactome.server.graph.aop.LazyFetchAspect;
import org.reactome.server.graph.config.GraphCoreNeo4jConfig;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ContextConfiguration(classes = {GraphCoreNeo4jConfig.class})
@ExtendWith(SpringExtension.class)
public abstract class BaseTest {

    static final Logger logger = LoggerFactory.getLogger("testLogger");

    private static Boolean checkedOnce = false;
    private static Boolean isFit = false;

    @Autowired
    protected GeneralService generalService;

    @Autowired
    protected LazyFetchAspect lazyFetchAspect;

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeAll
    public static void setUpStatic(@Value("${spring.neo4j.uri}") String uri,
                                   @Value("${spring.neo4j.authentication.username}") String user,
                                   @Value("${spring.neo4j.authentication.password}") String pass) {
        ReactomeGraphCore.initialise(uri, user, pass);
    }

    @BeforeEach
    public void setUp() {
        if (!checkedOnce) {
            isFit = generalService.fitForService();
            checkedOnce = true;
        }
        Assumptions.assumeTrue(isFit);
    }
}
