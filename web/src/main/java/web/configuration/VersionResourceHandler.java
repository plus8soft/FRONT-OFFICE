/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.application.ResourceWrapper;

public class VersionResourceHandler extends ResourceHandlerWrapper {

    private static final ConcurrentHashMap<String, String> DIGEST_INDEX = new ConcurrentHashMap<>();

    private ResourceHandler wrapped;

    public VersionResourceHandler(ResourceHandler wrapped) {
        this.wrapped = wrapped;
    }

    private static String digest(Resource resource) {
        String digest = DIGEST_INDEX.get(resource.getRequestPath());
        if (digest == null) {
            try (InputStream inputStream = resource.getInputStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                int read;
                byte[] data = new byte[1024];
                while ((read = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, read);
                }
                DIGEST_INDEX.put(resource.getRequestPath(),
                                 digest = new BigInteger(1, MessageDigest.getInstance("MD5").digest(outputStream.toByteArray())).toString(16));
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException();
            }
        }
        return digest;
    }

    @Override
    public Resource createResource(String resourceName) {
        return createResource(resourceName, null, null);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName) {
        return createResource(resourceName, libraryName, null);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName, String contentType) {
        final Resource resource = super.createResource(resourceName, libraryName, contentType);
        if (resource == null) {
            return null;
        }
        return new ResourceWrapper() {
            @Override
            public String getRequestPath() {
                return super.getRequestPath() + (super.getRequestPath().contains("?") ? "&v=" : "?v=") + digest(resource);
            }

            @Override
            public Resource getWrapped() {
                return resource;
            }
        };
    }

    @Override
    public ResourceHandler getWrapped() {
        return wrapped;
    }
}
