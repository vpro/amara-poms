package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.Platform;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.nep.domain.NEPItemizeRequest;
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

    private String nepUrl = Config.getRequiredConfig("nep.player.itemize.url");
    private String nepKey = Config.getRequiredConfig("nep.player.itemize.key");

    private int nepBitRate = Config.getRequiredConfigAsInt("bitrate");

    private String ftpUrl = Config.getRequiredConfig("nep.sftp.url");
    private String username = Config.getRequiredConfig("nep.sftp.username");
    private String password = Config.getRequiredConfig("nep.sftp.password");
    private String hostKey = Config.getRequiredConfig("nep.sftp.hostkey");



    private static final String starttime = "00:00:00.000";
    private static final String endtime = "02:00:00.000";


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
            Arrays.asList("/local/bin/scp", "/usr/bin/scp"),
        Arrays.asList("/usr/bin/sshpass", "/opt/local/bin/sshpass"));
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
                    NEPItemizeRequest request = NEPItemizeRequest.builder()
                        .identifier(mid)
                        .starttime(starttime)
                        .endtime(endtime)
                        .max_bitrate(nepBitRate)
                        .build();
                    outputFileName = requestItem(request);

                    File outputFile = produce(null, mid);
                    final Instant start = Instant.now();
                    log.info("Writing to {}", outputFile);
                    nepDownloadService.download(outputFileName, () -> {
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

    protected String requestItem(NEPItemizeRequest request) {
        NEPItemizeResponse response = nepItemizeService.itemize(request);
        String outputFileName = response.getOutput_filename();
        return outputFileName;
    }

    @Override
    protected File produce(File file, String mid) {
        return new File(destDirectory, mid + ".mp4");
    }

}
