package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.*;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.nep.domain.NEPItemizeResponse;
import nl.vpro.nep.service.NEPDownloadService;
import nl.vpro.nep.service.NEPItemizeService;
import nl.vpro.nep.service.impl.NEPItemizeServiceImpl;
import nl.vpro.nep.service.impl.NEPScpDownloadServiceImpl;

/**

 */
@Slf4j
@ToString(callSuper = true)
public class NEPFetcher extends AbstractFileFetcher {

    private final NEPItemizeService nepItemizeService;
    private final NEPDownloadService nepDownloadService;

    private final String nepUrl = Config.getRequiredConfig("nep.player.itemize.url");
    private final String nepKey = Config.getRequiredConfig("nep.player.itemize.key");

    private final int nepBitRate = Config.getRequiredConfigAsInt("bitrate");

    private final String ftpUrl = Config.getRequiredConfig("nep.sftp.url");
    private final String username = Config.getRequiredConfig("nep.sftp.username");
    private final String password = Config.getRequiredConfig("nep.sftp.password");
    private final String hostKey = Config.getRequiredConfig("nep.sftp.hostkey");



    private static final Duration starttime = Duration.ZERO;
    private static final Duration endtime = Duration.ofHours(2);


    public NEPFetcher() {
        super(
            new File(Config.getRequiredConfig("nep.videofile.dir")),
            "mp4",
            Config.getRequiredConfig("nep.download.url.base"));
        nepItemizeService = new NEPItemizeServiceImpl(nepUrl, nepKey);

        nepDownloadService = new NEPScpDownloadServiceImpl(
            ftpUrl,
            username,
            password,
            hostKey,
            false,
            Arrays.asList("/local/bin/scp", "/usr/bin/scp"),
            Arrays.asList("/usr/bin/sshpass", "/opt/local/bin/sshpass"),
            5, false
        );
    }


    @Override
    public FetchResult fetch(MediaObject program) {
        Duration MAX_DURATION = Duration.ofMinutes(10);
        String mid = program.getMid();
        String outputFileName;

        for (Location location : program.getLocations()) {

            if (location.getOwner() == OwnerType.AUTHORITY && location.getPlatform() == Platform.INTERNETVOD) {
                try {
                    //call to NEP
                    outputFileName = requestItem(mid);

                    File outputFile = produce(null, mid);
                    final Instant start = Instant.now();
                    log.info("Writing to {}", outputFile);
                    nepDownloadService.download(
                        "",
                        outputFileName,
                        () -> {
                        try {
                            return new FileOutputStream(outputFile);
                        } catch (FileNotFoundException fne) {
                            throw new RuntimeException(fne);
                        }
                        }
                        , MAX_DURATION, (mf) -> {
                            log.info("File  {} ({} bytes) appeared in {}, now copying to {}", outputFileName, mf.getSize(), Duration.between(start, Instant.now()), outputFile);
                            return NEPDownloadService.Proceed.TRUE;
                        }
                    );
                    return success(outputFile, mid);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return FetchResult.error();
                }
            }
        }
        return FetchResult.notAble();
    }

    protected String requestItem(String mid) {
        NEPItemizeResponse response = nepItemizeService.itemizeMid(mid, starttime,  endtime, nepBitRate);
        String outputFileName = response.getOutput_filename();
        return outputFileName;
    }

    @Override
    protected File produce(File file, String mid) {
        return new File(destDirectory, mid + ".mp4");
    }

}
