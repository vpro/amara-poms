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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import nl.vpro.amara_poms.Config;
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

    private String amaraUrl = Config.getRequiredConfig("nep.player.itemize.url");
    private String amaraKey = Config.getRequiredConfig("nep.player.itemize.key");


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
        String outputFileName = null;


        for (Location location : program.getLocations()) {

            if (location.getOwner() == OwnerType.AUTHORITY && location.getPlatform() == Platform.INTERNETVOD)
                //call to NEP
                outputFileName = requestItem(ItemizeRequest.builder().identifier(mid).build());

            //wait for availability on FTP
            final SSHClient sessionFactory = createSessionFactory(amaraKey, amaraUrl);
            final SFTPClient sftp = sessionFactory.newSFTPClient();
            Instant start = Instant.now();
            InputStream remoteFile;
            while(true) {
                try {
                    final RemoteFile handle = sftp.open(mid, EnumSet.of(OpenMode.READ));
                    remoteFile = handle.new RemoteFileInputStream();
                    break;
                } catch (SFTPException sftpe) {
                    if (Duration.between(start, Instant.now()).compareTo(MAX_DURATION) > 0) {
                        throw new IllegalStateException("File " + outputFileName + " didn't appear in " + MAX_DURATION);
                    }
                    Thread.sleep(Duration.ofSeconds(10).toMillis());
                }
            }

            // copy from FTP server to files. -- even lokaal een directory maken. Voor test is het die op de server
            File destFile = new File(destDirectory, mid + ".mp4");
            IOUtils.copy(remoteFile, new FileOutputStream(destFile));
            remoteFile.close();

            // verwijder van FTP
            sftp.rm(outputFileName);

            // bedenk daarvoor uri
            return success(destFile, mid);
        }
        return FetchResult.notAble();
    }



    protected String requestItem(ItemizeRequest request) {
        RequestEntity<ItemizeRequest> req = RequestEntity
            .post(URI.create(amaraUrl))
            .accept(APPLICATION_JSON_UTF8)
            .header(AUTHORIZATION, amaraKey)
            .body(request);
        ResponseEntity<ItemizeResponse> response = http.exchange(req, ItemizeResponse.class);
        if (! response.getStatusCode().is2xxSuccessful()) {
            //throw exception
        }
        String outputFileName = response.getBody().getOutput_filename();
        return outputFileName;

    }

    private static SSHClient createSessionFactory(String hostKey, String ftpUrl) throws IOException {
        final SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(hostKey);
        ssh.loadKnownHosts();
        ssh.connect(ftpUrl);
        return ssh;
    }


    @Override
    protected File produce(File file, String mid) throws IOException {
        // copier van FTP naar files. ??
        return new File("file");

    }

}
