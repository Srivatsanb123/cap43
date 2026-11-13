import { useEffect, useState } from "react";
import { getTenantIssues, createTenantIssue, getTenantDisputes, contestDisputeItem, uploadTenantEvidence } from "../api";

export default function TenantPortal() {
  const [activeTab, setActiveTab] = useState("issues");
  const [issues, setIssues] = useState([]);
  const [disputes, setDisputes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  
  const [category, setCategory] = useState("PLUMBING");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [photo, setPhoto] = useState(null);

  
  const [contestModal, setContestModal] = useState(null);
  const [position, setPosition] = useState("CONTESTED");
  const [comment, setComment] = useState("");

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const [iList, dList] = await Promise.all([getTenantIssues(), getTenantDisputes()]);
      setIssues(iList);
      setDisputes(dList);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSubmitIssue = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      const created = await createTenantIssue({
        category,
        title: title || `${category} Issue`,
        description,
      });

      if (photo) {
        const fd = new FormData();
        fd.append("file", photo);
        await uploadTenantEvidence(created.issueId, fd);
      }

      setSuccess(`Issue #${created.issueId} submitted! Priority: ${created.severity} (SLA Due: ${new Date(created.slaDueAt).toLocaleString()})`);
      setTitle("");
      setDescription("");
      setPhoto(null);
      await loadData();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleContestItem = async () => {
    if (!contestModal) return;
    try {
      await contestDisputeItem(contestModal.disputeId, contestModal.itemId, {
        position,
        comment,
      });
      setSuccess("Your position has been recorded.");
      setContestModal(null);
      setComment("");
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div>
      <div className="card-header">
        <div>
          <h2>Tenant Portal</h2>
          <p style={{ color: "#868e96", fontSize: "0.88rem" }}>
            Submit maintenance requests, upload inspection evidence, and manage deposit disputes.
          </p>
        </div>
        <button onClick={loadData} className="secondary small">Refresh</button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="tabs">
        <button
          className={`tab ${activeTab === "issues" ? "active" : ""}`}
          onClick={() => setActiveTab("issues")}
        >
          My Maintenance Requests ({issues.length})
        </button>
        <button
          className={`tab ${activeTab === "new-issue" ? "active" : ""}`}
          onClick={() => setActiveTab("new-issue")}
        >
          + Report New Issue
        </button>
        <button
          className={`tab ${activeTab === "disputes" ? "active" : ""}`}
          onClick={() => setActiveTab("disputes")}
        >
          Deposit Disputes ({disputes.length})
        </button>
      </div>

      {activeTab === "new-issue" && (
        <div className="card" style={{ maxWidth: 650 }}>
          <h3>Submit a Maintenance Request</h3>
          <p style={{ fontSize: "0.85rem", color: "#666", marginBottom: 16 }}>
            Urgent keywords (e.g. <em>leak, burst, gas, flood</em>) are automatically classified as <strong>EMERGENCY</strong> with a 4-hour SLA.
          </p>
          <form onSubmit={handleSubmitIssue}>
            <label>Category</label>
            <select value={category} onChange={(e) => setCategory(e.target.value)}>
              <option value="PLUMBING">Plumbing</option>
              <option value="ELECTRICAL">Electrical</option>
              <option value="HVAC">HVAC</option>
              <option value="APPLIANCE">Appliance</option>
              <option value="DOOR_WINDOW">Door / Window</option>
              <option value="GENERAL">General</option>
            </select>

            <label>Short Title</label>
            <input
              type="text"
              placeholder="e.g. Urgent pipe leak under kitchen sink"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />

            <label>Detailed Description</label>
            <textarea
              placeholder="Describe the issue in detail. If urgent, describe leak/damage location."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              rows={4}
            />

            <label>Attach Photo Evidence (Optional)</label>
            <input
              type="file"
              accept="image/jpeg,image/png,application/pdf"
              onChange={(e) => setPhoto(e.target.files[0])}
            />

            <button type="submit" disabled={loading} style={{ marginTop: 20 }}>
              {loading ? "Submitting..." : "Submit Maintenance Request"}
            </button>
          </form>
        </div>
      )}

      {activeTab === "issues" && (
        <div className="card">
          <h3>My Maintenance Issues</h3>
          {issues.length === 0 ? (
            <p style={{ color: "#868e96" }}>No issues reported yet.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title & Description</th>
                  <th>Category</th>
                  <th>Severity</th>
                  <th>Status</th>
                  <th>Submitted</th>
                  <th>SLA Due</th>
                </tr>
              </thead>
              <tbody>
                {issues.map((i) => (
                  <tr key={i.issueId}>
                    <td>#{i.issueId}</td>
                    <td>
                      <strong>{i.title}</strong>
                      <div style={{ fontSize: "0.82rem", color: "#555" }}>{i.description}</div>
                    </td>
                    <td>{i.category}</td>
                    <td><span className={`badge ${i.severity}`}>{i.severity}</span></td>
                    <td><span className={`badge ${i.status}`}>{i.status}</span></td>
                    <td>{new Date(i.createdAt).toLocaleString()}</td>
                    <td style={{ color: new Date(i.slaDueAt) < new Date() && i.status !== "RESOLVED" ? "red" : "inherit" }}>
                      {new Date(i.slaDueAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {activeTab === "disputes" && (
        <div className="card">
          <h3>Move-Out Deposit Disputes</h3>
          {disputes.length === 0 ? (
            <p style={{ color: "#868e96" }}>No active or past deposit disputes.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Dispute #</th>
                  <th>Property / Unit</th>
                  <th>Deposit Held</th>
                  <th>Total Claimed</th>
                  <th>Status</th>
                  <th>Opened</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {disputes.map((d) => (
                  <tr key={d.disputeId}>
                    <td>#{d.disputeId}</td>
                    <td>{d.propertyName} - Unit {d.unitNumber}</td>
                    <td>${Number(d.depositHeld).toLocaleString()}</td>
                    <td>${Number(d.totalClaimed).toLocaleString()}</td>
                    <td><span className={`badge ${d.status}`}>{d.status}</span></td>
                    <td>{new Date(d.openedAt).toLocaleDateString()}</td>
                    <td>
                      <button
                        className="small secondary"
                        onClick={() => {
                          
                          setContestModal({ disputeId: d.disputeId, itemId: 1 });
                        }}
                      >
                        Review / Contest Items
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {contestModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Contest / Accept Dispute Item</h3>
            <p style={{ fontSize: "0.85rem", color: "#666", marginBottom: 12 }}>
              Dispute #{contestModal.disputeId}
            </p>
            <label>Your Position</label>
            <select value={position} onChange={(e) => setPosition(e.target.value)}>
              <option value="CONTESTED">Contest Charge (Wear and tear / excessive cost)</option>
              <option value="ACCEPTED">Accept Charge</option>
            </select>

            <label>Comments / Explanation</label>
            <textarea
              rows={4}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="Provide evidence reference or reasons why this charge should be waived/reduced..."
            />

            <div style={{ display: "flex", gap: 10, marginTop: 16 }}>
              <button onClick={handleContestItem}>Submit Response</button>
              <button className="secondary" onClick={() => setContestModal(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

