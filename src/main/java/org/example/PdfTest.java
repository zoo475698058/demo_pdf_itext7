package org.example;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itextpdf.commons.utils.Base64;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.PdfSigFieldLock;
import com.itextpdf.forms.fields.*;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.util.StreamUtil;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.annot.PdfTextAnnotation;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextChunkLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextChunk;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.signatures.*;
import com.sun.corba.se.spi.orbutil.fsm.FSMTest;
import org.bouncycastle.its.ITSValidityPeriod;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * pdf测试
 *
 * @author Administrator
 */
public class PdfTest {
    public static void main(String[] args) {
//        repalceData();
//        addsign();
//        getSign();
//        signMd5();
        pdfForm();
//        Paragraph();

    }

    private static void Paragraph() {
        try {
            String inputFileName = "E:\\pdfTest\\168414_zf.pdf";
            String outputFileName = "E:\\pdfTest\\168414.pdf";
            String contentFile = "E:\\pdfTest\\168414.txt";
            String gouFile = "E:\\pdftest\\gou.png";
            String fontFile = "E:\\pdfTest\\simsun.ttf";

            PdfWriter writer = new PdfWriter(outputFileName);
            PdfReader reader = new PdfReader(inputFileName);
            PdfDocument document = new PdfDocument(reader, writer);

            PdfFont font = PdfFontFactory.createFont(fontFile, PdfEncodings.IDENTITY_H);
            float pdi = 72f / 120f;
            int page = 2;
            float llx = 701*pdi;
            float lly = (1403-1279)*pdi;
            float width = 100;
            int size = 12;

            String colorHex = "#2FA2DC";
            int[] colorArr = hexToRGB(colorHex);

            Document doc = new Document(document);
//            PdfCanvas canvas = new PdfCanvas(document, page);
//            Canvas cav = new Canvas(canvas, new Rectangle(llx, lly-size , width, size));

            //文本型
            Paragraph pa1 = new Paragraph(new Text("多行文本框与通常的文本框相比是会换行的，普通文本框如果添加的内容超出单行能显示的内容，则此字段中的文本将会只显示一部分，其余部分被包裹").setFont(font).setFontSize(size));
            pa1.setFixedPosition(page, llx, lly-size , width);
            pa1.setFontColor(new DeviceRgb(colorArr[0], colorArr[1], colorArr[2]));
            pa1.setBackgroundColor(new DeviceRgb(0, 0, 0));
            pa1.setUnderline();
            pa1.setBold();
            pa1.setItalic();
            doc.add(pa1);
//            cav.add(pa1);

            //勾选图片
            PdfCanvas canvas1 = new PdfCanvas(document.getPage(1));
            canvas1.addImageFittedIntoRectangle(ImageDataFactory.create(gouFile), new Rectangle(200, 602, 30, 30), false);


            document.close();
            System.out.println("===============PDF导出成功=============" + LocalDateTime.now());
        } catch (Exception e) {
            System.out.println("===============PDF导出失败=============");
            e.printStackTrace();
        }
    }

    private static void pdfForm() {
        try {
            String inputFileName = "E:\\pdftest\\tem.pdf";
            String outputFileName = "E:\\pdftest\\tem_2.pdf";
            String imageFile = "E:\\pdftest\\seal2.png";
            String gouFile = "E:\\pdftest\\gou.png";
            String fontFile = "E:\\pdfTest\\simsun.ttf";

            PdfWriter writer = new PdfWriter(outputFileName);
            PdfReader reader = new PdfReader(inputFileName);
            PdfDocument document = new PdfDocument(reader, writer);

            PdfFont font = PdfFontFactory.createFont(fontFile, PdfEncodings.IDENTITY_H);
            PdfPage page = document.getPage(1);
            PdfAcroForm form = PdfAcroForm.getAcroForm(document, true);

            String colorHex = "#2FA2DC";
            int[] colorArr = hexToRGB(colorHex);
            //文本框
            PdfTextFormField text = PdfTextFormField.createText(document, new Rectangle(95, 742, 200, 26), "jf_czf", "我是甲方啊 甲方啊 壹甲方啊 壹甲方啊 壹甲方啊 壹甲方啊 壹壹仟伍佰元整%啊", font, 24);
            //字体大小自适应
            text.setFontSizeAutoScale();
            //字体色
            text.setColor(new DeviceRgb(colorArr[0], colorArr[1], colorArr[2]));
            //背景色
            text.setBackgroundColor(new DeviceRgb(0, 0, 0));

            form.addField(text, page);

            PdfTextFormField text2 = PdfTextFormField.createText(document, new Rectangle(328, 742, 200, 14), "sfzh_jf", "430382199412341234", font, 12);
            form.addField(text2, page);

            //多行文本框
            String ttt = "多行文本框与通常的文本框相比是会换行的，普通文本框如果添加的内容超出单行能显示的内容，则此字段中的文本将会只显示一部分，其余部分被包裹。";
            PdfTextFormField infoField = PdfTextFormField.createMultilineText(document, new Rectangle(23, 270, 520, 65), "info", ttt, font, 12);
            form.addField(infoField, document.getPage(2));

            //勾选框
            PdfButtonFormField radio2 = PdfButtonFormField.createCheckBox(document, new Rectangle(225, 602, 14, 12), "bsmp", "Yes", PdfFormField.TYPE_CHECK);
            form.addField(radio2, page);

            //勾选图片
            PdfCanvas canvas1 = new PdfCanvas(document.getPage(1));
            canvas1.addImageFittedIntoRectangle(ImageDataFactory.create(gouFile), new Rectangle(200, 602, 30, 30), false);

            //图片
            PdfCanvas canvas2 = new PdfCanvas(document.getPage(2));
            canvas2.addImageFittedIntoRectangle(ImageDataFactory.create(imageFile), new Rectangle(115, 95, 89, 89), false);

            //表单扁平化
            form.flattenFields();
            document.close();
            System.out.println("===============PDF导出成功=============" + LocalDateTime.now());
        } catch (Exception e) {
            System.out.println("===============PDF导出失败=============");
            e.printStackTrace();
        }
    }

