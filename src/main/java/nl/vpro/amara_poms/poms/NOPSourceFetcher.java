package nl.vpro.amara_poms.poms;

import java.io.File;

import nl.vpro.domain.media.Program;

/**
 * @author Michiel Meeuwissen
 * @since 1.13
 */
public class NOPSourceFetcher implements SourceFetcher {
    @Override
    public FetchResult fetch(Program program) {
        return FetchResult.succes(new File("success"));
    }
}
