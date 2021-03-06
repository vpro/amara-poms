package nl.vpro.amara_poms.poms;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.vpro.amara_poms.Config;

/**
 * @author joost
 */
@Slf4j
public class PomsCollectionTest  {


    @BeforeEach
    public void setUp() {
        Config.init();
    }

    @Test
    public void testGetCollectionExists() {
        // load collection
        PomsCollection pomsCollection = new PomsCollection("POMS_S_VPRO_1416538");
        log.info(pomsCollection.getGroup().toString());
    }

    @Test
    public void testGetCollectionNotExists() {
        // load collection
        PomsCollection pomsCollection = new PomsCollection("ABC");
    }

 @Test
    public void testGetCollectionnWithdeleted() {
        // load collection
        PomsCollection pomsCollection = new PomsCollection("POMS_S_VPRO_3762086");
        log.info("{}", pomsCollection);
    }

}
