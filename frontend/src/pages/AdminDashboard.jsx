import { useEffect, useState } from "react";
import { getDashboardSummary, triggerS3Sync } from "../api";

export default function AdminDashboard() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [syncRunning, setSyncRunning] = useState(false);
  const [syncResult, setSyncResult] = useState(null);

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await getDashboardSummary();
      setSummary(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleRunSync = async () => {
    setSyncRunning(true);
    setSyncResult(null);
    setError("");
    try {
      const res = await triggerS3Sync();
      setSyncResult(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setSyncRunning(false);
    }
  };

  if (!summary && loading) return <p>Loading dashboard summary...</p>;

  return (
    <div>
      <div className="card-header">
        <div>
          <h2>Operational SLA & Property Analytics</h2>
          <p style={{ color: "#868e96", fontSize: "0.88rem" }}>
            Real-time maintenance triage, SLA breach monitoring, and S3 data sync trigger for Snowflake / Power BI.
          </p>
        </div>
        <div style={{ display: "flex", gap: 10 }}>
          <button onClick={handleRunSync} disabled={syncRunning} className="success-btn">
            {syncRunning ? "Syncing Data..." : "🔃 Sync Data"}
          </button>
          <button onClick={loadData} className="secondary small">Refresh</button>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {syncResult && (
        <div className={`alert ${syncResult.status === "SUCCESS" ? "alert-success" : "alert-error"}`}>
          <strong>{syncResult.status === "SUCCESS" ? "✓ S3 Sync Completed" : "⚠️ S3 Sync Alert"}:</strong> {syncResult.message}
          <div style={{ fontSize: "0.8rem", marginTop: 4 }}>
            Execution time: {syncResult.durationMs} ms | Delta CSVs staged to S3 for automated Snowpipe ingestion into Snowflake.
          </div>
        </div>
      )}

      {summary && (
        <>
          <div className="grid">
            <div className="stat-card">
              <div className="stat-title">Total Maintenance Issues</div>
              <div className="stat-value">{summary.total}</div>
            </div>
            <div className="stat-card">
              <div className="stat-title">Open & Active Work Orders</div>
              <div className="stat-value">{summary.open}</div>
            </div>
            <div className="stat-card">
              <div className="stat-title">Overdue (SLA Breached)</div>
              <div className="stat-value danger">{summary.overdue}</div>
            </div>
            <div className="stat-card">
              <div className="stat-title">Escalation Alarms</div>
              <div className="stat-value warning">{summary.escalated}</div>
            </div>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20 }}>
            <div className="card">
              <h3>Issues by Severity (Rules Engine SLA)</h3>
              <table>
                <thead>
                  <tr>
                    <th>Severity</th>
                    <th>Count</th>
                    <th>Standard SLA</th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(summary.bySeverity || {}).map(([sev, count]) => (
                    <tr key={sev}>
                      <td><span className={`badge ${sev}`}>{sev}</span></td>
                      <td><strong>{count}</strong></td>
                      <td>
                        {sev === "EMERGENCY" && "4 Hours"}
                        {sev === "HIGH" && "24 Hours"}
                        {sev === "MEDIUM" && "72 Hours"}
                        {sev === "LOW" && "168 Hours (7 Days)"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="card">
              <h3>Issues by Lifecycle Status</h3>
              <table>
                <thead>
                  <tr>
                    <th>Status</th>
                    <th>Count</th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(summary.byStatus || {}).map(([st, count]) => (
                    <tr key={st}>
                      <td><span className={`badge ${st}`}>{st}</span></td>
                      <td><strong>{count}</strong></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