    /**
     * 颜色 16进制转RGB格式
     *  #000000 转为 [0,0,0]
     * @param hexStr
     * @return
     */
    private static int[] hexToRGB(String hexStr) {
        if (StrUtil.isNotEmpty(hexStr) && hexStr.length() == 7) {
            int[] rgb = new int[3];
            rgb[0] = Integer.valueOf(hexStr.substring(1, 3), 16);
            rgb[1] = Integer.valueOf(hexStr.substring(3, 5), 16);
            rgb[2] = Integer.valueOf(hexStr.substring(5, 7), 16);
            return rgb;
        }
        return new int[]{0, 0, 0};
    }

    private static void getSign() {
        try {
            String inputFileName = "E:\\pdftest\\template_new4.pdf";
            PdfReader reader = new PdfReader(inputFileName);
            PdfDocument document = new PdfDocument(reader);

            SignatureUtil util = new SignatureUtil(document);
            List<String> signList = util.getSignatureNames();
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            for (String signName : signList) {
                System.out.println("signName：" + signName);
                System.out.println("签名覆盖全文档：" + util.signatureCoversWholeDocument(signName));
                PdfSignature signature = util.getSignature(signName);
                System.out.println("签名原因：" + signature.getReason());
                System.out.println("签名位置：" + signature.getLocation());

                PdfPKCS7 pkcs7 = util.readSignatureData(signName, "BC");
                X509Certificate certificate = pkcs7.getSigningCertificate();
                System.out.println("证书主题DN：" + certificate.getSubjectDN().getName());
                System.out.println("证书序列号：" + certificate.getSerialNumber().toString(16));
                System.out.println("证书有效开始时间：" + DateUtil.formatDateTime(certificate.getNotBefore()));
                System.out.println("证书有效截止时间：" + DateUtil.formatDateTime(certificate.getNotAfter()));
                System.out.println("证书颁发者：" + certificate.getIssuerDN().getName());
                System.out.println("证书格式：" + certificate.getPublicKey().getFormat());
                System.out.println("签名日期时间：" + DateUtil.formatDateTime(pkcs7.getSignDate().getTime()));
                System.out.println("签名有效性：" + pkcs7.verifySignatureIntegrityAndAuthenticity());
                System.out.println("-----------------------------------------------------");
            }

            System.out.println("===============PDF获取签名成功=============" + LocalDateTime.now());
        } catch (Exception e) {
            System.out.println("===============PDF获取签名失败=============");
            e.printStackTrace();
        }


//        String filePath = (String) map.get("filePath");
//        cfca.sadk.com.itextpdf.kernel.pdf.PdfReader reader = new cfca.sadk.com.itextpdf.kernel.pdf.PdfReader(filePath);
//        PdfDocument document = new PdfDocument(reader);
//        SignatureUtil util = new SignatureUtil(document);
//        List<String> signList = util.getSignatureNames();
//        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//
//        List<HashMap<String, Object>> list = new ArrayList<>();
//        for (String signName : signList) {
//            PdfSignature signature = util.getSignature(signName);
//            PdfPKCS7 pkcs7 = util.verifySignature(signName, "BC");
//            X509Certificate certificate = pkcs7.getSigningCertificate();
//
//            HashMap<String, Object> resultMap = new HashMap<>();
//            resultMap.put("signName", signName);
//            resultMap.put("covers", util.signatureCoversWholeDocument(signName));
//            resultMap.put("reason", signature.getReason());
//            resultMap.put("location", signature.getLocation());
//            resultMap.put("subjectDN_name", certificate.getSubjectDN().getName());
//            resultMap.put("serialNumber", certificate.getSerialNumber().toString(16));
//            resultMap.put("certificateBefore", DateUtil.formatDateTime(certificate.getNotBefore()));
//            resultMap.put("certificateAfter", DateUtil.formatDateTime(certificate.getNotAfter()));
//            resultMap.put("IssuerDN_name", certificate.getIssuerDN().getName());
//            resultMap.put("certificate_format", certificate.getPublicKey().getFormat());
//            resultMap.put("signDateTime", DateUtil.formatDateTime(pkcs7.getSignDate().getTime()));
//            resultMap.put("verify", pkcs7.verify());
//            list.add(resultMap);
//        }
//
//        return R.data(list);





//        cfca.sadk.com.itextpdf.kernel.pdf.PdfReader reader = new cfca.sadk.com.itextpdf.kernel.pdf.PdfReader(pdfBytes);
//        PdfDocument document = new PdfDocument(reader);
//        PdfCanvas canvas = new PdfCanvas(document, 2);
//        canvas.addImage(image, new Rectangle(200, 602, 30, 30), false);

    }

