/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public final class ClientPlaceholderImage {

    private static final String IMAGE_PATH = "/image/no-icon-client.png";

    /** 1x1 transparent PNG when classpath resource is missing. */
    private static final byte[] FALLBACK_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    private ClientPlaceholderImage() {
    }

    public static byte[] load() {
        try (InputStream in = ClientPlaceholderImage.class.getResourceAsStream(IMAGE_PATH)) {
            if (in != null) {
                return readStream(in);
            }
        } catch (IOException ignored) {
            // use fallback
        }
        return FALLBACK_PNG;
    }

    private static byte[] readStream(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }
}
