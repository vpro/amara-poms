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
import nl.vpro.domain.media.update.ItemizeRequest;
import nl.vpro.domain.media.update.ItemizeResponse;

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

    private static final Duration  starttime = Duration.ZERO;
    private static final Duration endtime = Duration.ofHours(2);


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
                    ItemizeRequest request = ItemizeRequest.builder()
                        .start(starttime)
                        .stop(endtime)
                        .mid(mid)
                        .build();

                    outputFileName = requestItem(request);
                    final SSHClient sessionFactory = createSessionFactory(hostKey, ftpUrl, username, password);
                    final SFTPClient sftp = sessionFactory.newSFTPClient();
                    Instant start = Instant.now();
                    InputStream in;
                    //wait for availability on the FTP server
                    while (true) {
                        try {
                            final RemoteFile handle = sftp.open(outputFileName, EnumSet.of(OpenMode.READ));
                            in = handle.new RemoteFileInputStream();
                            break;
                        } catch (SFTPException sftpe) {
                            if (Duration.between(start, Instant.now()).compareTo(MAX_DURATION) > 0) {
                                throw new IllegalStateException("File " + outputFileName + " didn't appear in " + MAX_DURATION);
                            }
                            Thread.sleep(Duration.ofSeconds(10).toMillis());
                        }
                    }

                    log.info("File appeared {} in {}, now copying.", outputFileName, Duration.between(start, Instant.now()));
                    Config.getDbManager().addOrUpdateTask(new DatabaseTask(mid, program.getLanguage().getLanguage(), DatabaseTask.NEPSTATUS_UPLOADEDTOFTPSERVER));


                    File outputFile = produce(null, mid);
                    OutputStream output = new FileOutputStream(outputFile);
                    IOUtils.copy(in, output);
                    output.close();

                    log.info("Copied to {}", outputFile);
                    Config.getDbManager().addOrUpdateTask(new DatabaseTask(mid, program.getLanguage().getLanguage(), DatabaseTask.NEPSTATUS_COPIEDFROMFTPSERVERTOFILES));

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
        String outputFileName = response.getBody().getId();
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
