/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.clientedit;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.imageio.ImageIO;
import org.primefaces.component.imagecropper.ImageCropper;
import org.primefaces.component.imagecropper.ImageCropperRenderer;
import org.primefaces.model.CroppedImage;

public class Base64CropperRenderer extends ImageCropperRenderer {

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        ImageCropper cropper = (ImageCropper) component;
        CroppedImage result = null;
        if (cropper.getConverter() != null) {
            ImageCropperResource resource = new ImageCropperResource(cropper);
            try (InputStream inputStream = resource.getInputStream(); ByteArrayOutputStream croppedOutImage = new ByteArrayOutputStream()) {
                String coords = (String) submittedValue;
                if (!isValueBlank(coords)) {
                    String[] cropCoords = coords.split("_");
                    int cutX = (int) Double.parseDouble(cropCoords[0]);
                    int cutY = (int) Double.parseDouble(cropCoords[1]);
                    int width = (int) Double.parseDouble(cropCoords[2]);
                    int height = (int) Double.parseDouble(cropCoords[3]);
                    BufferedImage image = ImageIO.read(inputStream).getSubimage(cutX, cutY, width, height);
                    ImageIO.write(image, resource.getContentType(), croppedOutImage);
                    result = new CroppedImage(cropper.getImage(), croppedOutImage.toByteArray(), cutX, cutY, width, height);
                }
            } catch (IOException e) {
                throw new ConverterException(e.getMessage(), e);
            }
        } else {
            result = (CroppedImage) super.getConvertedValue(context, component, submittedValue);
        }
        return result;
    }
}
