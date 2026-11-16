/** Compatibility entry point; the report renderer uses current Markdown and UML images. */
public class ReportPdf {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: java scripts/ReportPdf.java source.md report.pdf");
        int status = new ProcessBuilder("python3", "scripts/report.py", args[0], args[1]).inheritIO().start().waitFor();
        if (status != 0) throw new IllegalStateException("Report rendering failed: " + status);
    }
}
