package my.sample;

import com.google.common.collect.ImmutableMap;
import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.Store;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class SharedMethods {

    public static final URL createURL(String urlString) throws MaterialstoreException {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new MaterialstoreException(e);
        }
    }

    public static final byte[] downloadUrl(URL toDownload) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            byte[] chunk = new byte[4096];
            int bytesRead;
            InputStream stream = toDownload.openStream();
            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return outputStream.toByteArray();
    }

    public static final void write3images(Store store, JobName jn, JobTimestamp jt)          // (16)
            throws MaterialstoreException {
        String prefix =
                "https://kazurayam.github.io/materialstore/images/tutorial/";
        // Apple
        URL url1 = SharedMethods.createURL(prefix + "03_apple.png");
        store.write(jn, jt, FileType.PNG,
                Metadata.builder(url1)
                        .putAll(ImmutableMap.of(
                                "step", "01",
                                "label", "red apple"))
                        .build(),
                SharedMethods.downloadUrl(url1));
        // Mikan
        URL url2 = SharedMethods.createURL(prefix + "04_mikan.png");
        store.write(jn, jt, FileType.PNG,
                Metadata.builder(url2)
                        .putAll(ImmutableMap.of(
                                "step", "02",
                                "label", "mikan"))
                        .build(),
                SharedMethods.downloadUrl(url2));
        // Money
        URL url3 = SharedMethods.createURL(prefix + "05_money.png");
        store.write(jn, jt, FileType.PNG,
                Metadata.builder(url3)
                        .putAll(ImmutableMap.of(
                                "step", "03",
                                "label", "money"))
                        .build(),
                SharedMethods.downloadUrl(url3));;
    }

}
