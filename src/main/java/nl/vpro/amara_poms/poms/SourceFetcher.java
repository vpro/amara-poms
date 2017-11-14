package nl.vpro.amara_poms.poms;

import lombok.ToString;

import java.io.IOException;
import java.net.URI;

import nl.vpro.domain.media.MediaObject;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
public interface SourceFetcher {


    FetchResult fetch(MediaObject program) throws IOException, InterruptedException;

    @ToString
    class FetchResult {

        public final URI destination;
        public final Status status;

        public static FetchResult succes(URI dest) {
            return new FetchResult(dest, Status.SUCCESS);
        }
        public static FetchResult error() {
            return new FetchResult(null, Status.ERROR);
        }
        public static FetchResult notAble() {
            return new FetchResult(null, Status.NOTABLE);
        }

        private FetchResult(URI destination, Status status) {
            this.destination = destination;
            this.status = status;
        }
    }
    enum Status {
        SUCCESS,
        ERROR,
        NOTABLE
    }
}
