/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.clientedit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import javax.faces.application.Resource;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.imagecropper.ImageCropper;
import org.primefaces.util.Base64;

@Getter
@Setter
public class ImageCropperResource extends Resource {

    private ImageCropper cropper;

    public ImageCropperResource(ImageCropper cropper) {
        this.cropper = cropper;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (cropper != null) {
            return new ByteArrayInputStream(Base64.decode(cropper.getImage().replaceFirst("data:image/.*;base64,", "").getBytes()));
        }
        return null;
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        return null;
    }

    @Override
    public String getRequestPath() {
        return null;
    }

    @Override
    public URL getURL() {
        return null;
    }

    @Override
    public String getContentType() {
        if (cropper != null && cropper.getImage() != null) {
            return cropper.getImage().substring(cropper.getImage().indexOf("/") + 1, cropper.getImage().indexOf(";"));
        }
        return null;
    }

    @Override
    public boolean userAgentNeedsUpdate(FacesContext context) {
        return false;
    }
}
