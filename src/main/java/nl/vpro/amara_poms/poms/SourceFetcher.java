package nl.vpro.amara_poms.poms;

import lombok.ToString;

import java.io.File;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
public interface SourceFetcher {


   FetchResult fetch(String mid);

    @ToString
    class FetchResult {

        public final File destination;
        public final Status status;

        public static FetchResult succes(File dest) {
            return new FetchResult(dest, Status.SUCCESS);
        }
        public static FetchResult error() {
            return new FetchResult(null, Status.ERROR);
        }
        public static FetchResult notable() {
            return new FetchResult(null, Status.NOTABLE);
        }

        private FetchResult(File destination, Status status) {
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
