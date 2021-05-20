package org.reactome.server.tools.event.exporter;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactome.server.graph.aop.LazyFetchAspect;
import org.reactome.server.graph.config.Neo4jConfig;
import org.reactome.server.graph.service.GeneralService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ContextConfiguration(classes = {Neo4jConfig.class})
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

    @BeforeEach
    public void setUp() {
        if (!checkedOnce) {
            isFit = generalService.fitForService();
            checkedOnce = true;
        }
        Assumptions.assumeTrue(isFit);
    }
}