    private static void signMd5() {
        try {
            PdfReader reader = new PdfReader("E:\\pdfTest\\1.pdf");
            PdfWriter writer = new PdfWriter("E:\\pdfTest\\11.pdf");
            PdfDocument document = new PdfDocument(reader, writer);
            document.close();

            FileUtil.copy("E:\\pdfTest\\11.pdf", "E:\\pdfTest\\22.pdf", true);

            System.out.println(SecureUtil.md5(new File("E:\\pdfTest\\1.pdf")));
            System.out.println(SecureUtil.md5(new File("E:\\pdfTest\\11.pdf")));
            System.out.println(SecureUtil.md5(new File("E:\\pdfTest\\22.pdf")));
        } catch (Exception e) {
            System.out.println("===============失败=============");
            e.printStackTrace();
        }
    }

    private static void addsign() {
        try {
            String inputFileName = "E:\\pdftest\\template_new.pdf";
            String outputFileName = "E:\\pdftest\\template_new2.pdf";
            String privateKey = "E:\\pdftest\\tem.pfx";
            char[] pass = "123456".toCharArray();
            String imageFile = "E:\\pdftest\\tem.png";

            KeyStore ks = KeyStore.getInstance("pkcs12");
            FileInputStream fis = new FileInputStream(privateKey);
            ks.load(fis, pass);
            String alias = ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, pass);
            Certificate[] chain = ks.getCertificateChain(alias);

            PdfReader reader = new PdfReader(inputFileName);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(outputFileName), new StampingProperties());

            ImageData image = ImageDataFactory.create(imageFile);
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setSignatureGraphic(image).setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
            appearance.setReason("原因").setLocation("位置");
            signer.setFieldName("Signature1");

            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            IExternalSignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();

            signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);

            System.out.println("===============PDF签名成功=============" + LocalDateTime.now());
        } catch (Exception e) {
            System.out.println("===============PDF签名失败=============");
            e.printStackTrace();
        }
    }

    private static void repalceData() {
        try {
            String inputFileName = "E:\\pdftest\\template.pdf";
            String outputFileName = "E:\\pdftest\\template_new.pdf";
            String imageFile = "E:\\pdftest\\seal.jpeg";

            PdfWriter writer = new PdfWriter(outputFileName);
            PdfReader reader = new PdfReader(inputFileName);
            PdfDocument document = new PdfDocument(reader, writer);
            PdfFont font = PdfFontFactory.createFont("Font/SongTi.otf", PdfEncodings.IDENTITY_H);

            String jsonS = "{\"fill_1\":\"测试甲方\",\"fill_5\":\"测试乙方\",\"fill_9\":\"测试丙方\",\"fill_2\":\"1111111111111111\",\"fill_6\":\"2222222222222222\",\"undefined\":\"测试门店号\",\"fill_11\":\"0731-11111111\",\"fill_12\":\"人民中路222号\",\"fill_13\":\"ABC123456\",\"fill_14\":\"测试甲方\",\"fill_15\":\"199\",\"fill_16\":\"住宅\",\"fill_6_2\":\"√\",\"fill_7_2\":\"自定义条约1111\",\"fill_8_2\":\"自定义条约2222\",\"fill_9_2\":\"2022年五月二十日\"}";
            JSONObject maps = JSONUtil.parseObj(jsonS);

            //获取pdf表单
            PdfAcroForm form = PdfAcroForm.getAcroForm(document, true);
            Map<String, PdfFormField> fields = form.getFormFields();

            for (String key : maps.keySet()) {
                PdfFormField field = fields.get(key);
                if (field != null) {
                    field.setValue((String) maps.get(key)).setFont(font);
                }
            }
//            fields.get("fill_1").setValue("李四").setFont(font);
//            fields.get("fill_2").setValue("430382200808081235X");
//            fields.get("fill_3").setValue("Seven");
//            fields.get("fill_4").setValue("430382200808081234");
            fields.get("toggle_2").setCheckType(PdfFormField.TYPE_CHECK).setValue("Yes");
//            fields.get("toggle_3").setValue("Off");
//            fields.get("fill_9_2").setValue(String.valueOf(LocalDate.now()));
            //图片处理
            InputStream is = new FileInputStream(imageFile);
            String str = Base64.encodeBytes(StreamUtil.inputStreamToArray(is));
            fields.get("image_1").setValue(str);

            //表单扁平化到文件模式
//            form.flattenFields();
            document.close();

            System.out.println("===============PDF导出成功=============" + LocalDateTime.now());
        } catch (Exception e) {
            System.out.println("===============PDF导出失败=============");
            e.printStackTrace();
        }
    }
}
