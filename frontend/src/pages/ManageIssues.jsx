import { useEffect, useState } from "react";
import {
  getAdminIssues,
  getVendors,
  assignVendor,
  changeIssueSeverity,
  getEscalations,
  acknowledgeEscalation
} from "../api";

const SEVERITIES = ["EMERGENCY", "HIGH", "MEDIUM", "LOW"];

export default function ManageIssues() {
  const [issues, setIssues] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [escalations, setEscalations] = useState([]);
  const [filterSeverity, setFilterSeverity] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [filterOverdue, setFilterOverdue] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  
  const [assignModal, setAssignModal] = useState(null);
  const [vendorId, setVendorId] = useState("");
  const [assignNotes, setAssignNotes] = useState("");

  
  const [severityModal, setSeverityModal] = useState(null);
  const [newSeverity, setNewSeverity] = useState("HIGH");
  const [severityReason, setSeverityReason] = useState("");

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const [iList, vList, eList] = await Promise.all([
        getAdminIssues({
          severity: filterSeverity,
          status: filterStatus,
          overdue: filterOverdue,
        }),
        getVendors(),
        getEscalations()
      ]);
      setIssues(iList);
      setVendors(vList);
      setEscalations(eList);
      if (vList.length > 0 && !vendorId) {
        setVendorId(String(vList[0].vendorId));
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [filterSeverity, filterStatus, filterOverdue]);

  const handleAssign = async () => {
    if (!assignModal || !vendorId) return;
    try {
      await assignVendor(assignModal.issueId, {
        vendorId: Number(vendorId),
        notes: assignNotes,
      });
      setSuccess(`Work Order assigned to vendor successfully!`);
      setAssignModal(null);
      setAssignNotes("");
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleSeverityChange = async () => {
    if (!severityModal || !severityReason) return;
    try {
      await changeIssueSeverity(severityModal.issueId, {
        severity: newSeverity,
        reason: severityReason,
      });
      setSuccess(`Issue #${severityModal.issueId} priority updated to ${newSeverity}`);
      setSeverityModal(null);
      setSeverityReason("");
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleAcknowledgeEscalation = async (escId) => {
    try {
      await acknowledgeEscalation(escId);
      setSuccess("Escalation acknowledged.");
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div>
      <div className="card-header">
        <div>
          <h2>Manage Maintenance Issues & Work Orders</h2>
          <p style={{ color: "#868e96", fontSize: "0.88rem" }}>
            Prioritize tenant requests, dispatch vendors, monitor SLA countdowns, and manage escalation alarms.
          </p>
        </div>
        <button onClick={loadData} className="secondary small">Refresh</button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {escalations.length > 0 && (
        <div className="card" style={{ borderLeft: "4px solid #e03131", background: "#fff5f5" }}>
          <h3 style={{ color: "#c92a2a" }}>⚠️ Active SLA Breaches & Emergency Escalations ({escalations.length})</h3>
          <table>
            <thead>
              <tr>
                <th>Escalation ID</th>
                <th>Issue ID</th>
                <th>Level</th>
                <th>Reason</th>
                <th>Triggered</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {escalations.map((esc) => (
                <tr key={esc.escalationId}>
                  <td>#{esc.escalationId}</td>
                  <td><strong>Issue #{esc.issueId}</strong></td>
                  <td><span className="badge EMERGENCY">Level {esc.level}</span></td>
                  <td>{esc.reason}</td>
                  <td>{new Date(esc.triggeredAt).toLocaleString()}</td>
                  <td>
                    <button
                      className="small danger"
                      onClick={() => handleAcknowledgeEscalation(esc.escalationId)}
                    >
                      Acknowledge
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="card">
        <div style={{ display: "flex", gap: 16, alignItems: "center", marginBottom: 16, flexWrap: "wrap" }}>
          <div>
            <label style={{ margin: 0 }}>Filter Severity</label>
            <select value={filterSeverity} onChange={(e) => setFilterSeverity(e.target.value)}>
              <option value="">All Severities</option>
              {SEVERITIES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>

          <div>
            <label style={{ margin: 0 }}>Filter Status</label>
            <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
              <option value="">All Statuses</option>
              <option value="OPEN">OPEN</option>
              <option value="ASSIGNED">ASSIGNED</option>
              <option value="IN_PROGRESS">IN_PROGRESS</option>
              <option value="RESOLVED">RESOLVED</option>
            </select>
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: 6, marginTop: 18 }}>
            <input
              type="checkbox"
              id="overdueOnly"
              checked={filterOverdue}
              onChange={(e) => setFilterOverdue(e.target.checked)}
              style={{ width: "auto" }}
            />
            <label htmlFor="overdueOnly" style={{ margin: 0, cursor: "pointer", color: "#c92a2a" }}>
              Show Overdue (SLA Breached) Only
            </label>
          </div>
        </div>

        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Property & Unit</th>
              <th>Issue Summary</th>
              <th>Priority</th>
              <th>Status</th>
              <th>Vendor</th>
              <th>SLA Due</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {issues.map((i) => (
              <tr key={i.issueId} className={i.overdue ? "overdue-row" : ""}>
                <td>#{i.issueId}</td>
                <td>
                  <strong>{i.propertyName}</strong>
                  <div style={{ fontSize: "0.8rem", color: "#666" }}>Unit {i.unitNumber} ({i.tenantRef})</div>
                </td>
                <td>
                  <strong>{i.title}</strong>
                  <div style={{ fontSize: "0.82rem", color: "#555" }}>{i.description}</div>
                  <div style={{ fontSize: "0.75rem", color: "#868e96" }}>Category: {i.category}</div>
                </td>
                <td>
                  <span className={`badge ${i.severity}`}>{i.severity}</span>
                  {i.escalationLevel > 0 && (
                    <div style={{ marginTop: 4 }}>
                      <span className="badge EMERGENCY">L{i.escalationLevel} Escalated</span>
                    </div>
                  )}
                </td>
                <td><span className={`badge ${i.status}`}>{i.status}</span></td>
                <td>{i.vendorName || <span style={{ color: "#868e96" }}>Unassigned</span>}</td>
                <td style={{ color: i.overdue ? "#c92a2a" : "inherit", fontWeight: i.overdue ? 700 : 400 }}>
                  {new Date(i.slaDueAt).toLocaleString()}
                  {i.overdue && <div>SLA BREACHED</div>}
                </td>
                <td>
                  <div style={{ display: "flex", gap: 6, flexDirection: "column" }}>
                    <button
                      className="small"
                      onClick={() => {
                        setAssignModal(i);
                        setAssignNotes(`Dispatching for ${i.title}`);
                      }}
                    >
                      Assign Vendor
                    </button>
                    <button
                      className="small secondary"
                      onClick={() => {
                        setSeverityModal(i);
                        setNewSeverity(i.severity);
                        setSeverityReason("Operator reassessment based on tenant evidence.");
                      }}
                    >
                      Change Severity
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {assignModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Assign Vendor for Issue #{assignModal.issueId}</h3>
            <p style={{ fontSize: "0.85rem", color: "#666", marginBottom: 12 }}>
              Task: {assignModal.title} (Unit {assignModal.unitNumber})
            </p>

            <label>Select Certified Vendor</label>
            <select value={vendorId} onChange={(e) => setVendorId(e.target.value)}>
              {vendors.map((v) => (
                <option key={v.vendorId} value={v.vendorId}>
                  {v.companyName} ({v.trade})
                </option>
              ))}
            </select>

            <label>Work Order Notes / Instructions</label>
            <textarea
              rows={3}
              value={assignNotes}
              onChange={(e) => setAssignNotes(e.target.value)}
            />

            <div style={{ display: "flex", gap: 10, marginTop: 16 }}>
              <button onClick={handleAssign}>Dispatch Vendor</button>
              <button className="secondary" onClick={() => setAssignModal(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {severityModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Update Severity for Issue #{severityModal.issueId}</h3>
            <label>New Severity</label>
            <select value={newSeverity} onChange={(e) => setNewSeverity(e.target.value)}>
              {SEVERITIES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>

            <label>Reason for Change (Audit Requirement)</label>
            <textarea
              rows={3}
              value={severityReason}
              onChange={(e) => setSeverityReason(e.target.value)}
              placeholder="Explain why severity is being upgraded or downgraded..."
              required
            />

            <div style={{ display: "flex", gap: 10, marginTop: 16 }}>
              <button onClick={handleSeverityChange}>Save & Recalculate SLA</button>
              <button className="secondary" onClick={() => setSeverityModal(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
