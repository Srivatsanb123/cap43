import { useEffect, useState } from "react";
import { getVendorWorkOrders, updateVendorWorkOrder } from "../api";

export default function VendorPortal() {
  const [workOrders, setWorkOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [activeModal, setActiveModal] = useState(null);
  const [newStatus, setNewStatus] = useState("IN_PROGRESS");
  const [notes, setNotes] = useState("");

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const list = await getVendorWorkOrders();
      setWorkOrders(list);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleUpdate = async () => {
    if (!activeModal) return;
    try {
      await updateVendorWorkOrder(activeModal.workOrderId, {
        status: newStatus,
        note: notes,
      });
      setSuccess(`Work Order #${activeModal.workOrderId} updated to ${newStatus}`);
      setActiveModal(null);
      setNotes("");
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div>
      <div className="card-header">
        <div>
          <h2>Vendor Work Orders</h2>
          <p style={{ color: "#868e96", fontSize: "0.88rem" }}>
            Review assigned maintenance tasks, accept orders, and record progress updates.
          </p>
        </div>
        <button onClick={loadData} className="secondary small">Refresh</button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="card">
        {workOrders.length === 0 ? (
          <p style={{ color: "#868e96" }}>No work orders currently assigned to you.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>WO #</th>
                <th>Issue / Target</th>
                <th>Priority</th>
                <th>Current Status</th>
                <th>Assigned Date</th>
                <th>SLA Due</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {workOrders.map((wo) => (
                <tr key={wo.workOrderId}>
                  <td>#{wo.workOrderId}</td>
                  <td>
                    <strong>{wo.issueTitle || "Inspection Repair"}</strong>
                    <div style={{ fontSize: "0.82rem", color: "#555" }}>
                      Issue #{wo.issueId} | Unit: {wo.unitNumber || "N/A"}
                    </div>
                  </td>
                  <td><span className={`badge ${wo.severity || "MEDIUM"}`}>{wo.severity || "MEDIUM"}</span></td>
                  <td><span className={`badge ${wo.status}`}>{wo.status}</span></td>
                  <td>{new Date(wo.assignedAt).toLocaleString()}</td>
                  <td>{wo.slaDueAt ? new Date(wo.slaDueAt).toLocaleString() : "N/A"}</td>
                  <td>
                    <button
                      className="small"
                      onClick={() => {
                        setActiveModal(wo);
                        setNewStatus(wo.status === "ASSIGNED" ? "ACCEPTED" : wo.status === "ACCEPTED" ? "IN_PROGRESS" : "COMPLETED");
                      }}
                    >
                      Update Progress
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {activeModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Update Work Order #{activeModal.workOrderId}</h3>
            <p style={{ fontSize: "0.85rem", color: "#666", marginBottom: 12 }}>
              Task: {activeModal.issueTitle}
            </p>

            <label>Update Status To</label>
            <select value={newStatus} onChange={(e) => setNewStatus(e.target.value)}>
              <option value="ACCEPTED">ACCEPTED (Acknowledged)</option>
              <option value="IN_PROGRESS">IN_PROGRESS (On-site work started)</option>
              <option value="COMPLETED">COMPLETED (Repair finished)</option>
            </select>

            <label>Notes / Work Completed</label>
            <textarea
              rows={4}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Provide details on parts replaced, diagnostic findings, or invoice references..."
            />

            <div style={{ display: "flex", gap: 10, marginTop: 16 }}>
              <button onClick={handleUpdate}>Save Update</button>
              <button className="secondary" onClick={() => setActiveModal(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

