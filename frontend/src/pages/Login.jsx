import { useState } from "react";
import { login, setAuth } from "../api";

export default function Login({ onLoginSuccess }) {
  const [email, setEmail] = useState("admin@cap43.test");
  const [password, setPassword] = useState("Demo@12345");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const res = await login(email, password);
      setAuth(res.token, {
        userId: res.userId,
        role: res.role,
        fullName: res.fullName,
        tenantId: res.tenantId,
        vendorId: res.vendorId,
      });
      if (onLoginSuccess) onLoginSuccess();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const setRoleDemo = (roleEmail) => {
    setEmail(roleEmail);
    setPassword("Demo@12345");
  };

  return (
    <div className="card" style={{ maxWidth: 480, margin: "60px auto" }}>
      <h2>Sign In to CAP-43</h2>
      <p style={{ color: "#868e96", fontSize: "0.88rem", marginBottom: 16 }}>
        Rental Property Maintenance Risk & Deposit Dispute Ledger
      </p>

      {error && <div className="alert alert-error">{error}</div>}

      <form onSubmit={handleSubmit}>
        <label>Email Address</label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <label>Password</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <button type="submit" disabled={loading} style={{ width: "100%", marginTop: 20 }}>
          {loading ? "Signing in..." : "Sign In"}
        </button>
      </form>

      <div style={{ marginTop: 24, paddingTop: 16, borderTop: "1px solid #dee2e6" }}>
        <p style={{ fontSize: "0.82rem", fontWeight: 700, color: "#495057", marginBottom: 8 }}>
          DEMO ACCOUNTS (One-Click Select):
        </p>
        <div style={{ display: "flex", flexWrap: "wrap", gap: 6 }}>
          <button
            type="button"
            className="small secondary"
            onClick={() => setRoleDemo("admin@cap43.test")}
          >
            Admin (Operations)
          </button>
          <button
            type="button"
            className="small secondary"
            onClick={() => setRoleDemo("priya.raman@cap43.test")}
          >
            Tenant 1 (Priya)
          </button>
          <button
            type="button"
            className="small secondary"
            onClick={() => setRoleDemo("arun.kumar@cap43.test")}
          >
            Tenant 2 (Arun - Disputes)
          </button>
          <button
            type="button"
            className="small secondary"
            onClick={() => setRoleDemo("fixit@cap43.test")}
          >
            Vendor (FixIt Plumbing)
          </button>
          <button
            type="button"
            className="small secondary"
            onClick={() => setRoleDemo("brightspark@cap43.test")}
          >
            Vendor (BrightSpark)
          </button>
          <button
            type="button"
            className="small secondary"
            onClick={() => setRoleDemo("meena.iyer@cap43.test")}
          >
            Tenant 3 (Meena)
          </button>
        </div>
      </div>
    </div>
  );
}

