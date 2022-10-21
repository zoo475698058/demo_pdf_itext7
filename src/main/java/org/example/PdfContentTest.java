package org.example;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfTextFormField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Zero
 */
public class PdfContentTest {
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static List<String> imgTypeList = Arrays.asList("sign", "seal", "barcode", "img");
    public static List<String> gouTypeList = Arrays.asList("checkBox", "radio", "linkageCheckbox");

    public static void main(String[] args) {
        try {
            String inputFileName = "E:\\pdfTest\\168447_sf.pdf";
            String outputFileName = "E:\\pdfTest\\168447.pdf";
            String contentFile = "E:\\pdfTest\\168447.txt";
            String fontFile = "E:\\pdfTest\\simsun.ttf";
            String gouImgFile = "E:\\pdfTest\\gou.png";
            int defaultFontSize = 12;

            PdfWriter writer = new PdfWriter(outputFileName);
            PdfReader reader = new PdfReader(inputFileName);
            PdfDocument document = new PdfDocument(reader, writer);
            PdfFont font = PdfFontFactory.createFont(fontFile, PdfEncodings.IDENTITY_H);

            int pages = document.getNumberOfPages();
            PdfAcroForm form = PdfAcroForm.getAcroForm(document, true);

            String contentJson = FileUtil.readUtf8String(contentFile);
            JsonNode jsonNode = objectMapper.readTree(contentJson);
            if (!jsonNode.hasNonNull("fileAttribute") || !jsonNode.get("fileAttribute").hasNonNull("attribute")) {
                System.out.println("fileAttribute|attribute 不存在");
                return;
            }

            JsonNode attribute = jsonNode.get("fileAttribute").get("attribute");
            JsonNode pdfContent = attribute.get(0);
            for (int i = 0; i < pdfContent.size(); i++) {
                int pageNum = i + 1;
                PdfPage page = document.getPage(pageNum);
                PdfCanvas canvas = new PdfCanvas(page);

                JsonNode pageContent = pdfContent.get(i);
                if (!pageContent.hasNonNull("blocks")) {
                    System.out.println("blocks 不存在, pageNum: " + pageNum);
                    continue;
                }
                int height = pageContent.get("height").asInt();
                int width = pageContent.get("width").asInt();
                JsonNode blocks = pageContent.get("blocks");
                for (int j = 0; j < blocks.size(); j++) {
                    JsonNode blockContent = blocks.get(j);
                    if (!blockContent.hasNonNull("type") || !blockContent.hasNonNull("x") || !blockContent.hasNonNull("y")) {
                        System.out.println("type|x|y 属性不存在, blocks：" + j + ", pageNum: " + pageNum);
                        continue;
                    }

                    String type = blockContent.get("type").asText();
                    if (type.isEmpty()){
                        continue;   //type为空的不要
                    }
                    int x = blockContent.get("x").asInt();
                    int y = height - blockContent.get("y").asInt();

                    if (blockContent.hasNonNull("hide") && blockContent.get("hide").equals(1)){
                        continue;   //隐藏的不要
                    }
                    if (blockContent.hasNonNull("empty") && blockContent.get("empty").equals(1)){
                        continue;   //留白的不要
                    }

                    if (gouTypeList.contains(type)) {
                        ImageData img = ImageDataFactory.create(gouImgFile);
                        float w = img.getWidth();
                        float h = img.getHeight();
                        canvas.addImageFittedIntoRectangle(img, new Rectangle(x, y, w, h), false);
                    } else if (imgTypeList.contains(type)) {
                        if (!blockContent.hasNonNull("src") || !blockContent.hasNonNull("w") || !blockContent.hasNonNull("h")) {
                            System.out.println("src|w|h 属性不存在, type: " + type + ", blocks：" + j + ", pageNum: " + pageNum);
                            continue;
                        }
                        String src = blockContent.get("src").asText();
                        int w = blockContent.get("w").asInt();
                        int h = blockContent.get("h").asInt();
                        ImageData img = ImageDataFactory.create(src);
                        canvas.addImageFittedIntoRectangle(img, new Rectangle(x, y, w, h), false);
                    } else {
                        if (!blockContent.hasNonNull("text")) {
                            System.out.println("text属性不存在, type: " + type + ", blocks：" + j + ", pageNum: " + pageNum);
                            continue;
                        }
                        String text = blockContent.get("text").asText();
                        int size = blockContent.hasNonNull("size") ? blockContent.get("size").asInt() : defaultFontSize;
                        int w = blockContent.hasNonNull("w") ? blockContent.get("w").asInt() : width - x;
                        int h = blockContent.hasNonNull("h") ? blockContent.get("h").asInt() : size + 2;
                        PdfTextFormField field = PdfTextFormField.createMultilineText(document, new Rectangle(x, y, w, h), "text_" + i + "_" + j, text, font, size);
                        form.addField(field, page);
                    }

                }

            }

//            form.flattenFields();
            document.close();

            System.out.println("==========SUCCESS=====");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
