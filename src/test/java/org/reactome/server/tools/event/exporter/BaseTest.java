package org.reactome.server.tools.event.exporter;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.reactome.server.graph.aop.LazyFetchAspect;
import org.reactome.server.graph.config.Neo4jConfig;
import org.reactome.server.graph.service.GeneralService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assume.assumeTrue;

@ContextConfiguration(classes = { Neo4jConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BaseTest {

    static final Logger logger = LoggerFactory.getLogger("testLogger");

    private static Boolean checkedOnce = false;
    private static Boolean isFit = false;

    @Autowired
    protected GeneralService generalService;

    @Autowired
    protected LazyFetchAspect lazyFetchAspect;

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        if (!checkedOnce) {
            isFit = generalService.fitForService();
            checkedOnce = true;
        }
        assumeTrue(isFit);
    }


}
