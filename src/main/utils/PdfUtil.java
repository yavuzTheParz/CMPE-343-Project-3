package main.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for generating invoice PDFs from plain text. If Apache
 * PDFBox is available on the classpath the implementation will use it via
 * reflection; otherwise a minimal native PDF writer is used to produce a
 * valid PDF document.
 */
public class PdfUtil {

    public static byte[] createPdfFromText(String title, String body) {
        // Try to use PDFBox via reflection so compilation does not require the library.
        try {
            Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
        } catch (ClassNotFoundException e) {
            // PDFBox not available -> create a small native PDF writer
            try {
                return createSimplePdf(title, body);
            } catch (Throwable ex) {
                ex.printStackTrace();
                return fallbackBytes(title, body);
            }
        }

        try {
            byte[] bytes = createWithPdfBoxReflection(title, body);
            // validate bytes look like a PDF
            if (bytes != null && bytes.length > 4) {
                String hdr = new String(bytes, 0, Math.min(bytes.length, 8), StandardCharsets.ISO_8859_1);
                String tail = new String(bytes, Math.max(0, bytes.length - 8), Math.min(8, bytes.length), StandardCharsets.ISO_8859_1);
                if (hdr.startsWith("%PDF-") && tail.contains("%%EOF")) {
                    return bytes;
                }
            }
            // if PDFBox produced unexpected output, use our simple generator
            try { return createSimplePdf(title, body); } catch (Throwable ex) { ex.printStackTrace(); return fallbackBytes(title, body); }
        } catch (Throwable t) {
            t.printStackTrace();
            try { return createSimplePdf(title, body); } catch (Throwable ex) { ex.printStackTrace(); return fallbackBytes(title, body); }
        }
    }

    private static byte[] fallbackBytes(String title, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append(title == null ? "Invoice" : title).append("\n\n");
        sb.append(body == null ? "" : body);
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // Minimal PDF generator (one page, Helvetica, plain text). Good enough for invoices.
    private static byte[] createSimplePdf(String title, String body) throws Exception {
        if (title == null) title = "Invoice";
        if (body == null) body = "";

        String[] lines = (title + "\n\n" + body).split("\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // We'll assemble objects and track offsets
        out.write("%PDF-1.4\n%âãÏÓ\n".getBytes(StandardCharsets.ISO_8859_1));

        int[] offsets = new int[6];

        // 1: Catalog
        offsets[1] = out.size();
        out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

        // 2: Pages
        offsets[2] = out.size();
        out.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

        // 3: Page
        offsets[3] = out.size();
        out.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

        // 4: Font
        offsets[4] = out.size();
        out.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

        // 5: Content stream
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        int y = 750;
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i];
            l = l.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
            String lineCmd = "BT /F1 12 Tf 50 " + y + " Td (" + l + ") Tj ET\n";
            content.write(lineCmd.getBytes(StandardCharsets.ISO_8859_1));
            y -= 14;
            if (y < 40) break;
        }

        byte[] contentBytes = content.toByteArray();

        offsets[5] = out.size();
        out.write(("5 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
        out.write(contentBytes);
        out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

        int xrefPos = out.size();
        out.write("xref\n0 6\n0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
        for (int i = 1; i <= 5; i++) {
            String off = String.format("%010d 00000 n \n", offsets[i]);
            out.write(off.getBytes(StandardCharsets.ISO_8859_1));
        }

        out.write(("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefPos + "\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));

        return out.toByteArray();
    }

    private static byte[] createWithPdfBoxReflection(String title, String body) throws Exception {
        Class<?> PDDocument = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
        Class<?> PDPage = Class.forName("org.apache.pdfbox.pdmodel.PDPage");
        Class<?> PDPageContentStream = Class.forName("org.apache.pdfbox.pdmodel.PDPageContentStream");
        Class<?> PDType1Font = Class.forName("org.apache.pdfbox.pdmodel.font.PDType1Font");
        Class<?> PDFont = Class.forName("org.apache.pdfbox.pdmodel.font.PDFont");

        Object doc = PDDocument.getDeclaredConstructor().newInstance();
        Object page = PDPage.getDeclaredConstructor().newInstance();

        // doc.addPage(page)
        PDDocument.getMethod("addPage", PDPage).invoke(doc, page);

        // new PDPageContentStream(doc, page)
        java.lang.reflect.Constructor<?> csCtor = PDPageContentStream.getConstructor(PDDocument, PDPage);
        Object cs = csCtor.newInstance(doc, page);

        try {
            Object font = PDType1Font.getField("HELVETICA").get(null);

            PDPageContentStream.getMethod("beginText").invoke(cs);
            PDPageContentStream.getMethod("setFont", PDFont, float.class).invoke(cs, font, 14f);
            PDPageContentStream.getMethod("newLineAtOffset", float.class, float.class).invoke(cs, 50f, 750f);
            PDPageContentStream.getMethod("showText", String.class).invoke(cs, title == null ? "Invoice" : title);
            PDPageContentStream.getMethod("endText").invoke(cs);

            PDPageContentStream.getMethod("beginText").invoke(cs);
            PDPageContentStream.getMethod("setFont", PDFont, float.class).invoke(cs, font, 10f);
            PDPageContentStream.getMethod("newLineAtOffset", float.class, float.class).invoke(cs, 50f, 730f);

            if (body != null) {
                String[] lines = body.split("\\n");
                boolean first = true;
                for (String line : lines) {
                    if (!first) {
                        PDPageContentStream.getMethod("newLineAtOffset", float.class, float.class).invoke(cs, 0f, -12f);
                    }
                    PDPageContentStream.getMethod("showText", String.class).invoke(cs, line);
                    first = false;
                }
            }

            PDPageContentStream.getMethod("endText").invoke(cs);
        } finally {
            PDPageContentStream.getMethod("close").invoke(cs);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PDDocument.getMethod("save", OutputStream.class).invoke(doc, baos);
        PDDocument.getMethod("close").invoke(doc);
        return baos.toByteArray();
    }

    // Normalize text to remove characters that may not be representable in simple PDF writer
    public static String normalizeTextForPdf(String input) {
        if (input == null) return "";
        try {
            String n = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
            // remove diacritics
            n = n.replaceAll("\\p{M}", "");
            // replace control chars and non-printable with space, BUT preserve newlines and tabs
            n = n.replaceAll("[^\u0020-\u007E\n\r\t]", " ");
            return n;
        } catch (Exception e) {
            return input.replaceAll("[^\u0020-\u007E\n\r\t]", " ");
        }
    }
}
