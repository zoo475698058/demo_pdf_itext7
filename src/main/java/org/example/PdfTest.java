package org.example;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.HashUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itextpdf.commons.utils.Base64;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.util.StreamUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.jcajce.provider.digest.MD5;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * pdf测试
 * @author Administrator
 */
public class PdfTest {
    public static void main(String[] args) {
        repalceData();
        addsign();
//        signMd5();
    }

    private static void signMd5() {
        try {
            PdfWriter writer = new PdfWriter("E:\\template_new2.pdf");
            PdfReader reader = new PdfReader("E:\\template_new.pdf");
            PdfDocument document = new PdfDocument(reader, writer);
            document.close();
            System.out.println(SecureUtil.md5(new File("E:\\template_new.pdf")));
            System.out.println(SecureUtil.md5(new File("E:\\template_new2.pdf")));
        } catch (Exception e) {
            System.out.println("===============失败=============");
            e.printStackTrace();
        }
    }

    private static void addsign() {
        try {
            String inputFileName = "E:\\template_new.pdf";
            String outputFileName = "E:\\template_new2.pdf";
            String privateKey = "E:\\tem.pfx";
            char[] pass = "123456".toCharArray();
            String imageFile = "E:\\tem.png";

            KeyStore ks = KeyStore.getInstance("pkcs12");
            FileInputStream fis = new FileInputStream(privateKey);
            ks.load(fis, pass);
            String alias = ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, pass);
            Certificate[] chain = ks.getCertificateChain(alias);

            PdfReader reader = new PdfReader(inputFileName);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(outputFileName), new StampingProperties());

            ImageData image= ImageDataFactory.create(imageFile);
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setSignatureGraphic(image).setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
            signer.setFieldName("Signature1");

            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            IExternalSignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();

            signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);

            System.out.println("===============PDF签名成功============="+ LocalDateTime.now());
        } catch (Exception e) {
            System.out.println("===============PDF签名失败=============");
            e.printStackTrace();
        }
    }

    private static void repalceData() {
        try {
            String inputFileName = "E:\\template.pdf";
            String outputFileName = "E:\\template_new.pdf";
            String imageFile = "E:\\tem.jpeg";

            PdfWriter writer = new PdfWriter(outputFileName);
            PdfReader reader = new PdfReader(inputFileName);
            PdfDocument document = new PdfDocument(reader, writer);
            PdfFont font = PdfFontFactory.createFont("Font/SIMYOU.TTF", PdfEncodings.IDENTITY_H);

            String jsonS = "{\"fill_1\":\"测试甲方\",\"fill_5\":\"测试乙方\",\"fill_9\":\"测试丙方\",\"fill_2\":\"1111111111111111\",\"fill_6\":\"2222222222222222\",\"undefined\":\"测试门店号\",\"fill_11\":\"0731-11111111\",\"fill_12\":\"人民中路222号\",\"fill_13\":\"ABC123456\",\"fill_14\":\"测试甲方\",\"fill_15\":\"199\",\"fill_16\":\"住宅\",\"fill_6_2\":\"√\",\"fill_7_2\":\"自定义条约1111\",\"fill_8_2\":\"自定义条约2222\",\"fill_9_2\":\"2022年五月二十日\"}";
            JSONObject maps = JSONUtil.parseObj(jsonS);

            //获取pdf表单
            PdfAcroForm form = PdfAcroForm.getAcroForm(document, true);
            Map<String, PdfFormField> fields = form.getFormFields();

            for (String key:maps.keySet()) {
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

            //锁定表单,不让修改
//            form.flattenFields();
            document.close();

            System.out.println("===============PDF导出成功============="+ LocalDateTime.now());
        } catch (Exception e) {
            System.out.println("===============PDF导出失败=============");
            e.printStackTrace();
        }
    }
}
