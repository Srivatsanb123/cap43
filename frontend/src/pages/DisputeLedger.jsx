import { useEffect, useState } from "react";
import {
  getAdminDisputes,
  getAdminDispute,
  createAdminDispute,
  decideDisputeItem,
  getReviewQueue,
  resolveReviewQueueItem,
  getWearDamageRules,
  getLeases,
  getLeaseInspections
} from "../api";

export default function DisputeLedger() {
  const [tab, setTab] = useState("disputes");
  const [disputes, setDisputes] = useState([]);
  const [reviewQueue, setReviewQueue] = useState([]);
  const [rules, setRules] = useState([]);
  const [selectedDispute, setSelectedDispute] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  
  const [decisionModal, setDecisionModal] = useState(null);
  const [operatorDecision, setOperatorDecision] = useState("CHARGE_REDUCED");
  const [decidedAmount, setDecidedAmount] = useState("");
  const [decisionNotes, setDecisionNotes] = useState("");

  
  const [reviewModal, setReviewModal] = useState(null);
  const [outcomeNotes, setOutcomeNotes] = useState("");

  
  const [leases, setLeases] = useState([]);
  const [selectedLeaseId, setSelectedLeaseId] = useState("");
  const [claimAmount, setClaimAmount] = useState("5000");

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const [dList, rqList, rList, lList] = await Promise.all([
        getAdminDisputes(),
        getReviewQueue(),
        getWearDamageRules(),
        getLeases("ENDED")
      ]);
      setDisputes(dList);
      setReviewQueue(rqList);
      setRules(rList);
      setLeases(lList);
      if (lList.length > 0 && !selectedLeaseId) {
        setSelectedLeaseId(String(lList[0].leaseId));
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const openDisputeDetail = async (id) => {
    try {
      const detail = await getAdminDispute(id);
      setSelectedDispute(detail);
      setTab("detail");
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDecideItem = async () => {
    if (!decisionModal || !selectedDispute) return;
    try {
      const updated = await decideDisputeItem(
        selectedDispute.disputeId,
        decisionModal.disputeItemId,
        {
          operatorDecision,
          decidedAmount: Number(decidedAmount),
          decisionNotes,
        }
      );
      setSelectedDispute(updated);
      setSuccess("Operator decision applied successfully and audit trail logged.");
      setDecisionModal(null);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleResolveReview = async () => {
    if (!reviewModal) return;
    try {
      await resolveReviewQueueItem(reviewModal.reviewId, {
        outcomeNotes,
      });
      setSuccess("Review item marked as DECIDED by human operator.");
      setReviewModal(null);
      setOutcomeNotes("");
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div>
      <div className="card-header">
        <div>
          <h2>Deposit Dispute & Wear-and-Tear Ledger</h2>
          <p style={{ color: "#868e96", fontSize: "0.88rem" }}>
            Deterministic rule engine for wear-and-tear vs. damage classification, evidence metadata linking, and human review routing.
          </p>
        </div>
        <button onClick={loadData} className="secondary small">Refresh</button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="tabs">
        <button
          className={`tab ${tab === "disputes" ? "active" : ""}`}
          onClick={() => setTab("disputes")}
        >
          Active Disputes ({disputes.length})
        </button>
        <button
          className={`tab ${tab === "review-queue" ? "active" : ""}`}
          onClick={() => setTab("review-queue")}
        >
          Human Review Queue ({reviewQueue.filter(r => r.status === "PENDING").length} Pending)
        </button>
        <button
          className={`tab ${tab === "rules" ? "active" : ""}`}
          onClick={() => setTab("rules")}
        >
          Configurable Wear/Damage Rules ({rules.length})
        </button>
        {selectedDispute && (
          <button
            className={`tab ${tab === "detail" ? "active" : ""}`}
            onClick={() => setTab("detail")}
          >
            Dispute #{selectedDispute.disputeId} Detail
          </button>
        )}
      </div>

      {tab === "disputes" && (
        <div className="card">
          <div className="card-header">
            <h3>Dispute Register</h3>
          </div>
          <table>
            <thead>
              <tr>
                <th>Dispute #</th>
                <th>Property / Unit</th>
                <th>Tenant Ref</th>
                <th>Deposit Held</th>
                <th>Total Claimed</th>
                <th>Status</th>
                <th>Opened At</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {disputes.map((d) => (
                <tr key={d.disputeId}>
                  <td><strong>#{d.disputeId}</strong></td>
                  <td>{d.propertyName} - Unit {d.unitNumber}</td>
                  <td><code>{d.tenantRef}</code></td>
                  <td>${Number(d.depositHeld).toLocaleString()}</td>
                  <td>${Number(d.totalClaimed).toLocaleString()}</td>
                  <td><span className={`badge ${d.status}`}>{d.status}</span></td>
                  <td>{new Date(d.openedAt).toLocaleDateString()}</td>
                  <td>
                    <button
                      className="small"
                      onClick={() => openDisputeDetail(d.disputeId)}
                    >
                      View Items & Evidence
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === "review-queue" && (
        <div className="card">
          <div className="card-header">
            <div>
              <h3>AI-Resistant Evaluation: Human Review Escalations</h3>
              <p style={{ fontSize: "0.85rem", color: "#666" }}>
                Items where the deterministic system cannot decide automatically (e.g. missing baseline, unknown cause, ambiguous evidence) and routes to human operator.
              </p>
            </div>
          </div>
          <table>
            <thead>
              <tr>
                <th>Review ID</th>
                <th>Dispute Item</th>
                <th>Reason Code</th>
                <th>Detailed Human Routing Justification</th>
                <th>Status</th>
                <th>Triggered At</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {reviewQueue.map((rq) => (
                <tr key={rq.reviewId}>
                  <td>#{rq.reviewId}</td>
                  <td>
                    {rq.disputeItemId ? `Dispute Item #${rq.disputeItemId}` : `Issue #${rq.issueId}`}
                  </td>
                  <td><span className="badge EMERGENCY">{rq.reasonCode}</span></td>
                  <td style={{ maxWidth: 450, fontSize: "0.85rem" }}>{rq.reasonText}</td>
                  <td><span className={`badge ${rq.status}`}>{rq.status}</span></td>
                  <td>{new Date(rq.createdAt).toLocaleString()}</td>
                  <td>
                    {rq.status === "PENDING" ? (
                      <button
                        className="small danger"
                        onClick={() => {
                          setReviewModal(rq);
                          setOutcomeNotes("Reviewed move-out photos and tenant move-in notes. Verified reasonable wear and tear.");
                        }}
                      >
                        Resolve Review
                      </button>
                    ) : (
                      <span style={{ fontSize: "0.8rem", color: "#2b8a3e" }}>✓ Decided</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === "rules" && (
        <div className="card">
          <h3>Configurable Wear-and-Tear vs Damage Rules</h3>
          <p style={{ fontSize: "0.85rem", color: "#666", marginBottom: 12 }}>
            Explicit criteria evaluated in priority order. No ambiguous LLM or black-box inference used for deposit deductions.
          </p>
          <table>
            <thead>
              <tr>
                <th>Priority</th>
                <th>Rule Name</th>
                <th>Category</th>
                <th>Min / Max Age</th>
                <th>Move-Out Condition</th>
                <th>Cause Noted</th>
                <th>System Classification</th>
              </tr>
            </thead>
            <tbody>
              {rules.map((r) => (
                <tr key={r.ruleId}>
                  <td>{r.priority}</td>
                  <td><strong>{r.ruleName}</strong></td>
                  <td>{r.itemCategory}</td>
                  <td>{r.minAgeYears ? `${r.minAgeYears}+ yrs` : "Any"}</td>
                  <td>{r.moveOutCondition || "Any"}</td>
                  <td>{r.cause || "Any"}</td>
                  <td><span className={`badge ${r.classification}`}>{r.classification}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === "detail" && selectedDispute && (
        <div>
          <div className="card">
            <div className="card-header">
              <div>
                <h3>Dispute #{selectedDispute.disputeId} Details</h3>
                <p style={{ color: "#666", fontSize: "0.85rem" }}>
                  {selectedDispute.propertyName} | Unit {selectedDispute.unitNumber} | Tenant {selectedDispute.tenantRef} | Deposit Held: ${Number(selectedDispute.depositHeld).toLocaleString()}
                </p>
              </div>
              <span className={`badge ${selectedDispute.status}`}>{selectedDispute.status}</span>
            </div>

            <h4 style={{ marginTop: 20 }}>Disputed Items & Evidence Analysis</h4>
            <table>
              <thead>
                <tr>
                  <th>Item #</th>
                  <th>Category & Name</th>
                  <th>Move-In / Move-Out Condition</th>
                  <th>Claimed Amount</th>
                  <th>System Classification & Rule Trace</th>
                  <th>Tenant Position</th>
                  <th>Operator Decision</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {selectedDispute.items.map((it) => (
                  <tr key={it.disputeItemId}>
                    <td>#{it.disputeItemId}</td>
                    <td>
                      <strong>{it.moveOutItemName}</strong>
                      <div style={{ fontSize: "0.8rem", color: "#666" }}>Category: {it.moveOutCategory}</div>
                    </td>
                    <td>
                      <div>In: <strong>{it.moveInCondition || "NO RECORD"}</strong> ({it.moveInEvidenceCount} photos)</div>
                      <div>Out: <strong>{it.moveOutCondition}</strong> ({it.moveOutEvidenceCount} photos)</div>
                      {it.moveOutCause && <div style={{ fontSize: "0.78rem", color: "#c92a2a" }}>Cause: {it.moveOutCause}</div>}
                    </td>
                    <td><strong>${Number(it.claimedAmount).toLocaleString()}</strong></td>
                    <td>
                      <span className={`badge ${it.systemClassification}`}>{it.systemClassification}</span>
                      {it.appliedRuleName && (
                        <div style={{ fontSize: "0.78rem", color: "#555", marginTop: 4 }}>
                          Rule: {it.appliedRuleName}
                        </div>
                      )}
                      {it.ruleTrace && (
                        <pre style={{ fontSize: "0.7rem", background: "#f1f3f5", padding: 4, borderRadius: 3, marginTop: 4 }}>
                          {it.ruleTrace}
                        </pre>
                      )}
                    </td>
                    <td>
                      <span className={`badge ${it.tenantPosition === "CONTESTED" ? "EMERGENCY" : "LOW"}`}>
                        {it.tenantPosition}
                      </span>
                      {it.tenantComment && (
                        <div style={{ fontSize: "0.8rem", fontStyle: "italic", marginTop: 4 }}>
                          "{it.tenantComment}"
                        </div>
                      )}
                    </td>
                    <td>
                      {it.operatorDecision ? (
                        <div>
                          <strong>{it.operatorDecision}</strong>
                          <div style={{ color: "#2b8a3e", fontWeight: 700 }}>
                            ${Number(it.decidedAmount).toLocaleString()}
                          </div>
                          {it.decisionNotes && (
                            <div style={{ fontSize: "0.78rem", color: "#555" }}>{it.decisionNotes}</div>
                          )}
                        </div>
                      ) : (
                        <span style={{ color: "#868e96" }}>Pending Decision</span>
                      )}
                    </td>
                    <td>
                      <button
                        className="small"
                        onClick={() => {
                          setDecisionModal(it);
                          setDecidedAmount(String(it.decidedAmount ?? it.claimedAmount));
                          setOperatorDecision("CHARGE_REDUCED");
                          setDecisionNotes(`Adjusted according to inspection evidence & invoice ledger.`);
                        }}
                      >
                        Decide Charge
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <h4 style={{ marginTop: 28 }}>Traceable Dispute Audit & Event Trail</h4>
            <table>
              <thead>
                <tr>
                  <th>Time</th>
                  <th>Event Type</th>
                  <th>Actor</th>
                  <th>Event Details</th>
                </tr>
              </thead>
              <tbody>
                {selectedDispute.events.map((ev) => (
                  <tr key={ev.eventId}>
                    <td>{new Date(ev.createdAt).toLocaleString()}</td>
                    <td><code>{ev.eventType}</code></td>
                    <td><span className="badge LOW">{ev.actorType}</span></td>
                    <td>{ev.detail}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {decisionModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Operator Decision: Item #{decisionModal.disputeItemId} ({decisionModal.moveOutItemName})</h3>
            <p style={{ fontSize: "0.85rem", color: "#666", marginBottom: 12 }}>
              Original Claim: ${Number(decisionModal.claimedAmount).toLocaleString()} | System Classification: {decisionModal.systemClassification}
            </p>

            <label>Decision Type</label>
            <select value={operatorDecision} onChange={(e) => setOperatorDecision(e.target.value)}>
              <option value="CHARGE_REDUCED">CHARGE_REDUCED (Reduce based on repair invoice/ledger)</option>
              <option value="CHARGE_WAIVED">CHARGE_WAIVED (Waive entirely as wear and tear)</option>
              <option value="CHARGE_APPLIED">CHARGE_APPLIED (Maintain full claimed amount)</option>
            </select>

            <label>Decided Amount ($)</label>
            <input
              type="number"
              step="0.01"
              value={decidedAmount}
              onChange={(e) => setDecidedAmount(e.target.value)}
              required
            />

            <label>Decision Justification / Notes</label>
            <textarea
              rows={3}
              value={decisionNotes}
              onChange={(e) => setDecisionNotes(e.target.value)}
              placeholder="e.g. Reduced from 6000 to the ledger repair cost of 4200 (invoice INV-1004). Evidence: move-in photo 1, move-out photo 2."
            />

            <div style={{ display: "flex", gap: 10, marginTop: 16 }}>
              <button onClick={handleDecideItem}>Confirm & Resolve</button>
              <button className="secondary" onClick={() => setDecisionModal(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {reviewModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Resolve Human Review Escalation #{reviewModal.reviewId}</h3>
            <p style={{ fontSize: "0.85rem", color: "#c92a2a", marginBottom: 12 }}>
              Reason: {reviewModal.reasonCode} - {reviewModal.reasonText}
            </p>

            <label>Human Decision & Findings</label>
            <textarea
              rows={4}
              value={outcomeNotes}
              onChange={(e) => setOutcomeNotes(e.target.value)}
              placeholder="Document human findings, review of photographic evidence, and decision justification..."
              required
            />

            <div style={{ display: "flex", gap: 10, marginTop: 16 }}>
              <button onClick={handleResolveReview}>Submit Resolution</button>
              <button className="secondary" onClick={() => setReviewModal(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

