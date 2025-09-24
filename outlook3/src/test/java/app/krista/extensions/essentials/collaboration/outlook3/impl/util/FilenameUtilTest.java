package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import org.junit.Assert;
import org.junit.Test;

public class FilenameUtilTest {

    @Test
    public void toSafeFilename_handlesSmartApostropheAndPreservesExtension() {
        String in = "Clyde’s Animal Clinic - Client Service Agreement - Evette 2024 - signed.pdf";
        String out = FilenameUtil.toSafeFilename(in);
        Assert.assertTrue(out.endsWith(".pdf"));
        Assert.assertFalse("Output should not contain Unicode curly apostrophe", out.contains("’"));
    }

    @Test
    public void toSafeFilename_replacesReservedAndPathCharacters() {
        String in = "a<b>:c\"d/e\\f|g?h*i.txt";
        String out = FilenameUtil.toSafeFilename(in);
        // All reserved/path characters should be replaced by underscores
        Assert.assertEquals("a_b__c_d_e_f_g_h_i.txt", out);
    }

    @Test
    public void toSafeFilename_replacesEmojiAndNonAscii() {
        String in = "report-🐱-日本語-áéíóú.docx";
        String out = FilenameUtil.toSafeFilename(in);
        Assert.assertTrue(out.endsWith(".docx"));
        // Expect non-ASCII replaced with underscores
        Assert.assertEquals("report-_-____-_.docx", out);
    }

    @Test
    public void toSafeFilename_trimsLeadingTrailingDotsAndCollapsesSpaces() {
        String in = "..  my  file  ..  .png";
        String out = FilenameUtil.toSafeFilename(in);
        Assert.assertTrue(out.endsWith(".png"));
        Assert.assertTrue(out.startsWith("my file"));
        Assert.assertFalse(out.startsWith("."));
    }

    @Test
    public void toSafeFilename_limitsLengthAndKeepsExtension() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append('a');
        String in = sb.toString() + ".zip";
        String out = FilenameUtil.toSafeFilename(in);
        Assert.assertTrue(out.endsWith(".zip"));
        // base name should be truncated to <= 100 chars
        Assert.assertTrue(out.substring(0, out.length() - 4).length() <= 100);
    }

    @Test
    public void toSafeFilename_handlesNullOrEmpty() {
        Assert.assertEquals("attachment", FilenameUtil.toSafeFilename(null));
        Assert.assertEquals("attachment", FilenameUtil.toSafeFilename("   "));
    }

    @Test
    public void uuidFallback_preservesSanitizedExtension() {
        String in = "name with : bad / chars.pdf";
        String fallback = FilenameUtil.uuidFallback(in);
        Assert.assertTrue("Fallback should preserve extension", fallback.endsWith(".pdf"));
        Assert.assertTrue("Fallback should look like 'file-xxxxxxxx.ext'", fallback.startsWith("file-"));
    }
}

