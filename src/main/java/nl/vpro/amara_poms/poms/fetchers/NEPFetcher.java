package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;

import org.apache.commons.io.IOUtils;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.database.task.DatabaseTask;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.Platform;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.nep.ItemizeRequest;
import nl.vpro.nep.ItemizeResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

/**

 */
@Slf4j
@ToString(callSuper = true)
public class NEPFetcher extends AbstractFileFetcher {

    private final RestTemplate http = new RestTemplate();
    private String nepUrl = Config.getRequiredConfig("nep.player.itemize.url");
    private String nepKey = Config.getRequiredConfig("nep.player.itemize.key");

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
    }


    @Override
    public FetchResult fetch(MediaObject program) throws IOException, InterruptedException {
        Duration MAX_DURATION = Duration.ofMinutes(10);
        String mid = program.getMid();
        String outputFileName;

        for (Location location : program.getLocations()) {

            if (location.getOwner() == OwnerType.AUTHORITY && location.getPlatform() == Platform.INTERNETVOD) {
                File file;
                try {
                    //call to NEP
                    ItemizeRequest request = new ItemizeRequest(mid, starttime, endtime);
                    outputFileName = requestItem(request);
                    final SSHClient sessionFactory = createSessionFactory(hostKey, ftpUrl, username, password);
                    final SFTPClient sftp = sessionFactory.newSFTPClient();
                    Instant start = Instant.now();
                    InputStream in;
                    long remoteLength = -1;
                    //wait for availability on the FTP server
                    while (true) {
                        try {
                            final RemoteFile handle = sftp.open(outputFileName, EnumSet.of(OpenMode.READ));
                            remoteLength = handle.length();
                            in = handle.new RemoteFileInputStream();

                            break;
                        } catch (SFTPException sftpe) {
                            if (Duration.between(start, Instant.now()).compareTo(MAX_DURATION) > 0) {
                                throw new IllegalStateException("File " + outputFileName + " didn't appear in " + MAX_DURATION);
                            }
                            Thread.sleep(Duration.ofSeconds(10).toMillis());
                        }
                    }
                    File outputFile = produce(null, mid);

                    if (! outputFile.exists() || outputFile.length() != remoteLength) {
                        if (outputFile.exists()) {
                            log.info("Target file {} already exists, but doesn't have correct size {} != {}", outputFile.length(), remoteLength);
                        }
                        log.info("File  {} ({} bytes) appeared in {}, now copying to {}", outputFileName, remoteLength, Duration.between(start, Instant.now()), outputFile);
                        Instant copyStart = Instant.now();

                        OutputStream output = new FileOutputStream(outputFile);
                        IOUtils.copyLarge(in, output);
                        output.close();

                        Duration duration = Duration.between(copyStart, Instant.now());
                        log.info("Copied to {} in {} ({} MiB/s)", outputFile, duration,
                            (1000000L * outputFile.length() / (duration.toMillis() * 1024 * 1024)) / 1000f);
                    } else {
                        log.info("File {} already exists", outputFile);
                    }
                    try {
                        sftp.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    try {
                        sessionFactory.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    return success(outputFile, mid);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return FetchResult.error();
                }
            }
        }
        return FetchResult.notAble();
    }

    protected String requestItem(ItemizeRequest request) {
        RequestEntity<ItemizeRequest> req = RequestEntity
            .post(URI.create(nepUrl))
            .accept(APPLICATION_JSON_UTF8)
            .header(AUTHORIZATION, nepKey)
            .body(request);
        ResponseEntity<ItemizeResponse> response = http.exchange(req, ItemizeResponse.class);
        if (! response.getStatusCode().is2xxSuccessful()) {
            //throw exception
        }
        String outputFileName = response.getBody().getOutput_filename();
        return outputFileName;
    }

    private static SSHClient createSessionFactory(String hostKey, String ftpUrl, String username, String password) throws IOException {
        final SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(hostKey);
        ssh.loadKnownHosts();
        ssh.connect(ftpUrl);
        ssh.authPassword(username, password);
        return ssh;
    }

    @Override
    protected File produce(File file, String mid) throws IOException {
        return new File(destDirectory, mid + ".mp4");
    }

}
