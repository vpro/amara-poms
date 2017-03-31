package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.io.Files;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.AVFileFormat;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.logging.LoggerOutputStream;
import nl.vpro.util.CommandExecutor;
import nl.vpro.util.CommandExecutorImpl;

/**
 * WO_NTR_425372
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
@ToString(callSuper = true)
public class HaspFetcher extends AbstractFileFetcher {


    CommandExecutor MP4SPLIT = new CommandExecutorImpl(Config.getRequiredConfig("mp4split"));

    public HaspFetcher() {
        super(
            new File(Config.getRequiredConfig("hasp.videofile.dir")),
            "mp4",
            Config.getRequiredConfig("hasp.download.url.base"));
    }

    @Override
    public FetchResult fetch(MediaObject program) {
        String mid = program.getMid();

        File sourceDir = new File(Config.getRequiredConfig("hasp.source.dir"));
        for (Location location : program.getLocations()) {
            if (location.getAvFileFormat() == AVFileFormat.HASP) {
                URI url = URI.create(location.getProgramUrl());
                String[] path = url.getPath().split("/");
                String fileName = path[path.length - 1];

                // Consided java.nio But it is unuseable because of no way of error handling.
                // https://bugs.openjdk.java.net/browse/JDK-8039910
                // You can use Files.walkFileTree, but that will not be simple any more.
                //Files.find(Paths.get(Config.getRequiredConfig("hasp.source.dir")), 100, (p, a) -> Files.isDirectory(p))

                // this is easier
                log.info("Search files in {}", sourceDir);
                for (File f : Files.fileTreeTraverser().preOrderTraversal(sourceDir)) {
                    if (f.isDirectory() && f.getName().equals(mid)) {
                        String[] fileNames = {mid + ".ism", fileName + ".ism"};
                        for (String fn : fileNames) {
                            File smilFile = new File(f, fn);
                            if (smilFile.canRead()) {
                                try {
                                    List<Video> list = getVideos(new FileInputStream(smilFile));
                                    for (Video v : list) {
                                        File file = new File(f, v.src);
                                        if (file.canRead()) {
                                            return success(file, mid);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        }
                        log.info("No ism found in {}", f);
                        for (String name : new String[] {mid + ".mp4", fileName + "_1092.ismv"}) {
                            try {
                                File file = new File(f, name);
                                if (file.canRead()) {
                                    return success(file, mid);
                                }
                            } catch (IOException e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
        }
        return FetchResult.notable();

    }

    @Override
    protected File produce(File file, String mid) throws IOException {
        File destFile = new File(destDirectory, mid + ".mp4");
        if (file.getName().endsWith(".ismv")) {
            MP4SPLIT.execute(LoggerOutputStream.info(log), LoggerOutputStream.info(log), "-o", destFile.toString(), file.toString());
        } else {
            log.info("File {} is an mp4 already", file);
            Files.copy(file, destFile);
        }
        return destFile;

    }


    public static class Video implements  Comparable<Video> {
        public final  String src;
        public final int systemBitRate;

        public Video(String src, int systemBitRate) {
            this.src = src;
            this.systemBitRate = systemBitRate;
        }

        @Override
        public int compareTo(Video o) {
            return systemBitRate - o.systemBitRate;

        }
    }


    List<Video> getVideos(InputStream file) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/smil/body/switch/video";
        NodeList elements = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        List<Video> result = new ArrayList<>(elements.getLength());
        for (int i = 0; i < elements.getLength(); i++) {
            NamedNodeMap map = elements.item(i).getAttributes();
            result.add(new Video(map.getNamedItem("src").getTextContent(), Integer.parseInt(map.getNamedItem("systemBitrate").getTextContent())));
        }

        Collections.sort(result);
        return result;
    }
}
