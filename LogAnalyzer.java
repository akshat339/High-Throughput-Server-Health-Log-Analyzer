import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogAnalyzer {

    static class LogEntry {
        String endpoint;
        int statusCode;
        int responseTimeMs;

        LogEntry(String endpoint, int statusCode, int responseTimeMs) {
            this.endpoint = endpoint;
            this.statusCode = statusCode;
            this.responseTimeMs = responseTimeMs;
        }
    }

    public static void main(String[] args) throws IOException {
        // Sample realistic raw web server logs (Endpoint, Status, Latency in ms)
        List<String> rawLogs = Arrays.asList(
            "GET /api/v1/users 200 45ms",
            "POST /api/v1/checkout 200 320ms",
            "GET /api/v1/products 200 65ms",
            "POST /api/v1/checkout 500 1200ms",
            "GET /api/v1/users 404 30ms",
            "GET /api/v1/products 200 80ms",
            "POST /api/v1/checkout 504 5000ms",
            "GET /api/v1/users 200 52ms"
        );

        Pattern pattern = Pattern.compile("(GET|POST)\\s+(\\S+)\\s+(\\d{3})\\s+(\\d+)ms");
        List<LogEntry> entries = new ArrayList<>();

        for (String line : rawLogs) {
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                entries.add(new LogEntry(m.group(2), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4))));
            }
        }

        // Analytical Computations
        long totalRequests = entries.size();
        long serverErrors = entries.stream().filter(e -> e.statusCode >= 500).count();
        double avgLatency = entries.stream().mapToInt(e -> e.responseTimeMs).average().orElse(0.0);
        int maxLatency = entries.stream().mapToInt(e -> e.responseTimeMs).max().orElse(0);

        // Generate Self-Contained HTML Audit Report
        String htmlReport = """
        <!DOCTYPE html>
        <html>
        <head>
          <title>API Performance & Reliability Audit</title>
          <style>
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; background: #0f172a; color: #f8fafc; padding: 32px; }
            .container { max-width: 800px; margin: 0 auto; background: #1e293b; padding: 24px; border-radius: 8px; border: 1px solid #334155; }
            h2 { margin-bottom: 4px; }
            .sub { color: #94a3b8; font-size: 14px; margin-bottom: 24px; }
            .grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 24px; }
            .stat-box { background: #0f172a; padding: 14px; border-radius: 6px; border-left: 3px solid #38bdf8; }
            .label { font-size: 11px; text-transform: uppercase; color: #64748b; font-weight: 700; }
            .val { font-size: 20px; font-weight: 700; margin-top: 4px; }
            .danger { border-left-color: #ef4444; color: #f87171; }
            table { width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 14px; }
            th, td { text-align: left; padding: 10px 12px; border-bottom: 1px solid #334155; }
            th { color: #94a3b8; font-weight: 600; }
            .tag { padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
            .tag-200 { background: #064e3b; color: #6ee7b7; }
            .tag-err { background: #7f1d1d; color: #fca5a5; }
          </style>
        </head>
        <body>
          <div class="container">
            <h2>Log Metrics & Health Report</h2>
            <div class="sub">Generated from automated batch log parser</div>
            <div class="grid">
              <div class="stat-box"><div class="label">Total Logs</div><div class="val">%d</div></div>
              <div class="stat-box danger"><div class="label">5xx Errors</div><div class="val">%d</div></div>
              <div class="stat-box"><div class="label">Avg Latency</div><div class="val">%.1f ms</div></div>
              <div class="stat-box"><div class="label">Peak Latency</div><div class="val">%d ms</div></div>
            </div>
            <table>
              <thead>
                <tr><th>Endpoint</th><th>Status</th><th>Latency</th></tr>
              </thead>
              <tbody>
        """.formatted(totalRequests, serverErrors, avgLatency, maxLatency);

        StringBuilder rows = new StringBuilder();
        for (LogEntry e : entries) {
            String badge = e.statusCode >= 500 ? "tag-err" : "tag-200";
            rows.append(String.format("<tr><td>%s</td><td><span class=\"tag %s\">%d</span></td><td>%d ms</td></tr>",
                    e.endpoint, badge, e.statusCode, e.responseTimeMs));
        }

        String closingHtml = """
              </tbody>
            </table>
          </div>
        </body>
        </html>
        """;

        Files.writeString(Path.of("log_report.html"), htmlReport + rows + closingHtml);
        System.out.println("Processing complete. Generated 'log_report.html'.");
    }
}
