package org.example;

import cn.hutool.core.io.FileUtil;
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
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;

import java.util.Arrays;
import java.util.List;

/**
 * @author Zero
 */
public class PdfContentTest {
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static List<String> IMG_LIST = Arrays.asList("barcode");
    public static List<String> GOU_LIST = Arrays.asList("checkBox", "radio", "linkageCheckbox");
    public static List<String> TEXT_LIST = Arrays.asList("time", "input", "select", "regular", "textarea", "time2", "amountAdd", "associationSelect", "linkageSelect", "newRegular");
    public static String MTEXT = "mtext";
    public static List<String> OTHER_LIST = Arrays.asList("sign", "seal", "img", "baseColor");

    public static void main(String[] args) {
        try {
            String inputFileName = "E:\\pdfTest\\file\\topdf\\20221104\\test.pdf";
            String outputFileName = "E:\\pdfTest\\file\\topdf\\20221104\\testsign.pdf";
            String contentFile = "E:\\pdfTest\\file\\topdf\\20221104\\test.txt";
            String fontFile = "E:\\pdfTest\\simsun.ttf";
//            String fontFile = "E:\\pdfTest\\simhei.ttf";
//            String fontFile = "E:\\pdfTest\\msyh.ttf";
            String gouImgFile = "E:\\pdfTest\\gou.png";
            int defaultFontSize = 12;
            //itext pdi ???72
            float pdi = 72f / 120f;

            PdfWriter writer = new PdfWriter(outputFileName);
            PdfReader reader = new PdfReader(inputFileName);
            PdfDocument document = new PdfDocument(reader, writer);
            PdfFont font = PdfFontFactory.createFont(fontFile, PdfEncodings.IDENTITY_H);

            Document doc = new Document(document);
            PdfAcroForm form = PdfAcroForm.getAcroForm(document, true);
            int pages = document.getNumberOfPages();

            String contentJson = FileUtil.readUtf8String(contentFile);
            JsonNode jsonNode = objectMapper.readTree(contentJson);
            if (!jsonNode.hasNonNull("fileAttribute") || !jsonNode.get("fileAttribute").hasNonNull("attribute")) {
                System.out.println("fileAttribute|attribute ?????????");
                return;
            }

            JsonNode attribute = jsonNode.get("fileAttribute").get("attribute");
            JsonNode pdfContent = attribute.get(0);
            if (pdfContent.size() > pages) {
                System.out.println("???????????? " + pdfContent.size() + " ?????????????????? " + pages);
                return;
            }
            for (int i = 0; i < pdfContent.size(); i++) {
                int pageNum = i + 1;
                PdfPage page = document.getPage(pageNum);
                PdfCanvas canvas = new PdfCanvas(page);

                JsonNode pageContent = pdfContent.get(i);
                if (!pageContent.hasNonNull("blocks")) {
                    System.out.println("blocks ?????????, pageNum: " + pageNum);
                    continue;
                }
                float height = pageContent.get("height").asInt() * pdi;
                float width = pageContent.get("width").asInt() * pdi;
                JsonNode blocks = pageContent.get("blocks");
                for (int j = 0; j < blocks.size(); j++) {
                    JsonNode blockContent = blocks.get(j);
                    if (blockContent.hasNonNull("hide") && blockContent.get("hide").asInt() == 1) {
                        continue;   //???????????????
                    }
                    if (blockContent.hasNonNull("empty") && blockContent.get("empty").asInt() == 1) {
                        continue;   //???????????????
                    }
                    if (!blockContent.hasNonNull("type") || !blockContent.hasNonNull("x") || !blockContent.hasNonNull("y")) {
                        System.out.println("type|x|y ???????????????, blocks???" + j + ", pageNum: " + pageNum);
                        continue;
                    }

                    String type = blockContent.get("type").asText();
                    if (type.isEmpty()) {
                        continue;   //type???????????????
                    }
                    if (OTHER_LIST.contains(type)) {
                        continue;   //?????????????????????
                    }

                    float x = blockContent.get("x").asInt() * pdi;
                    float y = height - blockContent.get("y").asInt() * pdi;

                    if (GOU_LIST.contains(type)) {
                        if (!blockContent.hasNonNull("text")) {
                            System.out.println("text???????????????, type: " + type + ", blocks???" + j + ", pageNum: " + pageNum);
                            continue;
                        }
                        String text = blockContent.get("text").asText();
                        if (!"???".equals(text)){
                            continue;
                        }
                        ImageData img = ImageDataFactory.create(gouImgFile);
                        float w = img.getWidth() * pdi;
                        float h = img.getHeight() * pdi;
                        canvas.addImageFittedIntoRectangle(img, new Rectangle(x, y - h, w, h), false);
                    } else if (IMG_LIST.contains(type)) {
                        if (!blockContent.hasNonNull("src") || !blockContent.hasNonNull("w") || !blockContent.hasNonNull("h")) {
                            System.out.println("src|w|h ???????????????, type: " + type + ", blocks???" + j + ", pageNum: " + pageNum);
                            continue;
                        }
                        String src = blockContent.get("src").asText();
                        float w = blockContent.get("w").asInt() * pdi;
                        float h = blockContent.get("h").asInt() * pdi;
                        ImageData img = ImageDataFactory.create(src);
                        canvas.addImageFittedIntoRectangle(img, new Rectangle(x, y - h, w, h), false);
                    } else if (TEXT_LIST.contains(type)) {
                        if (!blockContent.hasNonNull("text")) {
                            System.out.println("text???????????????, type: " + type + ", blocks???" + j + ", pageNum: " + pageNum);
                            continue;
                        }
                        String text = blockContent.get("text").asText();
                        int size = blockContent.hasNonNull("size") ? blockContent.get("size").asInt(defaultFontSize) : defaultFontSize;
                        float w = width - x;
                        float h = size + 2;
                        Paragraph paragraph = new Paragraph(new Text(text).setFont(font).setFontSize(size));
                        paragraph.setFixedPosition(pageNum, x, y-h , w);
//                        pa1.setFontColor(new DeviceRgb(colorArr[0], colorArr[1], colorArr[2]));
//                        pa1.setBackgroundColor(new DeviceRgb(0, 0, 0));
                        if (blockContent.hasNonNull("B")&&blockContent.get("B").asInt()==1){
                            paragraph.setBold();    //??????
                        }
                        if (blockContent.hasNonNull("U")&&blockContent.get("U").asInt()==1) {
                            paragraph.setUnderline();   //?????????
                        }
                        if (blockContent.hasNonNull("I")&&blockContent.get("I").asInt()==1) {
                            paragraph.setItalic();      //??????
                        }
                        doc.add(paragraph);
                    } else if (MTEXT.equals(type)) {
                        if (!blockContent.hasNonNull("text") || !blockContent.hasNonNull("w") || !blockContent.hasNonNull("h")) {
                            System.out.println("text|w|h???????????????, type: " + type + ", blocks???" + j + ", pageNum: " + pageNum);
                            continue;
                        }
                        String text = blockContent.get("text").asText();
                        int size = blockContent.hasNonNull("size") ? blockContent.get("size").asInt(defaultFontSize) : defaultFontSize;
                        float w = blockContent.get("w").asInt() * pdi;
                        float h = blockContent.get("h").asInt() * pdi;
                        PdfTextFormField field = PdfTextFormField.createMultilineText(document, new Rectangle(x, y - h, w, h), "mtext_" + i + "_" + j, text, font, size);
                        form.addField(field, page);
                    } else {
                        System.out.println("??????Type, type: " + type + ", blocks???" + j + ", pageNum: " + pageNum);
                    }
                }
            }
            form.flattenFields();
            document.close();

            System.out.println("==========SUCCESS=====");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static int[] hexToRGB(String hexStr){
        if(hexStr != null && !"".equals(hexStr) && hexStr.length() == 7){
            int[] rgb = new int[3];
            rgb[0] = Integer.valueOf(hexStr.substring( 1, 3 ), 16);
            rgb[1] = Integer.valueOf(hexStr.substring( 3, 5 ), 16);
            rgb[2] = Integer.valueOf(hexStr.substring( 5, 7 ), 16);
            return rgb;
        }
        return new int[]{0,0,0};
    }
}
