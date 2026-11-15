import { useEffect, useState } from "react";
import {
  getProperties,
  createProperty,
  getUnits,
  createUnit,
  getLeases,
  getTenantsAdmin,
  unmaskTenant,
  getRepairCosts,
  createRepairCost,
  reverseRepairCost
} from "../api";

export default function RegistryAndCosts() {
  const [tab, setTab] = useState("properties");
  const [properties, setProperties] = useState([]);
  const [units, setUnits] = useState([]);
  const [leases, setLeases] = useState([]);
  const [tenants, setTenants] = useState([]);
  const [costs, setCosts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  
  const [newPropName, setNewPropName] = useState("");
  const [newPropAddress, setNewPropAddress] = useState("");
  const [newPropCity, setNewPropCity] = useState("Chennai");

  
  const [selectedPropId, setSelectedPropId] = useState("");
  const [newUnitNumber, setNewUnitNumber] = useState("");
  const [newUnitBedrooms, setNewUnitBedrooms] = useState(2);

  
  const [unmaskModal, setUnmaskModal] = useState(null);
  const [unmaskReason, setUnmaskReason] = useState("");
  const [unmaskedData, setUnmaskedData] = useState(null);

  
  const [costModal, setCostModal] = useState(false);
  const [costIssueId, setCostIssueId] = useState("");
  const [costAmount, setCostAmount] = useState("");
  const [costType, setCostType] = useState("PARTS");
  const [costDesc, setCostDesc] = useState("");
  const [costInvoice, setCostInvoice] = useState("");

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const [pList, lList, tList, cList] = await Promise.all([
        getProperties(),
        getLeases(),
        getTenantsAdmin(),
        getRepairCosts()
      ]);
      setProperties(pList);
      setLeases(lList);
      setTenants(tList);
      setCosts(cList.entries || []);
      if (pList.length > 0 && !selectedPropId) {
        setSelectedPropId(String(pList[0].propertyId));
        const uList = await getUnits(pList[0].propertyId);
        setUnits(uList);
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

  const handlePropertySelect = async (propId) => {
    setSelectedPropId(propId);
    try {
      const uList = await getUnits(propId);
      setUnits(uList);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCreateProperty = async (e) => {
    e.preventDefault();
    try {
      await createProperty({
        name: newPropName,
        addressLine: newPropAddress,
        city: newPropCity,
        state: "Tamil Nadu",
        postalCode: "600001",
      });
      setSuccess("Property created successfully!");
      setNewPropName("");
      setNewPropAddress("");
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCreateUnit = async (e) => {
    e.preventDefault();
    if (!selectedPropId) return;
    try {
      await createUnit(Number(selectedPropId), {
        unitNumber: newUnitNumber,
        floorNo: 1,
        bedrooms: Number(newUnitBedrooms),
      });
      setSuccess("Unit added successfully!");
      setNewUnitNumber("");
      const uList = await getUnits(selectedPropId);
      setUnits(uList);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleUnmask = async () => {
    if (!unmaskModal || !unmaskReason) return;
    try {
      const contact = await unmaskTenant(unmaskModal.tenantId, unmaskReason);
      setUnmaskedData(contact);
      setSuccess("PII unmasked and audited to system log.");
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCreateCost = async (e) => {
    e.preventDefault();
    try {
      await createRepairCost({
        issueId: costIssueId ? Number(costIssueId) : null,
        entryType: "DEBIT",
        amount: Number(costAmount),
        costType,
        description: costDesc,
        invoiceRef: costInvoice,
      });
      setSuccess("Repair cost entry added to ledger!");
      setCostModal(false);
      setCostAmount("");
      setCostDesc("");
      setCostInvoice("");
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleReverseCost = async (costId) => {
    const reason = prompt("Enter reason for reversing this cost entry (Audit Requirement):");
    if (!reason) return;
    try {
      await reverseRepairCost(costId, { reason });
      setSuccess("Cost entry reversed with offsetting credit!");
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div>
      <div className="card-header">
        <div>
          <h2>Registry, Tenant Privacy & Repair Ledger</h2>
          <p style={{ color: "#868e96", fontSize: "0.88rem" }}>
            Property/unit registry, PII masked tenant directory, and repair ledger accounting.
          </p>
        </div>
        <button onClick={loadData} className="secondary small">Refresh</button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="tabs">
        <button className={`tab ${tab === "properties" ? "active" : ""}`} onClick={() => setTab("properties")}>
          Properties & Units
        </button>
        <button className={`tab ${tab === "leases" ? "active" : ""}`} onClick={() => setTab("leases")}>
          Leases ({leases.length})
        </button>
        <button className={`tab ${tab === "tenants" ? "active" : ""}`} onClick={() => setTab("tenants")}>
          Tenant Directory (PII Protected)
        </button>
        <button className={`tab ${tab === "costs" ? "active" : ""}`} onClick={() => setTab("costs")}>
          Repair Cost Ledger ({costs.length})
        </button>
      </div>

      {tab === "properties" && (
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20 }}>
          <div className="card">
            <h3>Properties</h3>
            <table>
              <thead>
                <tr>
                  <th>Property Name</th>
                  <th>Location</th>
                  <th>Units</th>
                </tr>
              </thead>
              <tbody>
                {properties.map((p) => (
                  <tr
                    key={p.propertyId}
                    style={{
                      cursor: "pointer",
                      background: String(p.propertyId) === selectedPropId ? "#e7f5ff" : "inherit",
                    }}
                    onClick={() => handlePropertySelect(String(p.propertyId))}
                  >
                    <td><strong>{p.name}</strong></td>
                    <td>{p.city}, {p.state}</td>
                    <td><span className="badge LOW">{p.unitCount} units</span></td>
                  </tr>
                ))}
              </tbody>
            </table>

            <h4 style={{ marginTop: 24 }}>Add Property</h4>
            <form onSubmit={handleCreateProperty}>
              <label>Property Name</label>
              <input value={newPropName} onChange={(e) => setNewPropName(e.target.value)} required />
              <label>Address</label>
              <input value={newPropAddress} onChange={(e) => setNewPropAddress(e.target.value)} required />
              <button type="submit" className="small" style={{ marginTop: 12 }}>Create Property</button>
            </form>
          </div>

          <div className="card">
            <h3>Units for Selected Property</h3>
            <table>
              <thead>
                <tr>
                  <th>Unit Number</th>
                  <th>Bedrooms</th>
                  <th>Floor</th>
                </tr>
              </thead>
              <tbody>
                {units.map((u) => (
                  <tr key={u.unitId}>
                    <td><strong>Unit {u.unitNumber}</strong></td>
                    <td>{u.bedrooms} BHK</td>
                    <td>Floor {u.floorNo}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <h4 style={{ marginTop: 24 }}>Add Unit to Selected Property</h4>
            <form onSubmit={handleCreateUnit}>
              <label>Unit Number</label>
              <input value={newUnitNumber} onChange={(e) => setNewUnitNumber(e.target.value)} required placeholder="e.g. C301" />
              <label>Bedrooms</label>
              <input type="number" min="1" max="10" value={newUnitBedrooms} onChange={(e) => setNewUnitBedrooms(e.target.value)} />
              <button type="submit" className="small" style={{ marginTop: 12 }}>Add Unit</button>
            </form>
          </div>
        </div>
      )}

      {tab === "leases" && (
        <div className="card">
          <h3>Lease Agreements</h3>
          <table>
            <thead>
              <tr>
                <th>Lease ID</th>
                <th>Property & Unit</th>
                <th>Tenant Ref</th>
                <th>Duration</th>
                <th>Monthly Rent</th>
                <th>Deposit Held</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {leases.map((l) => (
                <tr key={l.leaseId}>
                  <td>#{l.leaseId}</td>
                  <td><strong>{l.propertyName}</strong> - Unit {l.unitNumber}</td>
                  <td><code>{l.tenantRef}</code></td>
                  <td>{l.startDate} to {l.endDate}</td>
                  <td>${Number(l.monthlyRent).toLocaleString()}</td>
                  <td>${Number(l.depositAmount).toLocaleString()}</td>
                  <td><span className={`badge ${l.status}`}>{l.status}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === "tenants" && (
        <div className="card">
          <div className="card-header">
            <div>
              <h3>Tenant Privacy & PII Protection</h3>
              <p style={{ fontSize: "0.85rem", color: "#666" }}>
                PII (Email and Phone) is masked by default. Any unmasking requires an explicit operational justification and is recorded in the permanent audit trail.
              </p>
            </div>
          </div>
          <table>
            <thead>
              <tr>
                <th>Tenant Ref</th>
                <th>Full Name</th>
                <th>Masked Email</th>
                <th>Masked Phone</th>
                <th>Active Lease Unit</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {tenants.map((t) => (
                <tr key={t.tenantId}>
                  <td><code>{t.tenantRef}</code></td>
                  <td><strong>{t.fullName}</strong></td>
                  <td><code>{t.maskedEmail}</code></td>
                  <td><code>{t.maskedPhone}</code></td>
                  <td>{t.unitNumber ? `Unit ${t.unitNumber}` : "None"}</td>
                  <td>
                    <button
                      className="small secondary"
                      onClick={() => {
                        setUnmaskModal(t);
                        setUnmaskReason("");
                        setUnmaskedData(null);
                      }}
                    >
                      Unmask PII
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === "costs" && (
        <div className="card">
          <div className="card-header">
            <div>
              <h3>Repair Cost Ledger</h3>
              <p style={{ fontSize: "0.85rem", color: "#666" }}>
                Immutable financial record of maintenance and inspection repairs linked to vendor work orders and invoices.
              </p>
            </div>
            <button className="small" onClick={() => setCostModal(true)}>+ Add Repair Cost Entry</button>
          </div>
          <table>
            <thead>
              <tr>
                <th>Cost ID</th>
                <th>Issue ID</th>
                <th>Entry Type</th>
                <th>Amount</th>
                <th>Cost Type</th>
                <th>Description</th>
                <th>Invoice Ref</th>
                <th>Incurred Date</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {costs.map((c) => (
                <tr key={c.costId}>
                  <td>#{c.costId}</td>
                  <td>{c.issueId ? `#${c.issueId}` : "N/A"}</td>
                  <td><span className={`badge ${c.entryType === "DEBIT" ? "EMERGENCY" : "RESOLVED"}`}>{c.entryType}</span></td>
                  <td><strong>${Number(c.amount).toLocaleString()}</strong></td>
                  <td>{c.costType}</td>
                  <td>{c.description}</td>
                  <td><code>{c.invoiceRef || "-"}</code></td>
                  <td>{new Date(c.incurredAt).toLocaleDateString()}</td>
                  <td>
                    {c.entryType === "DEBIT" && !c.reversed && (
                      <button className="small danger" onClick={() => handleReverseCost(c.costId)}>
                        Reverse
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {unmaskModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Unmask Tenant PII: {unmaskModal.fullName} ({unmaskModal.tenantRef})</h3>
            <p style={{ fontSize: "0.85rem", color: "#666", marginBottom: 12 }}>
              Access to personal contact data is strictly monitored under privacy constraints.
            </p>

            {unmaskedData ? (
              <div className="alert alert-success">
                <div><strong>Full Name:</strong> {unmaskedData.fullName}</div>
                <div><strong>Actual Email:</strong> {unmaskedData.email}</div>
                <div><strong>Actual Phone:</strong> {unmaskedData.phone}</div>
                <button className="small" style={{ marginTop: 12 }} onClick={() => setUnmaskModal(null)}>Close</button>
              </div>
            ) : (
              <>
                <label>Reason for Unmasking (Required for Audit Trail)</label>
                <textarea
                  rows={3}
                  value={unmaskReason}
                  onChange={(e) => setUnmaskReason(e.target.value)}
                  placeholder="e.g. Urgent contact required regarding flood leak escalation in unit."
                  required
                />
                <div style={{ display: "flex", gap: 10, marginTop: 16 }}>
                  <button onClick={handleUnmask} disabled={!unmaskReason.trim()}>
                    Confirm & Unmask
                  </button>
                  <button className="secondary" onClick={() => setUnmaskModal(null)}>Cancel</button>
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {costModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Add Repair Cost Entry</h3>
            <form onSubmit={handleCreateCost}>
              <label>Associated Issue ID (Optional)</label>
              <input value={costIssueId} onChange={(e) => setCostIssueId(e.target.value)} placeholder="e.g. 1" />

              <label>Amount ($)</label>
              <input type="number" step="0.01" value={costAmount} onChange={(e) => setCostAmount(e.target.value)} required />

              <label>Cost Category</label>
              <select value={costType} onChange={(e) => setCostType(e.target.value)}>
                <option value="PARTS">PARTS</option>
                <option value="LABOR">LABOR</option>
                <option value="MATERIALS">MATERIALS</option>
                <option value="CALL_OUT_FEE">CALL_OUT_FEE</option>
              </select>

              <label>Description</label>
              <input value={costDesc} onChange={(e) => setCostDesc(e.target.value)} required placeholder="e.g. Replaced leaking PVC valve and seal" />

              <label>Invoice Reference</label>
              <input value={costInvoice} onChange={(e) => setCostInvoice(e.target.value)} placeholder="e.g. INV-1008" />

              <div style={{ display: "flex", gap: 10, marginTop: 16 }}>
                <button type="submit">Save Cost Entry</button>
                <button type="button" className="secondary" onClick={() => setCostModal(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

